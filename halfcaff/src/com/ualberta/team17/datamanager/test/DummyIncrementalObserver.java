package com.ualberta.team17.datamanager.test;

import java.util.List;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IncrementalObserver;

public class DummyIncrementalObserver implements IncrementalObserver {
	private boolean notified;
	
	public DummyIncrementalObserver() {
		notified = false;
	}
	
	@Override
	public void itemsArrived(List<QAModel> item, int index) {
		notified = true;
	}
	
	public boolean wasNotified() {
		return notified;
	}
	
	public void acknowledgeNotified() {
		notified = false;
	}

}
