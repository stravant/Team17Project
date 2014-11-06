package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.StoragePolicy;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;

public class DataManager {
	/**
	 * The Context for this DataManager
	 */
	private Context mContext;
	
	/**
	 * The UserContext for this DataManager, that is, what user is currently using the app
	 */
	private UserContext mUserContext;
	
	/**
	 * Our local data storage location
	 */
	private IDataSourceManager mLocalDataStore;
	
	/**
	 * Our network data storage location
	 */
	private IDataSourceManager mNetworkDataStore;
	
	/**
	 * Construct a new DataManager, using a given android Context to communicate 
	 * @param ctx
	 */
	public DataManager(Context ctx) {
		this(ctx, null, new NetworkDataManager("", ""));
	}
	
	/**
	 * Construct a new DataManager, using a given Network and Local data manager
	 * @param ctx The context to use
	 * @param local The local data manager to use
	 * @param net The network data manager to use
	 */
	public DataManager(Context ctx, LocalDataManager local, NetworkDataManager net) {
		mContext = ctx;
		
		// Set the members
		mNetworkDataStore = net;
		mLocalDataStore = local;
		
		// When an item arrives from the network, cache it locally
		mNetworkDataStore.addDataLoadedListener(new IDataLoadedListener() {
			@Override
			public void dataItemLoaded(IDataSourceManager manager, QAModel item) {
				if (mUserContext != null) {
					mLocalDataStore.saveItem(item);
				}
			}
		});
	}
	
	/**
	 * Query the DataManager for items, using a given filter and sort.
	 * @param filter          A filter, specifying which items to get
	 * @param sortComparator  A sort, specifying how to order the items in the IncrementalResult
	 * @return An IncrementalResult to which the items will be added as they arrive, or null if the
	 */
	public IncrementalResult doQuery(DataFilter filter, IItemComparator sortComparator) {
		IncrementalResult result = new IncrementalResult(sortComparator);
		mLocalDataStore.query(filter, sortComparator, result);
		mNetworkDataStore.query(filter, sortComparator, result);
		return result;
	}

	/**
	 * Save an item to one or more destinations.
	 * Rules for storing items:
	 *  1) For now, just save all items to the network and local
	 * @param item
	 */
	public void saveItem(QAModel item) {
		mNetworkDataStore.saveItem(item);
		mLocalDataStore.saveItem(item);
	}

	/**
	 * Set the user context to a given context
	 * @param userContext
	 */
	public void setUserContext(UserContext userContext) {
		UserContext oldCtx = userContext;
		mUserContext = userContext;
		
		// If we got a new context, create a new local data store for that context
		if (userContext != oldCtx) {
			if (mLocalDataStore != null) {
				((LocalDataManager)mLocalDataStore).close();
			}
			if (userContext != null) {
				mLocalDataStore = new LocalDataManager(mContext, userContext);
			}
		}
	}

	public UserContext getUserContext() {
		return mUserContext;
	}

	public static Gson getGsonObject() {
		// TODO: This is missing attachment items
		return new GsonBuilder()
			.registerTypeAdapter(AnswerItem.class, new AnswerItem.GsonTypeAdapter())
			.registerTypeAdapter(CommentItem.class, new CommentItem.GsonTypeAdapter())
			.registerTypeAdapter(QuestionItem.class, new QuestionItem.GsonTypeAdapter())
			.registerTypeAdapter(UpvoteItem.class, new UpvoteItem.GsonTypeAdapter())
			.serializeNulls()
			.create();
	}
}
