package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
	 * When were we last available?
	 */
	private Date mLastAvailable;
	
	/**
	 * Our availability listeners
	 */
	private List<IDataSourceAvailableListener> mAvailableListeners = new ArrayList<IDataSourceAvailableListener>();
	
	/**
	 * Our DataLoaded listeners
	 */
	private List<IDataLoadedListener> mDataLoadedListeners = new ArrayList<IDataLoadedListener>();
	
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
		
		// Start a task to read in and cache the local items
		new LoadDataFromFilesystemTask().execute();
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
	private void doFilterQuery(final DataFilter filter, final IItemComparator compare, final IncrementalResult result) {
		// Main query loop
		for (QAModel item: mData) {
			if (filter.accept(item)) {
				List<QAModel> packedItem = new ArrayList<QAModel>();
				packedItem.add(item);
				result.addObjects(packedItem);
			}
		}
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
				// Main query loop
				for (QAModel item: mData) {
					// TODO: Make this more efficient
					for (UniqueId id: ids) {
						if (item.getUniqueId().equals(id)) {
							List<QAModel> packedResult = new ArrayList<QAModel>();
							packedResult.add(item);
							result.addObjects(packedResult);
						}
					}
				}
			}
		};
	}

	
	@Override
	public boolean saveItem(QAModel item) {
		if (!isAvailable())
			throw new IllegalStateException("Must be available to be queried.");	
	
		// Add the item to our data array if it isn't there already
		found: {
			for (QAModel currentItem: mData) {
				if (currentItem.getUniqueId().equals(item.getUniqueId())) {
					break found;
				}
			}
			mData.add(item);
		}
		
		// Save our data array back to the file
		FileOutputStream out = mUserContext.getLocalDataDestination(mContext, LDM_CATEGORY);
		if (out != null) {
			writeItemData(out, mData);
			try {
				out.close();
			} catch (IOException e) { /* Nothing we can do to handle failure closing a file handle */ }
			return true;
		} else {
			return false;
		}
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
		mDataLoadedListeners.add(listener);
	}

	@SuppressWarnings("unused")
	private void notifyDataItemLoaded(QAModel item) {
		for (IDataLoadedListener listener: mDataLoadedListeners) {
			listener.dataItemLoaded(this, item);
		}
	}

	@Override
	public void addDataSourceAvailableListener(
			IDataSourceAvailableListener listener) {
		mAvailableListeners.add(listener);
	}

	private void notifyDataSourceAvailable() {
		for (IDataSourceAvailableListener listener: mAvailableListeners) {
			listener.DataSourceAvailable(this);
		}
	}

}
