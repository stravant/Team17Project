package com.ualberta.team17.view.test;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.view.QuestionContent;
import com.ualberta.team17.view.QuestionViewActivity;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionViewActivityTest extends ActivityInstrumentationTestCase2<QuestionViewActivity> {
	private QuestionContent mContent;
	private QuestionViewActivity mActivity;
	private TextView mTitleText;
	private ListView mListView;
	
	public QuestionViewActivityTest() {
		super(QuestionViewActivity.class);
	}

	public void setUp() throws Exception {
		super.setUp();
		Instrumentation instrumentation = getInstrumentation();
		mContent = new QuestionContent();
		QuestionItem question = new QuestionItem(new UniqueId(), null, "author", null, "body", 0, "title");
		mContent.setQuestion(question);
		mActivity = getActivity();
		mActivity.setContent(mContent);
		mTitleText = ((TextView) mActivity.findViewById(com.ualberta.team17.R.id.titleView));
		mListView = ((ListView) mActivity.findViewById(com.ualberta.team17.R.id.qaItemView));
	}
	
	/**
	 * Tests that setUp didn't fail to create objects.
	 */
	public void test_QVA0_preconditions() {
		assertNotNull(mActivity);
		assertNotNull(mContent);
		assertNotNull(mContent.getQuestion());
		assertNotNull(mTitleText);
		assertNotNull(mListView);
	}
	
	/**
	 * Tests that the activity will correctly display the question.
	 */
	public void test_QVA1_displayQuestion() {
		assertEquals("title", mTitleText.getText().toString());
		
		View questionBody = mListView.getChildAt(0);
		
		TextView bodyText = (TextView) questionBody.findViewById(R.id.bodyText);
		TextView authorText = (TextView) questionBody.findViewById(R.id.authorText);
		
		assertEquals("author", authorText.getText().toString());
		assertEquals("body", bodyText.getText().toString());
	}

	/**
	 * Tests that the activity will correctly display an answer.
	 */
	public void test_QVA2_displayAnswer() {
		AnswerItem answer = new AnswerItem(new UniqueId(), mContent.getQuestion().mUniqueId, "answer author", null, "answer body", 0);
		mContent.addAnswers(answer);
		
		View answerBody = mListView.getChildAt(1);
		
		TextView bodyText = (TextView) answerBody.findViewById(R.id.bodyText);
		TextView authorText = (TextView) answerBody.findViewById(R.id.authorText);
		
		assertEquals("answer author", authorText.getText().toString());
		assertEquals("answer body", bodyText.getText().toString());
	}
}
