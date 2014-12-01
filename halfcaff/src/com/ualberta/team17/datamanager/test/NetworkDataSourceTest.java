package com.ualberta.team17.datamanager.test;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.content.res.Resources;

import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.ESSearchBuilder;
import com.ualberta.team17.datamanager.IDataItemSavedListener;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;
import com.ualberta.team17.datamanager.IDataLoadedListener;
import com.ualberta.team17.datamanager.IDataSourceAvailableListener;
import com.ualberta.team17.datamanager.IDataSourceManager;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.MoreLikeThisFilter;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.comparators.DateComparator;

@SuppressLint("DefaultLocale")
public class NetworkDataSourceTest extends DataManagerTester<NetworkDataManager> {
	JestClientFactory mJestClientFactory;
	JestClient mJestClient;

	String mEsServerUrl;
	String mEsServerIndex;

	/**
	 * Waits for an item save to complete.
	 * @param item The item to save
	 * @return True on save success, false on any failure.
	 */
	private boolean waitForItemSaved(QAModel item) {
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();

		class DataItemSavedListener implements IDataItemSavedListener {
			boolean mSuccess = false;
			Exception mException;

			@Override
			public void dataItemSaved(boolean success, Exception e) {
				mSuccess = success;
				mException = e;
				lock.lock();
				condition.signal();
				lock.unlock();
			}
		}

		lock.lock();
		DataItemSavedListener savedListener = new DataItemSavedListener();
		dataManager.saveItem(item, userContext, savedListener);
		boolean success = false;
		try {
			success = condition.await(maxWaitSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}

		if (!success) {
			return false;
		}

		// We can't re-query immediately (elastic search needs time to index)
		// So wait a bit.
		waitForModOperation();

		return savedListener.mSuccess && null == savedListener.mException;
	}

	/**
	 * Waits for a mod operation to complete by sleeping the thread.
	 *
	 * This is necessary because insert, update, and delete operations return immediately, but
	 * take a non-negligible amount of time to complete before the changes are reflected in queries.
	 */
	private void waitForModOperation() {
		try {
			Thread.sleep(maxModOperationWaitMs);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the UTs, instantiating a NetworkDataManager and a bare filter.
	 */
	public void setUp() {
		if (null == mJestClient) {
			Resources resources = getInstrumentation().getTargetContext().getResources();
			mEsServerUrl = resources.getString(R.string.esTestServer);
			mEsServerIndex = resources.getString(R.string.esTestIndex);

			mJestClientFactory = new JestClientFactory();
			mJestClientFactory.setDroidClientConfig(new DroidClientConfig.Builder(mEsServerUrl).multiThreaded(false).build());

			mJestClient = mJestClientFactory.getObject();
		}

		dataManager = new NetworkDataManager(mEsServerUrl, mEsServerIndex);
		dataFilter = new DataFilter();
		userContext = new UserContext("test_username");
	}

	/**
	 * Tests that filtering by type=question works correctly, and only returns questions.
	 */
	public void test_FilterTypeQuestion() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setTypeFilter(ItemType.Question);
		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, 3));

		// Verify this against the expected test dataset
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Question count", 3, results.size());

		// Ensure each item is a question
		for (QAModel item: results) {
			assertEquals(ItemType.Question, item.getItemType());
		}
	}

