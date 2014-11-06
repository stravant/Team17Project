package com.ualberta.team17.test.utility;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemComparator;

/**
 * A comparator for OrderedQAItems, that sorts based on the orderable identifier
 * provided by the user for the OrderedQAItems.
 */
public class OrderedQAItemComparator implements IItemComparator {
	private SortDirection mCompareDirection = SortDirection.Ascending;

	@Override
	public int compare(QAModel lhs, QAModel rhs) {
		return
			(mCompareDirection == SortDirection.Ascending ? 1 : -1)
			* directionalCompare(lhs, rhs);
	}

	private int directionalCompare(QAModel lhs, QAModel rhs) {
		if (lhs == rhs) 
			return 0;
		
		if (((OrderedQAItem)lhs).getSeq() == ((OrderedQAItem)rhs).getSeq())
			return 0;
		
		return (((OrderedQAItem)lhs).getSeq() < ((OrderedQAItem)rhs).getSeq()) ? -1 : 1;
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
		return "seq";
	}
}
