package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.NotImplementedException;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.DroidClientConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;

/**
 * Manages all interaction with the Elastic Search server.
 *
 * @author michaelblouin
 */
@SuppressLint("DefaultLocale") public class NetworkDataManager implements IDataSourceManager {
	protected Boolean mIsAvailable = null;

	protected String mEsServerUrl;
	protected String mEsServerIndex;

	protected final Lock mJestClientLock = new ReentrantLock();
	protected JestClientFactory mJestClientFactory;
	protected JestClient mJestClient;

	protected final Lock mDataSourceAvailableListenersLock = new ReentrantLock();
	protected List<IDataSourceAvailableListener> mDataSourceAvailableListeners;

	protected final Lock mDataLoadedListenersLock = new ReentrantLock();
	protected List<IDataLoadedListener> mDataLoadedListeners;

	/**
	 * Class used for executing elastic search queries. Class is a child of AsyncTask<>, so it is asynchronous.
	 *
	 * @author michaelblouin
	 */
	private class QueryTask extends AsyncTask<Void, Void, Void> {
		private Search mSearch;
		private IncrementalResult mResult;

		public QueryTask(Search search, IncrementalResult result) {
			mSearch = search;
			mResult = result;
		}

		@Override
		protected Void doInBackground(Void ... nothing) {
			SearchResult searchResult = beginSearch(mSearch);

			if (null == searchResult) {
				return null;
			}

			System.out.println(String.format("NetworkDataManager received %d results", searchResult.getTotal()));
			parseSearchResults(searchResult, mResult);

			return null;
		}

		/**
		 * Begins the search against the elastic search server. This function blocks until the request has been completed.
		 *
		 * If an error occurs, this function returns null for the SearchResult.
		 *
		 * @param search The search to perform
		 * @return The SearchResult containing the results from the elastic search server.
		 */
		private SearchResult beginSearch(Search search) {
			if (null == mJestClient) {
				initJestClient();
			}

			SearchResult searchResult = null;
			boolean available = false;

			mJestClientLock.lock();
			try {
				searchResult = mJestClient.execute(search);
				available = true;
			} catch (java.net.UnknownHostException e) {
				System.out.println("Could not resolve server host");
				available = false;
			} catch (java.net.NoRouteToHostException e) {
				System.out.println("Could not resolve server host");
				available = false;
			} catch (Exception e) {
				System.out.println("Exception occured performing query");
				e.printStackTrace();
				available = false;
			}
			mJestClientLock.unlock();

			setIsAvailable(available);

			if (null == searchResult) {
				return null;
			}

			if (!searchResult.isSucceeded()) {
				System.out.println(String.format("Error performing elastic search query: %s", searchResult.getErrorMessage()));
				return null;
			}

			return searchResult;
		}

		/**
		 * Parses the searchResults contained within a SearchResult object, and adds them to the provided IncrementalResult.
		 * @param searchResult The SearchResults to parse
		 * @param IncrementalResult The incremental result to load parsed results into
		 */
		private void parseSearchResults(SearchResult searchResult, IncrementalResult result) {
			Gson gson = DataManager.getGsonObject();
			List<QAModel> objects = new ArrayList<QAModel>();
			for (JsonElement element: searchResult.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits")) {
				JsonObject obj = element.getAsJsonObject();
				try {
					if (!obj.getAsJsonPrimitive("_index").getAsString().equals(mEsServerIndex)) {
						continue;
					}

					String source = obj.getAsJsonObject("_source").toString();
					QAModel newObject = null;
					switch (ItemType.fromString(obj.getAsJsonPrimitive("_type").getAsString())) {
						case Question:
							newObject = gson.fromJson(source, QuestionItem.class);
							break;

						case Answer:
							newObject = gson.fromJson(source, AnswerItem.class);
							break;

						case Comment:
							newObject = gson.fromJson(source,  CommentItem.class);
							break;

						case Upvote:
							newObject = gson.fromJson(source, UpvoteItem.class);
							break;

						default:
							System.out.println("Unknown object type encountered!");
					}

					if (null == newObject)
						continue;

					objects.add(newObject);
					notifyDataItemLoaded(newObject);
				} catch (Exception e) {
					System.out.println("Error occured parsing object: " + obj.toString());
					e.printStackTrace();
				}
			}

			result.addObjects(objects);
		}
	}

	/**
	 * Class used for executing elastic search saves.
	 *
	 * @author michaelblouin
	 */
	private class SaveTask extends AsyncTask<Void, Void, Void> {
		Index mIndex;
		IDataItemSavedListener mListener;

