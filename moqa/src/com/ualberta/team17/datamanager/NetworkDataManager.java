package com.ualberta.team17.datamanager;

import java.util.Date;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import android.os.AsyncTask;

import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.DroidClientConfig;
import com.google.gson.Gson;

import com.ualberta.team17.QAModel;

public class NetworkDataManager implements IDataSourceManager {
	protected boolean useTestServer = false;
	protected JestClientFactory mJestClientFactory;
	protected JestClient mJestClient;
	protected String mEsServerUrl;
	protected String mEsServerIndex;

	public NetworkDataManager(String esServerUrl, String esServerIndex) {
		mEsServerUrl = esServerUrl;
	}

	@Override
	public void query(DataFilter filter, IItemComparator comparator, IncrementalResult result) {
		if (null == mJestClient) {
			initJestClient();
		}

		ESSearchBuilder searchBuilder = new ESSearchBuilder(filter, comparator);
		Search search = 
			new Search.Builder(searchBuilder.toString())
				.addIndex(mEsServerIndex)
				.build();

		QueryTask task = new QueryTask(search, result);
		task.execute();
	}

	@Override
	public boolean saveItem(QAModel item) {
		throw new UnsupportedOperationException();
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

	private void initJestClient() {
		if (null == mJestClientFactory) {
			mJestClientFactory = new JestClientFactory();
			mJestClientFactory.setDroidClientConfig(new DroidClientConfig.Builder(mEsServerUrl).multiThreaded(false).build());
		}
		
		if (null == mJestClient) {
			mJestClient = mJestClientFactory.getObject();
		}
	}

	private class QueryTask extends AsyncTask<Void, Void, Void> {
		private Search mSearch;
		private IncrementalResult mResult;

		public QueryTask(Search search, IncrementalResult result) {
			mSearch = search;
			mResult = result;
		}

		@Override
		protected Void doInBackground(Void ... nothing) {
			if (null == mJestClient) {
				initJestClient();
			}

			SearchResult searchResult;

			try {
				searchResult = mJestClient.execute(mSearch);
			} catch (Exception e) {
				System.out.println("Exception occured performing query");
				e.printStackTrace();
				searchResult = null;
			}

			if (null == searchResult) {
				return null;
			}

			System.out.println(String.format("NetworkDataManager received %d results", searchResult.getTotal()));
			System.out.println(searchResult.getJsonObject().toString());

			Gson gson = DataManager.getGsonObject();

			return null;
		}
	}
}
