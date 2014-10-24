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
import com.ualberta.team17.test.utility.OrderedQAItem;
import com.ualberta.team17.test.utility.OrderedQAItemComparator;

import junit.framework.TestCase;

public class IncrementalResultTest extends TestCase {
	private IncrementalResult result;
	private RecordingIncrementalObserver obs;
	public void setUp() {
		result = new IncrementalResult(new OrderedQAItemComparator());
		obs = new RecordingIncrementalObserver();
		result.addObserver(obs);
	}
	
	@SuppressWarnings("serial")
	public void test_IR1_BasicObjectInsertion() {
		result.addObjects(new ArrayList<QAModel>(){{}});
		
		assertFalse("Empty list of items to add, should not be notified", obs.wasNotified());
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(new OrderedQAItem(ItemType.Question, 0));
		}});
		
		assertTrue("Item was added", obs.wasNotified());
	}
	
	@SuppressWarnings("serial")
	public void test_IR2_TestNotificationCount() {
		result.addObjects(new ArrayList<QAModel>(){{
			add(new OrderedQAItem(ItemType.Question, 0));
			add(new OrderedQAItem(ItemType.Question, 1));
		}});
		
		assertEquals("Two items added -> one notification", obs.getNotificationCount(), 1);
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(new OrderedQAItem(ItemType.Answer, 2));
		}});
		
		assertEquals("One further item added -> one further notification", obs.getNotificationCount(), 2);
	}
	
	public void test_IR3_OutOfOrderInsert() {
		final OrderedQAItem q1 = new OrderedQAItem(ItemType.Question, 0);
		final OrderedQAItem q2 = new OrderedQAItem(ItemType.Question, 1);
		final OrderedQAItem q3 = new OrderedQAItem(ItemType.Question, 2);
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(q1);
			add(q3);
			add(q2);
		}});
		
		assertEquals("Thee items added with last out of order -> two notifications", obs.getNotificationCount(), 2);
		
		assertEquals(0, obs.getNotification(0).Index);
		assertEquals(2, obs.getNotification(0).List.size());
		assertEquals(q1, obs.getNotification(0).List.get(0));
		assertEquals(q3, obs.getNotification(0).List.get(1));
		assertEquals(1, obs.getNotification(1).Index);
		assertEquals(1, obs.getNotification(1).List.size());
		assertEquals(q2, obs.getNotification(1).List.get(0));
	}
	
//	public void test_IR2_GetCurrentResultCount() {
//		assertEquals( "No results initially", result.getCurrentResultCount(), 0 );
//		
//		List<QAModel> itemsToAdd = new ArrayList<QAModel>();
//		itemsToAdd.add(new QuestionItem(new UniqueId(), null, null, null, null, 0, null));
//		
//		result.addObjects(itemsToAdd);
//		
//		assertEquals( "One result after adding", result.getCurrentResultCount(), 1);
//	}
	
//	public void test_IR3_GetCurrentResult() {
//		QuestionItem item1 = new QuestionItem(new UniqueId(), null, null, null, null, 0, null);
//		QuestionItem item2 = new QuestionItem(new UniqueId(), null, null, null, null, 0, null);
//		
//		List<QAModel> itemsToAdd = new ArrayList<QAModel>();
//		itemsToAdd.add(item1);
//		itemsToAdd.add(item2);
//		
//		result.addObjects(itemsToAdd);
//		
//		assertEquals("First result", result.getCurrentResult(0), item1);
//		assertEquals("Second result", result.getCurrentResult(1), item2);
//	}
	
	public void test_IR4_GetCurrentResultsOfType() {
		OrderedQAItem item1 = new OrderedQAItem(ItemType.Question, 0);
		OrderedQAItem item2 = new OrderedQAItem(ItemType.Answer,   1);
		OrderedQAItem item3 = new OrderedQAItem(ItemType.Question, 2);
		
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
