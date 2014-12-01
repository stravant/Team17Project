package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;
import com.ualberta.team17.datamanager.comparators.IdentityComparator;

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
	private LocalDataManager mLocalDataStore;
	
	/**
	 * Our network data storage location
	 */
	private IDataSourceManager mNetworkDataStore;
	
	/**
	 * Where to store UserContext data
	 */
	private final static String USER_CONTEXT_STORAGE = "UserContext";
	
	/**
	 * Construct a new DataManager, using a given android Context to communicate 
	 * @param ctx
	 */
	public DataManager(Context ctx) {
		this(ctx, 
				new LocalDataManager(ctx), 
				new NetworkDataManager(ctx.getResources().getString(com.ualberta.team17.R.string.esProductionServer),
				                       ctx.getResources().getString(com.ualberta.team17.R.string.esProductionIndex)));
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
					mLocalDataStore.saveItem(item, mUserContext);
				}
			}
		});
	}
	
	/**
	 * Query the DataManager for items, using a given filter and sort.
	 * @param filter          A filter, specifying which items to get
	 * @param sortComparator  A sort, specifying how to order the items in the IncrementalResult
	 * @return An IncrementalResult to which the items will be added as they arrive
	 */
	public IncrementalResult doQuery(DataFilter filter, IItemComparator sortComparator) {
		return doQuery(filter, new IncrementalResult(sortComparator));
	}

	/**
	 * Query the DataManager for items, using a given filter and previous Incremental Result.
	 * 
	 * Note that the IItemComparator that the IncrementalResult was defined with will be used
	 * to query the data source.
	 * @param filter
	 * @param result
	 * @return The incremental result that was passed in.
	 */
	public IncrementalResult doQuery(DataFilter filter, IncrementalResult result) {
		IItemComparator sortComparator = result.getComparator();
		// Chain to the networkDataStore, that is, after the local query completes, the
		// network one will be done.
		mLocalDataStore.query(filter, sortComparator, result, mNetworkDataStore);
		return result;
	}

	/**
	 * Query the DataManager for items with the given ids, returning them in the order defined by the given sort.
	 * @param idList          The list of ids to query for
	 * @param sortComparator  A sort, specifying how to order the items in the IncrementalResult
	 * @return An IncrementalResult to which the items will be added as they arrive
	 */
	public IncrementalResult doQuery(List<UniqueId> idList, IItemComparator sortComparator) {
		IncrementalResult result = new IncrementalResult(sortComparator);
		mLocalDataStore.query(idList, result, mNetworkDataStore);
		return result;
	}

	/**
	 * Save an item to one or more destinations.
	 * Rules for storing items:
	 *  1) For now, just save all items to the network and local
	 * @param item
	 */
	public void saveItem(final QAModel item) {
		// Do the save to the local and network managers
		((NetworkDataManager)mNetworkDataStore).saveItem(item, mUserContext, new IDataItemSavedListener() {
			@Override
			public void dataItemSaved(boolean success, Exception e) {
				if (!success) {
					mUserContext.addLocalOnlyItem(item.getUniqueId());
				}
			}
		});
		mLocalDataStore.saveItem(item, mUserContext);
	}
	
	/**
	 * Favorite an item
	 * @param item The item to favorite
	 */
	public void favoriteItem(QAModel item) {
		// Add it to the favorites list
		mUserContext.addFavorite(item.getUniqueId());
		
		// Update the favorited flag right away while we're at it if it's not already flagged
		if (item instanceof QuestionItem && !((QuestionItem)item).isFavorited()) {
			((QuestionItem)item).setFavorited();
		}
		
		// We might may need to update the saved-ness of this item locally
		mLocalDataStore.saveItem(item, mUserContext);
		
		// TODO: Maybe make call async
		saveUserContextData(mUserContext);
	}
	
	/**
	 * Mark an item as to be viewed later.
	 * @param item The uniqueId of the item to mark as view later
	 */
	public void markViewLater(QAModel item) {
		mUserContext.addViewLater(item.getUniqueId());
		
		// Update the view later flag right away while we're at it
		if (item instanceof QuestionItem) {
			((QuestionItem)item).setViewLater();
		}
		
		// We may need to update the saved-ness of this item locally
		((LocalDataManager)mLocalDataStore).saveItemIfCached(item.getUniqueId(), mUserContext);
		
		saveUserContextData(mUserContext);
	}
	
	/**
	 * Mark an item as recently viewed at this time
	 * @param item The uniqueId of the item to mark as recently viewed
	 */
	public void markRecentlyViewed(QAModel item) {
		// Mark as viewed in the user context
		mUserContext.addRecentItem(item.getUniqueId());
		
		// The item should no longer be view later
		mUserContext.removeViewLater(item.getUniqueId());
		
		// Update the view later flag to false
		if (item instanceof QuestionItem) {
			((QuestionItem)item).clearViewLater();
		}
		
		// We may need to update the saved-ness of this item locally
		((LocalDataManager)mLocalDataStore).saveItemIfCached(item.getUniqueId(), mUserContext);
		
		saveUserContextData(mUserContext);
	}
	
	/**
	 * Save user context data out to a user context
	 * @param context
	 */
	private void saveUserContextData(UserContext context) {
		DataManager.writeLocalData(mContext, USER_CONTEXT_STORAGE, context.saveToJson().toString());
	}
	
	/**
	 * Try to save out any local only data that we currently have
	 * to the network data source if it is available.
	 */
	private void saveLocalOnlyData() {
		// Create a new IncrementalResult that will save items to the network as they arrive
		IncrementalResult toSave = new IncrementalResult(new IdentityComparator());
		toSave.addObserver(new IIncrementalObserver() {	
			@Override
			public void itemsArrived(List<QAModel> items, int index) {
				for (QAModel item: items) {
					// Save the item that we got
					final QAModel itemToSave = item;
					((NetworkDataManager)mNetworkDataStore).saveItem(itemToSave, mUserContext, new IDataItemSavedListener() {
						@Override
						public void dataItemSaved(boolean success, Exception e) {
							// If the save succeeded, remove the item from the local only list
							if (success) {
								mUserContext.removeLocalOnlyItem(itemToSave.getUniqueId());
							}
						}
					});
				}
			}
		});
		
		// Fire off the query for the local only items to save
		mLocalDataStore.query(mUserContext.getLocalOnlyItems(), toSave, null);
	}
	
	/**
	 * Reset user context data
	 */
	public void resetUserContextData() {
		saveUserContextData(new UserContext("<ignored>"));
	}
	
	/**
	 * Load user context data into a user context
	 * @param context
	 */
	private void loadUserContextData(UserContext context) {
		JsonParser parser = new JsonParser();
		String data = DataManager.readLocalData(mContext, USER_CONTEXT_STORAGE);
		if (data != null) {
			JsonElement tree = parser.parse(data);
			if (tree != null) {
				context.loadFromJson(tree);
			}
		}
	}

	/**
	 * Set the user context to a given context
	 * @param userContext
	 */
	public void setUserContext(UserContext userContext) {
		mUserContext = userContext;
		
		// Load in the user context data
		loadUserContextData(mUserContext);
		
		// Set the local data store usercontext
		((LocalDataManager)mLocalDataStore).setUserContext(userContext);
		
		// Try to save out any local only items
		saveLocalOnlyData();
	}

	public UserContext getUserContext() {
		if (mUserContext == null) {
			throw new RuntimeException("Attempt to getUserContext before user context has been set.");
		}
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
	
	//////////////////////////////////////////////////////////////////////////////

	public Bitmap readImageFromUri(Uri uri) {
		try {
			return MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);	
		} catch (IOException e) {
			return null;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Get the file a handle to the source of settings data
	 * @param ctx The context to get the source from
	 * @param fileName The name of the data file to read
	 * @return A String, that is the contents of the file, or null, if 
	 *         there was no data to read, or the read failed.
	 */
	public static String readLocalData(Context ctx, String fileName) {
		try {
			FileInputStream inStream = ctx.openFileInput("AppData_" + fileName);
			byte[] tmpBuffer = new byte[1024];
			int readCount;
			StringBuffer fileContent = new StringBuffer("");
			while ((readCount = inStream.read(tmpBuffer)) != -1) { 
				fileContent.append(new String(tmpBuffer, 0, readCount)); 
			}
			inStream.close();
			Log.i("app", "DataManager :: Read local file `AppData_" + fileName + "`");
			return fileContent.toString();
		} catch (FileNotFoundException e) {
			Log.e("app", "Data Source file not found!:" + e.getMessage());
			// File was not found, we return null, letting the caller
			// create a file if they want to.
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Save changes to a given data file, given a context
	 * @param ctx The context to write under
	 * @param fileName The name of the data file to write to
	 * @param data 
	 */
	public static void writeLocalData(Context ctx, String fileName, String data) {
		try {
			FileOutputStream outStream = ctx.openFileOutput("AppData_" + fileName, Context.MODE_PRIVATE);
			OutputStreamWriter outWrite = new OutputStreamWriter(outStream);
			outWrite.write(data);
			outWrite.flush();
			outStream.close();
			Log.i("app", "DataManager :: Wrote local file `AppData_" + fileName + "`");
		} catch (FileNotFoundException e) {
			throw new Error("Fatal Error: Can't write to application directory");
		} catch (IOException e) {
			
		}
	}
}
