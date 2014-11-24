package com.ualberta.team17.datamanager.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.IdComparator;
import com.ualberta.team17.view.QuestionListActivity;

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
		dataManager = new LocalDataManager(getActivity());
		dataFilter = new DataFilter();
		result = new IncrementalResult(new IdComparator());
		
		// Clean out any old data
		dataManager.writeTestData("[]");
		dataManager.flushData();
	}
	
	public static final String TEST_DATA = "[[\"Question\",{\"id\":\"c4ca4238a0b923820dcc509a6f75849b\",\"type\":\"question\",\"parent\":\"0\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testBody\",\"title\":\"testTitle\"}],[\"Answer\",{\"id\":\"c81e728d9d4c2f636f067f89cc14862c\",\"type\":\"answer\",\"parent\":\"c4ca4238a0b923820dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply\"}],[\"Answer\",{\"id\":\"eccbc87e4b5ce2fe28308fd9f2a7baf3\",\"type\":\"answer\",\"parent\":\"c4ca4238a0b923820dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply2\"}],[\"Comment\",{\"id\":\"a87ff679a2f3e71d9181a67b7542122c\",\"type\":\"comment\",\"parent\":\"c81e728d9d4c2f636f067f89cc14862c\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"comment!\"}]]";
	
	// Wait for N results to arrive in a given incremental result, with a timeout
	// WARNING: DO NOT CALL MULTIPLE TIMES FROM THE SAME THREAD sometimes hangs for some reason
	private boolean waitForResults(final IncrementalResult targetResult, final int numResults) {
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();
		boolean success = false;
		long maxWaitSeconds = 8;

		lock.lock();

		if (targetResult.getCurrentResults().size() >= numResults) {
			return true;
		}
		
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

		try {
			success = condition.await(maxWaitSeconds, TimeUnit.SECONDS) && dataManager.isAvailable();
		} catch (InterruptedException e) {
		}

		return success;
	}
	
	/**
	 * Create a question new question object and return it
	 * @param id
	 * @param title
	 * @param body
	 * @return
	 */
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
	
	/**
	 * Create a new answer object and return it
	 * @param id
	 * @param body
	 * @param inReplyTo
	 * @return
	 */
	public AnswerItem newAnswer(int id, String body, UniqueId inReplyTo) {
		return new AnswerItem(new UniqueId(Integer.toString(id)), 
				inReplyTo, 
				userContext.getUserName(), 
				new Date(0), 
				body, 
				0);
	}
	
	/**
	 * Create a new comment item and return it
	 * @param id
	 * @param text
	 * @param inReplyTo
	 * @return
	 */
	public CommentItem newComment(int id, String text, UniqueId inReplyTo) {
		return new CommentItem(new UniqueId(Integer.toString(id)), 
				inReplyTo, 
				userContext.getUserName(), 
				new Date(0), 
				text, 
				0);		
	}
	
	/**
	 * Create a new upvote item
	 * @param id
	 * @param target
	 * @return
	 */
	public UpvoteItem newUpvote(int id, UniqueId target) {
		return new UpvoteItem(new UniqueId(Integer.toString(id)), 
				target, 
				userContext.getUserName(), 
				new Date(0));
	}
	
	/**
	 * Test that the Filter & Sort part of the LocalDataManager works by
	 * adding an item and then doing a query that should find it.
	 */
	public void test_SaveAndGetItem_Cycle() {		
		Log.i("app", "===================================");
		// Make and save an item
		QuestionItem item = newQuestion(0, "testTitle", "testBody");
		dataManager.saveItem(item, userContext);
		
		// Query for it with a filter
		dataFilter.addFieldFilter(QuestionItem.FIELD_TITLE, "testTitle", FilterComparison.EQUALS);
		dataManager.query(dataFilter, new IdComparator(), result);
		
		// Wait for the results
		Log.i("app", "========= waiting... ==============");
		assertTrue(waitForResults(result, 1));
		
		// Check that we got the item back
		assertEquals(item, result.getCurrentResults().get(0));
		
		// Close down
		dataManager.close();
		Log.i("app", "===================================");
	}
	
	/**
	 * Test writing out a known set of data, and reading back a dump of the
	 * LocalDataManager file to see if it matches.
	 */
	public void test_WritingToLocalFile() {
		// Add a question
		QuestionItem item = newQuestion(1, "testTitle", "testBody");
		dataManager.saveItem(item, userContext);
		
		// Add an answer
		AnswerItem answerItem = newAnswer(2, "testReply", item.getUniqueId());
		dataManager.saveItem(answerItem, userContext);
		
		// Add another answer
		dataManager.saveItem(newAnswer(3, "testReply2", item.getUniqueId()), userContext);
		
		// Add a comment to the first answer
		CommentItem commentItem = newComment(4, "comment!", answerItem.getUniqueId());
		dataManager.saveItem(commentItem, userContext);
		
		// Wait for a save to happen
		dataManager.waitForSave();
		
		// Dump the local data, see if it matches the TEST_DATA
		assertEquals(TEST_DATA, dataManager.dumpLocalData());
		
		// Close down
		dataManager.close();
	}
	
	/**
	 * Test writing out a test set of data, and then reading in an item from it,
	 * and checking if it's the correct item.
	 */
	@SuppressWarnings("serial")
	public void test_ReadingFromLocalFile() {
		// Load in our test data
		dataManager.writeTestData(TEST_DATA);
		
		// Get the items
		dataManager.query(new ArrayList<UniqueId>(){{add(new UniqueId(Integer.toString(1)));}}, result);
		assertTrue("Didn't get a result", waitForResults(result, 1));
		QuestionItem item = (QuestionItem)result.getCurrentResults().get(0);
		assertEquals("c4ca4238a0b923820dcc509a6f75849b", item.getUniqueId().toString());
		assertEquals("testTitle", item.getTitle());
		
		// See if the items have the right derived information
		assertEquals(2, item.getReplyCount());
		
		// Close down
		dataManager.close();
	}
	
	
	/**
	 * Check Querying twice for all of the items in the test set of data
	 * and seeing if all of the items arrive correctly.
	 */
	@SuppressWarnings("serial")
	public void test_MultipleRead() {
		// Load in our test data
		dataManager.writeTestData(TEST_DATA);
		
		// Get the items
		dataManager.query(new ArrayList<UniqueId>(){{add(new UniqueId(Integer.toString(1)));}}, result);		
		dataManager.query(new ArrayList<UniqueId>(){{
			add(new UniqueId(Integer.toString(2)));
			add(new UniqueId(Integer.toString(3)));
			add(new UniqueId(Integer.toString(4)));
		}}, result);

		// Check result count
		assertTrue("Didn't get 4 results", waitForResults(result, 4));
		assertEquals(4, result.getCurrentResults().size());
		
		// Check for one question with two answers, and a comment
		assertEquals(2, result.getCurrentResultsOfType(ItemType.Answer).size());
		assertEquals(1, result.getCurrentResultsOfType(ItemType.Comment).size());
		assertEquals(1, result.getCurrentResultsOfType(ItemType.Question).size());
		
		// Upvote one of the questions, and see if it works
		QuestionItem q = ((QuestionItem)result.getCurrentResultsOfType(ItemType.Question).get(0));
		UpvoteItem up = newUpvote(32, q.getUniqueId());
		dataManager.saveItem(up, userContext);
		dataManager.waitForSave();
		assertEquals(1, q.getUpvoteCount());
		
		// Close down
		dataManager.close();	
	}
	
	
	/**
	 * Test that adding an upvote updates the derived info
	 */
	@SuppressWarnings("serial")
	public void test_Derived_UpvoteCount() {
		// Load in our test data
		dataManager.writeTestData(TEST_DATA);
		
		// Get the items
		dataManager.query(new ArrayList<UniqueId>(){{add(new UniqueId(Integer.toString(1)));}}, result);		

		// Check result count
		assertTrue("Didn't get 1 results", waitForResults(result, 1));
		assertEquals(1, result.getCurrentResults().size());
		
		// Upvote one of the questions, and see if it works
		QuestionItem q = ((QuestionItem)result.getCurrentResults().get(0));
		UpvoteItem up = newUpvote(32, q.getUniqueId());
		dataManager.saveItem(up, userContext);
		assertEquals(1, q.getUpvoteCount());
		
		// Close down
		dataManager.close();	
	}

	/**
	 * Test that adding an reply updates the derived info
	 */
	@SuppressWarnings("serial")
	public void test_Derived_ReplyCount() {
		// Load in our test data
		dataManager.writeTestData(TEST_DATA);
		
		// Get the items
		dataManager.query(new ArrayList<UniqueId>(){{add(new UniqueId(Integer.toString(1)));}}, result);		

		// Check result count
		assertTrue("Didn't get 1 results", waitForResults(result, 1));
		assertEquals(1, result.getCurrentResults().size());
		
		// Upvote one of the questions, and see if it works
		QuestionItem q = ((QuestionItem)result.getCurrentResults().get(0));
		AnswerItem answer = newAnswer(32, "TestBody", q.getUniqueId());
		dataManager.saveItem(answer, userContext);
		
		// Test for 3 replies, there were already 2 in the TEST_DATA
		assertEquals(3, q.getReplyCount());
		
		// Close down
		dataManager.close();	
	}
	
	/**
	 * Test that adding comments updates the comment derived info
	 */
	@SuppressWarnings("serial")
	public void test_Derived_CommentCount() {
		// Load in our test data
		dataManager.writeTestData(TEST_DATA);
		
		// Get the items
		dataManager.query(new ArrayList<UniqueId>(){{add(new UniqueId(Integer.toString(1)));}}, result);		

		// Check result count
		assertTrue("Didn't get 1 results", waitForResults(result, 1));
		assertEquals(1, result.getCurrentResults().size());
		
		// Upvote one of the questions, and see if it works
		QuestionItem q = ((QuestionItem)result.getCurrentResults().get(0));
		CommentItem comment = newComment(32, "TestBody", q.getUniqueId());
		dataManager.saveItem(comment, userContext);
		
		// Test for 1 replies, there were none in the test data
		assertEquals(1, q.getCommentCount());
		
		// Close down
		dataManager.close();			
	}
}