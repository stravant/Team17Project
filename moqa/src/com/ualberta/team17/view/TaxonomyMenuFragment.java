package com.ualberta.team17.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
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

public class TaxonomyMenuFragment extends Fragment {
	private String[] myTaxonomy;
	private DrawerLayout mDrawerLayout;
	ListView mDrawerList;
	ActionBarDrawerToggle mDrawerToggle;
	OnItemSelectedListener mListener;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnItemSelectedListener");
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
		
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Activity activity = getActivity();
		if(activity == null)
			return null;
		myTaxonomy = getResources().getStringArray(R.array.taxonomies);
		mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) activity.findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerList.setAdapter(new ArrayAdapter<String>(activity,
				R.layout.drawer_list_item, myTaxonomy));
		mDrawerList.setOnItemClickListener(new LeftDrawerItemClickListener());

		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
		activity.getActionBar().setHomeButtonEnabled(true);
		
		mDrawerToggle = new ActionBarDrawerToggle(
				activity,                  
				mDrawerLayout,
				R.drawable.ic_drawer,  
				R.string.drawer_open,  
				R.string.drawer_close  
				) {
			public void onDrawerClosed(View view) {
				activity.getActionBar().setTitle(activity.getTitle());
				activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				activity.getActionBar().setTitle(activity.getTitle());
				activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};	
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		if (savedInstanceState == null) {
			mListener.onItemSelected(0);
			mDrawerList.setItemChecked(0, true);
			getActivity().setTitle(myTaxonomy[0]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}

		View rootView = inflater.inflate(R.layout.question_list, container, false);
		return rootView;
	}
	private class LeftDrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mListener.onItemSelected(position);
			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}
	public interface OnItemSelectedListener {
		public void onItemSelected(int position);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		return super.onOptionsItemSelected(item);
	}	
}