package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.JsonWriter;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;
import com.ualberta.team17.datamanager.DataFilter.FieldFilter;

public class LocalDataManager implements IDataSourceManager {
	/**
	 * The File Context to use
	 */
	private Context mContext;
	
	/**
	 * Our set of in-memory data
	 */
	private volatile List<QAModel> mData;
	
	/**
	 * Additional pointers to our data by ID
	 */
	private HashMap<UniqueId, WeakReference<QAModel>> mDataById = new HashMap<UniqueId, WeakReference<QAModel>>();
	
	/**
	 * Do we need to do a save on the data?
	 */
	private boolean mDataDirty = false;
	
	/**
	 * Has the data load started yet
	 */
	private boolean mDataLoadStarted = false;
	
	/**
	 * A lock on our data
	 */
	private ReentrantLock mDataLock = new ReentrantLock();
	
	/**
	 * Become ready condition
	 */
	private Condition mDataBecomeReady = mDataLock.newCondition();
	
	/**
	 * Save task completed condition
	 */
	private Condition mSaveCompleted = mDataLock.newCondition();
	
	/**
	 * When were we last available?
	 */
	private Date mLastAvailable;
	
	/**
	 * Our availability listeners
	 */
	private List<IDataSourceAvailableListener> mAvailableListeners = new ArrayList<IDataSourceAvailableListener>();
	
	/**
	 * Lock on availability listeners
	 */
	private ReentrantLock mAvailableListenersLock = new ReentrantLock();
	
	/**
	 * Our DataLoaded listeners
	 */
	private List<IDataLoadedListener> mDataLoadedListeners = new ArrayList<IDataLoadedListener>();
	
	/**
	 * Data Loaded listeners lock
	 */
	private ReentrantLock mDataLoadedListenersLock = new ReentrantLock();
	
	/**
	 * How often to do a batched save if one is needed
	 */
	private static final long SAVE_INTERVAL = 2000; // 2 seconds
	
	/**
	 * A worker pool to handle delayed+batched saving
	 */
	private static final ScheduledExecutorService mSaveWorkerPool = Executors.newSingleThreadScheduledExecutor();
	
	/**
	 * The currently scheduled save task
	 */
	private ScheduledFuture<?> mCurrentSaveWorker;
	
	/**
	 * Next time that the worker pool
	 */
	private long mLastSaveTimeMillis;
	
	/**
	 * The local data tag that the LocalDataManager uses
	 */
	private static final String LDM_CATEGORY = "LocalDataManager";
	
