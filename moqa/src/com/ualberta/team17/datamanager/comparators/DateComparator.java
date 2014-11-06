package com.ualberta.team17.datamanager.comparators;

import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;

public class DateComparator implements IItemComparator {
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

		if ((lhs instanceof AuthoredItem) && (rhs instanceof AuthoredItem)) {
			AuthoredItem left = (AuthoredItem)lhs;
			AuthoredItem right = (AuthoredItem)rhs;

			if (left.getDate() == null) {
				if (right.getDate() == null) {
					return 0;
				}

				return -compare(rhs, lhs);
			}

			if (right.getDate() == null) {
				return 1;
			}

			return ((AuthoredItem)lhs).getDate().compareTo(((AuthoredItem)rhs).getDate());
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
		return AuthoredItem.FIELD_DATE;
	}
}
