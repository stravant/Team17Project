package com.ualberta.team17.view;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
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
	
	private IncrementalResult mIR;
    private String[] myTaxonomy;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
	
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
		setContentView(R.layout.question_list);
		
		myTaxonomy = getResources().getStringArray(R.array.taxonomies);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, myTaxonomy));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  
                mDrawerLayout,
                R.drawable.ic_drawer,  
                R.string.drawer_open,  
                R.string.drawer_close  
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (savedInstanceState == null) {
            selectItem(0);
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
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
	
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        QuestionListFragment fragment = new QuestionListFragment();
        Bundle args = new Bundle();
        args.putInt(QuestionListFragment.TAXONOMY_NUM, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(myTaxonomy[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        CharSequence mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	public class QuestionListFragment extends Fragment {
		public static final String TAXONOMY_NUM = "taxonomy_number";
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
			qList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> av, View view, int i, long l) {
					handleListViewItemClick(av, view, i, l);
				}
			});
			
			if (mIR != null) {
				qList.setAdapter(new QuestionListAdapter(QuestionListActivity.this, R.id.questionListView, mIR.getCurrentResults()));
			}
			
			return rootView;
	    }
		
	}
	public static enum taxonomies {
		AllQuestions(0), 
		MyActivity(1),
		Favorites(2) ,
		MostUpvotedQs(3), 
		MostUpvotedAs(4),
		RecentlyViewed(5);
		int value;
		private taxonomies(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	}
}
