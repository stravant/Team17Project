package com.ualberta.team17.datamanager.test;

import java.util.Date;

import android.test.ActivityInstrumentationTestCase2;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataFilter.CombinationMode;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.IdComparator;
import com.ualberta.team17.view.QuestionListActivity;

public class DataFilterTest extends ActivityInstrumentationTestCase2<QuestionListActivity> {
	DataFilter dataFilter;
	QAModel item;

	public DataFilterTest() {
		super(QuestionListActivity.class);
	}
	
	public void setUp() {
		// Set up our variables
		dataFilter = new DataFilter();
	}
	
	/**
	 * Check that the QUERY_STRING comparison mode works
	 */
	public void test_TextSearch() {
		dataFilter.addFieldFilter(QuestionItem.FIELD_BODY, "body", FilterComparison.QUERY_STRING);
		
		// Matching item
		item = new QuestionItem(new UniqueId(), new UniqueId(), "author", new Date(), "Test body", 0, "Test Title");
		assertTrue(dataFilter.accept(item));
		
		// Non matching item with matching in other field
		item = new QuestionItem(new UniqueId(), new UniqueId(), "author", new Date(), "Test text", 0, "Test Title with body");
		assertFalse(dataFilter.accept(item));
		
		// Case does not match but text does
		item = new QuestionItem(new UniqueId(), new UniqueId(), "author", new Date(), "Test BODY in caps", 0, "Test Title with body");
		assertTrue(dataFilter.accept(item));
	}
	
	/**
	 * Check greater than and greater than or equal to
	 */
	public void test_GreaterThan() {
		item = new QuestionItem(new UniqueId(), new UniqueId(), "author", new Date(), "Test body", 2, "Test Title");
		
		DataFilter f1 = new DataFilter();
		f1.addFieldFilter(AnswerItem.FIELD_UPVOTES, "2", FilterComparison.GREATER_THAN);
		assertFalse(f1.accept(item));
		
		DataFilter f2 = new DataFilter();
		f2.addFieldFilter(AnswerItem.FIELD_UPVOTES, "2", FilterComparison.GREATER_THAN_OR_EQUAL);
		assertTrue(f2.accept(item));	
	}
	
	/**
	 * Check that all must parameters are all satisfied
	 */
	public void test_AllMustAreSatisfied() {
		item = new QuestionItem(new UniqueId(), new UniqueId(), "mark", new Date(), "Test body", 0, "Test Title");
		
		// Only the author field is satisfied -> fails
		DataFilter fmust = new DataFilter();
		fmust.addFieldFilter(QuestionItem.FIELD_AUTHOR, "mark", FilterComparison.EQUALS, CombinationMode.MUST);
		fmust.addFieldFilter(QuestionItem.FIELD_UPVOTES, "0", FilterComparison.GREATER_THAN, CombinationMode.MUST);
		assertFalse(fmust.accept(item));
		
		// The author field is satisfied, but with shoulds -> succeeds
		DataFilter fshould = new DataFilter();
		fshould.addFieldFilter(QuestionItem.FIELD_AUTHOR, "mark", FilterComparison.EQUALS, CombinationMode.SHOULD);
		fshould.addFieldFilter(QuestionItem.FIELD_UPVOTES, "0", FilterComparison.GREATER_THAN, CombinationMode.SHOULD);
		assertTrue(fshould.accept(item));		
	}
	
	/**
	 * Check that must params override the should params
	 */
	public void test_MustOverrides() {
		item = new QuestionItem(new UniqueId(), new UniqueId(), "mark", new Date(), "Test body", 0, "Test Title");
		
		// Only the author field is satisfied -> but the upvote field is a Should, so still succeed
		DataFilter fmust = new DataFilter();
		fmust.addFieldFilter(QuestionItem.FIELD_AUTHOR, "mark", FilterComparison.EQUALS, CombinationMode.MUST);
		fmust.addFieldFilter(QuestionItem.FIELD_UPVOTES, "0", FilterComparison.GREATER_THAN, CombinationMode.SHOULD);
		assertTrue(fmust.accept(item));	
	}
	
	/**
	 * Check that should params only require one to be true
	 */
	public void test_ShouldOnlyOneTrue() {
		item = new QuestionItem(new UniqueId(), new UniqueId(), "mark", new Date(), "Test body", 0, "Test Title");
		
		// The author field is satisfied, but with shoulds -> succeeds
		DataFilter fshould = new DataFilter();
		fshould.addFieldFilter(QuestionItem.FIELD_AUTHOR, "mark", FilterComparison.EQUALS, CombinationMode.SHOULD);
		fshould.addFieldFilter(QuestionItem.FIELD_UPVOTES, "0", FilterComparison.GREATER_THAN, CombinationMode.SHOULD);
		fshould.addFieldFilter(QuestionItem.FIELD_TITLE, "non title", FilterComparison.QUERY_STRING, CombinationMode.SHOULD);
		assertTrue(fshould.accept(item));		
		
		// No fields are satisfied -> fail
		DataFilter fshould2 = new DataFilter();
		fshould2.addFieldFilter(QuestionItem.FIELD_AUTHOR, "dan", FilterComparison.EQUALS, CombinationMode.SHOULD);
		fshould2.addFieldFilter(QuestionItem.FIELD_UPVOTES, "0", FilterComparison.GREATER_THAN, CombinationMode.SHOULD);
		fshould2.addFieldFilter(QuestionItem.FIELD_TITLE, "non title", FilterComparison.QUERY_STRING, CombinationMode.SHOULD);
		assertFalse(fshould2.accept(item));
	}
}