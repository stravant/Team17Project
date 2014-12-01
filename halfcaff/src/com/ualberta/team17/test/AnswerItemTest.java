package com.ualberta.team17.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	/**
	 * Tests that AnswerItems are properly upvoted on call to upvote()
	 */
	public void test_AI1_Upvote()
	{
		assertEquals("No upvotes", testAnswer.getUpvoteCount(), 0);
		
		testAnswer.upvote();
		
		assertEquals("One upvote", testAnswer.getUpvoteCount(), 1);
		
		testAnswer.upvote();
		
		assertEquals("Two upvotes", testAnswer.getUpvoteCount(), 2);
	}
	
	/**
	 * Tests that when an AnswerItem is upvoted it notifies a listening view
	 */
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
	
	/**
	 * Tests that when an AnswerItem is upvoted it notified all listening views
	 */
	public void test_AI2b_UpvoteNotifiesViews()
	{
		List<ViewNotifiedChecker> views = new ArrayList<ViewNotifiedChecker>();
		views.add(new ViewNotifiedChecker());
		views.add(new ViewNotifiedChecker());
		views.add(new ViewNotifiedChecker());
		views.add(new ViewNotifiedChecker());

		for (IQAView view: views) {
			testAnswer.addView(view);
		}

		testAnswer.upvote();
		for (ViewNotifiedChecker view: views) {
			assertTrue("View was notified", view.mNotified);
		}
	}
	
	/**
	 * Tests that a view that is deleted from the listeners does not get notified on update.
	 */
	public void test_AI3_DeleteView()
	{		
		IQAView dummyView = new IQAView() {
			public void update(QAModel model) {
				notified = true;
			}
		};

		testAnswer.addView(dummyView);
		
		ViewNotifiedChecker checker = new ViewNotifiedChecker();
		testAnswer.addView(checker);

		testAnswer.deleteView(dummyView);

		testAnswer.upvote();

		assertFalse("Removed view was not notified", notified);
		assertTrue("Remaining view was notified", checker.mNotified);
	}
	
	/**
	 * Tests that many views are notified on update.
	 */
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

	private class ViewNotifiedChecker implements IQAView {
		boolean mNotified = false;
		public void update(QAModel model) {
			mNotified = true;
		}
	}
}
