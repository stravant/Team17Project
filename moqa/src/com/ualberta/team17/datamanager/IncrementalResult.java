package com.ualberta.team17.datamanager;

import java.util.List;

import com.ualberta.team17.IQAModel;

public abstract class IncrementalResult {
	public synchronized void addObserver(IncrementalObserver observer) {
		
	}
	
	public synchronized void addObserver(IncrementalObserver observer, String type) {
		
	}
	
	public synchronized void addObject(IQAModel object) {
		
	}
	
	public List<IQAModel> getCurrentResults() {
		return null;
	}
	
	public List<IQAModel> getCurrentResultsOfType(String type) {
		return null;
	}
}
