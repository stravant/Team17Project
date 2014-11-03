package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.os.AsyncTask;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;

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
	private List<QAModel> mData;
	
	/**
	 * Do we need to do a save on the data?
	 */
	private boolean mDataDirty = false;
	
	/**
	 * A lock on our data
	 */
	private ReentrantLock mDataLock = new ReentrantLock();
	
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
			if (in != null) {
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
			// Actual line that sets our data
			mData = result;
			
			// Notify that we are available now
			notifyDataSourceAvailable();
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
		
		// Start a task to read in and cache the local items
		new LoadDataFromFilesystemTask().execute();
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
		// TODO: implement
		throw new UnsupportedOperationException();
		// TODO: notify data item loaded
	}
	
	/**
	 * Private function that handles writing items to a file
	 * @param out
	 * @param buffer The buffer to write out items from
	 */
	private void writeItemData(FileOutputStream out, List<QAModel> buffer) {
		// TODO: implement
		throw new UnsupportedOperationException();
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
		for (QAModel item: mData) {
			if (filter.accept(item)) {
				packedItem.add(item);
			}
		}
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
		for (QAModel item: mData) {
			// TODO: Make this more efficient
			for (UniqueId id: ids) {
				if (item.getUniqueId().equals(id)) {
					packedResult.add(item);
				}
			}
		}
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
			throw new IllegalStateException("Must be available to be queried.");	
	
		// Add the item to our data array if it isn't there already, and if we
		// have to add it, mark as dirty.
		mDataLock.lock();
		found: {
			for (QAModel currentItem: mData) {
				if (currentItem.getUniqueId().equals(item.getUniqueId())) {
					break found;
				}
			}
			mData.add(item);
			mDataDirty = true;
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
				}, Calendar.getInstance().getTimeInMillis() + SAVE_INTERVAL, TimeUnit.MILLISECONDS);
			}
		}
		
		mDataLock.unlock();
		
		return true;
	}
	
	/**
	 * Do a save of the current item data to our file, called asynchronously
	 * when a scheduled save should be done.
	 */
	private void doSave() {
		mDataLock.lock(); // Lock the data while iterating to save
		
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
		}
		
		mDataLock.unlock();
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
		mDataLoadedListeners.add(listener);
		mDataLoadedListenersLock.unlock();
	}

	@SuppressWarnings("unused")
	private void notifyDataItemLoaded(QAModel item) {
		mDataLoadedListenersLock.lock();
		for (IDataLoadedListener listener: mDataLoadedListeners) {
			listener.dataItemLoaded(this, item);
		}
		mDataLoadedListenersLock.unlock();
	}

	@Override
	public void addDataSourceAvailableListener(
			IDataSourceAvailableListener listener) {
		mAvailableListenersLock.lock();
		mAvailableListeners.add(listener);
		mAvailableListenersLock.unlock();
	}

	private void notifyDataSourceAvailable() {
		mAvailableListenersLock.lock();
		for (IDataSourceAvailableListener listener: mAvailableListeners) {
			listener.DataSourceAvailable(this);
		}
		mAvailableListenersLock.unlock();
	}

}