	/**
	 * Construct a LocalDataManager, doing FileIO for a given 
	 * user, using a given App context.
	 * @param context The app context to use.
	 * @param user    The user to do the IO for
	 */
	public LocalDataManager(Context context) {
		// Set the params
		mContext = context;
		
		// Availability
		// Currently: Since Linux epoch to start
		mLastAvailable = new Date(0);
		
		// Saving
		mLastSaveTimeMillis = Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * Force a  load of the local data
	 */
	public void asyncLoadData() {
		if (mData == null) {
			new RunTaskHelper<Void>() {
				@Override
				public Void task() {
					mDataLock.lock();
					maybeDoInitialDataQuery();
					mDataLock.unlock();
					return null;
				}
			};
		}
	}
	
	/**
	 * Close down the local data manager for the current context / usercontext
	 * Closes any file handles or tasks currently open.
	 */
	public void close() {
		mDataLock.lock();
		
		if (mCurrentSaveWorker != null) {
			mCurrentSaveWorker.cancel(false);
		}
		
		mDataLock.unlock();
	}
	
	/**
	 * Private function that handles reading items from a file
	 * @param in
	 * @param buffer Buffer to put the read items into
	 */
	private void readItemData(String input, List<QAModel> buffer) {
		// Get the JSON element and parse
		JsonParser parser = new JsonParser();
		JsonElement elem = parser.parse(input);
		
		// Read the objects into our in-memory format
		Gson gson = DataManager.getGsonObject();
		for (JsonElement item: elem.getAsJsonArray()) {
			// Break down the item
			JsonArray arr = item.getAsJsonArray();
			ItemType type = ItemType.fromString(arr.get(0).getAsString());
			JsonElement obj = arr.get(1);
			
			// Parse based on type
			QAModel newObject;
			switch (type) {
			case Question:
				newObject = gson.fromJson(obj, QuestionItem.class);
				break;

			case Answer:
				newObject = gson.fromJson(obj, AnswerItem.class);
				break;

			case Comment:
				newObject = gson.fromJson(obj,  CommentItem.class);
				break;

			case Upvote:
				newObject = gson.fromJson(obj, UpvoteItem.class);
				break;

			default:
				newObject = null;
				Log.e("app", "Unknown object type encountered!!~~");
			}
			
			// Add to the buffer
			if (newObject != null) {
				buffer.add(newObject);
			}
		}
		
		Log.i("app", "LocalDataManager :: loaded in " + buffer.size() + " items.");
	}
	
	/**
	 * Private function that handles writing items to a file
	 * @param out
	 * @param buffer The buffer to write out items from
	 */
	private String writeItemData(List<QAModel> buffer) {
		// Encode the JSOn for the buffer
		Log.i("app", "LocalDataManager :: Writing out " + buffer.size() + " objects.");
		Gson gson = DataManager.getGsonObject();
		
		// Encode
		JsonArray array = new JsonArray();
		for (QAModel item: buffer) {
			JsonArray itemAndType = new JsonArray();
			itemAndType.add(new JsonPrimitive(item.getItemType().name()));
			itemAndType.add(gson.toJsonTree(item));
			array.add(itemAndType);
		}
		
		// Return the encoded data
		return gson.toJson(array);
	}
	
	/**
	 * Get an item from the hashmap
	 */
	public QAModel getItemById(UniqueId id) {
		Reference<QAModel> ref = mDataById.get(id);
		if (ref != null) {
			return ref.get();
		} else {
			return null;
		}
	}
	
	/**
	 * Used at startup by the Initial Data Query. Calculates the current 
	 * derived info for all of the {@link QAModel} items that were loaded. 
	 * Examples of derived info: Upvote Count & Reply Count
	 */
	private void calculateInitialDerivedInfo() {
		// Initially all of the items will have UpvoteCount and ReplyCount = 0
		// when they are first constructed.
		// That means that we just have to iterate through all of the items and
		// tally them to their parent item if it exists.
		// That is, we call handleDerivedInfo on each of our items... elegant!
		for (QAModel item: mData) {
			updateParentDerivedInfo(item);
		}
	}
	
	/**
	 * Apply an item to its parent items derived info, that is, if it is
	 * an upvote, add an upvote to its parent's derived info, and if it is
	 * an answer, add to its parent's reply count.
	 * @param item
	 */
	private void updateParentDerivedInfo(QAModel item) {
		if (item instanceof AuthoredItem) {
			QAModel parentItem = getItemById(((AuthoredItem)item).getParentItem());
			if (parentItem != null) {
				if (parentItem instanceof AuthoredTextItem && item instanceof UpvoteItem) {
					// Tally upvotes
					((AuthoredTextItem)parentItem).upvote();
				}
				if (parentItem instanceof QuestionItem && item instanceof AnswerItem) {
					// Tally replies
					((QuestionItem)parentItem).incrementReplyCount();
				}
			}
		} 	
	}
	
	/**
	 * Handle the derived information related to an item
	 * that has just been added, updating it's parent's info
	 * if it has a parent, and adding already present children's
	 * info to this item.
	 * @param item
	 */
	private void handleDerivedInfo(QAModel item) {
		updateParentDerivedInfo(item);
		for (QAModel child: mData) {
			if (child instanceof AuthoredItem) {
				QAModel parent = getItemById(((AuthoredItem)child).getParentItem());
				if (parent == item) {
					updateParentDerivedInfo(child);
				}
			}
		}
	}
	
	/**
	 * Testing method: Write a test set of data to the file
	 * @return Whether the write was successful
	 */
	public boolean writeTestData(String data) {
		DataManager.writeLocalData(mContext, LDM_CATEGORY, data);
		return true;
	}
	
	/**
	 * Debug method: Dump local data
	 */
	public String dumpLocalData() {
		return DataManager.readLocalData(mContext, LDM_CATEGORY);
	}
	
	/**
	 * Testing method: Force a flush to the file
	 */
	public void flushData() {
		doSave();
	}
	
	/**
	 * Testing method: Wait for the local data to be ready
	 */
	public void waitForData() {
		// Wait for the notification that data is ready
		// Dubious code: There's probably a better way to do this
		mDataLock.lock();
		if (isAvailable()) {
			mDataLock.unlock();
		} else {
			try {
				mDataBecomeReady.await();
				mDataLock.unlock();
			} catch (InterruptedException e) {
				Log.e("app", "interruptedexception??");
				return;
			}
		}
	}
	
	/**
	 * Testing method, wait for a save to complete
	 */
	public void waitForSave() {
		mDataLock.lock();
		if (mDataDirty && mCurrentSaveWorker != null) {
			try {
				mSaveCompleted.await();
			} catch (InterruptedException e) {
				return;
			}
		} else {
			mDataLock.unlock();	
		}
	}
	
	/**
	 * Private utility method to do the initial query of data on the
	 * data manager. Called by the query methods.
	 * Yields until the save has completed if it was needed.
	 * PRECONDITION: Current thread must be holding the mDataLock
	 */
	private void maybeDoInitialDataQuery() {
		if (mDataLoadStarted) {
			if (mData == null) {
				// Wait for the data
				try {
					mSaveCompleted.await();
				} catch (InterruptedException e) {}
			} else {
				// Otherwise, data is ready, just return
				return;
			}
		} else {	
			// Start loading data
			mDataLoadStarted = true;
			
			// Start loading the data
			List<QAModel> result = new ArrayList<QAModel>();
			
			// Read in the data
			String in = DataManager.readLocalData(mContext, LDM_CATEGORY);
			if (in == null) {
				// No data yet, return empty array, nothing to do
			} else {
				readItemData(in, result);
			}
			
			// Actual line that sets our data array and hashmap
			mData = result;
			for (QAModel item: mData) {
				mDataById.put(item.getUniqueId(), new WeakReference<QAModel>(item));
			}
			
			// Calculate the derived info
			calculateInitialDerivedInfo();
			
			// Now available
			mLastAvailable = Calendar.getInstance().getTime();
			
			// Notify that we are available now
			notifyDataSourceAvailable();
			
			// Notify that we are ready
			mDataBecomeReady.signalAll();
		}
	}
	
	/**
	 * Query the LocalData using a given query, and a given result to put the results
	 * into.
	 * @param filter
	 * @param result
	 */
	@Override
	public void query(final DataFilter filter, final IItemComparator compare, final IncrementalResult result) {
		new RunTaskHelper<List<QAModel>>() {
			@Override
			public List<QAModel> task() {
				Log.i("app", "LocalDataManager :: Doing Filter query...");
				List<QAModel> resultList = doFilterQuery(filter, compare);
				Log.i("app", "LocalDataManager :: Filter query finished with " + result.getCurrentResults().size() + " results.");
				return resultList;
			}
			@Override
			public void done(List<QAModel> resultList) {
				result.addObjects(resultList);
			}
		};
	}
	
	/**
	 * Main query implementation for this task, is run in a separate thread for each query.
	 * @param filter
	 * @param compare
	 * @return The list of items found to insert
	 */
	private List<QAModel> doFilterQuery(DataFilter filter, IItemComparator compare) {		
		// Create an array for the results
		List<QAModel> packedItem = new ArrayList<QAModel>();
		
		// Main query loop
		mDataLock.lock(); // Lock during iteration
		
		// If the initial data load has not started yet, then we
		// need to do the data load in this task.
		maybeDoInitialDataQuery();
		
		//Log.i("lock", "DoFilterQuery Lock <" + mData.size() + ">");
		for (QAModel item: mData) {
			if (filter.accept(item)) {
				packedItem.add(item);
			}
		}
		//Log.i("lock", "DoFilterQuery Unlock");
		mDataLock.unlock();
		
		return packedItem;
	}
	
	/**
	 * Main query implementation for this task by ID
	 * @param ids
	 */
	@Override
	public void query(final List<UniqueId> ids, final IncrementalResult result) {
		// Do query async
		new RunTaskHelper<List<QAModel>>() {
			@Override
			public List<QAModel> task() {
				List<QAModel> resultList = doIdListQuery(ids);
				Log.i("app", "LocalDataManager :: Id query finished with " + result.getCurrentResults().size() + " results.");
				return resultList;
			}
			@Override
			public void done(List<QAModel> resultList) {
				result.addObjects(resultList);
			}
		};
	}
	
	/**
	 * Query based on a list of unique IDs, is run in a separate thread for 
	 * each query
	 * @param ids
	 * @param result
	 */
	private List<QAModel> doIdListQuery(List<UniqueId> ids) {
		// Set up an array for our results
		List<QAModel> packedResult = new ArrayList<QAModel>();
		
		// Main query loop
		mDataLock.lock(); // Lock during iteration
		
		// Do the initial query if needed
		maybeDoInitialDataQuery();
		
		//Log.i("lock", "doIdListQuery Lock");
		for (UniqueId id: ids) {
			QAModel item = getItemById(id);
			if (item != null) {
				packedResult.add(item);
			}
		}
		//Log.i("lock", "doIdListQuery Unlock");
		mDataLock.unlock();
		
		return packedResult;
	}
	

	/**
	 * Main save item method. Saves an item to the local storage. If the item
	 * already exists, do nothing.
	 * @param item The item to save.
	 */
	@Override
	public boolean saveItem(QAModel item) {
		// First, make a local copy of the item, without derived information in it
		// Copy via a serialize / deserialize pair.
		Gson gson = DataManager.getGsonObject();
		item = (QAModel)gson.fromJson(gson.toJson(item, item.getClass()), item.getClass());
		
		// Add the item to our data array if it isn't there already, and if we
		// have to add it, mark as dirty.
		mDataLock.lock();
		
		// Test
		maybeDoInitialDataQuery();
		
		//Log.i("lock", "saveItem Lock");
		if (getItemById(item.getUniqueId()) == null) {
			// Add to our tracking
			mData.add(item);
			mDataById.put(item.getUniqueId(), new WeakReference<QAModel>(item));
			
			// Calculate derived info
			handleDerivedInfo(item);
			
			// Mark us as needing a save
			mDataDirty = true;
			
			Log.i("app", "LocalDataManager :: Item <" + item.getUniqueId() + "> added.");
		}
		
		// Do we need to save?
		if (mDataDirty) {
			if (mCurrentSaveWorker == null) {
				// Schedule a save
				mCurrentSaveWorker = mSaveWorkerPool.schedule(new Runnable() {	
					@Override
					public void run() {
						doSave();
					}
				}, SAVE_INTERVAL, TimeUnit.MILLISECONDS);
			}
		}
		
		//Log.i("lock", "saveItem Unlock");
		mDataLock.unlock();
		
		return true;
	}
	
	/**
	 * Do a save of the current item data to our file, called asynchronously
	 * when a scheduled save should be done.
	 */
	private void doSave() {
		mDataLock.lock(); // Lock the data while iterating to save
		//Log.i("lock", "doSave Lock");
		
		// Remove the reference to this task
		mCurrentSaveWorker = null;
		
		// If we are still dirty, do the save
		if (mDataDirty) {
			String data = writeItemData(mData);
			DataManager.writeLocalData(mContext, LDM_CATEGORY, data);
			mDataDirty = false;
			mSaveCompleted.signalAll();
		}
		
		//Log.i("lock", "doSave Unlock");
		mDataLock.unlock();
		
		Log.i("app", "LocalDataManager :: Data Saved");
	}

	@Override
	public boolean isAvailable() {
		return mData != null;
	}

	@Override
	public Date getLastDataSourceAvailableTime() {
		if (isAvailable()) {
			return Calendar.getInstance().getTime();
		} else {
			return mLastAvailable;
		}
	}

	@Override
	public void addDataLoadedListener(IDataLoadedListener listener) {
		mDataLoadedListenersLock.lock();
		//Log.i("lock", "addDataLoadedListener Lock");
		mDataLoadedListeners.add(listener);
		//Log.i("lock", "addDataLoadedListener Unlock");
		mDataLoadedListenersLock.unlock();
	}

	@SuppressWarnings("unused")
	private void notifyDataItemLoaded(QAModel item) {
		mDataLoadedListenersLock.lock();
		//Log.i("lock", "notifyDataItemLoaded Lock");
		for (IDataLoadedListener listener: mDataLoadedListeners) {
			listener.dataItemLoaded(this, item);
		}
		//Log.i("lock", "notifyDataItemLoaded Unlock");
		mDataLoadedListenersLock.unlock();
	}

	@Override
	public void addDataSourceAvailableListener(
			IDataSourceAvailableListener listener) {
		mAvailableListenersLock.lock();
		//Log.i("lock", "addDataSourceAvailListener Lock");
		mAvailableListeners.add(listener);
		//Log.i("lock", "addDataSourceAvailListener Unlock");
		mAvailableListenersLock.unlock();
	}

	private void notifyDataSourceAvailable() {
		mAvailableListenersLock.lock();
		//Log.i("lock", "notifyAvailable Lock");
		for (IDataSourceAvailableListener listener: mAvailableListeners) {
			listener.DataSourceAvailable(this);
		}
		//Log.i("lock", "notifyAvailable Unlock");
		mAvailableListenersLock.unlock();
	}

}
