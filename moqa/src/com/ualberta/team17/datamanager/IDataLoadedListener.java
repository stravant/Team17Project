package com.ualberta.team17.datamanager;

import com.ualberta.team17.IQAModel;

public interface IDataLoadedListener {
	public void dataItemLoaded(IDataSourceManager manager, IQAModel item);
}
