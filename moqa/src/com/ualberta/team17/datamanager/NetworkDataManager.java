package com.ualberta.team17.datamanager;

import java.util.Date;

import com.ualberta.team17.IQAModel;
import com.ualberta.team17.datamanager.filters.DataFilter;

public class NetworkDataManager implements IDataSourceManager {

	@Override
	public void query(DataFilter filter, IncrementalResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean saveItem(IQAModel item) {
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
	public void notifyDataItemLoaded(IQAModel item) {
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
