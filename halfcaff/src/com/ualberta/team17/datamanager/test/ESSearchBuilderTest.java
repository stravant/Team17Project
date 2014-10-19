package com.ualberta.team17.datamanager.test;

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
}

