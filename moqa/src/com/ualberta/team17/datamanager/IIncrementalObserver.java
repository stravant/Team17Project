package com.ualberta.team17.datamanager;

import java.util.List;

import com.ualberta.team17.QAModel;

/**
 * An IIncrementalObserver is used to watch for new results within
 * an IncrementalResult.
 * 
 * @author michaelblouin
 */
public interface IIncrementalObserver {
	/* A set of new items arrived, to be inserted after index |index| */
	/**
	 * The itemsArrived method is called when items are added to an observed IncrementalResult.
	 * 
	 * @param items The list of items that were added, starting at the given index.
	 * @param index The index at which the items were added
	 */
	public void itemsArrived(List<QAModel> items, int index);
}
