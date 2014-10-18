package com.ualberta.team17.test;

import java.util.Date;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.view.IQAView;

import junit.framework.TestCase;

public class AnswerItemTest extends TestCase {
	private AnswerItem testAnswer;
	boolean notified;
	int notifyCount;
	public void setUp() {
		testAnswer = new AnswerItem( new UniqueId(), new UniqueId(), "author", new Date(), "body", 0);
		notified = false;
		notifyCount = 0;
	}
	
	public void test_AI1_Upvote()
	{
		assertEquals("No upvotes", testAnswer.getUpvoteCount(), 0);
		
		testAnswer.upvote();
		
		assertEquals("One upvote", testAnswer.getUpvoteCount(), 1);
	}
	
	public void test_AI2_UpvoteNotifiesView()
	{
		IQAView dummyView = new IQAView() {
			public void update(QAModel model) {
				notified = true;
			}
		};
		testAnswer.addView(dummyView);
		testAnswer.upvote();		
		assertTrue( "View was notified", notified );			
	}
	
	public void test_AI3_DeleteView()
	{		
		IQAView dummyView = new IQAView() {
			public void update(QAModel model) {
				notified = true;
			}
		};
		testAnswer.addView(dummyView);
		testAnswer.deleteView(dummyView); 
		testAnswer.upvote();		
		assertFalse("No views were notified", notified);		
	}
	
	public void test_AI4_NotifyViews()
	{		
		IQAView dummyViewA = new IQAView() {
			public void update(QAModel model) {
				notifyCount++;
			}
		};
		IQAView dummyViewB = new IQAView() {
			public void update(QAModel model) {
				notifyCount++;
			}
		};
		IQAView dummyViewC = new IQAView() {
			public void update(QAModel model) {
				notifyCount++;
			}
		};
		
		testAnswer.addView(dummyViewA);
		testAnswer.addView(dummyViewB);
		testAnswer.addView(dummyViewC);
		testAnswer.notifyViews();
		assertEquals("All views were notified", notifyCount, 3);
	}
}
