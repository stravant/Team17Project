package com.ualberta.team17.datamanager;

import java.util.Date;

import com.ualberta.team17.IQAModel;

public interface IDataSourceManager {
	public void query(DataFilter filter, IncrementalResult result);
	public boolean saveItem(IQAModel item);
	
	public boolean isAvailable();
	public Date getLastDataSourceAvailableTime();
	
	public void addDataLoadedListener(IDataLoadedListener listener);
	public void notifyDataItemLoaded(IQAModel item);
	
	public void addDataSourceAvailableListener(IDataSourceAvailableListener listener);
	public void notifyDataSourceAvailable();
}
