package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.QAModel;

public class DataManager {
	/**
	 * The UserContext for this DataManager, that is, what user to 
	 */
	private UserContext mContext;
	
	/**
	 * Our local data storage location
	 */
	private IDataSourceManager mLocalDataStore;
	
	/**
	 * Our network data storage location
	 */
	private IDataSourceManager mNetworkDataStore = new NetworkDataManager("", "");

	/**
	 * Construct a new DataManager, using a given android Context to communicate 
	 * @param ctx
	 */
	public DataManager(Context ctx) {
		mLocalDataStore = new LocalDataManager(ctx);
	}
	
	/**
	 * Query the DataManager for items, using a given filter and sort.
	 * @param filter          A filter, specifying which items to get
	 * @param sortComparator  A sort, specifying how to order the items in the IncrementalResult
	 * @return An IncrementalResult to which the items will be added as they arrive, or null if the
	 */
	public IncrementalResult doQuery(DataFilter filter, IItemComparator sortComparator) {
		
	}

	/**
	 * Save an item to one or more destinations.
	 * @param item
	 */
	public void saveItem(QAModel item) {
		
	}

	public void setUserContext(UserContext context) {
		mContext = context;
	}

	public UserContext getUserContext() {
		return mContext;
	}
}
