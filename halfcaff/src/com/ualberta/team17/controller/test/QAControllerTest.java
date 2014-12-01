package com.ualberta.team17.controller.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.test.ActivityTestCase;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.comparators.UpvoteComparator;

import junit.framework.TestCase;

/**
 * Tests the QAController by manipulating its Model-controlling functions.
 * 
 * @author michaelblouin
 */
public class QAControllerTest extends ActivityTestCase {
	private UserContext userContext;
	private QAController controller;
	private DummyDataManager dataManager;
	private QuestionItem parentQuestion;
	private AnswerItem parentAnswer;
	
	public void setUp() {
		userContext = new UserContext("QAControllerTestUser");
		dataManager = new DummyDataManager(userContext, getInstrumentation().getTargetContext());
		controller = new QAController( dataManager );
		parentQuestion = new QuestionItem(new UniqueId(), null, null, null, null, 0, null);
		parentAnswer = new AnswerItem(new UniqueId(), parentQuestion.getUniqueId(), null, null, null, 0);
		
		dataManager.setItems(new ArrayList<QAModel>() {{
			add(parentQuestion);
			add(parentAnswer);
			add(new QuestionItem(new UniqueId(), null, userContext.getUserName(), new Date(), "FirstBody", 0, "FirstTitle"));
			add(new QuestionItem(new UniqueId(), null, userContext.getUserName(), new Date(), "FirstBody", 0, "FirstTitle"));
			add(new QuestionItem(new UniqueId(), null, userContext.getUserName(), new Date(), "FirstBody", 0, "FirstTitle"));
			add(new AnswerItem(new UniqueId(), new UniqueId(), userContext.getUserName(), new Date(), "FirstAnswer", 0));
			add(new AnswerItem(new UniqueId(), parentQuestion.getUniqueId(), userContext.getUserName(), new Date(), "SecondAnswer", 0));
			add(new CommentItem(new UniqueId(), parentQuestion.getUniqueId(), userContext.getUserName(), new Date(), "FirstComment", 0));
			add(new CommentItem(new UniqueId(), parentQuestion.getUniqueId(), userContext.getUserName(), new Date(), "SecondComment", 0));
			add(new CommentItem(new UniqueId(), new UniqueId(), userContext.getUserName(), new Date(), "ThirdComment", 0));
			add(new CommentItem(new UniqueId(), parentAnswer.getUniqueId(), userContext.getUserName(), new Date(), "ThirdComment", 0));
		}});
	}

	/**
	 * Tests that objects can be properly retrieved from the controller by calling QAController.getObjects().
	 */
	public void test_QAC1_GetObjects() {
		IncrementalResult result = controller.getObjects(new DataFilter(), new UpvoteComparator() );
		
		assertEquals("getObjects", dataManager.getItemCount(), result.getCurrentResults().size());
	}

	/**
	 * Tests that a questions children may be retrieved using the QAController.
	 */
	public void test_QAC2_GetQuestionChildren() {
		// Question with multiple children
		IncrementalResult questionChildren = controller.getChildren( parentQuestion, new UpvoteComparator() );
		assertEquals( "Question has 4 children", 4, questionChildren.getCurrentResults().size() );
		
		for (QAModel item: questionChildren.getCurrentResults()) {
			assertEquals("Parent id", parentQuestion.getUniqueId(), ((AuthoredTextItem)item).getParentItem());
		}
		
		// Answer with single child
		IncrementalResult answerChildren = controller.getChildren( parentAnswer, new UpvoteComparator() );
		assertEquals( "Answer has 1 child", 1, answerChildren.getCurrentResults().size() );
		
		for (QAModel item: answerChildren.getCurrentResults()) {
			assertEquals("Parent id", parentAnswer.getUniqueId(), ((AuthoredTextItem)item).getParentItem());
		}
		
		// ID that should have no children
		IncrementalResult noChildren = controller.getChildren( new UniqueId(), new UpvoteComparator() );
		assertEquals( "Nas no child", 0, noChildren.getCurrentResults().size() );
	}
	
	private void assertValidAuthoredQAItem(AuthoredItem item) {
		assertNotNull(item);
		assertNotNull(item.getUniqueId());
		assertNotNull(item.getDate());
	}
	
	private void assertValidAuthoredTextItem(AuthoredTextItem item, UniqueId parent, String body) {
		assertValidAuthoredQAItem(item);
		assertEquals(body, item.getBody());
		assertEquals(parent, item.getParentItem());
	}
	
