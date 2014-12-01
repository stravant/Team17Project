package com.ualberta.team17.datamanager;

import io.searchbox.core.Search.Builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ualberta.team17.DateStringFormat;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;

/**
 * A DataFilter represents a Data Manager query. Combined with a Comparator it is everything
 * you need to query either the local or network data manager (although it has knowledge of neither.)
 * 
 * @author michaelblouin
 */
public class DataFilter {
	public static final int DEFAULT_NUM_RESULTS = 10;
	private ItemType mTypeFilter;
	private List<FieldFilter> mFieldFilters;
	private Integer mMaxResults = DEFAULT_NUM_RESULTS;
	private Integer mResultsPage;

	private DataFilterType mFilterType = DataFilterType.QUERY;

	/**
	 * Describes all of the filtering information for a field in the DataFilter.
	 * 
	 * @author michaelblouin
	 */
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

	public enum DataFilterType {
		QUERY,
		MORE_LIKE_THIS
	}

	/**
	 * Sets the item type filter that must be satisfied. It is preferred to use this method than
	 * to query for item type using a field filter, as it will execute faster.
	 * 
	 * @param type
	 */
	public void setTypeFilter(ItemType type) {
		mTypeFilter = type;
	}

	/**
	 * Gets the item type filter.
	 * @return
	 */
	public ItemType getTypeFilter() {
		return mTypeFilter;
	}

