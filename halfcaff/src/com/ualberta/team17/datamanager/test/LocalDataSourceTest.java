package com.ualberta.team17.datamanager.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.test.ActivityInstrumentationTestCase;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityTestCase;
import android.util.Log;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IDataSourceAvailableListener;
import com.ualberta.team17.datamanager.IDataSourceManager;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.IdComparator;
import com.ualberta.team17.view.QuestionListActivity;
import com.ualberta.team17.view.QuestionTaxonomyActivity;

public class LocalDataSourceTest extends ActivityInstrumentationTestCase2<QuestionListActivity> {
	DataFilter dataFilter;
	IncrementalResult result;
	UserContext userContext;
	LocalDataManager dataManager;
	
	public LocalDataSourceTest() {
		super(QuestionListActivity.class);
	}
	
	public void setUp() {
		// Set up our variables
		userContext = new UserContext("test_user");
		dataManager = new LocalDataManager(getActivity(), userContext);
		dataFilter = new DataFilter();
		result = new IncrementalResult(new IdComparator());
		
		// Clean out any old data
		dataManager.writeTestData("[]");
		dataManager.flushData();
	}
	
	// Wait for N results to arrive in a given incremental result, with a timeout
	private boolean waitForResults(final IncrementalResult targetResult, final int numResults) {
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();

		// If the data source becomes unavailable, signal the condition so we don't wait unnecessarily
		dataManager.addDataSourceAvailableListener(new IDataSourceAvailableListener() {
			@Override
			public void DataSourceAvailable(IDataSourceManager manager) {
				if (!manager.isAvailable()) {
					lock.lock();
					condition.signal();
					lock.unlock();
				}
			}
		});

		targetResult.addObserver(new IIncrementalObserver() {
			@Override
			public void itemsArrived(List<QAModel> item, int index) {
				if (targetResult.getCurrentResults().size() >= numResults) {
					lock.lock();
					condition.signalAll();
					lock.unlock();
				}
			}
		});

		lock.lock();

		boolean success = false;
		try {
			long maxWaitSeconds = 2;
			success = condition.await(maxWaitSeconds, TimeUnit.SECONDS) && dataManager.isAvailable();
		} catch (InterruptedException e) {

		}

		return success;
	}
	
	public QuestionItem newQuestion(int id, String title, String body) {
		return new QuestionItem(
				new UniqueId(Integer.toString(id)), 
				null, 
				userContext.getUserName(), 
				new Date(0), 
				body, 
				0, 
				title);
	}
	
	public AnswerItem newAnswer(int id, String body, UniqueId inReplyTo) {
		return new AnswerItem(new UniqueId(Integer.toString(id)), 
				inReplyTo, 
				userContext.getUserName(), 
				new Date(0), 
				body, 
				0);
	}
	
	public CommentItem newComment(int id, String text, UniqueId inReplyTo) {
		return new CommentItem(new UniqueId(Integer.toString(id)), 
				inReplyTo, 
				userContext.getUserName(), 
				new Date(0), 
				text, 
				0);		
	}
	
	/*
	public void test_SaveAndGetItem_Cycle() {
		// Wait for the dataManager to be ready
		Log.i("app", "Waiting for data ready...");
		dataManager.waitForData();
		Log.i("app", "Data is ready");
		
		// Make and save an item
		QuestionItem item = newQuestion("testTitle", "testBody");
		dataManager.saveItem(item);
		
		// Query for it with a filter
		dataFilter.addFieldFilter(QuestionItem.FIELD_TITLE, "testTitle", FilterComparison.EQUALS);
		dataManager.query(dataFilter, new IdComparator(), result);
		
		// Wait for the results
		assertTrue(waitForResults(result, 1));
		
		// Check that we got the item back
		assertEquals(item, result.getCurrentResults().get(0));
	}
	*/
	
	public static final String TEST_DATA = "[[\"Question\",{\"id\":\"c4ca4238a0b92382dcc509a6f75849b\",\"parent\":\"0\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testBody\",\"title\":\"testTitle\"}],[\"Answer\",{\"id\":\"c81e728d9d4c2f636f67f89cc14862c\",\"parent\":\"c4ca4238a0b92382dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply\"}],[\"Answer\",{\"id\":\"eccbc87e4b5ce2fe28308fd9f2a7baf3\",\"parent\":\"c4ca4238a0b92382dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply2\"}],[\"Comment\",{\"id\":\"a87ff679a2f3e71d9181a67b7542122c\",\"parent\":\"c81e728d9d4c2f636f67f89cc14862c\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"comment!\"}]]";
	/*
	public void test_WritingToLocalFile() {
		Log.i("app", "===TESTWrite===");
		
		// Wait for ready
		dataManager.writeTestData("[]");
		dataManager.asyncLoadData();
		dataManager.waitForData();
		
		// Add a question
		QuestionItem item = newQuestion(1, "testTitle", "testBody");
		dataManager.saveItem(item);
		
		// Add an answer
		AnswerItem answerItem = newAnswer(2, "testReply", item.getUniqueId());
		dataManager.saveItem(answerItem);
		
		// Add another answer
		dataManager.saveItem(newAnswer(3, "testReply2", item.getUniqueId()));
		
		// Add a comment to the first answer
		CommentItem commentItem = newComment(4, "comment!", answerItem.getUniqueId());
		dataManager.saveItem(commentItem);
		
		// Wait for a save to happen
		dataManager.waitForSave();
		
		// Dump the local data, see if it matches the TEST_DATA
		Log.e("lock", dataManager.dumpLocalData());
		assertEquals(TEST_DATA, dataManager.dumpLocalData());
		
		dataManager.close();
	}
	*/
	public void test_ReadingFromLocalFile() {
		Log.i("app", "===TESTRead===");
		
		// Load in our test data
		dataManager.writeTestData(TEST_DATA);
		dataManager.asyncLoadData();
		dataManager.waitForData();
		
		// Get the items
		dataManager.query(new ArrayList<UniqueId>(){{add(new UniqueId(Integer.toString(1)));}}, result);
		assertTrue("Didn't get a result", waitForResults(result, 1));
		QuestionItem item = (QuestionItem)result.getCurrentResults().get(0);
		assertEquals("c4ca4238a0b92382dcc509a6f75849b", item.getUniqueId().toString());
		assertEquals(item.getTitle(), "testTitle");
		
		dataManager.query(new ArrayList<UniqueId>(){{
			add(new UniqueId(Integer.toString(2)));
			add(new UniqueId(Integer.toString(3)));
			add(new UniqueId(Integer.toString(4)));
		}}, result);
		
		assertTrue("Didn't get 4 results", waitForResults(result, 4));
		assertEquals(4, result.getCurrentResults().size());
		
		assertEquals(2, result.getCurrentResultsOfType(ItemType.Answer).size());
		
		dataManager.close();
	}
	
}














