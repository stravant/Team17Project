package com.ualberta.team17.test;

import java.util.Date;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.view.IQAView;

import junit.framework.TestCase;

public class QuestionItemTest extends TestCase {
	private QuestionItem testQuestion;
	boolean notified;
	int notifyCount;
	public void SetUp() {
		testQuestion = new QuestionItem( new UniqueId(), new UniqueId(), "author", new Date(), "body", 0, "title" );
		notified = false;
		notifyCount = 0;
	}
	
	public void Test_QI1_Upvote()
	{
		assertEquals( "No upvotes", testQuestion.getUpvoteCount(), 0 );
		
		testQuestion.upvote();
		
		assertEquals( "One upvote", testQuestion.getUpvoteCount(), 1 );
	}
	
	public void Test_QI2_UpvoteNotifiesView()
	{
		IQAView dummyView = new IQAView() {
			public void update(QAModel model) {
				notified = true;
			}
		};
		testQuestion.addView( dummyView );
		testQuestion.upvote();
		
		assertTrue( "View was notified", notified );
	}
	
	public void Test_QI3_DeleteView()
	{		
		IQAView dummyView = new IQAView() {
			public void update(QAModel model) {
				notified = true;
			}
		};
		testQuestion.addView(dummyView);
		testQuestion.deleteView(dummyView); 
		testQuestion.upvote();		
		assertFalse("No views were nofified", notified);		
	}
	
	public void Test_QI4_NotifyViews()
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
		
		testQuestion.addView(dummyViewA);
		testQuestion.addView(dummyViewB);
		testQuestion.addView(dummyViewC);
		testQuestion.notifyViews();
		assertEquals("All views were notified", notifyCount, 3);
	}
}
