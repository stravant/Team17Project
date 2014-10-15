package com.ualberta.team17.datamanager;

import com.ualberta.team17.IUnique;

public interface IncrementalObserver {
	public void itemArrived(IUnique item, int index);
}
