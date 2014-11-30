package com.ualberta.team17.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.R;
import com.ualberta.team17.view.TaxonomyMenuFragment.OnItemSelectedListener;

/**
 * This class displays the list of requested questions and answers. It receives an intent
 * from QuestionTaxonomyActivity and sends intents to QuestionViewActivity. It also supports
 * sorting of the data on display. Search functionality is also present, although it is not 
 * implemented in this class.
 * 
 * @author Jared
 *
 */
public class QuestionListActivity extends Activity implements OnItemSelectedListener, IQAView {
	public final static String SEARCH_TERM = "search_term";
	
	private String[] sortOptions;
	private DrawerLayout rightDrawerLayout;
	private ListView rightDrawerList;
	private ActionBarDrawerToggle rightDrawerToggle;
	TaxonomyMenuFragment leftDrawer = new TaxonomyMenuFragment();
	SortMenuFragment rightDrawer = new SortMenuFragment();
	ListFragment fragment = new ListFragment();
	Bundle args;

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
		if (savedInstanceState == null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().add(R.id.content_frame, leftDrawer).commit();
			fragmentManager.beginTransaction().replace(R.id.content_frame, rightDrawer).commit();
		}
		
		Intent intent = this.getIntent();
				
		if (intent == null) {
			return;
		}
		
		if (intent.getSerializableExtra(SEARCH_TERM) != null) {
			String searchValue = (String) intent.getSerializableExtra(SEARCH_TERM);	
			
			args = new Bundle();
			args.putString(SEARCH_TERM, searchValue);
			fragment = new ListFragment();
			FragmentManager fragmentManager = getFragmentManager();
			fragment.setArguments(args);
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			setTitle(getResources().getText(R.string.action_search));
		}
		
		// set the actionbar to use the custom view
		getActionBar().setDisplayShowCustomEnabled(true);
		//set the custom view to use
		getActionBar().setCustomView(R.layout.sort_menu_icon);
		ImageButton iv = (ImageButton) findViewById(R.id.imgRightMenu);
		iv.setOnClickListener(new View.OnClickListener() {
		                @Override
		                public void onClick(View v) {
		                	if(rightDrawerLayout.isDrawerOpen(Gravity.START)
		                			&& !rightDrawerLayout.isDrawerOpen(Gravity.END))
		                		return;
		                	if(!rightDrawerLayout.isDrawerOpen(Gravity.END))
		                        rightDrawerLayout.openDrawer(Gravity.END);
		                    else
		                        rightDrawerLayout.closeDrawer(Gravity.END); }
		            });

	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		leftDrawer.mDrawerToggle.syncState();
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		String[] myTaxonomy = getResources().getStringArray(R.array.taxonomies);
		args = new Bundle();
		args.putInt(ListFragment.TAXONOMY_NUM, position);
		fragment = new ListFragment();
		FragmentManager fragmentManager = getFragmentManager();
		fragment.setArguments(args);
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		setTitle(myTaxonomy[position]);
	}


	/**
	 * Creates the toolbar at the top of the app.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.moqa_menu, menu);
		
		menu.setGroupVisible(R.id.questionlist_group, true);
		menu.setGroupVisible(R.id.questionview_group, false);
		
		// Get the search menu item
		MenuItem mi = menu.findItem(R.id.action_search);
		SearchItem si = new SearchItem(this.getBaseContext());
		mi.setActionView(si);
		si.setReturnListener(new SearchReturnListener());

		return true;
	}

	/**
	 * This handles the menu at the top of this view.
	 * 
	 * @author Jared
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (leftDrawer.mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (id == R.id.action_new_question) {
			fragment.createNewQuestion();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(int position) {
		selectItem(position);
	}

	/**
	 * Recreates the ListFragment with the previous bundle.
	 */
	private void refresh() {
		if (args == null) {
			return;
		}
		
		String[] myTaxonomy = getResources().getStringArray(R.array.taxonomies);
		int taxonomy = args.getInt(ListFragment.TAXONOMY_NUM);
		
		fragment = new ListFragment();
		FragmentManager fragmentManager = getFragmentManager();
		fragment.setArguments(args);
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		setTitle(myTaxonomy[taxonomy]);
	}
	
	public class SortMenuFragment extends Fragment {
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			sortOptions = getResources().getStringArray(R.array.sortOptions);
			rightDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			rightDrawerList = (ListView) findViewById(R.id.right_drawer);
			rightDrawerList.setAdapter(new ArrayAdapter<String>(getActivity(),
					R.layout.sort_drawer_item, sortOptions));
			setHasOptionsMenu(true);
			rightDrawerList.setOnItemClickListener(new RightDrawerItemClickListener());
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
			View rootView = inflater.inflate(R.layout.question_list, container, false);
			return rootView;
		}
		private class RightDrawerItemClickListener implements ListView.OnItemClickListener {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int num_taxonomies = 5;
				//String[] mySort = getResources().getStringArray(R.array.sortOptions);
				Bundle args = new Bundle();
				args.putInt(ListFragment.TAXONOMY_NUM, position+num_taxonomies);
				fragment = new ListFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragment.setArguments(args);
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
				//setTitle(mySort[position]);
			}
		}
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
			leftDrawer.mDrawerToggle.onConfigurationChanged(newConfig);
		}
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		
			return super.onOptionsItemSelected(item);
		}	

	}
	
	/**
	 * Triggered whenever the enter is hit while the search bar is active.
	 * 
	 * @author Jared
	 *
	 */
	private class SearchReturnListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId,
                KeyEvent event) {
            if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                ViewGroup g = (ViewGroup) v.getParent();
    			if (g != null) {
    				
    				EditText et = (EditText) g.findViewById(R.id.searchBar);
    				if (et != null) {
    					
    					if (et.isShown()) {
    						et.setVisibility(View.GONE);
    						
    						String searchTerm = et.getText().toString();
    						if (searchTerm.equals("") || searchTerm.equals("\n")) {
    							return false;
    						}						

    						Bundle args = new Bundle();
    						args.putString(SEARCH_TERM, searchTerm);
    						fragment = new ListFragment();
    						FragmentManager fragmentManager = getFragmentManager();
    						fragment.setArguments(args);
    						fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    						QuestionListActivity.this.getActionBar().setTitle(getResources().getText(R.string.action_search));
    																	
    					}
    					else {
    						// Show the bar and activate it
    						et.setVisibility(View.VISIBLE);
    						et.setSelected(true);
    					}							
    				}
    			}
                
               return true;

            }
            return false;
        }
    }

	@Override
	public void update(QAModel model) {
		refresh();
	}
}