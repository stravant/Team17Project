package com.ualberta.team17.datamanager.test;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.comparators.UpvoteComparator;

import junit.framework.TestCase;

public class IncrementalResultTest extends TestCase {
	private IncrementalResult result;
	public void SetUp() {
		result = new IncrementalResult( new UpvoteComparator() );
	}
	
	public void Test_IR1_ObserverTest() {
		DummyIncrementalObserver obs = new DummyIncrementalObserver();
		
		result.addObserver(obs);
		result.addObjects(new ArrayList<QAModel>() );
		
		assertTrue( "Observer notified", obs.wasNotified() );
	}
	
	public void Test_IR2_GetCurrentResultCount() {
		assertEquals( "No results initially", result.getCurrentResultCount(), 0 );
		
		List<QAModel> itemsToAdd = new ArrayList<QAModel>();
		itemsToAdd.add(new QuestionItem(new UniqueId(), null, null, null, null, 0, null));
		
		result.addObjects(itemsToAdd);
		
		assertEquals( "One result after adding", result.getCurrentResultCount(), 1);
	}
	
	public void Test_IR3_GetCurrentResult() {
		QuestionItem item1 = new QuestionItem(new UniqueId(), null, null, null, null, 0, null);
		QuestionItem item2 = new QuestionItem(new UniqueId(), null, null, null, null, 0, null);
		
		List<QAModel> itemsToAdd = new ArrayList<QAModel>();
		itemsToAdd.add(item1);
		itemsToAdd.add(item2);
		
		result.addObjects(itemsToAdd);
		
		assertEquals( "First result", result.getCurrentResult(0), item1);
		assertEquals( "Second result", result.getCurrentResult(1), item2);
	}
	
	public void Test_IR4_GetCurrentResultsOfType() {
		QuestionItem item1 = new QuestionItem(new UniqueId(), null, null, null, null, 0, null);
		AnswerItem item2 = new AnswerItem(new UniqueId(), null, null, null, null, 0);
		QuestionItem item3 = new QuestionItem(new UniqueId(), null, null, null, null, 0, null);
		
		List<QAModel> itemsToAdd = new ArrayList<QAModel>();
		itemsToAdd.add(item1);
		itemsToAdd.add(item2);
		itemsToAdd.add(item3);
		
		result.addObjects(itemsToAdd);
		
		List<QAModel> questionItems = result.getCurrentResultsOfType(ItemType.Question);
		List<QAModel> answerItems = result.getCurrentResultsOfType(ItemType.Answer);
		
		assertSame("First Question", questionItems.get(0), item1);
		assertSame("Second Question", questionItems.get(1), item3);
		assertSame("Answer", answerItems.get(0), item2);
	}
}
