package com.ualberta.team17.test.utility;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.R;
import com.ualberta.team17.view.QuestionTaxonomyActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Adapter;
import android.widget.ListView;
import junit.framework.TestCase;

public class QuestionTaxonomyActivityTest extends ActivityInstrumentationTestCase2<QuestionTaxonomyActivity> {
	QuestionTaxonomyActivity myActivity;
	ListView myListView;
	Adapter myAdapter;

	public QuestionTaxonomyActivityTest(
			Class<QuestionTaxonomyActivity> activityClass) {
		super(QuestionTaxonomyActivity.class);
	}
	public void setUp() throws Exception {
		super.setUp();
		myActivity = getActivity();
		myListView = (ListView) myActivity.findViewById(com.ualberta.team17.R.id.taxonomyView);
		myAdapter = myListView.getAdapter();
	}
	/**
	 * Ensure QuestionListActivity is receiving my passed intents for loading questions.
	 * 
	 * @author Divyank
	 * @throws Throwable 
	 */
	public void testIntentReceived() throws Throwable {
		ArrayList<String> options = (ArrayList<String>) myAdapter.getItem(0);
		if (options != null) {

			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					myListView.performItemClick(myListView, 0, myListView.getItemIdAtPosition(0));
				}
			});

			try {
				Thread.sleep(1000);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			ActivityManager activityManager = (ActivityManager) this.getActivity().getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

			ComponentName componentInfo = tasks.get(0).topActivity;

			String actString = "com.ualberta.team17.view.QuestionListActivity";
			String activeActivity = componentInfo.getClassName();
			assertTrue(actString.equals(activeActivity));
		}
	}
}

