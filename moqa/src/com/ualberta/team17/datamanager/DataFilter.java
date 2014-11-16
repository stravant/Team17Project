package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;

public class DataFilter {
	private ItemType mTypeFilter;
	private List<FieldFilter> mFieldFilters;
	private Integer mMaxResults;
	private Integer mResultsPage;

	public class FieldFilter {
		private String mField;
		private String mFilter;
		private FilterComparison mComparisonMode;
		private CombinationMode mCombinationMode;

		private FieldFilter(String field, String filter, FilterComparison comparisonMode, CombinationMode combinationMode) {
			mField = field;
			mFilter = filter;

			if (null == comparisonMode) {
				comparisonMode = FilterComparison.EQUALS;
			}
			mComparisonMode = comparisonMode;

			if (null == combinationMode) {
				combinationMode = CombinationMode.MUST;
			}
			mCombinationMode = combinationMode;
		}

		public String getField() {
			return mField;
		}

		public String getFilter() {
			return mFilter;
		}

		public FilterComparison getComparisonMode() {
			return mComparisonMode;
		}

		public CombinationMode getCombinationMode() {
			return mCombinationMode;
		}
	}

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

	public enum CombinationMode {
		MUST,
		SHOULD
	}

	public void setTypeFilter(ItemType type) {
		mTypeFilter = type;
	}

	public ItemType getTypeFilter() {
		return mTypeFilter;
	}

	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode) {
		addFieldFilter(field, filter, comparisonMode, null);
	}

	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode, CombinationMode combinationMode) {
		mFieldFilters.add(new FieldFilter(field, filter, comparisonMode, combinationMode));
	}
	
	public List<FieldFilter> getFieldFilters() {
		return mFieldFilters;
	}
	
	// TODO: Only accepts items exact equality right now
	public Boolean accept(QAModel item) {
		// Type filter
		if (getTypeFilter() != null && item.getItemType() != getTypeFilter())
			return false;
		
		// For each filter
		for (FieldFilter f: mFieldFilters) {
			Object value = item.getField(f.getField());
			
			// If this object does not have the field, it can't pass
			if (value == null && f.getField() != null)
				return false;
			
			// See how we compare
			int cmp = f.getFilter().compareTo(value.toString());
			
			// Check if we pass given the comparator to use
			if (f.getComparisonMode() == FilterComparison.EQUALS) {
				if (cmp != 0) return false;
			} else if (f.getComparisonMode() == FilterComparison.NOT_EQUAL) {
				if (cmp == 0) return false;
			} else {
				throw new UnsupportedOperationException("TODO: Implement more comparators");
			}	
		}
		
		// Passed all filters, accept
		return true;
	}

	public Integer getMaxResults() {
		return mMaxResults;
	}

	public void setMaxResults(Integer maxResults) {
		mMaxResults = maxResults;
	}

	public Integer getPage() {
		return mResultsPage;
	}

	public void setPage(Integer page) {
		mResultsPage = page;
	}
}
