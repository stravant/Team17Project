package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Index.Builder;
import io.searchbox.params.Parameters;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.DroidClientConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredItem;
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
	private class QueryTask extends AsyncTask<Void, Void, List<List<QAModel>>> {
		private Search mSearch;
		private IncrementalResult mResult;
		private Callable<Void> mChain;

		public QueryTask(Search search, IncrementalResult result, Callable<Void> doChain) {
			mSearch = search;
			mResult = result;
			mChain = doChain;
		}

		@Override
		protected List<List<QAModel>> doInBackground(Void ... nothing) {
			SearchResult searchResult = beginSearch(mSearch);
			final List<QAModel> results = parseSearchResults(searchResult);
			final List<List<QAModel>> metaResults = queryModelMetaData(results);
			
			if (null == results) {
				return null;
			}

			System.out.println(String.format("NetworkDataManager received %d result, and %d meta results", results.size(), metaResults.size()));

			return new ArrayList<List<QAModel>>() {{
				add(results);
				addAll(metaResults);
			}};
		}

		@Override
		protected void onPostExecute(List<List<QAModel>> results) {
			if (null == results) {
				return;
			}

			for (List<QAModel> resultList: results) {
				if (null == resultList) {
					continue;
				}

				// Need to notify for every data item we load
				for (QAModel result: resultList) {
					notifyDataItemLoaded(result);
				}
			}

			// Only add the first items, which are from the original result set
			if (null != results.get(0)) {
				mResult.addObjects(results.get(0));					
			}
			
			// Do the query chain
			try {
				mChain.call();
			} catch (Exception e) {};
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
		private List<QAModel> parseSearchResults(SearchResult searchResult) {
			if (null == searchResult) {
				return null;
			}

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
				} catch (Exception e) {
					System.out.println("Error occured parsing object: " + obj.toString());
					e.printStackTrace();
				}
			}

			return objects;
		}
		
		private List<List<QAModel>> queryModelMetaData(List<QAModel> results) {
			List<List<QAModel>> resultsList = new ArrayList<List<QAModel>>();
			if (null == results) {
				return resultsList;
			}

			// Get the items we need to query for
			Map<UniqueId, QAModel> answerCountQueryItems = new HashMap<UniqueId, QAModel>();
			Map<UniqueId, QAModel> upvoteCountQueryItems = new HashMap<UniqueId, QAModel>();
			for (QAModel result: results) {
				if (ItemType.Question == result.getItemType()) {
					answerCountQueryItems.put(result.getUniqueId(), result);
					upvoteCountQueryItems.put(result.getUniqueId(), result);
				} else if (ItemType.Answer == result.getItemType()) {
					upvoteCountQueryItems.put(result.getUniqueId(), result);
				}
			}

			// Build the queries
			if (0 != upvoteCountQueryItems.size()) {
				DataFilter filter = new DataFilter();
				filter.setTypeFilter(ItemType.Upvote);
				for (QAModel item: upvoteCountQueryItems.values()) {
					filter.addFieldFilter(
							AuthoredItem.FIELD_PARENT, 
							item.getUniqueId().toString(), 
							DataFilter.FilterComparison.EQUALS,
							DataFilter.CombinationMode.SHOULD);
				}
				
				filter.setMaxResults(ESSearchBuilder.MAX_ES_RESULTS);
				ESSearchBuilder searchBuilder = new ESSearchBuilder(filter, null);
				List<QAModel> metaResults = parseSearchResults(doMetaQuery(searchBuilder));
				if (null != metaResults) {
					resultsList.add(metaResults);
				}
			}
			
			if (0 != answerCountQueryItems.size()) {
				DataFilter filter = new DataFilter();
				filter.setTypeFilter(ItemType.Answer);
				for (QAModel item: answerCountQueryItems.values()) {
					filter.addFieldFilter(
							AuthoredItem.FIELD_PARENT, 
							item.getUniqueId().toString(), 
							DataFilter.FilterComparison.EQUALS,
							DataFilter.CombinationMode.SHOULD);
				}
				
				filter.setMaxResults(ESSearchBuilder.MAX_ES_RESULTS);
				ESSearchBuilder searchBuilder = new ESSearchBuilder(filter, null);
				List<QAModel> metaResults = parseSearchResults(doMetaQuery(searchBuilder));
				if (null != metaResults) {
					resultsList.add(metaResults);
				}
			}
			
			return resultsList;
		}
		
		private SearchResult doMetaQuery(ESSearchBuilder searchBuilder) {
			Search search = 
				searchBuilder
				.getBuilder()
				.addIndex(mEsServerIndex)
				.build();

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

			return searchResult;
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
		boolean mSuccess;
		Exception mException = null;

		public SaveTask(Index index, IDataItemSavedListener listener) {
			mIndex = index;
			mListener = listener;
		}

		@Override
		protected Void doInBackground(Void... params) {
			mJestClientLock.lock();
			try {
				JestResult result = mJestClient.execute(mIndex);
				mSuccess = null != result && result.isSucceeded();
				if (!mSuccess && null != result) {
					System.out.println(result.getErrorMessage());
				}
			} catch (Exception e) {
				// TODO: Set isAvailable on network error
				System.out.println("SaveTask encountered error:");
				e.printStackTrace();
				mSuccess = false;
				mException = e;
			}
			mJestClientLock.unlock();

			return null;
		}

		@Override
		protected void onPostExecute(Void nothing) {
			if (null != mListener) {
				mListener.dataItemSaved(mSuccess, mException);
			}
		}
	}

	public NetworkDataManager(String esServerUrl, String esServerIndex) {
		mEsServerUrl = esServerUrl;
		mEsServerIndex = esServerIndex;
	}

	@Override
	public void query(final DataFilter filter, final IItemComparator comparator, final IncrementalResult result, final IDataSourceManager chainTo) {
		if (null == mJestClient) {
			initJestClient();
		}

		ESSearchBuilder searchBuilder = new ESSearchBuilder(filter, comparator);
		System.out.println("Search query: " + searchBuilder);
		Search search = 
			searchBuilder
			.getBuilder()
			.addIndex(mEsServerIndex)
			.build();

		QueryTask task = new QueryTask(search, result, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				chainTo.query(filter, comparator, result, null);
				return null;
			}
			
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			task.execute();
	}

	@Override
	public void query(List<UniqueId> ids, IncrementalResult result, IDataSourceManager chainTo) {
		return;
	}

	@Override
	public boolean saveItem(QAModel item, UserContext ctx) {
		return saveItem(item, ctx, null);
	}

	public boolean saveItem(QAModel item, UserContext ctx, IDataItemSavedListener listener) {
		if (null == mJestClient) {
			initJestClient();
		}

		String type = item.getItemType().toString().toLowerCase();
		if (item instanceof UpvoteItem && null != ((UpvoteItem)item).getParentType()) {
			type += "_" + ((UpvoteItem)item).getParentType().toString().toLowerCase();
		}

		Builder builder = 
		 new Index.Builder(DataManager.getGsonObject().toJson(item))
			.index(mEsServerIndex)
			.type(type)
			.id(item.getUniqueId().toString());

		if (null != item.getField(AuthoredItem.FIELD_PARENT) && !(item instanceof CommentItem)) {
			builder.setParameter(Parameters.PARENT, item.getField(AuthoredItem.FIELD_PARENT).toString());
		}

		SaveTask task = new SaveTask(builder.build(), listener);
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
