package com.ualberta.team17.test;

import java.util.Date;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.view.IQAView;

import junit.framework.TestCase;

public class AttachmentItemTest extends TestCase {
	private AttachmentItem testAttachment;
	boolean notified;
	int notifyCount;
	public void SetUp() {
		testAttachment = new AttachmentItem( new UniqueId(), new UniqueId(), "author", new Date(), "body", "data");
		notified = false;
		notifyCount = 0;
	}
	
	public void Test_AtI1_NotifyViews()
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
		
		testAttachment.addView(dummyViewA);
		testAttachment.addView(dummyViewB);
		testAttachment.addView(dummyViewC);
		testAttachment.notifyViews();
		assertEquals("All views were notified", notifyCount, 3);
	}
	
	public void Test_AtI2_DeleteView()
	{		
		IQAView dummyView = new IQAView() {
			public void update(QAModel model) {
				notified = true;
			}
		};
		testAttachment.addView(dummyView);
		testAttachment.deleteView(dummyView); 
		testAttachment.notifyViews();		
		assertFalse("No views were nofified", notified);		
	}
	
	
}
