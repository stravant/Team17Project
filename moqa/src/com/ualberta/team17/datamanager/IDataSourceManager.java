package com.ualberta.team17.datamanager;

import java.util.Date;
import java.util.List;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;

/**
 * A source of QAModel data for the DataManager to work with.
 * Asynchronously handles querying for or saving items.
 */
public interface IDataSourceManager {
	/**
	 * Query for items using a filter & sort approach
	 * @param filter
	 * @param comparator
	 * @param result The IncrementalResult to put the results in
	 */
	public void query(DataFilter filter, IItemComparator comparator, IncrementalResult result);
	
	/**
	 * Query for specific known items using a list of IDs of those items.
	 * @param ids The ids to get 
	 * @param result The IncrementalResult to put the results in.
	 */
	public void query(List<UniqueId> ids, IncrementalResult result);
	
	/**
	 * Save an item to our source. Note: This is a SYNCHRONOUS method, which
	 * may have a long running time, it should not be called on the UI thread.
	 * @param item
	 * @return Whether the operation was successful
	 */
	public boolean saveItem(QAModel item);
	
	/**
	 * Availability: Is this source currently available?
	 * @return Whether the source is available as a boolean
	 */
	public boolean isAvailable();
	
	/**
	 * Availability: When did this source last successfully return data?
	 * @return The Date that data was last successfully returned
	 */
	public Date getLastDataSourceAvailableTime();
	
	/**
	 * Add a listener that is notified when an item is loaded by this source.
	 * @param listener
	 */
	public void addDataLoadedListener(IDataLoadedListener listener);
	
	/**
	 * Add a listener to notify when this source becomes available.
	 * Also called immediately if the source is already available.
	 * @param listener
	 */
	public void addDataSourceAvailableListener(IDataSourceAvailableListener listener);
}