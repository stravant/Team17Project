package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import android.content.Context;

import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.StoragePolicy;
import com.ualberta.team17.UniqueId;

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
	private IDataSourceManager mNetworkDataStore = new NetworkDataManager("", "");
	
	/**
	 * Construct a new DataManager, using a given android Context to communicate 
	 * @param ctx
	 */
	public DataManager(Context ctx) {
		mContext = ctx;
		
		// When an item arrives from the network, save it if we need to, that is, if it is a
		// descendant of one of our items.
		// TODO: Only handles direct descendants right now, not sub ones
		mNetworkDataStore.addDataLoadedListener(new IDataLoadedListener() {
			@Override
			public void dataItemLoaded(IDataSourceManager manager, QAModel item) {
				if (mUserContext != null) {
					for (UniqueId id: mUserContext.getReplies()) {
						if (id.equals(item.getUniqueId())) {
							asyncSaveCachedItem(item);
							break;
						}
					}
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
		if (mLocalDataStore.isAvailable()) {
			mLocalDataStore.query(filter, sortComparator, result);
		}
		if (mNetworkDataStore.isAvailable()) {
			mNetworkDataStore.query(filter, sortComparator, result);
		}
		return result;
	}

	/**
	 * Save an item to one or more destinations.
	 * Rules for storing items:
	 *  1) If it is an AuthoredItem, then look at the StoragePolicy
	 *  2) Otherwise, just save it network
	 * @param item
	 */
	public void saveItem(QAModel item) {
		// Is it authored (does it have a StoragePolicy)
		if (item instanceof AuthoredItem) {
			AuthoredItem authItem = (AuthoredItem)item;
			if (authItem.getStoragePolicy() == StoragePolicy.Transient) {
				// Save only on the network
				asyncSaveTransientItem(item);
				
			} else if (authItem.getStoragePolicy() == StoragePolicy.Cached) {
				// Save the item local and on the network
				asyncSaveTransientItem(item);
				asyncSaveCachedItem(item);
			}
		} else {
			// Otherwise, only save network
			asyncSaveTransientItem(item);
		}
	}
	
	/**
	 * Implementation for saving an item that is on the network
	 * @param item
	 */
	private void asyncSaveTransientItem(final QAModel item) {
		if (mNetworkDataStore.isAvailable())
			new RunTaskHelper() {
				@Override
				public void task() {
					mNetworkDataStore.saveItem(item);
				}
			};
	}
	
	/**
	 * Implementation for saving an item that is on the local system
	 * @param item
	 */
	private void asyncSaveCachedItem(final QAModel item) {
		if (mLocalDataStore.isAvailable())
			new RunTaskHelper() {
				@Override
				public void task() {
					mLocalDataStore.saveItem(item);
				}
			};
	}

	public void setUserContext(UserContext userContext) {
		UserContext oldCtx = userContext;
		mUserContext = userContext;
		
		// If we got a new context, create a new local data store for that context
		if (userContext != null && userContext != oldCtx)
			mLocalDataStore = new LocalDataManager(mContext, userContext);
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
