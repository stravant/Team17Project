package com.ualberta.team17.datamanager.comparators;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;

public class AttachmentComparator implements IItemComparator {
	private SortDirection mCompareDirection = SortDirection.Ascending;

	@Override
	public int compare(QAModel lhs, QAModel rhs) {
		return
			(mCompareDirection == SortDirection.Ascending ? 1 : -1)
			* directionalCompare(lhs, rhs);
	}

	private int directionalCompare(QAModel lhs, QAModel rhs) {
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
		return "<num_attachment>";
	}
}
