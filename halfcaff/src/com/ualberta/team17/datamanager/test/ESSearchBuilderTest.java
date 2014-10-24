package com.ualberta.team17.datamanager.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.ESSearchBuilder;

import junit.framework.TestCase;

public class ESSearchBuilderTest extends TestCase {
	private void assertValidQueryObject(JsonObject obj) {
		JsonObject filterObject = obj.getAsJsonObject("filtered");
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

	public void test_Equals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.EQUALS);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.toString().contains("{\"must\":[{\"term\":{\"testField1\":\"Hello World!\"}}]}"));
	}

	public void test_NotEquals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.NOT_EQUAL);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);
		assertTrue("Correct Json Output", searchBuilderResult.toString().contains("{\"must_not\":[{\"term\":{\"testField1\":\"Hello World!\"}}]}"));
	}
	
	public void test_Multiple_Equals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.EQUALS);
		dataFilter.addFieldFilter("testField2", "This is a second filter", DataFilter.FilterComparison.EQUALS);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);

		JsonArray mustObject = searchBuilderResult
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
	
	public void test_Multiple_NotEquals_Query() {
		DataFilter dataFilter = new DataFilter();
		dataFilter.addFieldFilter("testField1", "Hello World!", DataFilter.FilterComparison.NOT_EQUAL);
		dataFilter.addFieldFilter("testField2", "This is a second filter", DataFilter.FilterComparison.NOT_EQUAL);

		ESSearchBuilder searchBuilder = new ESSearchBuilder(dataFilter, null);
		JsonObject searchBuilderResult = searchBuilder.getJsonQueryObject();

		assertValidQueryObject(searchBuilderResult);

		JsonArray mustObject = searchBuilderResult
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
}