	private void assertValidQuestion(QuestionItem question, String title, String body) {
		assertValidAuthoredTextItem(question, null, body);
		assertEquals(title, question.getTitle());
	}
	
	private void assertDifferentIds(QAModel ... models) {
		for (QAModel model: models) {
			for (QAModel innerModel: models) {
				// Note that comparison using "==" here is intentional -- we only want to skip the same object instance.
				if (model == innerModel) {
					continue;
				}
				
				assertNotSame(model.getUniqueId(), innerModel.getUniqueId());
			}
		}
	}
	
	/**
	 * Tests that the QAController can properly create questions using createQuestion()
	 */
	public void test_QAC3_CreateQuestions() {
		QuestionItem question1 = controller.createQuestion( "title", "body" );
		assertValidQuestion(question1, "title", "body");
		
		QuestionItem question2 = controller.createQuestion("someTitle", null);
		assertValidQuestion(question2, "someTitle", null);
		
		QuestionItem question3 = controller.createQuestion(null, "someBody");
		assertValidQuestion(question3, null, "someBody");
		
		assertDifferentIds(question1, question2, question3);
	}
	
	private void assertValidAttachment(AttachmentItem item, UniqueId parent) {
		assertNotNull(item);
		assertEquals(parent, item.getParentItem());
	}

	/**
	 * Tests that the QAController can properly create attachments.
	 */
	public void test_QAC4_CreateAttachment() {
		AttachmentItem attachment1 = controller.createAttachment(parentQuestion.getUniqueId(), "AttachmentName", new byte[0]);
		assertValidAttachment(attachment1, parentQuestion.getUniqueId());
		
		assertNull("Cant create attachment without parent", controller.createAttachment(null, "AttachmentName", new byte[0]));
		
		CommentItem attachment2 = controller.createComment( parentQuestion, null);
		assertValidAuthoredTextItem(attachment2, parentQuestion.getUniqueId(), null);
		
		assertDifferentIds(parentQuestion, attachment1, attachment2);
	}

	/**
	 * Tests that the QAController can properly create answers in reply to a question.
	 * 
	 * Also asserts that you cannot create an answer without a parent.
	 */
	public void test_QAC5_CreateAnswers() {
		AnswerItem answer1 = controller.createAnswer(parentQuestion, "answer1 body" );
		assertValidAuthoredTextItem(answer1, parentQuestion.getUniqueId(), "answer1 body");
		
		assertNull("Cant create answer without parent", controller.createAnswer( (QuestionItem)null, "answer2 body" ));
		assertNull("Cant create answer without parent", controller.createAnswer( (QuestionItem)null, null ));
		assertNull("Cant create answer without parent", controller.createAnswer( (UniqueId)null, "answer2 body" ));
		assertNull("Cant create answer without parent", controller.createAnswer( (UniqueId)null, null ));
		
		AnswerItem answer2 = controller.createAnswer( parentQuestion, null );
		assertValidAuthoredTextItem(answer2, parentQuestion.getUniqueId(), null);
		
		AnswerItem answer3 = controller.createAnswer( parentQuestion, "answer3 body" );
		assertValidAuthoredTextItem(answer3, parentQuestion.getUniqueId(), "answer3 body");
		
		assertDifferentIds(parentQuestion, answer1, answer2, answer3);
	}

	/**
	 * Tests that the QAController can properly create comments in reply to a question or answer.
	 * 
	 * Also tests that comments cannot be created without a parent.
	 */
	public void test_QAC6_CreateComments() {
		CommentItem comment1 = controller.createComment( parentQuestion, "comment1 body" );
		assertValidAuthoredTextItem(comment1, parentQuestion.getUniqueId(), "comment1 body");
		
		assertNull("Cant create comment without parent", controller.createComment( (AuthoredTextItem)null, "comment2 body" ));
		assertNull("Cant create comment without parent", controller.createComment( (AuthoredTextItem)null, null ));
		assertNull("Cant create comment without parent", controller.createComment( (UniqueId)null, "comment2 body" ));
		assertNull("Cant create comment without parent", controller.createComment( (UniqueId)null, null ));
		
		CommentItem comment2 = controller.createComment( parentQuestion, null );
		assertValidAuthoredTextItem(comment2, parentQuestion.getUniqueId(), null);
		
		CommentItem comment3 = controller.createComment( parentQuestion, "comment2 body" );
		assertValidAuthoredTextItem(comment3, parentQuestion.getUniqueId(), "comment2 body");
		
		assertDifferentIds(parentQuestion, comment1, comment2, comment3);
	}
}
