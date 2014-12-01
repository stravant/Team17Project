package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.TopUpvotedDataFilter;
import com.ualberta.team17.datamanager.comparators.DateComparator;
import com.ualberta.team17.datamanager.comparators.IdentityComparator;
import com.ualberta.team17.datamanager.comparators.UpvoteComparator;

public class ListFragment extends Fragment {
	public static final String TAXONOMY_NUM = "taxonomy_number";
	public static final String FILTER_EXTRA = "FILTER";
	public final static String SORT_TYPE = "sort_type";

	
	private IncrementalResult mIR;
	private DataFilter datafilter = new DataFilter();
	private QuestionListAdapter mAdapter;
	private List<QAModel> mItems;
	
	private UpdateOnItemUpdateListener mItemUpdatedListener = new UpdateOnItemUpdateListener();
	
	private class UpdateOnItemUpdateListener implements IQAView {
		@Override
		public void update(QAModel model) {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_question_list, container, false);
	    int mTaxonomy = getArguments().getInt(TAXONOMY_NUM, -1);
	    
		IItemComparator comp = null;
		int mSort = getArguments().getInt(SORT_TYPE);
		if (mSort != 0) {
			switch (mSort) {
			//Ascending Date
			case 0:
				comp = new DateComparator();
				comp.setCompareDirection(SortDirection.Ascending);
				break;
			//Descending Date	
			case 1:
				comp = new DateComparator();
				comp.setCompareDirection(SortDirection.Descending);
				break;
			//Descending Attachments	
			case 2:
				/*
				 * TODO set comp to AttachmentComparator()
				 * 
				 */
				break;
			}
			
		}
		
		switch (mTaxonomy) {
		case -1:
			// This means it wasn't sent by a taxonomy.
			// Must be a search.
			String searchTerm = getArguments().getString(QuestionListActivity.SEARCH_TERM);
			
			if (searchTerm == null) {
				break;
			}
			
			comp = new IdentityComparator();
			
			datafilter.setTypeFilter(ItemType.Question);
			datafilter.addFieldFilter(AuthoredTextItem.FIELD_BODY, searchTerm, 
					DataFilter.FilterComparison.QUERY_STRING, DataFilter.CombinationMode.SHOULD);
			datafilter.addFieldFilter(QuestionItem.FIELD_TITLE, searchTerm, 
					DataFilter.FilterComparison.QUERY_STRING, DataFilter.CombinationMode.SHOULD);
			mIR = QAController.getInstance().getObjects(datafilter, comp);
			
			break;
		case 0:
			if (comp == null) {
				comp = new DateComparator();
			}	
			datafilter.setTypeFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(datafilter, comp);
			break;
		case 1:
			if (comp == null) {
				comp = new DateComparator();
				comp.setCompareDirection(SortDirection.Descending);
			}	
			datafilter.addFieldFilter(AuthoredItem.FIELD_AUTHOR, QAController.getInstance().getUserContext().getUserName(), FilterComparison.EQUALS);
			mIR = QAController.getInstance().getObjects(datafilter, comp);
			break;
		case 2:
			mIR = QAController.getInstance().getFavorites(comp);
			datafilter = null;
			break;
		case 3:
			comp = new IdentityComparator();
			datafilter = new TopUpvotedDataFilter(ItemType.Question);
			mIR = QAController.getInstance().getObjects(datafilter, comp);
			break;	
		case 4:
			comp = new IdentityComparator();
			datafilter = new TopUpvotedDataFilter(ItemType.Answer);
			mIR = QAController.getInstance().getObjects(datafilter, comp);
			break;
		case 5:
			mIR = QAController.getInstance().getRecentItems(comp);
			datafilter = null;
			break;
		}
		
		addObserver(mIR);		
		ListView qList = (ListView) rootView.findViewById(R.id.questionListView);
		qList.setOnItemClickListener(new listItemClickedListener());
		mItems = new ArrayList<QAModel>();
		if (mIR != null) {
			mItems.addAll(mIR.getCurrentResults());
			mAdapter = new QuestionListAdapter(ListFragment.this.getActivity(), R.id.questionListView, mItems);
			qList.setAdapter(mAdapter);
		}

		qList.setOnScrollListener(new InfiniteScoller());
		
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
		QAModel qaModel = mIR.getCurrentResults().get(i);
		QAController.getInstance().markRecentlyViewed(qaModel);
		
