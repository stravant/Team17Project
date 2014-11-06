package com.ualberta.team17.controller.test;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.comparators.UpvoteComparator;

import junit.framework.TestCase;

public class QAControllerTest extends TestCase {
	private QAController controller;
	private DummyDataManager dataManager;
	private QuestionItem parentQuestion;
	
	public void setUp() {
		dataManager = new DummyDataManager();
		controller = new QAController( dataManager );
		parentQuestion = new QuestionItem(null, null, null, null, null, 0, null);
	}
	
	public void test_QAC1_GetObjects() {
		IncrementalResult result = controller.getObjects(new DataFilter(), new UpvoteComparator() );
		
		assertEquals("getObjects", result.getCurrentResults().size(), dataManager.getItemCount());
	}
	
	public void test_QAC2_GetQuestionChildren() {
		IncrementalResult childrenResult = controller.getChildren( parentQuestion, new UpvoteComparator() );
		
		assertEquals( "Question has 2 children", childrenResult.getCurrentResults().size(), 2 );
	}
	
	public void test_QAC3_CreateQuestion() {
		QuestionItem question = controller.createQuestion( "title", "body" );
		assertNotNull( question );
	}
	
	public void test_QAC4_CreateAttachment() {
		AttachmentItem attachment = controller.createAttachment( parentQuestion );
		assertNotNull( attachment );
	}
	
	public void test_QAC5_CreateAnswer() {
		AnswerItem answer = controller.createAnswer(parentQuestion, "answer body" );
		assertNotNull( answer );
	}
	
	public void test_QAC6_CreateComment() {
		CommentItem comment = controller.createComment( parentQuestion, "comment body" );
		assertNotNull( comment );
	}
}
