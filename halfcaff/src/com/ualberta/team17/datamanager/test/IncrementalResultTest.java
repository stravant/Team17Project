package com.ualberta.team17.datamanager.test;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IncrementalResult;
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
	
	/**
	 * Check basic insertion of objects
	 */
	@SuppressWarnings("serial")
	public void test_IR1_BasicObjectInsertion() {
		result.addObjects(new ArrayList<QAModel>(){{}});
		
		assertFalse("Empty list of items to add, should not be notified", obs.wasNotified());
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(new OrderedQAItem(ItemType.Question, 0));
		}});
		
		assertTrue("Item was added", obs.wasNotified());
	}
	
	/**
	 * 
	 * Check that different add invocations generate separate, and the correct 
	 * number of notifications.
	 */
	@SuppressWarnings("serial")
	public void test_IR2_TestNotificationCount() {
		result.addObjects(new ArrayList<QAModel>(){{
			add(new OrderedQAItem(ItemType.Question, 0));
			add(new OrderedQAItem(ItemType.Question, 1));
		}});
		
		assertEquals("Two items added -> one notification", 1, obs.getNotificationCount());
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(new OrderedQAItem(ItemType.Answer, 2));
		}});
		
		assertEquals("One further item added -> one further notification", 2, obs.getNotificationCount());
	}
	
	/**
	 * Check that items being passed to the addObjects out of order end up 
	 * in the right order in the notification and result, and are grouped
	 * together optimally.
	 */
	@SuppressWarnings("serial")
	public void test_IR3_OutOfOrderInsert() {
		final OrderedQAItem q1 = new OrderedQAItem(ItemType.Question, 0);
		final OrderedQAItem q2 = new OrderedQAItem(ItemType.Question, 1);
		final OrderedQAItem q3 = new OrderedQAItem(ItemType.Question, 2);
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(q1);
			add(q3);
			add(q2);
		}});
		
		assertEquals("Three items added with last out of order -> two notifications", 2, obs.getNotificationCount());
		
		assertEquals(0, obs.getNotification(0).Index);
		assertEquals(2, obs.getNotification(0).List.size());
		assertEquals(q1, obs.getNotification(0).List.get(0));
		assertEquals(q3, obs.getNotification(0).List.get(1));
		assertEquals(1, obs.getNotification(1).Index);
		assertEquals(1, obs.getNotification(1).List.size());
		assertEquals(q2, obs.getNotification(1).List.get(0));
	}
	
	/**
	 * Check that double insertion of an item into the a result ignores the
	 * second copy of the item, only adding it to the result once.
	 */
	@SuppressWarnings("serial")
	public void test_IR4_TestDoubleInsert() {
		final OrderedQAItem q1 = new OrderedQAItem(ItemType.Question, 0);
		final OrderedQAItem q2 = new OrderedQAItem(ItemType.Question, 1);
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(q1);
			add(q1); //double add q1
			add(q2);
		}});
		
		assertEquals("Two identical items, and one other item added -> one notification.", 1, obs.getNotificationCount());
		assertEquals("That notification should only have two items, not three.", 2, obs.getNotification(0).List.size());
		assertEquals("The result should now have only the two unique items in it.", 2, result.getCurrentResults().size());
	}	

	/**
	 * Check that items with identical references are ignored, but items with identical 
	 * values under our sort are not ignored.
	 */
	@SuppressWarnings("serial")
	public void test_IR5_TestEqualItems() {
		final OrderedQAItem q1 = new OrderedQAItem(ItemType.Question, 0);
		final OrderedQAItem q2 = new OrderedQAItem(ItemType.Question, 0);
		final OrderedQAItem q3 = new OrderedQAItem(ItemType.Question, 1);
		
		result.addObjects(new ArrayList<QAModel>(){{
			add(q1);
			add(q1); // reference is same -> ignore duplicate q1
			add(q2); // same under sort used, but difference refs -> don't ignore
			add(q3);
		}});
		
		assertEquals(1, obs.getNotificationCount());
		List<QAModel> notification = obs.getNotification(0).List;
		assertEquals(3, notification.size());
		assertEquals(q1, notification.get(0));
		assertEquals(q2, notification.get(1));
		assertEquals(q3, notification.get(2));	
	}
	
	/**
	 * Check that items with the same different references, but the same hash code
	 * are correctly treated as the same item, and only inserted once into the
	 * result.
	 */
	@SuppressWarnings("serial")
	public void test_IR6_TestHashcodeEqualItems() {
		class OrderedQAItemWithHashCode extends OrderedQAItem {
			int mHashCode;
			public OrderedQAItemWithHashCode(ItemType type, int seq, int hashCode) {
				super(type, seq);
				mHashCode = hashCode;
			}
			@Override
			public int hashCode() {
				return mHashCode;
			}
		}
		
		final OrderedQAItem q1 = new OrderedQAItemWithHashCode(ItemType.Question, 0, 0);
		final OrderedQAItem q2 = new OrderedQAItemWithHashCode(ItemType.Question, 0, 1);
		final OrderedQAItem q3 = new OrderedQAItemWithHashCode(ItemType.Question, 0, 1);
		result.addObjects(new ArrayList<QAModel>(){{
			add(q1);
			add(q1); // double add -> reference is same -> ignore
			add(q2);
			add(q3); // hash code is same -> ignore
		}});
		
		assertEquals(1, obs.getNotificationCount());
		List<QAModel> notification = obs.getNotification(0).List;
		assertEquals(2, notification.size());
		assertEquals(q1, notification.get(0));
		assertEquals(q2, notification.get(1));
	}
	
	/*
	 * Check that filtered notifications and gets work.
	 */
	public void test_IR7_GetCurrentResultsOfType() {
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
