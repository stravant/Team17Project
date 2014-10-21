package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;

public class DataFilter {
	private ItemType mTypeFilter;
	private List<String> mFilterFields;
	private List<String> mFieldFilters;
	private List<FilterComparison> mFieldFilterComparisons;

	public DataFilter() {
		mFilterFields = new ArrayList<String>();
		mFieldFilters = new ArrayList<String>();
		mFieldFilterComparisons = new ArrayList<FilterComparison>();
	}

	public enum FilterComparison {
		CONTAINS,
		EQUALS,
		NOT_EQUAL,
		LESS_THAN,
		GREATER_THAN
	}

	public void setTypeFilter(ItemType type) {
		mTypeFilter = type;
	}

	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode) {
		mFilterFields.add(field);
		mFieldFilters.add(filter);
		mFieldFilterComparisons.add(comparisonMode);
	}

	public String getFilterString() {
		throw new UnsupportedOperationException();
	}
	
	public Boolean accept(QAModel item) {
		throw new UnsupportedOperationException();
	}
}
