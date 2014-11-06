package com.ualberta.team17.datamanager.comparators;

import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;

public class IdComparator implements IItemComparator {
	private SortDirection mCompareDirection = SortDirection.Ascending;

	@Override
	public int compare(QAModel lhs, QAModel rhs) {
		if (lhs.getUniqueId().equals(rhs.getUniqueId()))
			return 0;
		return lhs.getUniqueId().toString().compareTo(rhs.getUniqueId().toString());
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