		Intent intent = new Intent(this.getActivity(), QuestionViewActivity.class);
		
		if (qaModel instanceof AnswerItem) {
			intent.putExtra(QuestionViewActivity.QUESTION_ID_EXTRA, ((AnswerItem) qaModel).getParentItem().toString());
		} else {
			intent.putExtra(QuestionViewActivity.QUESTION_ID_EXTRA, qaModel.getUniqueId().toString());
		}
		startActivity(intent);
	}
	
	/**
	 * Creates an intent that is passed to QuestionViewActivity.
	 * This allows for a new question to be created.
	 * 
	 * @author Jared
	 */
	void createNewQuestion() {
		Intent intent = new Intent(ListFragment.this.getActivity(), QuestionViewActivity.class);		
		startActivity(intent);
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
				public void itemsArrived(List<QAModel> items, int index) {
					Activity activity = ListFragment.this.getActivity();
					if (activity == null) {
						return;
					}
					for (QAModel item: items) {
						item.addView(mItemUpdatedListener);
					}
					mItems.clear();
					mItems.addAll(mIR.getCurrentResults());
					mAdapter.notifyDataSetChanged();				
				}
			});
		}
	}
	
	/**
	 * Handles listview item click events.
	 * 
	 * @author Jared
	 *
	 */
	private class listItemClickedListener implements AdapterView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> av, View view, int i, long l) {
			handleListViewItemClick(av, view, i, l);
		}
	}
	
	/**
	 * The adapter for QAModel. Binds the title of the question, the upvote count
	 * and the comment count (the number of answers).
	 * 
	 * @author Jared
	 *
	 */
	private class QuestionListAdapter extends ArrayAdapter<QAModel> {
		Context mContext;
		static final int titleSize = 300;
		
		public QuestionListAdapter(Context context, int textViewResourceId, List<QAModel> objects) {
			super(context, textViewResourceId, objects);
			mContext = context;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.qa_listview_item, parent, false);
			}

			QAModel item = (QAModel) this.getItem(position);
			if (item == null) {
				return convertView;
			}

			TextView titleTextView = (TextView) convertView.findViewById(R.id.titleText);
			TextView commentTextView = (TextView) convertView.findViewById(R.id.commentText);
			TextView upvoteTextView = (TextView) convertView.findViewById(R.id.upvoteText);
			TextView userTextView = (TextView) convertView.findViewById(R.id.userText);
			TextView repliesTextView = (TextView) convertView.findViewById(R.id.answerCountView);
			
			// Set the data using getField
			if (null != item.getField(AuthoredTextItem.FIELD_COMMENTS)) {
				commentTextView.setText(item.getField(AuthoredTextItem.FIELD_COMMENTS).toString());
			}

			if (null != item.getField(AuthoredTextItem.FIELD_UPVOTES)) {
				upvoteTextView.setText(item.getField(AuthoredTextItem.FIELD_UPVOTES).toString());
			}

			if (null != item.getField(AuthoredItem.FIELD_AUTHOR)) {
				userTextView.setText(item.getField(AuthoredItem.FIELD_AUTHOR).toString());
			}

			if (null != item.getField(QuestionItem.FIELD_TITLE)) {
				titleTextView.setText(item.getField(QuestionItem.FIELD_TITLE).toString());
			} else if (null != item.getField(AuthoredTextItem.FIELD_BODY)) {
				String body = (String)item.getField(AuthoredTextItem.FIELD_BODY);
				titleTextView.setText(
						body.length() > titleSize ? 
						body.substring(0, titleSize) + "..." :
						body
				);
			}

			return convertView;
		}
	}

	public class InfiniteScoller implements AbsListView.OnScrollListener {
		int pageNo = 0;
		int loadedItemCount = 0;
		boolean isLoading;

		/**
		 * Queries elastic search for the specified pageNumber
		 * @param pageNumber
		 */
		public void getPage(int pageNumber) {
			if (null == datafilter) {
				return;
			}

			datafilter.setPage(pageNumber);
			QAController.getInstance().getObjectsWithResult(datafilter, mIR);
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			 if (isLoading && (totalItemCount > loadedItemCount)) {
				isLoading = false;
				loadedItemCount = totalItemCount;
				++pageNo;
			 }

			 if (!isLoading && (firstVisibleItem + visibleItemCount) >= totalItemCount) {
				getPage(pageNo + 1);
				isLoading = true;
			 }
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			return; // We don't do anything here right now
		}
	}
}