package com.ualberta.team17.datamanager.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.res.Resources;
import android.test.ActivityTestCase;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;
import com.ualberta.team17.datamanager.IDataLoadedListener;
import com.ualberta.team17.datamanager.IDataSourceAvailableListener;
import com.ualberta.team17.datamanager.IDataSourceManager;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.comparators.DateComparator;

public class NetworkDataSourceTest extends ActivityTestCase {
	final Integer maxWaitSeconds = 5;
	DataFilter dataFilter;
	IncrementalResult result;
	NetworkDataManager dataManager;

	/**
	 * Waits for at least numResults results to arrive in the IncrementalResult before returning, or before 
	 * the maximum amount of time (maxWaitSeconds) has surpassed.
	 *
	 * @param result The IncrementalResult to wait on.
	 * @param numResults The minimum number of results to wait for.
	 * @return True if the results were returned before the max wait time.
	 */
	private boolean waitForResults(final IncrementalResult result, final int numResults) {
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

		result.addObserver(new IIncrementalObserver() {
			@Override
			public void itemsArrived(List<QAModel> item, int index) {
				if (result.getCurrentResults().size() >= numResults) {
					lock.lock();
					condition.signal();
					lock.unlock();
				}
			}
		});

		lock.lock();

		boolean success = false;
		try {
			success = condition.await(maxWaitSeconds, TimeUnit.SECONDS) && dataManager.isAvailable();
		} catch (InterruptedException e) {

		}

		return success;
	}

	/**
	 * Sets up the UTs, instantiating a NetworkDataManager and a bare filter.
	 */
	public void setUp() {
		Resources resources = getInstrumentation().getTargetContext().getResources();

		dataManager = 
			new NetworkDataManager(
				resources.getString(R.string.esTestServer),
				resources.getString(R.string.esTestIndex));
		dataFilter = new DataFilter();
	}

	/**
	 * Tests that filtering by type=question works correctly, and only returns questions.
	 */
	public void test_FilterType_Question() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setTypeFilter(ItemType.Question);
		dataManager.query(dataFilter, comparator, result);

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
	public void test_FilterType_Answer() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setTypeFilter(ItemType.Answer);
		dataManager.query(dataFilter, comparator, result);

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
	 * Tests that filtering by type=answer works correctly, and only returns comments.
	 */
	public void test_FilterType_Comment() {
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.setTypeFilter(ItemType.Comment);
		dataManager.query(dataFilter, comparator, result);

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
	 * Tests that searching for a fields' contents works correctly, and only returns results whose body
	 * contain the search phrase.
	 */
	public void test_TextSearch() {
		String searchStr = "is";
		IItemComparator comparator = new DateComparator();
		result = new IncrementalResult(comparator);
		dataFilter.addFieldFilter(AuthoredTextItem.FIELD_BODY, searchStr, FilterComparison.QUERY_STRING);

		dataManager.query(dataFilter, comparator, result);

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
		dataManager.query(dataFilter, comparator, result);

		assertTrue("Results arrived", waitForResults(result, 9));

		// Verify this against the expected test dataset
		assertEquals("Item count", 9, result.getCurrentResults().size());

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
			add(new UniqueId("0db7fac6e3ceb0add59ab9cd030ffd7c"));
			add(new UniqueId("b2a7c2e2e477495eb77bdfca23dcc13a"));
			add(new UniqueId("ba0baf9d538a14f982c09c6f6c1f4666"));
		}};

		IItemComparator comparator = new DateComparator();
		comparator.setCompareDirection(SortDirection.Descending);

		dataFilter.setTypeFilter(ItemType.Answer);
		dataFilter.setMaxResults(3);

		result = new IncrementalResult(comparator);
		dataManager.query(dataFilter, comparator, result);

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
		test_FilterType_Question();

		dataFilter = new DataFilter();
		test_FilterType_Answer();

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

		dataManager.query(dataFilter, comparator, result);

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
		test_FilterType_Question();
		assertEquals(5, loadedListener.timesNotified);
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
		test_FilterType_Question();
		assertEquals(3, availableListener.timesNotified);

		assertTrue("Data source available", dataManager.isAvailable());
	}
}

