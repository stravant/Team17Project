package com.ualberta.team17.controller.test;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.comparators.UpvoteComparator;

/**
 * This class overrides DataManager to provide a limited
 * data manager that isn't dependent on external data
 * to provide reliable results to test.
 * @author Corey
 *
 */
public class DummyDataManager extends DataManager {
	private UserContext mUserContext;
	List<QAModel> items;
	
	public DummyDataManager() {
		super(null);
		mUserContext = null;
		items = new ArrayList<QAModel>();
		
		UniqueId questionId = new UniqueId();
		
		items.add( new QuestionItem(questionId, null, null, null, null, 0, null) );
		items.add( new AnswerItem(null, questionId, null, null, null, 0) );
		items.add( new CommentItem(null, questionId, null, null, null, 0) );
	}
	
	public IncrementalResult doQuery(DataFilter filter, IItemComparator sortComparator) {
		IncrementalResult incrementalResult = new IncrementalResult(new UpvoteComparator());
		incrementalResult.addObjects(items);
		
		return incrementalResult;
	}
	
	public void setUserContext(UserContext ctx) {
		mUserContext = ctx;
	}
	
	public UserContext getUserContext() {
		return mUserContext;
	}
	
	/**
	 * Test stuff
	 */
	public int getItemCount(){
		return items.size();
	}
}
