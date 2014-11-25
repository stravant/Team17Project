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
	List<QAModel> mItems;
	
	public DummyDataManager(UserContext userContext, Context context) {
		super(context);
		this.setUserContext(userContext);
	}

	public void setItems(List<QAModel> items) {
		mItems = items;
	}
	
	public IncrementalResult doQuery(DataFilter filter, IItemComparator sortComparator) {
		IncrementalResult incrementalResult = new IncrementalResult(new UpvoteComparator());
		List<QAModel> returnItems = new ArrayList<QAModel>();

		for (QAModel item: mItems) {
			if (filter.accept(item)) {
				returnItems.add(item);
			}
		}

		incrementalResult.addObjects(returnItems);

		return incrementalResult;
	}
	
	/**
	 * Test stuff
	 */
	public int getItemCount(){
		return mItems.size();
	}
}
