package com.ualberta.team17.datamanager;

import java.util.List;

import io.searchbox.core.Search.Builder;
import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.datamanager.comparators.IdentityComparator;
import com.ualberta.team17.datamanager.DataFilter.DataFilterType;

/**
 * The ESSearchBuilder is responsible for taking a DataFilter object and outputting
 * JSON that can be sent directly to Elastic Search to perform a query.
 * 
 * @author michaelblouin
 */
public class ESSearchBuilder {
	public static final Integer MAX_ES_RESULTS = 100;
	private JsonObject mQueryObject;
	private DataFilter mFilter;
	private IItemComparator mComparator;

	public ESSearchBuilder(DataFilter filter, IItemComparator comparator) {
		mFilter = filter;
		mComparator = comparator; 
	}

	/**
	 * Applies the required parameters to the supplied Jest builder.
	 * 
	 * @param builder The builder to apply parameters to.
	 * @return
	 */
	public Builder getBuilder(Builder builder) {
		Integer maxResults = mFilter.getMaxResults();

		if (null != maxResults) {
			if (maxResults > ESSearchBuilder.MAX_ES_RESULTS) {
				maxResults = ESSearchBuilder.MAX_ES_RESULTS;
			}

			builder.setParameter("size", maxResults);

			if (null != mFilter.getPage())
				builder.setParameter("from", maxResults * mFilter.getPage());
		}

		if (null != mFilter.getTypeFilter()) {
			builder.addType(mFilter.getTypeFilter().toString().toLowerCase());
		}

		return builder;
	}

	/**
	 * Gets a Jest query builder that contains all required non-query parameters.
	 * @return
	 */
	public Builder getBuilder() {
		return getBuilder(new Builder(toString()));
	}

	private JsonObject getJsonObjectWithProperty(String property, JsonElement value) {
		JsonObject obj = new JsonObject();
		obj.add(property, value);
		return obj;
	}
	
	private JsonObject getJsonObjectWithProperty(String property, String value) {
		JsonObject obj = new JsonObject();
		obj.addProperty(property, value);
		return obj;
	}

	@SuppressLint("DefaultLocale")
	private void addTypeFilter(JsonObject obj, String group, ItemType type) {
		if (!obj.has(group)) {
			obj.add(group, new JsonArray());
		}

		obj.getAsJsonArray(group).add(
			getJsonObjectWithProperty("type",
				getJsonObjectWithProperty("value", type.toString().toLowerCase()))
		);
	}

	private void addTermFilter(JsonObject obj, String group, DataFilter.FieldFilter filter) {
		if (!obj.has(group)) {
			obj.add(group, new JsonArray());
		}
		
		obj.getAsJsonArray(group).add(
			getJsonObjectWithProperty("term", 
				getJsonObjectWithProperty(filter.getField(), filter.getFilter()))
		);
	}

	private void addRangeFilter(JsonObject obj, String group, DataFilter.FieldFilter filter, String comparison) {
		if (!obj.has(group)) {
			obj.add(group, new JsonArray());
		}
		
		obj.getAsJsonArray(group).add(
			getJsonObjectWithProperty("range", 
				getJsonObjectWithProperty(filter.getField(), 
					getJsonObjectWithProperty(comparison, filter.getFilter())))
		);
	}

