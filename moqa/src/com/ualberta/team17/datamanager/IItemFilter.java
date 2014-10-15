package com.ualberta.team17.datamanager;

import com.ualberta.team17.IUnique;

public interface IItemFilter {
	public enum FilterComparison {
		contains,
		equals,
		notequal,
		lessthan,
		greaterthan
	}
	
	public boolean accept(IUnique item);
	
	/* Returns null if a non-database filter */
	public String getDatabaseFilter();
}
