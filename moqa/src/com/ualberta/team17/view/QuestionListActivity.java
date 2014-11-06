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
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.comparators.DateComparator;
import com.ualberta.team17.datamanager.comparators.UpvoteComparator;

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
	
	public static final String FILTER_EXTRA = "FILTER";
	
	private List<QAModel> mQAModels;
	private QuestionTaxonomyActivity.taxonomies mTaxonomy;
	private IncrementalResult mIR;
	private QuestionListAdapter mAdapter;
	
	/**
	 * The adapter for QAModel. Binds the title of the question, the upvote count
	 * and the comment count (the number of answers).
	 * 
	 * @author Jared
	 *
	 */
	protected class QuestionListAdapter extends ArrayAdapter<QAModel> {
		Context mContext;
		List<QAModel> mObjects;
		
		public QuestionListAdapter(Context context, int textViewResourceId, List<QAModel> objects) {
			super(context, textViewResourceId, objects);
			mContext = context;
			mObjects = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			QAModel item = (QAModel) this.getItem(position);
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
			
			QuestionItem qi = (QuestionItem)item;
			if (qi != null) {
				IItemComparator comp = new DateComparator();
				IncrementalResult children = QAController.getInstance().getChildren(item, comp);
				
				titleTextView.setText(qi.getTitle());
				commentTextView.setText(Integer.toString(children.getCurrentResults().size()));
				upvoteTextView.setText(Integer.toString(-1));
			}			
			
			return convertView;
		}
		
		public void setItems(List<QAModel> qa) {
			this.mObjects = qa;
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
		mTaxonomy = (QuestionTaxonomyActivity.taxonomies) filterIntent.getSerializableExtra(FILTER_EXTRA);
		
		
		IItemComparator comp;
		DataFilter df = new DataFilter();
		
		switch (mTaxonomy) {
		case AllQuestions:
			comp = new DateComparator();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			mQAModels = mIR.getCurrentResults();
			break;
		case MyActivity:
			//ir = QAController.getInstance().getActivity();
			break;
		case Favorites:
			mIR = QAController.getInstance().getFavorites();
			break;
		case MostUpvotedQs:
			comp = new UpvoteComparator();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			mQAModels = mIR.getCurrentResults();
			break;	
		case MostUpvotedAs:
			comp = new UpvoteComparator();
			df.setTypeFilter(ItemType.Answer);
			mIR = QAController.getInstance().getObjects(df, comp);
			mQAModels = mIR.getCurrentResults();
			break;
		default:
			mQAModels = new ArrayList<QAModel>();
			break;
		}
		
		mIR.addObserver(new IIncrementalObserver() {

			@Override
			public void itemsArrived(List<QAModel> item, int index) {
				ListView qList = (ListView) findViewById(R.id.questionListView);
				qList.invalidate();
				
				qList.setAdapter(new QuestionListAdapter(QuestionListActivity.this, R.id.questionListView, mIR.getCurrentResults()));
			}} );
		
		ListView qList = (ListView) findViewById(R.id.questionListView);
		qList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int i, long l) {
				QuestionListActivity.this.handleListViewItemClick(av, view, i, l);
			}
		});
		
		qList.setAdapter(new QuestionListAdapter(this, R.id.questionListView, mIR.getCurrentResults()));
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
		QAModel qaModel = mIR.getCurrentResults().get(i);
		QuestionItem question = (QuestionItem) qaModel;
		if (question != null) {
			Intent intent = new Intent(QuestionListActivity.this, QuestionViewActivity.class);
			intent.putExtra(QuestionViewActivity.QUESTION_ID_EXTRA, question.getUniqueId().toString());
			startActivity(intent);
		}
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
}
