package com.ualberta.team17.datamanager;

import com.ualberta.team17.QAModel;

public interface IDataLoadedListener {
	public void dataItemLoaded(IDataSourceManager manager, QAModel item);
}
