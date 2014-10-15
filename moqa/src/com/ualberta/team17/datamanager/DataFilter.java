package com.ualberta.team17.datamanager;

import com.ualberta.team17.IQAModel;

public abstract class DataFilter {
	public enum FilterComparison {
		contains,
		equals,
		notequal,
		lessthan,
		greaterthan
	}
	
	public void setTypeFilter(String type) {
		
	}
	
	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode) {
		
	}
	
	public String getFilterString() {
		return null;
	}
	
	public Boolean accept(IQAModel item) {
		return false;
	}
}
