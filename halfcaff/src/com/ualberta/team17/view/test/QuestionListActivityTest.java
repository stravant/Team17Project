package com.ualberta.team17.view.test;
import java.util.List;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ListView;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.R;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.view.QuestionListActivity;


public class QuestionListActivityTest extends
		ActivityInstrumentationTestCase2<QuestionListActivity> {
	
	QuestionListActivity mActivity;
	ListView mListView;
	Adapter mAdapter;
	
	public QuestionListActivityTest() {
	    super(QuestionListActivity.class);
	  }
	
	@Override
	public void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);
		
		mActivity = getActivity();
		
		mListView = (ListView) mActivity.findViewById(com.ualberta.team17.R.id.questionListView);
		
		mAdapter = mListView.getAdapter();
		
		QAController.getInstance().login(new UserContext("jareds_test"));
	}
	
	/**
	 * Ensure QuestionViewActivity is receiving my passed intents for loading questions.
	 * 
	 * @author Jared
	 * @throws Throwable 
	 */
	public void test_QLA1_SendQuestionIntent() throws Throwable {        
		QAModel item = (QAModel) mAdapter.getItem(0);
		if (item != null) {
			
			runTestOnUiThread(new Runnable() {
			    @Override
			    public void run() {
			       mListView.performItemClick(mListView, 0, mListView.getItemIdAtPosition(0));
			    }
			});
			
			try {
			    Thread.sleep(1000);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			ActivityManager activityManager = (ActivityManager) this.getActivity().getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
	        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
	        
	        ComponentName componentInfo = tasks.get(0).topActivity;
	        
	        String actString = "com.ualberta.team17.view.QuestionViewActivity";
	        String activeActivity = componentInfo.getClassName();
	        assertTrue(actString.equals(activeActivity));
		}
	}
	
	/**
	 * Ensure QuestionViewActivity is receiving my request to create a new question.
	 * 
	 * @author Jared
	 * @throws Throwable 
	 */
	public void test_QLA2_CreateQuestion() {

		MenuItem mi = (MenuItem)mActivity.findViewById(com.ualberta.team17.R.id.action_new_question);
    	mActivity.onOptionsItemSelected(mi);
		/*try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}*/
		
		ActivityManager activityManager = (ActivityManager) this.getActivity().getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        
        ComponentName componentInfo = tasks.get(0).topActivity;
        
        String actString = "com.ualberta.team17.view.QuestionViewActivity";
        String activeActivity = componentInfo.getClassName();
        assertTrue(actString.equals(activeActivity));
	}

}
