package com.ualberta.team17.test.utility;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemComparator;

public class OrderedQAItemComparator implements IItemComparator {
	@Override
	public int compare(QAModel lhs, QAModel rhs) {
		if (lhs == rhs) return 0;
		return (((OrderedQAItem)lhs).getSeq() < ((OrderedQAItem)rhs).getSeq()) ? -1 : 1;
	}
}