		public SaveTask(Index index, IDataItemSavedListener listener) {
			mIndex = index;
			mListener = listener;
		}

		@Override
		protected Void doInBackground(Void... params) {
			boolean success = false;
			Exception exception = null;

			mJestClientLock.lock();
			try {
				JestResult result = mJestClient.execute(mIndex);
				success = null != result && result.isSucceeded();
			} catch (Exception e) {
				// TODO: Set isAvailable on network error
				System.out.println("SaveTask encountered error:");
				e.printStackTrace();
				success = false;
			}
			mJestClientLock.unlock();

			if (null != mListener) {
				mListener.dataItemSaved(success, exception);
			}

			return null;
		}
	}

	public NetworkDataManager(String esServerUrl, String esServerIndex) {
		mEsServerUrl = esServerUrl;
		mEsServerIndex = esServerIndex;
	}

	@Override
	public void query(DataFilter filter, IItemComparator comparator, IncrementalResult result) {
		if (null == mJestClient) {
			initJestClient();
		}

		ESSearchBuilder searchBuilder = new ESSearchBuilder(filter, comparator);
		Search search = 
			searchBuilder
			.getBuilder()
			.addIndex(mEsServerIndex)
			.build();

		QueryTask task = new QueryTask(search, result);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			task.execute();
	}

	@Override
	public void query(List<UniqueId> ids, IncrementalResult result) {
		return;
	}

	@Override
	public boolean saveItem(QAModel item) {
		return saveItem(item, null);
	}

	public boolean saveItem(QAModel item, IDataItemSavedListener listener) {
		if (null == mJestClient) {
			initJestClient();
		}

		System.out.println("Item source: " + DataManager.getGsonObject().toJson(item));
		Index index = new Index.Builder(DataManager.getGsonObject().toJson(item))
			.index(mEsServerIndex)
			.type(item.getItemType().toString().toLowerCase())
			.id(item.getUniqueId().toString())
			.build();

		SaveTask task = new SaveTask(index, listener);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			task.execute();

		return false;
	}

	@Override
	public boolean isAvailable() {
		return null != mIsAvailable && mIsAvailable;
	}

	/**
	 * Sets whether the data manager is available. If the availability has changed, the data manager notifies.
	 * @param available The new available state of te data manager.
	 */
	protected void setIsAvailable(final boolean available) {
		if (null == mIsAvailable || available != mIsAvailable) {
			mIsAvailable = available;
			notifyDataSourceAvailable();
		}
	}

	@Override
	public Date getLastDataSourceAvailableTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDataLoadedListener(IDataLoadedListener listener) {
		mDataLoadedListenersLock.lock();
		if (null == mDataLoadedListeners) {
			mDataLoadedListeners = new ArrayList<IDataLoadedListener>();
		}

		mDataLoadedListeners.add(listener);
		mDataLoadedListenersLock.unlock();
	}

	public void notifyDataItemLoaded(QAModel item) {
		mDataLoadedListenersLock.lock();

		if (null == mDataLoadedListeners) {
			mDataLoadedListenersLock.unlock();
			return;
		}

		for (IDataLoadedListener listener: mDataLoadedListeners) {
			listener.dataItemLoaded(this, item);
		}

		mDataLoadedListenersLock.unlock();
	}

	@Override
	public void addDataSourceAvailableListener(IDataSourceAvailableListener listener) {
		mDataSourceAvailableListenersLock.lock();

		if (null == mDataSourceAvailableListeners) {
			mDataSourceAvailableListeners = new ArrayList<IDataSourceAvailableListener>();
		}

		mDataSourceAvailableListeners.add(listener);

		mDataSourceAvailableListenersLock.unlock();
	}

	public void notifyDataSourceAvailable() {
		mDataSourceAvailableListenersLock.lock();

		if (null == mDataSourceAvailableListeners) {
			return;
		}

		for (IDataSourceAvailableListener listener: mDataSourceAvailableListeners) {
			listener.DataSourceAvailable(this);
		}

		mDataSourceAvailableListenersLock.unlock();
	}

	/**
	 * Initializes the jest client and factory if they haven't already been initialized.
	 */
	private void initJestClient() {
		mJestClientLock.lock();

		if (null == mJestClientFactory) {
			mJestClientFactory = new JestClientFactory();
			mJestClientFactory.setDroidClientConfig(new DroidClientConfig.Builder(mEsServerUrl).multiThreaded(false).build());
		}

		if (null == mJestClient) {
			mJestClient = mJestClientFactory.getObject();
		}

		mJestClientLock.unlock();
	}
}
