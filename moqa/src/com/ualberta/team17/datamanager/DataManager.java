package com.ualberta.team17.datamanager;

import java.util.List;

import com.ualberta.team17.QAModel;

public class DataManager {
	private UserContext mContext;
	private List<IDataSourceManager> mDataManagers;

	public IncrementalResult doQuery(DataFilter filter, IItemComparator sortComparator) {
		throw new UnsupportedOperationException();
	}

	public void saveItem(QAModel item) {
		throw new UnsupportedOperationException();
	}

	public void setUserContext(UserContext context) {
		mContext = context;
	}

	public UserContext getUserContext() {
		return mContext;
	}
}
