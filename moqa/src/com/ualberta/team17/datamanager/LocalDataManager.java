package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;

public class LocalDataManager implements IDataSourceManager {
	/**
	 * The File Context to use
	 */
	private Context mContext;
	
	/**
	 * The UserContext of the user to load data for
	 */
	private UserContext mUserContext;
	
	/**
	 * Our set of in-memory data
	 */
	private volatile List<QAModel> mData;
	
	/**
	 * Do we need to do a save on the data?
	 */
	private boolean mDataDirty = false;
	
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
	 * An AsyncTask for loading in the QAModel entries initially.
	 */
	private class LoadDataFromFilesystemTask extends AsyncTask<Void, Void, List<QAModel>> {
		@Override
		protected List<QAModel> doInBackground(Void... input) {
			// Initialize a list for the results
			List<QAModel> result = new ArrayList<QAModel>();
			
			// Read in the data
			FileInputStream in = mUserContext.getLocalDataSource(mContext, LDM_CATEGORY);
			if (in == null) {
				// No data yet, return empty array
				return result;
			} else {
				readItemData(in, result);
			}
			try {
				in.close();
			} catch (IOException e) { /* WTF?? How can I handle closing a file failing? */ }
			
			// Now available
			mLastAvailable = Calendar.getInstance().getTime();
			
			// Return the result
			return result;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {}

		@Override
		protected void onPostExecute(List<QAModel> result) {
			mDataLock.lock();
			
			// Actual line that sets our data
			mData = result;
			
			// Notify that we are available now
			notifyDataSourceAvailable();	
			
			// Notify that we are ready
			mDataBecomeReady.signalAll();
			mDataLock.unlock();
			
			Log.i("app", "LocalDataManager :: Finish loading data.");
		}
	};
	
