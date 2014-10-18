package com.ualberta.team17.datamanager;

import java.util.Date;

import com.ualberta.team17.QAModel;

public class LocalDataManager implements IDataSourceManager {

	@Override
	public void query(DataFilter filter, IncrementalResult result) {
		throw new UnsupportedOperationException();
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
}
