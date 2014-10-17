package com.ualberta.team17.datamanager;

import com.ualberta.team17.IQAModel;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IItemFilter;

public abstract class DataFilter implements IItemFilter {

	
	public void setTypeFilter(String type) {
		
	}
	
	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode) {
		
	}
	
	public String getDatabaseFilter() {
		return null;
	}
	
	public boolean accept(QAModel item) {
		return false;
	}
}
