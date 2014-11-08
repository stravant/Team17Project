package com.ualberta.team17.view;

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

import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;
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
	
	private QuestionTaxonomyActivity.taxonomies mTaxonomy;
	private IncrementalResult mIR;
	
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
	 * 
	 * @author Jared
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question_list);
		
		Intent filterIntent = this.getIntent();
		mTaxonomy = (QuestionTaxonomyActivity.taxonomies) filterIntent.getSerializableExtra(FILTER_EXTRA);
		if (null == mTaxonomy) {
			System.out.println("No taxonomy specified -- defaulting to all questions");
			mTaxonomy = QuestionTaxonomyActivity.taxonomies.AllQuestions;
		}
		
		IItemComparator comp;
		DataFilter df = new DataFilter();
		
		switch (mTaxonomy) {
		case AllQuestions:
			comp = new DateComparator();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			break;
		case MyActivity:
			
			break;
		case Favorites:
			mIR = QAController.getInstance().getFavorites();
			break;
		case MostUpvotedQs:
			comp = new UpvoteComparator();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			break;	
		case MostUpvotedAs:
			comp = new UpvoteComparator();
			df.setTypeFilter(ItemType.Answer);
			mIR = QAController.getInstance().getObjects(df, comp);
			break;
		case RecentlyViewed:
			mIR = QAController.getInstance().getRecentItems();
		}
		
		this.addObserver(mIR);		
		
		ListView qList = (ListView) findViewById(R.id.questionListView);
		qList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int i, long l) {
				QuestionListActivity.this.handleListViewItemClick(av, view, i, l);
			}
		});
		
		if (mIR != null) {
			qList.setAdapter(new QuestionListAdapter(this, R.id.questionListView, mIR.getCurrentResults()));
		}
	}
	
	/**
	 * A convenience function for adding an observer to ir.
	 * @param ir The incremental result to observe.
	 * 
	 * @author Jared
	 */
	private void addObserver(IncrementalResult ir) {
		if (ir != null) {
			ir.addObserver(new IIncrementalObserver() {
	
				@Override
				public void itemsArrived(List<QAModel> item, int index) {
					ListView qList = (ListView) findViewById(R.id.questionListView);
					qList.invalidate();
					
					qList.setAdapter(new QuestionListAdapter(QuestionListActivity.this, R.id.questionListView, mIR.getCurrentResults()));
				}
			});
		}
	}
	
	/**
	 * Handles the event when a listview item is clicked.
	 * @param av
	 * @param view
	 * @param i
	 * @param l
	 * 
	 * @author Jared
	 */
	private void handleListViewItemClick(AdapterView<?> av, View view, int i, long l) {
		QAModel qaModel = mIR.getCurrentResults().get(i);
		QuestionItem question = (QuestionItem) qaModel;
		if (question != null) {
			QAController.getInstance().markRecentlyViewed(qaModel);
			
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

	/**
	 * This handles the menu at the top of this view. This is temporary.
	 * 
	 * @author Jared
	 */
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
		else if (id == R.id.action_sort_date) {
			applyDateSort(item);
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
	 * 
	 * @author Jared
	 */
	private void createNewQuestion() {
		Intent intent = new Intent(QuestionListActivity.this, QuestionViewActivity.class);		
		startActivity(intent);
	}
	
	/**
	 * Opens a pop up that will apply a sort to the data currently in this listview.
	 * TODO implement applySort().
	 */
	private void applySort() {
		
	}
	
	/**
	 * Applies a date sort. 
	 * @param toggleDate The menu item. It has its state changed to show which way the sort will sort.
	 * 
	 * @author Jared
	 */
	private void applyDateSort(MenuItem toggleDate) {

		if (toggleDate.getTitle().toString().equals(getString(R.string.action_sort_date_asc))) {
			toggleDate.setTitle(getString(R.string.action_sort_date_desc));
			
			IItemComparator comp = new DateComparator();
			comp.setCompareDirection(SortDirection.Ascending);
			DataFilter df = new DataFilter();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			
			this.addObserver(mIR);
		}
		else if (toggleDate.getTitle().toString().equals(getString(R.string.action_sort_date_desc))) {
			toggleDate.setTitle(getString(R.string.action_sort_date_asc));
			
			IItemComparator comp = new DateComparator();
			comp.setCompareDirection(SortDirection.Descending);
			DataFilter df = new DataFilter();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			
			this.addObserver(mIR);
		}
	}
	
	/**
	 * Activates search.
	 * TODO implement search().
	 */
	private void search() {
		
	}
}
