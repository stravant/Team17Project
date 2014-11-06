package com.ualberta.team17.datamanager.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.ESSearchBuilder;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;
import com.ualberta.team17.datamanager.comparators.DateComparator;

import junit.framework.TestCase;

/**
 * Tests the ESSearchBuilder class, that is responsible for taking data filters and sorting information
 * and turning it into a valid elastic search query.
 * 
 * @author michaelblouin
 */
public class ESSearchBuilderTest extends TestCase {
	/**
	 * Asserts that the given query object has a seemingly correct structure.
	 * 
	 * Supports blank and filtered DataFilters.
	 * 
	 * @param obj The JsonObject to test.
	 */
	private void assertValidQueryObject(JsonObject obj) {
		JsonObject queryObject = obj.getAsJsonObject("query");
		
		// Check the filter/query structure
		if (!queryObject.has("filtered")) {
			
			// This may be a filter-less data filter
			assertTrue(
				null != queryObject.getAsJsonObject("match_all")
				|| null != queryObject.getAsJsonObject("query_string"));
		} else {
			JsonObject filterObject = obj.getAsJsonObject("query").getAsJsonObject("filtered");
			assertNotNull("Has filter property", filterObject); 
			assertNotNull("Has filtered.query property", filterObject.getAsJsonObject("query"));
			assertNotNull("Has filtered.filter property", filterObject.getAsJsonObject("filter"));
			
			JsonObject boolFilterObject = filterObject.getAsJsonObject("filter").getAsJsonObject("bool");
			assertNotNull("Has filtered.filter.bool property", boolFilterObject);
			
			assertTrue(
				null != boolFilterObject.getAsJsonArray("must")
				|| null != boolFilterObject.getAsJsonArray("must_not")
				|| null != boolFilterObject.getAsJsonArray("should"));
		}

		if (queryObject.has("sort")) {
			JsonArray sort = queryObject.getAsJsonArray();

			assertNotNull("Sort is array", sort);
			assertTrue("Sort query has sort items", sort.size() > 0);

			for (JsonElement element: sort) {
				JsonObject sortObj = element.getAsJsonObject();
				assertNotNull(sortObj);
			}
		}
	}

