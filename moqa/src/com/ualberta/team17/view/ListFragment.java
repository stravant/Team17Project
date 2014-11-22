package com.ualberta.team17.view;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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

public class ListFragment extends Fragment {
	public static final String TAXONOMY_NUM = "taxonomy_number";
	public static final String FILTER_EXTRA = "FILTER";
	
	private IncrementalResult mIR;
	private Context mContext;
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
    }
	
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
		static final int titleSize = 300;
		
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
			
			if (item.getField("author") == null ||
					item.getField("body") == null || 
					item.getField("id") == null ||
					item.getField("date") == null) {
				// This info is required. If it isn't here, something is wrong.
				return convertView;
			}
			
			String body = (String)item.getField("body");
			String author = (String) item.getField("author");
			
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.qa_listview_item, parent, false);  
			}
			
			TextView titleTextView = (TextView) convertView.findViewById(R.id.titleText);
			TextView commentTextView = (TextView) convertView.findViewById(R.id.commentsText);
			TextView upvoteTextView = (TextView) convertView.findViewById(R.id.upvoteText);
			TextView userTextView = (TextView) convertView.findViewById(R.id.userText);
			
			// Set the data using getField
			IItemComparator comp = new DateComparator();
			IncrementalResult children = QAController.getInstance().getChildren(item, comp);
				
			commentTextView.setText(Integer.toString(children.getCurrentResults().size()));
			upvoteTextView.setText(Integer.toString(-1));
			userTextView.setText(author);
			
			if (item.getField("title") == null) {
				// Must be an answer
				titleTextView.setText(
						body.length() > titleSize ? 
						body.substring(0, titleSize) + "..." :
						body
				);
			}
			else {
				String title = (String) item.getField("title");
				if (title != null) {
					titleTextView.setText(title);
				}
			}
			
			return convertView;
		}
		
		public void setItems(List<QAModel> qa) {
			this.mObjects = qa;
		}
		
		// TODO implement comment and upvote graphics
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_question_list, container, false);
	    int mTaxonomy = getArguments().getInt(TAXONOMY_NUM);
	    
		IItemComparator comp;
		DataFilter df = new DataFilter();
		
		switch (mTaxonomy) {
		case 0:
			comp = new DateComparator();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			break;
		case 1:
			
			break;
		case 2:
			mIR = QAController.getInstance().getFavorites();
			break;
		case 3:
			comp = new UpvoteComparator();
			df.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(df, comp);
			break;	
		case 4:
			comp = new UpvoteComparator();
			df.setTypeFilter(ItemType.Answer);
			mIR = QAController.getInstance().getObjects(df, comp);
			break;
		case 5:
			mIR = QAController.getInstance().getRecentItems();
		}
		
		addObserver(mIR);		
		ListView qList = (ListView) rootView.findViewById(R.id.questionListView);
		
		if (qList != null) {
			qList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> av, View view, int i, long l) {
					handleListViewItemClick(av, view, i, l);
				}
			});
		}
		
		if (mIR != null) {
			Activity a = getActivity();
			
			if (a != null) {
				qList.setAdapter(new QuestionListAdapter(a, R.id.questionListView, mIR.getCurrentResults()));
			}			
		}
		
		return rootView;
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
		QAModel qaModel = mIR.getCurrentResults().get(i); //TODO support answers
		QuestionItem question = (QuestionItem) qaModel;
		if (question != null) {
			QAController.getInstance().markRecentlyViewed(qaModel);
			
			Activity a = getActivity();
			if (a != null) {
				Intent intent = new Intent(a, QuestionViewActivity.class);
				intent.putExtra(QuestionViewActivity.QUESTION_ID_EXTRA, question.getUniqueId().toString());
				startActivity(intent);
			}			
		}
	}
	
	
	
	/**
	 * Creates an intent that is passed to QuestionViewActivity.
	 * This allows for a new question to be created.
	 * 
	 * @author Jared
	 */
	void createNewQuestion() {
		Activity a = getActivity();
		if (a != null) {
			Intent intent = new Intent(a, QuestionViewActivity.class);		
			startActivity(intent);
		}		
	}
	
	/**
	 * Opens a pop up that will apply a sort to the data currently in this listview.
	 * TODO implement applySort().
	 */
	void applySort() {
		
	}
	
	/**
	 * Applies a date sort. 
	 * @param toggleDate The menu item. It has its state changed to show which way the sort will sort.
	 * 
	 * @author Jared
	 */
	void applyDateSort(MenuItem toggleDate) {

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
	void search() {
		
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
					Activity activity = ListFragment.this.getActivity();
					if (activity == null) {
						return;
					}
					ListView qList = (ListView) activity.findViewById(R.id.questionListView);

					if (qList != null) {
						qList.invalidate();
						
						qList.setAdapter(new QuestionListAdapter(ListFragment.this.getActivity(), R.id.questionListView, mIR.getCurrentResults()));
					}					
				}
			});
		}
	}
	
}