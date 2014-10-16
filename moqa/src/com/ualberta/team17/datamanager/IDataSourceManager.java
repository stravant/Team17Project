package com.ualberta.team17.datamanager;

import java.util.Date;

import com.ualberta.team17.IUnique;

public interface IDataSourceManager {
	public void query(IItemFilter filter, IncrementalResult result);
	public boolean saveItem(IUnique item);
	
	public boolean isAvailable();
	public Date getLastDataSourceAvailableTime();
	
	public void addDataLoadedListener(IDataLoadedListener listener);
	public void notifyDataItemLoaded(IUnique item);
	
	public void addDataSourceAvailableListener(IDataSourceAvailableListener listener);
	public void notifyDataSourceAvailable();
}
