package com.ualberta.team17.datamanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

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
	 * An AsyncTask for loading in the QAModel entries initially.
	 */
	private class LoadDataFromFilesystemTask extends AsyncTask<Void, Void, List<QAModel>> {
		@Override
		protected List<QAModel> doInBackground(Void... input) {
			// Initialize a list for the results
			List<QAModel> result = new ArrayList<QAModel>();
			
			// Read in the data
			FileInputStream in = getDataSource();
			if (in != null) {
				// TODO: Read in the data
			}
			
			// Return the result
			return result;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {}

		@Override
		protected void onPostExecute(List<QAModel> result) {
			mData = result;
		}
	};
	
	/**
	 * Construct a LocalDataManager, doing FileIO for a given 
	 * user, using a given App context.
	 * @param context The app context to use.
	 * @param user    The user to do the IO for
	 */
	public LocalDataManager(Context context, UserContext user) {
		mContext = context;
		mUserContext = user;
	}
	
	/**
	 * Get the name of the file that we are using to store the
	 * settings in
	 * @return The name of the file
	 */
	private String getDataLocationName() {
		// Use the user's Id as the location
		return mUserContext.getUserId().toString();
	}
	
	/**
	 * Get the file a handle to the source of our data.
	 * @return A file, that is the location to read from
	 *  given the current Context and UserContext. 
	 *  If the user hasn't saved any data yet, return null
	 */
	private FileInputStream getDataSource() {
		try {
			return mContext.openFileInput(getDataLocationName());
		} catch (FileNotFoundException e) {
			// File was not found, we return null, letting the caller
			// create a file if they want to.
			return null;
		}
	}
	
	/**
	 * Destination to save changes to, given the current Context and UserContext
	 * @return A file, that is the location to write
	 *  to given the current Context and UserContext
	 */
	private FileOutputStream getDataDestination() {
		try {
			return mContext.openFileOutput(getDataLocationName(), Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			throw new Error("Fatal Error: Can't write to application directory");
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
		new RunTaskHelper() {
			@Override
			public void task() {
				doQuery(filter, compare, result);
			}
		};
	}
	
	/**
	 * Main query implementation for this task, is run in a separate thread for each query.
	 * @param filter
	 * @param compare
	 * @param result
	 */
	private void doQuery(DataFilter filter, IItemComparator compare, IncrementalResult result) {
	}

	@Override
	public boolean saveItem(QAModel item) {
		new RunTaskHelper() {
			@Override
			public void task() {
				doSaveItem(item);
			}
		};
	}
	
	/**
	 * Save a new item or an item that changes have been made to, may be done in a different thread.
	 * @param item
	 */
	private void doSaveItem(QAModel item) {
		
	}

	@Override
	public boolean isAvailable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Date getLastDataSourceAvailableTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDataLoadedListener(IDataLoadedListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyDataItemLoaded(QAModel item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDataSourceAvailableListener(
			IDataSourceAvailableListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyDataSourceAvailable() {
		throw new UnsupportedOperationException();
	}
}