	/**
	 * Tests that filtering by type=answer works correctly, and only returns answers.
	 */
	public void test_FilterTypeAnswer() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setTypeFilter(ItemType.Answer);
		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, 5));

		// Verify this against the expected test dataset
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Answer count", 5, results.size());

		// Ensure each item is a question
		for (QAModel item: results) {
			assertEquals(ItemType.Answer, item.getItemType());
		}
	}

	/**
	 * Tests that filtering by type=comment works correctly, and only returns comments.
	 */
	public void test_FilterTypeComment() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setTypeFilter(ItemType.Comment);
		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, 1));

		// Verify this against the expected test dataset
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Comment count", 1, results.size());

		// Ensure each item is a question
		for (QAModel item: results) {
			assertEquals(ItemType.Comment, item.getItemType());
		}
	}

	/**
	 * Tests that searching for a field's contents works correctly, and only returns results whose body
	 * contain the search phrase.
	 */
	public void test_TextSearch() {
		String searchStr = "is";
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.addFieldFilter(AuthoredTextItem.FIELD_BODY, searchStr, FilterComparison.QUERY_STRING);

		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, 5));

		// Verify this against the expected test dataset
		List<QAModel> results = result.getCurrentResults();

		// Ensure each item is a question
		for (QAModel item: results) {
			assertTrue(item instanceof AuthoredTextItem);
			System.out.println("Search item body: " + ((AuthoredTextItem)item).getBody());

			assertTrue(((AuthoredTextItem)item).getBody().contains(searchStr));
		}

		assertEquals("Question count", 5, results.size());
	}

	/**
	 * Tests that a blank data filter produces a correct query, and returns everything in the ES Test db.
	 */
	public void test_BlankDataFilter() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setMaxResults(50);

		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, 31));

		// Verify this against the expected test dataset
		assertEquals("Item count", 31, result.getCurrentResults().size());

		// Ensure all the right items were returned
		assertEquals("Answer count", 5, result.getCurrentResultsOfType(ItemType.Answer).size());
		for (QAModel item: result.getCurrentResultsOfType(ItemType.Answer)) {
			assertEquals(ItemType.Answer, item.getItemType());
		}

		assertEquals("Question count", 3, result.getCurrentResultsOfType(ItemType.Question).size());
		for (QAModel item: result.getCurrentResultsOfType(ItemType.Question)) {
			assertEquals(ItemType.Question, item.getItemType());
		}

		assertEquals("Comment count", 1, result.getCurrentResultsOfType(ItemType.Comment).size());
		for (QAModel item: result.getCurrentResultsOfType(ItemType.Comment)) {
			assertEquals(ItemType.Comment, item.getItemType());
		}
	}

	/**
	 * Tests that sorting works correctly by grabbing some of the latest items.
	 *
	 * This test grabs the 3 latest answers, meaning that if sorting isn't working we shouldn't get
	 * the correct three items, as there are more answers than three.
	 */
	public void test_DataFilterWithSort() {
		Integer expectedResults = 3;

		@SuppressWarnings("serial")
		List<UniqueId> idList = new ArrayList<UniqueId>(){{
			add(UniqueId.fromString("0db7fac6e3ceb0add59ab9cd030ffd7c"));
			add(UniqueId.fromString("b2a7c2e2e477495eb77bdfca23dcc13a"));
			add(UniqueId.fromString("ba0baf9d538a14f982c09c6f6c1f4666"));
		}};

		IItemComparator comparator = new DateComparator();
		comparator.setCompareDirection(SortDirection.Descending);

		dataFilter.setTypeFilter(ItemType.Answer);
		dataFilter.setMaxResults(3);

		result = new IncrementalResult(comparator);
		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, expectedResults));

		// Verify this against the expected test dataset
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Result count", expectedResults, Integer.valueOf(results.size()));

		for (int i = 0; i < expectedResults; ++i) {
			// Ensure the item is the expected answer with the correct id
			assertTrue("Object instance of AnswerItem", results.get(i) instanceof AnswerItem);
			assertTrue("Item type is Answer", results.get(i).getItemType() == ItemType.Answer);
			assertEquals("Correct ID", idList.get(i), results.get(i).getUniqueId());
		}

		// Assert that the items are ordered properly
		assertTrue(((AuthoredItem)results.get(0)).getDate().compareTo(((AuthoredItem)results.get(1)).getDate()) >= 0);
		assertTrue(((AuthoredItem)results.get(1)).getDate().compareTo(((AuthoredItem)results.get(2)).getDate()) >= 0);
	}

	/**
	 * Performs multiple distinct queries on the same NetworkDataManager object. This is done to ensure that in no way does
	 * it's internal state become corrupted, stopping it from receiving valid results.
	 *
	 * This test is very lazy, it just runs the other tests without performing a setUp() in between.
	 */
	public void test_MultipleQueries() {
		test_TextSearch();

		dataFilter = new DataFilter();
		test_FilterTypeQuestion();

		dataFilter = new DataFilter();
		test_FilterTypeAnswer();

		dataFilter = new DataFilter();
		test_BlankDataFilter();

		dataFilter = new DataFilter();
		test_DataFilterWithSort();
	}

	/**
	 * Ensures that the NetworkDataManager doesn't completely blow up if it can't talk to the server, but rather
	 * just doesn't return anything in the IncrementalResult.
	 */
	public void test_BadESServer() {
		class DataSourceAvailableListener implements IDataSourceAvailableListener {
			Boolean wasNotified = false;

			@Override
			public void DataSourceAvailable(IDataSourceManager manager) {
				wasNotified = true;
			}
		};

		dataManager = new NetworkDataManager("http://thisisafakeserverthatshouldnotresolve.com:9200", "moqatest");

		DataSourceAvailableListener availableListener = new DataSourceAvailableListener();
		dataManager.addDataSourceAvailableListener(availableListener);

		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);

		dataManager.query(dataFilter, comparator, result, null);

		assertFalse("No results arrived", waitForResults(result, 1));
		assertTrue("Data source notified unavailable", availableListener.wasNotified);
		assertFalse("Network Data Manager Not Available", dataManager.isAvailable());

		assertEquals("Incremental result should be empty", 0, result.getCurrentResults().size());
	}

	/**
	 * Tests that the NetworkDataManager properly notifies listeners when a data item is loaded.
	 */
	public void test_DataItemLoadedNotification() {
		class DataSourceNotificationListener implements IDataLoadedListener {
			int timesNotified = 0;

			@Override
			public void dataItemLoaded(IDataSourceManager manager, QAModel item) {
				++timesNotified;
			}
		}

		DataSourceNotificationListener loadedListener = new DataSourceNotificationListener();
		dataManager.addDataLoadedListener(loadedListener);

		dataManager.notifyDataItemLoaded(null);
		assertEquals(1, loadedListener.timesNotified);

		dataManager.notifyDataItemLoaded(null);
		assertEquals(2, loadedListener.timesNotified);

		// Use the previous test to query for questions, and assert that they were all notified on
		test_FilterTypeQuestion();
		assertEquals(2 + 3 + 9 + 5, loadedListener.timesNotified);
	}

	/**
	 * Tests that the NetworkDataManager properly notifies listeners when it detects it is available
	 */
	public void test_DataSourceAvailableNotification() {
		class DataAvailableNotificationListener implements IDataSourceAvailableListener {
			int timesNotified = 0;

			@Override
			public void DataSourceAvailable(IDataSourceManager manager) {
				++timesNotified;
			}
		}

		DataAvailableNotificationListener availableListener = new DataAvailableNotificationListener();
		dataManager.addDataSourceAvailableListener(availableListener);

		dataManager.notifyDataSourceAvailable();
		assertEquals(1, availableListener.timesNotified);

		dataManager.notifyDataSourceAvailable();
		assertEquals(2, availableListener.timesNotified);

		assertFalse("Data source unavailable", dataManager.isAvailable());

		// Use the previous test to query for questions, and assert that they were notified on
		test_FilterTypeQuestion();
		assertEquals(3, availableListener.timesNotified);

		assertTrue("Data source available", dataManager.isAvailable());
	}

	/**
	 * Tests that simultaneous queries work correctly.
	 */
	public void test_SimultaneousQueries() {
		int numRepeats = 3;
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setTypeFilter(ItemType.Question);
		dataFilter.setMaxResults(1);

		for (int i = 0; i < numRepeats; ++i) {
			dataFilter.setPage(i);
			dataManager.query(dataFilter, comparator, result, null);
		}

		assertTrue("Results arrived", waitForResults(result, numRepeats));

		// Verify this against the expected test dataset
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Question count", numRepeats, results.size());

		// Ensure each item is a question
		for (QAModel item: results) {
			assertEquals(ItemType.Question, item.getItemType());
		}
	}

	/**
	 * Tests basic save functionality by saving an item, and then querying for it.
	 *
	 * If this or another write operation fails, the test server must be cleaned by running
	 * the tools/add_es_test_documents.bat script on Windows or Linux.
	 */
	public void test_DataSourceItemSave() {
		QuestionItem testQuestion = new QuestionItem(new UniqueId(), null, "author", new Date(), "body", 0, "title" );

		assertTrue("Save success", waitForItemSaved(testQuestion));

		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.addFieldFilter(
			QAModel.FIELD_ID, 
			testQuestion.getUniqueId().toString(), 
			DataFilter.FilterComparison.EQUALS);
		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, 1));

		// Verify this against the expected question
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Question count", 1, results.size());

		// Ensure each item is a question
		for (QAModel item: results) {
			assertEquals(ItemType.Question, item.getItemType());
		}

		boolean success = false;
		try {
			JestResult result = mJestClient.execute(new Delete.Builder(testQuestion.getUniqueId().toString())
		        .index(mEsServerIndex)
		        .type(testQuestion.getItemType().toString().toLowerCase())
		        .build());
			success = result.isSucceeded();
		} catch (Exception e) {
			
		}

		assertTrue("Delete item after test", success);
		waitForModOperation();
	}

	/**
	 * Tests that multiple items saved at the same time save correctly.
	 *
	 * If this or another write operation fails, the test server must be cleaned by running
	 * the tools/add_es_test_documents.bat script on Windows or Linux.
	 */
	public void test_DataSourceMultipleItemSave() {
		final UniqueId baseId = new UniqueId();
		List<QAModel> questionList = new ArrayList<QAModel>();
		
		questionList.add(new QuestionItem(baseId, null, "q1author1", new Date(), "test_DataSourceMultipleItemSave", 0, "title1" ));
		questionList.add(new CommentItem(new UniqueId(), baseId, "c1author2", new Date(), "test_DataSourceMultipleItemSave", 0));
		questionList.add(new AnswerItem(new UniqueId(), baseId, "a1author3", new Date(), "test_DataSourceMultipleItemSave", 0));
		questionList.add(new QuestionItem(new UniqueId(), null, "q2author4", new Date(), "test_DataSourceMultipleItemSave", 0, "title4" ));
		questionList.add(new QuestionItem(new UniqueId(), null, "q3author5", new Date(), "test_DataSourceMultipleItemSave", 0, "title5" ));

		// Save all items, waiting on the last operation to complete
		for (QAModel item: questionList) {
			System.out.println(String.format("Saving item %d (%s)", questionList.indexOf(item), item.getField(AuthoredItem.FIELD_AUTHOR)));
			assertTrue("Save success", waitForItemSaved(item));
		}

		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.addFieldFilter(
			AuthoredTextItem.FIELD_BODY,
			"test_DataSourceMultipleItemSave",
			DataFilter.FilterComparison.QUERY_STRING);
		dataManager.query(dataFilter, comparator, result, null);

		assertTrue("Results arrived", waitForResults(result, 5));

		// Verify this against the expected question
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Question count", questionList.size(), results.size());

		// Ensure each item is in the results
		assertTrue("Items found in results", results.containsAll(questionList));

		boolean success = true;
		try {
			for (QAModel item: questionList) {
				System.out.println(String.format("Deleting item %d", questionList.indexOf(item)));
				JestResult result = mJestClient.execute(new Delete.Builder(item.getUniqueId().toString())
			        .index(mEsServerIndex)
			        .type(item.getItemType().toString().toLowerCase())
			        .build());
				success &= null != result && result.isSucceeded();
			}
		} catch (Exception e) {
			success = false;
		}

		assertTrue("Delete items after test", success);
		waitForModOperation();
	}

	public void test_MoreLikeThisQuery() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		MoreLikeThisFilter mltDataFilter = new MoreLikeThisFilter();
		mltDataFilter.addMoreLikeThisObject(UniqueId.fromString("ecf5165525f1fde44c1ebbb55a0f2d1b"));
		mltDataFilter.setTypeFilter(ItemType.Question);
		dataManager.query(mltDataFilter, null, result, null);
		ESSearchBuilder builder = new ESSearchBuilder(mltDataFilter, null);
		System.out.println(builder.toString());

		assertTrue("Results arrived", waitForResults(result, 1));

		// Verify this against the expected test dataset
		List<QAModel> results = result.getCurrentResults();
		assertEquals("Question count", 1, results.size());

		// Ensure each item is a question
		assertEquals(UniqueId.fromString("703a31a2bf463ab46b7eba47a4801567"), results.get(0).getUniqueId());
	}
}

