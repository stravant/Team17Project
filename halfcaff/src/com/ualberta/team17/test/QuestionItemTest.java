package com.ualberta.team17.test;

import java.util.Date;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.view.IQAView;

import junit.framework.TestCase;

public class QuestionItemTest extends TestCase {
	private QuestionItem mQ1;
	boolean notified;
	public void SetUp() {
		mQ1 = new QuestionItem( new UniqueId(), new UniqueId(), "author", new Date(), "body", 0, "title" );
		notified = false;
	}
	
	public void TestUpvote()
	{
		assertEquals( "No upvotes", mQ1.getUpvoteCount(), 0 );
		
		mQ1.upvote();
		
		assertEquals( "One upvote", mQ1.getUpvoteCount(), 1 );
	}
	
	public void TestNotifyView()
	{
		IQAView dummyView = new IQAView() {
			public void update(QAModel model) {
				notified = true;
			}
		};
		mQ1.addView( dummyView );
		mQ1.upvote();
		
		assertTrue( "View was notified", notified );
	}
}
