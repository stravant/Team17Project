package com.ualberta.team17.view.test;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
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
import android.widget.TextView;

public class QuestionViewActivityTest extends ActivityInstrumentationTestCase2<QuestionViewActivity> {
	private QuestionViewActivity mActivity;
	private QAController mController;
	private DummyDataManager mDataManager;
	
	
	public QuestionViewActivityTest() {
		super(QuestionViewActivity.class);
	}

	public void setUp() throws Exception {
		super.setUp();
		Instrumentation instrumentation = getInstrumentation();
		
		QuestionItem question = new QuestionItem(new UniqueId(), null, "question author", null, "question body", 0, "question title");
		Intent intent = new Intent(this.getActivity(), QuestionViewActivity.class);
		intent.putExtra(QuestionViewActivity.QUESTION_ID_EXTRA, question.getUniqueId().toString());
		
		UserContext userContext = new UserContext("test_user");
		setActivityIntent(intent);
		mActivity = getActivity();
		mDataManager = new DummyDataManager(userContext, mActivity);
		mController = new QAController(mDataManager);
	}
	
	/**
	 * Tests that setUp didn't fail to create objects...
	 */
	public void test_QVA0_preconditions() {
		assertNotNull(mActivity);
		assertNotNull(mController);
		assertNotNull(mDataManager);
	}
	
	/**
	 * Tests that the activity will correctly display the question.
	 */
	public void test_QVA1_displayQuestion() {
	}

	/**
	 * Tests that the activity will correctly display an answer.
	 */
	public void test_QVA2_displayAnswer() {
	}
	
	/**
	 * Tests that the activity can display a comment.
	 * 
	 * Because commenting is agnostic to whether it's on a Question or Answer, we don't need seperate tests for each. 
	 */
	public void test_QVA3_displayComment() {
	}
}
