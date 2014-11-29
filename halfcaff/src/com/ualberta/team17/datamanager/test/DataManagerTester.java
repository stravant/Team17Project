package com.ualberta.team17.datamanager.test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IDataSourceAvailableListener;
import com.ualberta.team17.datamanager.IDataSourceManager;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.view.QuestionListActivity;

import android.test.ActivityInstrumentationTestCase2;

public abstract class DataManagerTester<T extends IDataSourceManager> extends ActivityInstrumentationTestCase2<QuestionListActivity> {
	public DataManagerTester() {
		super(QuestionListActivity.class);
	}

	// Max amount of time to wait for elastic search to return a query
	final Integer maxWaitSeconds = 4;

	// Time to wait after an operation that modifies the index before running another query
	final Integer maxModOperationWaitMs = 1000;

	T dataManager;
	UserContext userContext;
	DataFilter dataFilter;
	IncrementalResult result;

	/**
	 * Waits for at least numResults results to arrive in the IncrementalResult before returning, or before 
	 * the maximum amount of time (maxWaitSeconds) has surpassed.
	 *
	 * @param result The IncrementalResult to wait on.
	 * @param numResults The minimum number of results to wait for.
	 * @return True if the results were returned before the max wait time.
	 */
	public boolean waitForResults(final IncrementalResult result, final int numResults) {
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();

		lock.lock();

		if (result.getCurrentResults().size() >= numResults) {
			return true;
		}

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

		boolean success = false;
		try {
			success = condition.await(maxWaitSeconds, TimeUnit.SECONDS) && dataManager.isAvailable();
		} catch (InterruptedException e) {

		}

		lock.unlock();
		return success;
	}
}
