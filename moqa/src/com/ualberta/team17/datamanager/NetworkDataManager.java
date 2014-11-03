package com.ualberta.team17.datamanager;

import java.util.Date;
import java.util.List;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;

public class NetworkDataManager implements IDataSourceManager {

	public NetworkDataManager(String a, String b) {
		
	}
	
	@Override
	public void query(DataFilter filter, IItemComparator sort, IncrementalResult result) {
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
	public void addDataSourceAvailableListener(
			IDataSourceAvailableListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void query(List<UniqueId> ids, IncrementalResult result) {
		// TODO Auto-generated method stub
		
	}
}
