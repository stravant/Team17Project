package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import android.os.AsyncTask;

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
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.IdComparator;

public class NetworkDataManager implements IDataSourceManager {
	protected Boolean mIsAvailable = null;
	protected JestClientFactory mJestClientFactory;
	protected JestClient mJestClient;
	protected String mEsServerUrl;
	protected String mEsServerIndex;
	protected List<IDataSourceAvailableListener> mDataSourceAvailableListeners;
	protected List<IDataLoadedListener> mDataLoadedListeners;

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
		task.execute();
	}
	
	@Override
	public void query(List<UniqueId> ids, IncrementalResult result) {
		for (UniqueId id: ids) {
			DataFilter filter = new DataFilter();
			filter.addFieldFilter("id", id.toString(), FilterComparison.EQUALS);
			query(filter, new IdComparator(), result);
		}
	}

	@Override
	public boolean saveItem(QAModel item) {
		throw new UnsupportedOperationException();
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
		if (null == mDataLoadedListeners) {
			mDataLoadedListeners = new ArrayList<IDataLoadedListener>();
		}

		mDataLoadedListeners.add(listener);
	}

	public void notifyDataItemLoaded(QAModel item) {
		if (null == mDataLoadedListeners)
			return;

		for (IDataLoadedListener listener: mDataLoadedListeners) {
			listener.dataItemLoaded(this, item);
		}
	}

	@Override
	public void addDataSourceAvailableListener(
			IDataSourceAvailableListener listener) {
		if (null == mDataSourceAvailableListeners) {
			mDataSourceAvailableListeners = new ArrayList<IDataSourceAvailableListener>();
		}

		mDataSourceAvailableListeners.add(listener);
	}

	public void notifyDataSourceAvailable() {
		if (null == mDataSourceAvailableListeners)
			return;

		for (IDataSourceAvailableListener listener: mDataSourceAvailableListeners) {
			listener.DataSourceAvailable(this);
		}
	}

	/**
	 * Initializes the jest client and factory if they haven't already been initialized.
	 */
	private void initJestClient() {
		if (null == mJestClientFactory) {
			mJestClientFactory = new JestClientFactory();
			mJestClientFactory.setDroidClientConfig(new DroidClientConfig.Builder(mEsServerUrl).multiThreaded(false).build());
		}
		
		if (null == mJestClient) {
			mJestClient = mJestClientFactory.getObject();
		}
	}

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
}
