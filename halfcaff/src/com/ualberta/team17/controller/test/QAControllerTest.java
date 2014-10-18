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
	
	public void SetUp() {
		dataManager = new DummyDataManager();
		controller = new QAController( dataManager );
		parentQuestion = new QuestionItem(null, null, null, null, null, 0, null);
	}
	
	public void Test_QAC1_GetObjects() {
		IncrementalResult result = controller.getObjects(new DataFilter(), new UpvoteComparator() );
		
		assertEquals("getObjects", result.getCurrentResultCount(), dataManager.getItemCount() );
	}
	
	public void Test_QAC2_GetQuestionChildren() {
		IncrementalResult childrenResult = controller.getQuestionChildren( parentQuestion, new UpvoteComparator() );
		
		assertEquals( "Question has 2 children", childrenResult.getCurrentResultCount(), 2 );
	}
	
	public void Test_QAC3_CreateQuestion() {
		QuestionItem question = controller.createQuestion( "title", "body" );
		assertNotNull( question );
	}
	
	public void Test_QAC4_CreateAttachment() {
		AttachmentItem attachment = controller.createAttachment( parentQuestion );
		assertNotNull( attachment );
	}
	
	public void Test_QAC5_CreateAnswer() {
		AnswerItem answer = controller.createAnswer(parentQuestion, "answer body" );
		assertNotNull( answer );
	}
	
	public void Test_QAC6_CreateComment() {
		CommentItem comment = controller.createComment( parentQuestion, "comment body" );
		assertNotNull( comment );
	}
}
