package com.ualberta.team17.datamanager;

import java.util.List;

import com.ualberta.team17.IUnique;
import com.ualberta.team17.ItemType;

public abstract class IncrementalResult {
	// Note: We can't just use synchronized willy-nilly here when there is observer pattern
	// involved, since we can end up with deadlocks of:
	// addObject() --> notifies --> notified object calls getCurrentResults() --> this is still locked, deadlock
	
	public void addObserver(IncrementalObserver observer) {
		
	}
	
	public void addObserver(IncrementalObserver observer, ItemType type) {
		
	}
	
	public void addObject(IUnique object, int index) {
		
	}
	
	public IUnique getCurrentResult(int index) {
		return null;
	}
	
	public List<IUnique> getCurrentResults() {
		return null;
	}
	
	public List<IUnique> getCurrentResultsOfType(ItemType type) {
		return null;
	}
}
