package com.ualberta.team17.datamanager;

import java.util.List;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.ItemType;

/*
 * A synchronization / collection object that represents the results of a query.
 * The worker tasks involved in the query will add results to the IncrementalResult
 * as they are found. When results are added in this way, the a set of observers will
 * be notified.
 */
public class IncrementalResult {
	// Note: We can't just use synchronized willy-nilly here when there is observer pattern
	// involved, since we can end up with deadlocks of:
	// addObject() --> notifies --> notified object calls getCurrentResults() --> this is still locked, deadlock
	// Finer grained locking control is needed.
	
	public IncrementalResult(IItemComparator sort) {
		
	}
	
	public void addObserver(IncrementalObserver observer) {
		
	}
	
	public void addObserver(IncrementalObserver observer, ItemType type) {
		
	}
	
	public void addObjects(List<QAModel> objects) {
		
	}
	
	public int getCurrentResultCount() {
		return 0;
	}
	
	public QAModel getCurrentResult(int index) {
		return null;
	}
	
	public List<QAModel> getCurrentResults() {
		return null;
	}
	
	public List<QAModel> getCurrentResultsOfType(ItemType type) {
		return null;
	}
}
