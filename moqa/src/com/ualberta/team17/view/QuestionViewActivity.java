package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.R;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionViewActivity extends Activity {
	public final static String QUESTION_ID_EXTRA = "question_id";
	
	// Test stuff - can be deleted later
	private final static boolean GENERATE_TEST_DATA = true;
	private final static String LIPSUM = "Lorem ipsum dolor sit amet, consectetur " +
			"adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna " +
			"aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
			"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
			"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
			"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia " +
			"deserunt mollit anim id est laborum.";
	
	private QuestionContent mContent;
	
	/**
	 * Constructor
	 */
	public QuestionViewActivity() {
		mContent = new QuestionContent();
	}
	
	/**
	 * Initializes data depending on what is passed in the intent. Creates adapters and
	 * listeners for all data interactions that will happen.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionview);
		
		Intent intent = this.getIntent();
		
		UniqueId id = (UniqueId) intent.getSerializableExtra(QUESTION_ID_EXTRA);
		QuestionItem question = null; // get question from controller somehow
		if(question == null) {
			// TODO: implement Question Creation.
			
			// Generate our own data to test displaying before the other modules work.
			if(GENERATE_TEST_DATA) {
				question = new QuestionItem(new UniqueId(), null, "Question Author",
						null, "Question: " + LIPSUM, 0, "Question Title");
				AnswerItem answer1 = new AnswerItem(new UniqueId(), question.mUniqueId, "ans1 Author",
						null, "Answer 1: " + LIPSUM, 0);
				AnswerItem answer2 = new AnswerItem(new UniqueId(), question.mUniqueId, "ans2 Author",
						null, "Answer 2: " + LIPSUM, 0);
				CommentItem comment1 = new CommentItem(new UniqueId(), question.mUniqueId, "c1a", null, "comment1... I wanted a longer comment so yeah... words and things and stuff", 0);
				CommentItem comment2 = new CommentItem(new UniqueId(), answer1.mUniqueId, "c2a", null, "comment2", 0);
				CommentItem comment3 = new CommentItem(new UniqueId(), answer1.mUniqueId, "c3a", null, "comment3", 0);
				
				mContent.setQuestion(question);
				mContent.addAnswers(answer1, answer2);
				mContent.addComments(comment1, comment2, comment3);
				
				TextView title = (TextView) findViewById(R.id.titleView);
				title.setText(mContent.getQuestion().getTitle());
				
				ListView qaList = (ListView) findViewById(R.id.qaItemView);
				ListAdapter adapter = mContent.getListAdapter(this, R.id.qaItemView);
				
				qaList.setAdapter(adapter);
				((BaseAdapter) adapter).notifyDataSetChanged();
			}
		}
		else {
			// TODO: Implement interactions with the controller to get Answers/Comments.
		}
		
	}		
}
