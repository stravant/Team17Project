package com.ualberta.team17.datamanager;

import java.util.Date;

import com.ualberta.team17.QAModel;

public class LocalDataManager implements IDataSourceManager {

	@Override
	public boolean saveItem(QAModel item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Date getLastDataSourceAvailableTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDataLoadedListener(IDataLoadedListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void notifyDataItemLoaded(QAModel item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDataSourceAvailableListener(
			IDataSourceAvailableListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyDataSourceAvailable() {
		// TODO Auto-generated method stub

	}

}
