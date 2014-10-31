package com.ualberta.team17.datamanager.comparators;

import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemComparator;

public class UpvoteComparator implements IItemComparator {
	private SortDirection mCompareDirection = SortDirection.Ascending;

	@Override
	public int compare(QAModel lhs, QAModel rhs) {
		return
			(mCompareDirection == SortDirection.Ascending ? 1 : -1)
			* directionalCompare(lhs, rhs);
	}

	private int directionalCompare(QAModel lhs, QAModel rhs) {
		if (null == lhs) {
			return -1;
		}

		if (null == rhs) {
			return 1;
		}

		if ((lhs instanceof AuthoredTextItem) && (rhs instanceof AuthoredTextItem)) {
			AuthoredTextItem left = (AuthoredTextItem)lhs;
			AuthoredTextItem right = (AuthoredTextItem)rhs;

			return Integer.valueOf(left.getUpvoteCount()).compareTo(right.getUpvoteCount());
		}

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
		return "<num_upvotes>";
	}
}
