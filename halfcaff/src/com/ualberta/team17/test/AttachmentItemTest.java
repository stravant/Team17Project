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
	public void setUp() {
		byte[] tmp = null;
		testAttachment = new AttachmentItem(new UniqueId(), new UniqueId(), "author", new Date(), "body", tmp);
		notified = false;
		notifyCount = 0;
	}

	/**
	 * Tests that observing views are notified when required.
	 */
	public void test_AtI1_NotifyViews()
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

	/**
	 * Tests that a view that has been removed from the observer list
	 * is not notified on update.
	 */
	public void test_AtI2_DeleteView()
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
