package com.ualberta.team17.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import com.ualberta.team17.R;

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

	private String[] myTaxonomy;
	private String[] sortOptions;
	private DrawerLayout mDrawerLayout;
	private DrawerLayout rightDrawerLayout;
	private ListView mDrawerList;
	private ListView rightDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private ActionBarDrawerToggle rightDrawerToggle;


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
			TaxonomyMenuFragment leftDrawer = new TaxonomyMenuFragment();
			SortMenuFragment rightDrawer = new SortMenuFragment();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().add(R.id.content_frame, leftDrawer).commit();
			fragmentManager.beginTransaction().replace(R.id.content_frame, rightDrawer).commit();

		}
	}


	private void selectItem(int position) {
		// update the main content by replacing fragments
		ListFragment fragment = new ListFragment();
		Bundle args = new Bundle();
		args.putInt(ListFragment.TAXONOMY_NUM, position);
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(myTaxonomy[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}


	/**
	 * Creates the toolbar at the top of the app. This is temporary.
	 * TODO change all actions to be triggered by buttons and remove this toolbar.
	 */
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
		ListFragment fragment = new ListFragment();

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (id == R.id.action_new_question) {
			fragment.createNewQuestion();
			return true;
		}
		else if (id == R.id.action_sort) {
			fragment.applySort();
			return true;
		}
		else if (id == R.id.action_sort_date) {
			fragment.applyDateSort(item);

			return true;
		}
		else if (id == R.id.action_search) {
			fragment.search();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public class TaxonomyMenuFragment extends Fragment {
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);



			myTaxonomy = getResources().getStringArray(R.array.taxonomies);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerList = (ListView) findViewById(R.id.left_drawer);
			mDrawerList.setAdapter(new ArrayAdapter<String>(getActivity(),
					R.layout.drawer_list_item, myTaxonomy));
			mDrawerList.setOnItemClickListener(new LeftDrawerItemClickListener());

			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);


			mDrawerToggle = new ActionBarDrawerToggle(
					getActivity(),                  
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

			View rootView = inflater.inflate(R.layout.question_list, container, false);
			return rootView;
		}
		private class LeftDrawerItemClickListener implements ListView.OnItemClickListener {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItem(position);
			}
		}


		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
			mDrawerToggle.onConfigurationChanged(newConfig);
		}


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

			rightDrawerList.setOnItemClickListener(new RightDrawerItemClickListener());

			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);


			

			View rootView = inflater.inflate(R.layout.question_list, container, false);
			return rootView;
		}
		private class RightDrawerItemClickListener implements ListView.OnItemClickListener {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Does Nothing
			}
		}

		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
			rightDrawerToggle.onConfigurationChanged(newConfig);
		}

	}

}