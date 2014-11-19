package com.ualberta.team17.datamanager.comparators;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;

/**
 * A comparator under which all items compare equal.
 */
public class IdentityComparator implements IItemComparator {
	private SortDirection mCompareDirection = SortDirection.Ascending;

	@Override
	public int compare(QAModel lhs, QAModel rhs) {
		return 0;
	}

	@Override
	public void setCompareDirection(SortDirection direction) {
		mCompareDirection = direction;
	}

	@Override
	public SortDirection getCompareDirection() {
		return mCompareDirection;
	}

	@Override
	public String getFilterField() {
		return "id";
	}
}
