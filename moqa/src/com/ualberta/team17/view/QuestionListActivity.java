package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter;

/**
 * This class displays the list of requested questions and answers. It receives an intent
 * from QuestionTaxonomyActivity and sends intents to QuestionViewActivity. It also supports
 * sorting of the data on display. Search functionality is also present, although it is not 
 * implemented in this class.
 * 
 * @author Jared
 *
 */
public class QuestionListActivity extends Activity {
	
	private static final boolean GENERATE_TEST_DATA = true; //Test
	public static final String FILTER_EXTRA = "FILTER";
	
	private DataFilter mDataFilter;
	private List<QBody> mQuestions;
	
	/**
	 * This class holds a question and its child answers.
	 * TODO Implement support for answer browsing
	 * 
	 * @author Jared
	 *
	 */
	private class QBody {
		public QuestionItem parent;
		public List<AnswerItem> answers;
		
		public QBody(QuestionItem initParent) {
			parent = initParent;
			answers = new ArrayList<AnswerItem>();
		}
	}

	/**
	 * The adapter for QBody. Binds the title of the question, the upvote count
	 * and the comment count (the number of answers).
	 * 
	 * @author Jared
	 *
	 */
	protected class QuestionListAdapter extends ArrayAdapter<QBody> {
		Context mContext;
		List<QBody> mObjects;
		
		public QuestionListAdapter(Context context, int textViewResourceId, List<QBody> objects) {
			super(context, textViewResourceId, objects);
			mContext = context;
			mObjects = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			QBody item = (QBody) this.getItem(position);
			if (item == null) {
				return convertView;
			}
			
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.qa_listview_item, parent, false);
			}
			
			TextView titleTextView = (TextView) convertView.findViewById(R.id.titleText);
			TextView commentTextView = (TextView) convertView.findViewById(R.id.commentsText);
			TextView upvoteTextView = (TextView) convertView.findViewById(R.id.upvoteText);
			
			titleTextView.setText(mObjects.get(position).parent.getTitle());
			commentTextView.setText(Integer.toString(mObjects.get(position).answers.size()));
			upvoteTextView.setText(Integer.toString(mObjects.get(position).parent.getUpvoteCount()));
			
			return convertView;
		}

		// TODO implement comment and upvote graphics
	}
	
	/**
	 * Initializes data depending on what is passed in the intent. Creates adapters and
	 * listeners for all data interactions that will happen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question_list);
		
		Intent filterIntent = this.getIntent();
		mDataFilter = (DataFilter) filterIntent.getSerializableExtra(FILTER_EXTRA);
		
		mQuestions = new ArrayList<QBody>();
		
		if (mDataFilter == null) {
			if (GENERATE_TEST_DATA) {
				mQuestions = getTestData();			
			}
		}
		else {
			//TODO Use intent to get questions from controller.			
		}
		
		ListView qList = (ListView) findViewById(R.id.questionListView);
		qList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int i, long l) {
				QuestionListActivity.this.handleListViewItemClick(av, view, i, l);
			}
		});
		QuestionListAdapter adapter = new QuestionListAdapter(this, R.id.questionListView, mQuestions);
		
		qList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		//TODO add support for answers as well.
	}

	/**
	 * Handles the event when a listview item is clicked.
	 * TODO Sort out how to pass QuestionItem to QuestionViewActivity
	 * @param av
	 * @param view
	 * @param i
	 * @param l
	 */
	private void handleListViewItemClick(AdapterView<?> av, View view, int i, long l) {
		QBody body = mQuestions.get(i);
		QuestionItem question = body.parent;
		Intent intent = new Intent(QuestionListActivity.this, QuestionViewActivity.class);
		intent.putExtra(QuestionViewActivity.QUESTION_ID_EXTRA, question.getUniqueId().toString());
		startActivity(intent);
	}
	
	/**
	 * Creates the toolbar at the top of the app. This is temporary.
	 * TODO change all actions to be triggered by buttons and remove this toolbar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.question_list_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_new_question) {
			createNewQuestion();
			return true;
		}
		else if (id == R.id.action_sort) {
			applySort();
			return true;
		}
		else if (id == R.id.action_search) {
			search();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Creates an intent that is passed to QuestionViewActivity.
	 * This allows for a new question to be created.
	 * TODO implement createNewQuestion().
	 */
	private void createNewQuestion() {
		
	}
	
	/**
	 * Opens a pop up that will apply a sort to the data currently in this listview.
	 * TODO implement applySort().
	 */
	private void applySort() {
		
	}
	
	/**
	 * Activates search.
	 * TODO implement search().
	 */
	private void search() {
		
	}
	
	/**
	 * Generates test data for filling the listview with. Temporary.
	 * @return an ArrayList<QBody> for display purposes in the view.
	 */
	private ArrayList<QBody> getTestData() {
		ArrayList<QBody> questions = new ArrayList<QBody>();
		
		QuestionItem qi1 = new QuestionItem(new UniqueId(), null, "QAuthor1", null, "Question body", 4, "Question 1 test");
		AnswerItem ai1 = new AnswerItem(new UniqueId(), null, "AAuthor1", null, "Answer 1", 17);
		AnswerItem ai2 = new AnswerItem(new UniqueId(), null, "AAuthor2", null, "Answer 2", 3);
		QBody qb1 = new QBody(qi1);
		qb1.answers.add(ai1);
		qb1.answers.add(ai2);
		questions.add(qb1);
		
		QuestionItem qi2 = new QuestionItem(new UniqueId(), null, "QAuthor2", null, "Question 2 body", 26, "Question 2 Title");
		AnswerItem ai3 = new AnswerItem(new UniqueId(), null, "AAuthor1", null, "Answer 1", 0);
		AnswerItem ai4 = new AnswerItem(new UniqueId(), null, "AAuthor2", null, "Answer 2", 6);
		AnswerItem ai5 = new AnswerItem(new UniqueId(), null, "AAuthor3", null, "Answer 3", 174);
		AnswerItem ai6 = new AnswerItem(new UniqueId(), null, "AAuthor4", null, "Answer 4", 13);
		QBody qb2 = new QBody(qi2);
		qb2.answers.add(ai3);
		qb2.answers.add(ai4);
		qb2.answers.add(ai5);
		qb2.answers.add(ai6);
		questions.add(qb2);
		
		QuestionItem qi3 = new QuestionItem(new UniqueId(), null, "QAuthor3", null, "Question 3 body", 113, 
				"This demonstrates what a longer question title will look like. We wouldn't really need to " +
				"restrict the length of titles much at all. We could just ensure that all views that contain " +
				"a question are scrollable.");
		AnswerItem ai7 = new AnswerItem(new UniqueId(), null, "AAuthor1", null, "Answer 1", 1);
		QBody qb3 = new QBody(qi3);
		qb3.answers.add(ai7);
		questions.add(qb3);
		
		QuestionItem qi4 = new QuestionItem(new UniqueId(), null, "QAuthor1", null, "Question body", 1354, "Question 4 test.\n" +
				"NL\nNL\nNL\nNL");
		AnswerItem ai8 = new AnswerItem(new UniqueId(), null, "AAuthor1", null, "Answer 1", 0);
		AnswerItem ai9 = new AnswerItem(new UniqueId(), null, "AAuthor2", null, "Answer 2", 1);
		QBody qb4 = new QBody(qi4);
		qb4.answers.add(ai8);
		qb4.answers.add(ai9);
		questions.add(qb4);
		
		return questions;
	}
}
