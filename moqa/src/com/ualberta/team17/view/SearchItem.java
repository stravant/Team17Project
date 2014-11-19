package com.ualberta.team17.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ualberta.team17.R;

/**
 * Used to show a search bar on the action bar.
 * 
 * @author Jared
 *
 */
public class SearchItem extends LinearLayout {
    
    boolean mShowSearchBar;
	
    public SearchItem(Context context) {
    	super(context);
    	
    	init();
    }
    
	public SearchItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}
	
	private void init() {
<<<<<<< HEAD
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

=======
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
>>>>>>> Added a decent looking menu bar. Added groups to the menu.
		@SuppressWarnings("unused")
		View view = inflater.inflate(R.layout.searchitem, this, true); 

		ImageButton b = (ImageButton)this.findViewById(R.id.searchButton);
		if (b != null) {
			b.setImageResource(android.R.drawable.ic_menu_search);
<<<<<<< HEAD
			b.setOnClickListener(new searchClickedListener());
=======
			
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {

					ViewGroup g = (ViewGroup) view.getParent();
					if (g != null) {
						
						EditText et = (EditText) g.findViewById(R.id.searchBar);
						if (et != null) {
							
							if (et.isShown()) {
								// Do search things then hide the bar.
								et.setVisibility(GONE);
							}
							else {
								// Show the bar and activate it
								et.setVisibility(VISIBLE);
								et.setSelected(true);
							}							
						}
					}					
				}
				
			});
>>>>>>> Added a decent looking menu bar. Added groups to the menu.
		}		
		
		EditText et = (EditText)this.findViewById(R.id.searchBar);
		if (et != null) {
			et.setVisibility(GONE);
		}
	}
	
	/**
	 * Gets the value in the search bar.
	 * 
	 * @return the value inside the search bar.
	 */
	public String getSearchTerm() {
		EditText textBar = (EditText) this.findViewById(R.id.searchBar);
		
		if (textBar != null) {
			return textBar.getText().toString();
		}
		return null;
	}
	
	/**
	 * Sets the value inside the search bar.
	 * 
	 * @param term the value to be displayed in the search bar.
	 */
	public void setSearchTerm(String term) {
		EditText textBar = (EditText) this.findViewById(R.id.searchBar);
		
		if (textBar != null) {
			textBar.setText(term);
		}
		
		invalidate();
		requestLayout();
	}
	
	/**
	 * Allows an activity to attach a specific click listener to be done 
	 * when the search button is clicked.
	 * 
	 * @param l the listeber to execute upon the search buttons click.
	 */
	public void setOnClickListener(OnClickListener l) {
		ImageButton b = (ImageButton)this.findViewById(R.id.searchButton);
		if (b != null) {
			b.setOnClickListener(l);
		}
	}
	
	/**
	 * Returns whether the search bar is being shown.
	 * 
	 * @return the search bars display state. False for hidden. True for shown.
	 */
	public boolean isShowingSearchBar() {
		return mShowSearchBar;
	}
	
	/**
	 * Sets whether the searchbar is shown.
	 * 
	 * @param val Show or hide the search bar.
	 */
	public void setShowingSearchBar(boolean val) {
		mShowSearchBar = val;
		invalidate();
		requestLayout();
	}	
	
	/**
	 * Triggered whenever the search button is clicked.
	 * 
	 * @author Jared
	 *
	 */
	private class searchClickedListener implements OnClickListener {
		
		@Override
		public void onClick(View view) {

			ViewGroup g = (ViewGroup) view.getParent();
			if (g != null) {
				
				EditText et = (EditText) g.findViewById(R.id.searchBar);
				if (et != null) {
					
					if (et.isShown()) {
						// Do search things then hide the bar.
						et.setVisibility(GONE);
					}
					else {
						// Show the bar and activate it
						et.setVisibility(VISIBLE);
						et.setSelected(true);
					}							
				}
			}					
		}
	}
}
