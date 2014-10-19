package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;

public class DataFilter {
	private ItemType mTypeFilter;
	private List<FieldFilter> mFieldFilters;

	public DataFilter() {
		mFieldFilters = new ArrayList<FieldFilter>();
	}

	public enum FilterComparison {
		QUERY_STRING,
		EQUALS,
		NOT_EQUAL,
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL
	}

	public void setTypeFilter(ItemType type) {
		mTypeFilter = type;
	}

	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode) {
		mFieldFilters.add(new FieldFilter(field, filter, comparisonMode));
	}
	
	public List<FieldFilter> getFieldFilters() {
		return mFieldFilters;
	}
	
	public Boolean accept(QAModel item) {
		throw new UnsupportedOperationException();
	}
	
	public class FieldFilter {
		private String mField;
		private String mFilter;
		private FilterComparison mcomparisonMode;
		
		private FieldFilter(String field, String filter, FilterComparison comparisonMode) {
			mField = field;
			mFilter = filter;
			mcomparisonMode = comparisonMode;
		}
		
		public String getField() {
			return mField;
		}
		
		public String getFilter() {
			return mFilter;
		}

		public FilterComparison getComparisonMode() {
			return mcomparisonMode;
		}
	}
}
