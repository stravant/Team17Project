package com.ualberta.team17.view;

import android.app.Activity;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.ualberta.team17.R;


/**
 * This class enumerates each of the taxonomies and passes an intent to the
 * QuestionListActivity
 * 
 * @author Divyank
 *
 */


public class QuestionTaxonomyActivity extends Activity {
	public static enum taxonomies {
		AllQuestions(0), 
		MyActivity(1),
		Favorites(2) ,
		MostUpvotedQs(3), 
		MostUpvotedAs(4);
		
		int value;
		private taxonomies(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	}
	ArrayList<String> options = new ArrayList<String>();
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		options.add("All Questions");
		options.add("My Activity");
		options.add("Favorites");
		options.add("Most Upvoted Questions");
		options.add("Most Upvoted Answers");
		setContentView(R.layout.activity_taxonomy);
		//Intent userIntent = this.getIntent();
		ListView optionList = (ListView) findViewById(R.id.taxonomyView);
		optionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int i, long l) {
				QuestionTaxonomyActivity.this.handleListViewItemClick(av, view, i, l);
			}
		});
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
		optionList.setAdapter(adapter);
		
	}
	protected void handleListViewItemClick(AdapterView<?> av, View view, int i,
			long l) {
		Intent intent = new Intent(QuestionTaxonomyActivity.this, QuestionListActivity.class);
		switch (i) {
			case 0:
				intent.putExtra(QuestionListActivity.FILTER_EXTRA, taxonomies.AllQuestions);
				break;
			case 1:
				intent.putExtra(QuestionListActivity.FILTER_EXTRA, taxonomies.MyActivity);
				break;
			case 2:
				intent.putExtra(QuestionListActivity.FILTER_EXTRA, taxonomies.Favorites);
				break;
			case 3:
				intent.putExtra(QuestionListActivity.FILTER_EXTRA, taxonomies.MostUpvotedQs);
				break;	
			case 4:
				intent.putExtra(QuestionListActivity.FILTER_EXTRA, taxonomies.MostUpvotedAs);
				break;
		}
		
		startActivity(intent);				
	}
}
