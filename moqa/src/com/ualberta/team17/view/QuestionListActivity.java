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
public class QuestionListActivity extends Activity implements OnItemSelectedListener{

	
	private String[] sortOptions;
	private DrawerLayout rightDrawerLayout;
	private ListView rightDrawerList;
	private ActionBarDrawerToggle rightDrawerToggle;
	TaxonomyMenuFragment leftDrawer = new TaxonomyMenuFragment();
	SortMenuFragment rightDrawer = new SortMenuFragment();
	ListFragment fragment = new ListFragment();


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
		Bundle args = new Bundle();
		args.putInt(ListFragment.TAXONOMY_NUM, position);
		fragment = new ListFragment();
		FragmentManager fragmentManager = getFragmentManager();
		fragment.setArguments(args);
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		setTitle(myTaxonomy[position]);
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
		
		if (leftDrawer.mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (id == R.id.action_new_question) {
			fragment.createNewQuestion();
			return true;
		}
		if (id == R.id.action_sort_date) {
			fragment.applyDateSort(item);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
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


	@Override
	public void onItemSelected(int position) {
		selectItem(position);
	}

}