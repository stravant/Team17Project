package com.ualberta.team17.datamanager;

import java.util.List;

import com.ualberta.team17.IUnique;

public interface IncrementalObserver {
	/* A set of new items arrived, to be inserted after index |index| */
	public void itemsArrived(List<IUnique> item, int index);
}
