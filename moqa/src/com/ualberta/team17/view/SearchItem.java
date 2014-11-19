package com.ualberta.team17.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ualberta.team17.R;

public class SearchItem extends LinearLayout {
    
    boolean mShowSearchBar;
	
    public SearchItem(Context context) {
    	super(context);
    	
    	init();
    }
    
	public SearchItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.SearchItem,
		        0, 0);
		
		try {

		} finally {
		       a.recycle();
		}
		
		init();
	}
	
	private void init() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.searchitem, this, true); /* DO I ADD IT TO THIS VIEW */

		ImageButton b = (ImageButton)this.findViewById(R.id.searchButton);
		if (b != null) {
			b.setImageResource(android.R.drawable.ic_menu_search);
		}		
	}
	
	public String getSearchTerm() {
		EditText textBar = (EditText) this.findViewById(R.id.searchBar);
		
		if (textBar != null) {
			return textBar.getText().toString();
		}
		return null;
	}
	
	public void setSearchTerm(String term) {
		EditText textBar = (EditText) this.findViewById(R.id.searchBar);
		
		if (textBar != null) {
			textBar.setText(term);
		}
		
		invalidate();
		requestLayout();
	}
	
	public void setOnClickListener(OnClickListener l) {
		ImageButton b = (ImageButton)this.findViewById(R.id.searchButton);
		if (b != null) {
			b.setOnClickListener(l);
		}
	}
	
	
	public boolean isShowingSearchBar() {
		return mShowSearchBar;
	}
	
	public void setShowingSearchBar(boolean val) {
		mShowSearchBar = val;
		invalidate();
		requestLayout();
	}	
}