	/**
	 * Construct a LocalDataManager, doing FileIO for a given 
	 * user, using a given App context.
	 * @param context The app context to use.
	 * @param user    The user to do the IO for
	 */
	public LocalDataManager(Context context, UserContext user) {
		// Set the params
		mContext = context;
		mUserContext = user;
		
		// Availability
		// Currently: Since Linux epoch to start
		mLastAvailable = new Date(0);
		
		// Saving
		mLastSaveTimeMillis = Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * Load in the data from the file system
	 */
	public void asyncLoadData() {
		// Start a task to read in and cache the local items
		AsyncTask<Void, Void, List<QAModel>> load = new LoadDataFromFilesystemTask();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    load.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
		    load.execute();
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
	private void readItemData(FileInputStream in, List<QAModel> buffer) {
		// Read in the data
		StringBuffer fileContent = new StringBuffer("");
		try {
			byte[] tmpBuffer = new byte[1024];
			int readCount;
			while ((readCount = in.read(tmpBuffer)) != -1) { 
				fileContent.append(new String(tmpBuffer, 0, readCount)); 
			}
		} catch (IOException e) {
			// TODO: Handle
			return;
		}
		String data = fileContent.toString();
		
		// Get the JSON element and parse
		JsonParser parser = new JsonParser();
		JsonElement elem = parser.parse(data);
		
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
	private void writeItemData(FileOutputStream out, List<QAModel> buffer) {
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
		
		String data = gson.toJson(array);

		// Write out the encoded data
		try {
			OutputStreamWriter outWriter = new OutputStreamWriter(out);
			outWriter.append(data);
			outWriter.flush();
		} catch (IOException e) {
			// TODO: Handle
		}
	}
	
	/**
	 * Testing method: Write a test set of data to the file
	 * @return Whether the write was successful
	 */
	public boolean writeTestData(String data) {
		try {
			FileOutputStream out = mUserContext.getLocalDataDestination(mContext, LDM_CATEGORY);
			OutputStreamWriter outWriter = new OutputStreamWriter(out);
			outWriter.append(data);
			outWriter.flush();
			out.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Debug method: Dump local data
	 */
	public String dumpLocalData() {
		try {
			// Read in the data
			FileInputStream in = mUserContext.getLocalDataSource(mContext, LDM_CATEGORY);
			if (in == null) {
				Log.i("app", "Failed to open input stream");
				return null;
			}
			
			StringBuffer fileContent = new StringBuffer("");
			try {
				byte[] tmpBuffer = new byte[1024];
				int readCount;
				while ((readCount = in.read(tmpBuffer)) != -1) { 
					fileContent.append(new String(tmpBuffer, 0, readCount)); 
				}
			} catch (IOException e) {
				return null;
			} finally {
				in.close();
			}
			String data = fileContent.toString();
			
			// Return it
			return data;
		} catch (IOException e) {
			return null;
		}
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
	 * Query the LocalData using a given query, and a given result to put the results
	 * into.
	 * @param filter
	 * @param result
	 */
	@Override
	public void query(final DataFilter filter, final IItemComparator compare, final IncrementalResult result) {
		if (!isAvailable())
			throw new IllegalStateException("Must be available to be queried.");
		
		new RunTaskHelper() {
			@Override
			public void task() {
				doFilterQuery(filter, compare, result);
				Log.i("app", "LocalDataManager :: Filter query finished with " + result.getCurrentResults().size() + " results.");
			}
		};
	}
	
	/**
	 * Main query implementation for this task, is run in a separate thread for each query.
	 * @param filter
	 * @param compare
	 * @param result
	 */
	private void doFilterQuery(DataFilter filter, IItemComparator compare, IncrementalResult result) {
		// Create an array for the results
		List<QAModel> packedItem = new ArrayList<QAModel>();
		
		// Main query loop
		mDataLock.lock(); // Lock during iteration
		//Log.i("lock", "DoFilterQuery Lock <" + mData.size() + ">");
		for (QAModel item: mData) {
			if (filter.accept(item)) {
				packedItem.add(item);
			}
		}
		//Log.i("lock", "DoFilterQuery Unlock");
		mDataLock.unlock();
		
		// Notify on the results
		result.addObjects(packedItem);
	}
	
	/**
	 * Main query implementation for this task by ID
	 * @param ids
	 */
	@Override
	public void query(final List<UniqueId> ids, final IncrementalResult result) {
		if (!isAvailable())
			throw new IllegalStateException("Must be available to be queried.");
		
		// Do query async
		new RunTaskHelper() {
			@Override
			public void task() {
				doIdListQuery(ids, result);
				Log.i("app", "LocalDataManager :: Id query finished with " + result.getCurrentResults().size() + " results.");
			}
		};
	}
	
	/**
	 * Query based on a list of unique IDs, is run in a separate thread for 
	 * each query
	 * @param ids
	 * @param result
	 */
	private void doIdListQuery(List<UniqueId> ids, IncrementalResult result) {
		// Set up an array for our results
		List<QAModel> packedResult = new ArrayList<QAModel>();
		
		// Main query loop
		mDataLock.lock(); // Lock during iteration
		//Log.i("lock", "doIdListQuery Lock");
		for (QAModel item: mData) {
			// TODO: Make this more efficient
			for (UniqueId id: ids) {
				if (item.getUniqueId().equals(id)) {
					packedResult.add(item);
				}
			}
		}
		//Log.i("lock", "doIdListQuery Unlock");
		mDataLock.unlock();
		
		// Notify on the results
		result.addObjects(packedResult);
	}
	

	/**
	 * Main save item method. Saves an item to the local storage. If the item
	 * already exists, do nothing.
	 * @param item The item to save.
	 */
	@Override
	public boolean saveItem(QAModel item) {
		if (!isAvailable())
			throw new IllegalStateException("Must be available to save.");		
	
		// Add the item to our data array if it isn't there already, and if we
		// have to add it, mark as dirty.
		mDataLock.lock();
		//Log.i("lock", "saveItem Lock");
		found: {
			for (QAModel currentItem: mData) {
				if (currentItem.getUniqueId().equals(item.getUniqueId())) {
					break found;
				}
			}
			mData.add(item);
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
			FileOutputStream out = mUserContext.getLocalDataDestination(mContext, LDM_CATEGORY);
			if (out != null) {
				// Do the write
				writeItemData(out, mData);
			}
			try {
				out.close();
			} catch (IOException e) { /* nothing we can do about closing a file handle failing */ }
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
