package com.ualberta.team17.datamanager;

import java.util.List;

import com.ualberta.team17.QAModel;

public interface IncrementalObserver {
	/* A set of new items arrived, to be inserted after index |index| */
	public void itemsArrived(List<QAModel> item, int index);
}
