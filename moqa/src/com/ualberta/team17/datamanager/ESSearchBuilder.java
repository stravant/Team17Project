package com.ualberta.team17.datamanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ESSearchBuilder {
	JsonObject queryObject;
	DataFilter mFilter;
	IItemComparator mComparator;
	
	public ESSearchBuilder(DataFilter filter, IItemComparator comparator) {
		mFilter = filter;
		mComparator = comparator;
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
	
	public JsonObject getJsonQueryObject() {
		if (null != queryObject) {
			return queryObject;
		}

		// Build the Filter
		JsonObject filteredQueryObj = new JsonObject();
		JsonObject boolFilterObj = new JsonObject();

		for (DataFilter.FieldFilter filter: mFilter.getFieldFilters()) {
			switch (filter.getComparisonMode()) {
				case QUERY_STRING:
					if (!filteredQueryObj.has("query")) {
						filteredQueryObj.add("query", new JsonObject());
					}
					
					filteredQueryObj.getAsJsonObject("query").add("query_string", 
							getJsonObjectWithProperty("query", filter.getFilter()));
					break;
					
				case EQUALS:
					addTermFilter(boolFilterObj, "must", filter);
					break;
					
				case GREATER_THAN:
					addRangeFilter(boolFilterObj, "must", filter, "gt");
					break;
					
				case GREATER_THAN_OR_EQUAL:
					addRangeFilter(boolFilterObj, "must", filter, "gte");
					break;
					
				case LESS_THAN:
					addRangeFilter(boolFilterObj, "must", filter, "lt");
					break;
					
				case LESS_THAN_OR_EQUAL:
					addRangeFilter(boolFilterObj, "must", filter, "lte");
					break;
					
				case NOT_EQUAL:
					addTermFilter(boolFilterObj, "must_not", filter);
					break;
			}
		}
		
		if (!filteredQueryObj.has("query")) {
			filteredQueryObj.add("query", getJsonObjectWithProperty("match_all", new JsonObject()));
		}

		filteredQueryObj.add("filter", getJsonObjectWithProperty("bool", boolFilterObj));

		queryObject = getJsonObjectWithProperty("filtered", filteredQueryObj);
		return queryObject;
	}

	@Override
	public String toString() {
		return getJsonQueryObject().toString();
	}
}