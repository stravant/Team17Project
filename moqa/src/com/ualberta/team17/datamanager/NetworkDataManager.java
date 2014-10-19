package com.ualberta.team17.datamanager;

import java.util.Date;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.DroidClientConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.ualberta.team17.QAModel;

public class NetworkDataManager implements IDataSourceManager {
	JestClientFactory mJestClientFactory;
	JestClient mJestClient;
	String mEsServerUrl;
	
	public NetworkDataManager(String esServerUrl) {
		mEsServerUrl = esServerUrl;
	}

	@Override
	public void query(DataFilter filter, IItemComparator comparator, IncrementalResult result) {
		if (null == mJestClient) {
			initJestClient();
		}
		
		ESSearchBuilder searchBuilder = new ESSearchBuilder(filter, comparator);
		Search search = new Search.Builder(searchBuilder.toString()).build();
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
}
