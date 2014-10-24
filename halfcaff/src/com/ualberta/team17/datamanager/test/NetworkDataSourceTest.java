package com.ualberta.team17.datamanager.test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.ESSearchBuilder;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.comparators.DateComparator;

import junit.framework.TestCase;

public class NetworkDataSourceTest extends TestCase {
	NetworkDataManager dataManager;

	public void setUp() {
		dataManager = new NetworkDataManager("http://estest.michaelblouin.ca:9182", "moqatest");
	}

	public void test_Filter_Question() throws InterruptedException {
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();
		DataFilter dataFilter = new DataFilter();
		dataFilter.setTypeFilter(ItemType.Question);

		IItemComparator comparator = new DateComparator();

		IncrementalResult result = null;//new IncrementalResult(comparator);
		dataManager.query(dataFilter, comparator, result);

		result.addObserver(new IIncrementalObserver() {
			@Override
			public void itemsArrived(List<QAModel> item, int index) {
				lock.lock();
				condition.notify();
				lock.unlock();
			}
		});

		lock.lock();

		condition.await(5, TimeUnit.SECONDS);

		// Verify this against the expected test dataset
		assertEquals("Question count", 3, result.getCurrentResults().size());
	}
}

