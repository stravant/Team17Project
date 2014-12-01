package com.ualberta.team17.datamanager;

/**
 * The IDataSourceAvailableListener can be used to listen for changes in the 
 * availability of a data source.
 * 
 * @author michaelblouin
 */
public interface IDataSourceAvailableListener {
	public void DataSourceAvailable(IDataSourceManager manager);
}