	/**
	 * Tests a data filter having an equals filter.
	 */
	public void test_Equals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.EQUALS);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.toString().contains("{\"must\":[{\"term\":{\"testField1\":\"Hello World!\"}}]}"));
	}

	/**
	 * Tests a data filter having a not equals filter.
	 */
	public void test_NotEquals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.NOT_EQUAL);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.toString().contains("{\"must_not\":[{\"term\":{\"testField1\":\"Hello World!\"}}]}"));
	}

	/**
	 * Tests the format of a data filter having multiple equals filters.
	 */
	public void test_Multiple_Equals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.EQUALS);
		dataFilter.addFieldFilter("testField2", "This is a second filter", DataFilter.FilterComparison.EQUALS);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);

		JsonArray mustObject = searchBuilderResult
			.getAsJsonObject("query")
			.getAsJsonObject("filtered")
			.getAsJsonObject("filter")
			.getAsJsonObject("bool")
			.getAsJsonArray("must");

		boolean foundTestField1 = false;
		boolean foundTestField2 = false;

		for (JsonElement element: mustObject) {
			if (!element.isJsonObject()) {
				continue;
			}

			JsonObject obj = element.getAsJsonObject();
			if (!obj.has("term")) {
				continue;
			}

			JsonObject termObj = obj.getAsJsonObject("term");
			if (termObj.has("testField1")) {
				assertEquals("First filter text", "Hello World!",  termObj.get("testField1").getAsString());
				foundTestField1 = true;
			} else if (termObj.has("testField2")) {
				assertEquals("Second filter text", "This is a second filter",  termObj.get("testField2").getAsString());
				foundTestField2 = true;
			}
		}

		assertTrue("Found testField1", foundTestField1);
		assertTrue("Found testField2", foundTestField2);
	}

	/**
	 * Tests the format of a data filter having multiple not equals queries.
	 */
	public void test_Multiple_NotEquals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.NOT_EQUAL);
		dataFilter.addFieldFilter("testField2", "This is a second filter", DataFilter.FilterComparison.NOT_EQUAL);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);

		JsonArray mustObject = searchBuilderResult
			.getAsJsonObject("query")
			.getAsJsonObject("filtered")
			.getAsJsonObject("filter")
			.getAsJsonObject("bool")
			.getAsJsonArray("must_not");

		boolean foundTestField1 = false;
		boolean foundTestField2 = false;

		for (JsonElement element: mustObject) {
			if (!element.isJsonObject()) {
				continue;
			}

			JsonObject obj = element.getAsJsonObject();
			if (!obj.has("term")) {
				continue;
			}

			JsonObject termObj = obj.getAsJsonObject("term");
			if (termObj.has("testField1")) {
				assertEquals("First filter text", "Hello World!",  termObj.get("testField1").getAsString());
				foundTestField1 = true;
			} else if (termObj.has("testField2")) {
				assertEquals("Second filter text", "This is a second filter",  termObj.get("testField2").getAsString());
				foundTestField2 = true;
			}
		}

		assertTrue("Found testField1", foundTestField1);
		assertTrue("Found testField2", foundTestField2);
	}

	/**
	 * Tests the proper output when the data filter is empty, and a sort order is defined as ascending.
	 */
	public void test_SortEmptyFilterAsc() {
		DataFilter dataFilter = new DataFilter();
		IItemComparator comparator = new DateComparator();
		comparator.setCompareDirection(SortDirection.Ascending);
		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, comparator);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.has("sort"));

		assertTrue("Correct sort ordering", 
			searchBuilderResult
				.getAsJsonArray("sort")
				.get(0)
				.getAsJsonObject()
				.get(comparator.getFilterField())
				.getAsString()
				.equals("asc"));
	}

	/**
	 * Tests the proper output when the data filter is empty, and a sort order is defined as descending.
	 */
	public void test_SortEmptyFilterDesc() {
		DataFilter dataFilter = new DataFilter();
		IItemComparator comparator = new DateComparator();
		comparator.setCompareDirection(SortDirection.Descending);
		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, comparator);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.has("sort"));

		assertTrue("Correct sort ordering", 
			searchBuilderResult
				.getAsJsonArray("sort")
				.get(0)
				.getAsJsonObject()
				.get(comparator.getFilterField())
				.getAsString()
				.equals("desc"));
	}

	/**
	 * Tests the proper output when the data filter is not empty, and a sort order is defined as ascending.
	 */
	public void test_SortWithFilterAsc() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.setTypeFilter(ItemType.Question);
		IItemComparator comparator = new DateComparator();
		comparator.setCompareDirection(SortDirection.Ascending);
		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, comparator);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.has("sort"));

		assertTrue("Correct sort ordering", 
			searchBuilderResult
				.getAsJsonArray("sort")
				.get(0)
				.getAsJsonObject()
				.get(comparator.getFilterField())
				.getAsString()
				.equals("asc"));
	}

	/**
	 * Tests the proper output when the data filter is not empty, and a sort order is defined as descending.
	 */
	public void test_SortWithFilterDesc() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.setTypeFilter(ItemType.Question);
		IItemComparator comparator = new DateComparator();
		comparator.setCompareDirection(SortDirection.Descending);
		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, comparator);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.has("sort"));

		assertTrue("Correct sort ordering", 
			searchBuilderResult
				.getAsJsonArray("sort")
				.get(0)
				.getAsJsonObject()
				.get(comparator.getFilterField())
				.getAsString()
				.equals("desc"));
	}

	/**
	 * Tests that the query is properly formed when the filter and sort are empty.
	 */
	public void test_QueryWithBlankFilterAndBlankSort() {
		ESSearchBuilder searchBuilder = new ESSearchBuilder(new DataFilter(), new DateComparator());
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();
		assertValidQueryObject(searchBuilderResult);
	}
}

