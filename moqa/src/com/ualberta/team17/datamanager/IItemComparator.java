package com.ualberta.team17.datamanager;

import java.util.Comparator;

import com.ualberta.team17.QAModel;

/**
 * An interface that defines a comparator that can be used to sort a query and
 * an IncrementalResult.
 * 
 * @author michaelblouin
 */
public interface IItemComparator extends Comparator<QAModel> {
	enum SortDirection {
		Ascending,
		Descending
	}

	/**
	 * Sets the compare direction (Ascending or Descending) on the comparator.
	 * @param direction
	 */
	public void setCompareDirection(SortDirection direction);
	public SortDirection getCompareDirection();

	/**
	 * Gets the field of an object on which the IItemComparator operates.
	 * @return
	 */
	public String getFilterField();
}
