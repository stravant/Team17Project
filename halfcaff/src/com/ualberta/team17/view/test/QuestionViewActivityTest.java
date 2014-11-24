package com.ualberta.team17.view.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.controller.test.DummyDataManager;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.view.QuestionViewActivity;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class QuestionViewActivityTest extends ActivityInstrumentationTestCase2<QuestionViewActivity> {
	private QuestionViewActivity mActivity;
	private QAController mController;
	private DummyDataManager mDataManager;
	private ListView mListView;
	private UniqueId mId;
	private QuestionItem mQuestion;
	
	
	public QuestionViewActivityTest() {
		super(QuestionViewActivity.class);
	}

	public void setUp() throws Exception {
		super.setUp();	
		UserContext userContext = new UserContext("test_user");		
		mId = new UniqueId();
		mQuestion = new QuestionItem(mId, null, "question author", null, "question body", 0, "question title");
		AnswerItem answerItem = new AnswerItem(new UniqueId(), mId, userContext.getUserName(), new Date(), "answer body", 0);
		CommentItem commentItem = new CommentItem( new UniqueId(), mId, userContext.getUserName(), new Date(), "comment body", 0);
		List<QAModel> items = new ArrayList<QAModel>();
		items.add(mQuestion);
		items.add(answerItem);
		items.add(commentItem);				
		mDataManager = new DummyDataManager(userContext, getInstrumentation().getTargetContext());
		mController = new QAController(mDataManager);
		mDataManager.setItems(items);			
		Intent intent = new Intent();
		intent.setClassName("com.ualberta.team17.view", "com.ualberta.team17.view.QuestionViewActivity");
		intent.putExtra(QuestionViewActivity.QUESTION_ID_EXTRA, mId.toString());
		setActivityIntent(intent);
		mActivity = getActivity();
		
		mListView = (ListView) mActivity.findViewById(R.id.qaItemView);
	}
	
	/**
	 * Tests that setUp didn't fail to create objects...
	 */
	public void test_QVA0_preconditions() {
		assertNotNull(mActivity);
		assertNotNull(mController);
		assertNotNull(mDataManager);
		assertNotNull(mListView);
		try {
		    Thread.sleep(1000);                 //1000 milliseconds is one second.
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Tests that the activity will correctly display the question.
	 */
	public void test_QVA1_displayQuestion() {
		View questionBody = mListView.getChildAt(0);		
		TextView titleText = (TextView) questionBody.findViewById(R.id.titleText);
		TextView bodyText = (TextView) questionBody.findViewById(R.id.bodyText);
		TextView authorText = (TextView) questionBody.findViewById(R.id.authorText);
		
		assertEquals("question title", titleText.getText().toString());
		assertEquals("question author", authorText.getText().toString());
		assertEquals("question body", bodyText.getText().toString());
	}

	/**
	 * Tests that the activity will correctly display an answer.
	 */
	public void test_QVA2_displayAnswer() {
		//not working
		//mController.createAnswer(mQuestion, "answer body");		
		
		View answerBody = mListView.getChildAt(1);
		
		TextView bodyText = (TextView) answerBody.findViewById(R.id.bodyText);
		TextView authorText = (TextView) answerBody.findViewById(R.id.authorText);
		
		assertEquals("test_user", authorText.getText().toString());
		assertEquals("answer body", bodyText.getText().toString());
	}
	
	/**
	 * Tests that the activity can display a comment.
	 * 
	 * Because commenting is agnostic to whether it's on a Question or Answer, we don't need seperate tests for each. 
	 */
	public void test_QVA3_displayComment() {
		//not working
		//mController.createComment(mQuestion, "comment body");		
		
		View questionBody = mListView.getChildAt(0);
		LinearLayout commentsLL = (LinearLayout) questionBody.findViewById(R.id.commentView);
		RelativeLayout commentsRL = (RelativeLayout) commentsLL.getChildAt(0);
		TextView commentText = (TextView) commentsRL.getChildAt(0);
		TextView authorText = (TextView) commentsRL.getChildAt(1);
		
		assertEquals("comment body", commentText.getText().toString());
		assertEquals("-test_user", authorText.getText().toString());
	}
}