	/**
	 * Adds the field to the data filter, using the specified comparison mode.
	 * 
	 * This will cause items to be returned that contain the field matching the arguments.
	 * 
	 * @param field
	 * @param filter
	 * @param comparisonMode
	 */
	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode) {
		addFieldFilter(field, filter, comparisonMode, null);
	}

	/**
	 * Adds the field to the data filter, using the specified comparison mode and combination mode.
	 * 
	 * The combination mode specifies how this filter combines with the other filter types. With the MUST
	 * combination mode the filter must be satisfied by every object in the result. Using the SHOULD combination
	 * mode objects do not need to satisfy the requirement UNLESS it is the only filter on the object.
	 * 
	 * If an IdentityComparator is used, items matching one or more SHOULD filters will be boosted above those
	 * that do not match.
	 * 
	 * @param field
	 * @param filter
	 * @param comparisonMode
	 * @param combinationMode
	 */
	public void addFieldFilter(String field, String filter, FilterComparison comparisonMode, CombinationMode combinationMode) {
		mFieldFilters.add(new FieldFilter(field, filter, comparisonMode, combinationMode));
	}

	public void setDataFilterType(DataFilterType type) {
		mFilterType = type;
	}

	public DataFilterType getDataFilterType() {
		return mFilterType;
	}

	/**
	 * Returns the list of FieldFilters that make up the DataFilter.
	 * @return
	 */
	public List<FieldFilter> getFieldFilters() {
		return mFieldFilters;
	}
	
	/**
	 * Checks if the given item matches the Data Filter. 
	 * 
	 * Note that not all of the elastic search functionality is available in this method.
	 * 
	 * @param item
	 * @return Returns True if the item matches, and false if not.
	 */
	public Boolean accept(QAModel item) {
		// Accepted items must follow the following rules:
		//  - If there is a type filter, it must be satisfied
		//  - All MUST filters must be satisfied
		//  - If there are no MUST filters, then at least one SHOULD filter must be satisfied

		// Always satisfy must filter.
		if (getTypeFilter() != null && item.getItemType() != getTypeFilter())
			return false;
		
		// Have we matched a must / should item?
		boolean matchedShould = false; // Until contested
		boolean matchedMust = false; // Until contested
		
		// If there are no filters, trivially accept (we already passed the item type test).
		if (mFieldFilters.isEmpty()) {
			return true;
		}
		
		// For each filter
		for (FieldFilter f: mFieldFilters) {
			// Get the combination type of the filter
			CombinationMode combineMode = f.getCombinationMode();
			
			// If it's a should comparison, and we already passed a must comparison,
			// then there's no need to check it, as it won't effect the outcome.
			if (combineMode == CombinationMode.SHOULD && matchedMust) {
				continue;
			}
			
			// Get the value of the field
			Object value = item.getField(f.getField());
			
			// If this object does not have the field, it can't pass
			if (value == null && f.getField() != null) {
				if (combineMode == CombinationMode.MUST) {
					return false;
				} else {
					continue;
				}
			}
			
			// Query string? We handle this case differently
			if (f.getComparisonMode() == FilterComparison.QUERY_STRING) {
				// We need to check if the value contains the filter string
				if (value != null && f.getFilter() != null && value instanceof String) {
					// We have a valid string and field to filter with
					if (((String)value).toLowerCase().contains(f.getFilter())) {
						if (combineMode == CombinationMode.MUST) {
							matchedMust = true;
						} else {
							matchedShould = true;
						}
					} else {
						if (combineMode == CombinationMode.MUST) {
							return false;
						} else {
							continue;
						}
					}
				} else {
					if (combineMode == CombinationMode.MUST) {
						return false;
					} else {
						continue;
					}
				}
			} else {
				// Otherwise, we need to decode the filter into the type of interest
				String filterString = f.getFilter();
				int cmpResult = 0;
				boolean didCompare = false; // Until contested
				
				// Compare via a similar type if able
				if (value instanceof Integer) {
					// Compare integers
					try {
						Integer i = Integer.parseInt(filterString);
						cmpResult = ((Integer)value).compareTo(i);
						didCompare = true;
					} catch (NumberFormatException e) {}
				} else if (value instanceof Date) {
					// Compare dates
					Date d = DateStringFormat.parseDate(filterString);
					cmpResult = ((Date)value).compareTo(d);
					didCompare = true;
				}
				
				// If we didn't compare yet, fallthrough compare as strings
				if (!didCompare) {
					cmpResult = value.toString().compareTo(filterString);
				}
				
				// Now, see if the comparison result is what we wanted
				boolean pass;
				switch (f.getComparisonMode()) {
				case EQUALS:
					pass = (cmpResult == 0);
					break;
				case NOT_EQUAL:
					pass = (cmpResult != 0);
					break;
				case GREATER_THAN:
					pass = (cmpResult > 0);
					break;
				case GREATER_THAN_OR_EQUAL:
					pass = (cmpResult >= 0);
					break;
				case LESS_THAN:
					pass = (cmpResult < 0);
					break;
				case LESS_THAN_OR_EQUAL:
					pass = (cmpResult <= 0);
					break;
				default: /* ACTUALLY DEAD CODE, Already know that comparison mode != QUERY_STRING,
				            Java just can't understand the code path */
					pass = false;
				}
				
				// Did we pass?
				if (pass) {
					if (combineMode == CombinationMode.MUST) {
						matchedMust = true;
					} else {
						matchedShould = true;
					}
				} else {
					if (combineMode == CombinationMode.MUST) {
						return false;
					} else {
						continue;
					}
				}
			}
		} // End for: mFieldFilters
		
		// Did we match at least one must?
		if (matchedMust) {
			// Matched a must, and if we failed matching a must we would already have
			// returned false, so we matched all musts, we can accept this item.
			return true;
		} else {
			// No musts matched. We can accept this if we matched at least one should.
			return matchedShould;
		}
	}

	/**
	 * Gets the maximum number of results to be returned in a single page by the filter.
	 * @return
	 */
	public Integer getMaxResults() {
		return mMaxResults;
	}

	/**
	 * Sets the maximum number of results to be returned in a single page by the filter.
	 * @param maxResults
	 */
	public void setMaxResults(Integer maxResults) {
		mMaxResults = maxResults;
	}

	/**
	 * Gets the current page number of the data filter.
	 * @return
	 */
	public Integer getPage() {
		return mResultsPage;
	}

	/**
	 * Sets the current page number for the data filter.
	 * 
	 * Note that it is perfectly fine to use the same DataFilter instance to query subsequent pages
	 * in a single query (ie the filter need not be reconstructed.)
	 * 
	 * @param page
	 */
	public void setPage(Integer page) {
		mResultsPage = page;
	}
	
	/**
	 * Applies the this data filter's parameters to the supplied Jest builder.
	 */
	public void decorateBuilder(Builder builder) {
		Integer maxResults = getMaxResults();
		if (null != maxResults) {
			if (maxResults > ESSearchBuilder.MAX_ES_RESULTS) {
				maxResults = ESSearchBuilder.MAX_ES_RESULTS;
			}
	
			builder.setParameter("size", maxResults);
	
			if (null != getPage())
				builder.setParameter("from", maxResults * getPage());
		}
	
		if (null != getTypeFilter()) {
			builder.addType(getTypeFilter().toString().toLowerCase());
		}
	}
}