	/**
	 * Gets a JsonObject that represents the query that should be sent to elastic search.
	 * 
	 * Note that this build the query object and caches it. After this has been called once, 
	 * you cannot change the ESSearchBuilder in any way -- the changes will not be reflected in the
	 * query object.
	 * 
	 * @return
	 */
	public JsonObject getJsonQueryObject() {
		if (null != mQueryObject) {
			return mQueryObject;
		}

		// Build the Filter
		JsonObject filteredQueryObj = new JsonObject();
		JsonObject boolFilterObj = new JsonObject();

		for (DataFilter.FieldFilter filter: mFilter.getFieldFilters()) {
			switch (filter.getComparisonMode()) {
				case QUERY_STRING:
					if (DataFilterType.MORE_LIKE_THIS == mFilter.getDataFilterType()) {
						throw new UnsupportedOperationException("Cannot perform a QUERY_STRING search on a MORE_LIKE_THIS query.");
					}

					if (!filteredQueryObj.has("query")) {
						filteredQueryObj.add("query", new JsonObject());
					}

					filteredQueryObj.getAsJsonObject("query").add("query_string", 
							getJsonObjectWithProperty("query", filter.getFilter()));
					break;

				case EQUALS:
					addTermFilter(boolFilterObj, filter.getCombinationMode().toString().toLowerCase(), filter);
					break;

				case GREATER_THAN:
					addRangeFilter(boolFilterObj, filter.getCombinationMode().toString().toLowerCase(), filter, "gt");
					break;

				case GREATER_THAN_OR_EQUAL:
					addRangeFilter(boolFilterObj, filter.getCombinationMode().toString().toLowerCase(), filter, "gte");
					break;

				case LESS_THAN:
					addRangeFilter(boolFilterObj, filter.getCombinationMode().toString().toLowerCase(), filter, "lt");
					break;

				case LESS_THAN_OR_EQUAL:
					addRangeFilter(boolFilterObj, filter.getCombinationMode().toString().toLowerCase(), filter, "lte");
					break;

				case NOT_EQUAL:
					addTermFilter(boolFilterObj, "must_not", filter);
					break;
			}
		}

		if (mFilter instanceof TopUpvotedDataFilter) {
			TopUpvotedDataFilter filter = (TopUpvotedDataFilter)mFilter;
			JsonObject queryObj = getJsonObjectWithProperty("type", filter.getChildFilterType());
			queryObj.add("query", getJsonObjectWithProperty("match_all", new JsonObject()));
			queryObj.addProperty("score", filter.getScoringType());
			queryObj.addProperty("factor", filter.getScoringFactor());
			filteredQueryObj.add("query", getJsonObjectWithProperty("top_children", queryObj));

		} else if (mFilter instanceof MoreLikeThisFilter) {
			MoreLikeThisFilter mltFilter = (MoreLikeThisFilter)mFilter;
			Gson gson = DataManager.getGsonObject();
			JsonObject mlt = new JsonObject();

			List<JsonObject> documents = mltFilter.getMoreLikeThisDocs();
			if (null != documents) {
				mlt.add("docs", gson.toJsonTree(documents));
			}

			List<String> ids = mltFilter.getMoreLikeThisIds();
			if (null != ids) {
				mlt.add("ids", gson.toJsonTree(mltFilter.getMoreLikeThisIds(), List.class));
			}

			mlt.add("fields", gson.toJsonTree(mltFilter.getMLTFields(), List.class));
			mlt.addProperty("min_doc_freq", MoreLikeThisFilter.MLTMinDocFreq);
			mlt.addProperty("min_term_freq", MoreLikeThisFilter.MLTMinTermFreq);
			filteredQueryObj.add("query", getJsonObjectWithProperty("more_like_this", mlt));

		} else if (!filteredQueryObj.has("query")) {
			filteredQueryObj.add("query", getJsonObjectWithProperty("match_all", new JsonObject()));
		}

		if (!boolFilterObj.has("must") && !boolFilterObj.has("must_not") && !boolFilterObj.has("should")) {
			mQueryObject = filteredQueryObj;
		} else {
			filteredQueryObj.add("filter", getJsonObjectWithProperty("bool", boolFilterObj));

			mQueryObject = getJsonObjectWithProperty("query", getJsonObjectWithProperty("filtered", filteredQueryObj));
		}

		if (null != mComparator && !(mComparator instanceof IdentityComparator)) {
			JsonArray sortArray = new JsonArray();

			sortArray.add(
				getJsonObjectWithProperty(
					mComparator.getFilterField(), 
					mComparator.getCompareDirection() == IItemComparator.SortDirection.Ascending ? "asc" : "desc" ));

			mQueryObject.add("sort", sortArray);
		}

		return mQueryObject;
	}

	@Override
	public String toString() {
		return getJsonQueryObject().toString();
	}
}