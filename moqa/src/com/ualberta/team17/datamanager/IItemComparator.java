package com.ualberta.team17.datamanager;

import java.util.Comparator;

import com.ualberta.team17.QAModel;

public interface IItemComparator extends Comparator<QAModel> {
	enum SortDirection {
		Ascending,
		Descending
	}

	public void setCompareDirection(SortDirection direction);
	public SortDirection getCompareDirection();

	public String getFilterField();
}
