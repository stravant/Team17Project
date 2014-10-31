package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.provider.VoicemailContract;

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
	 * @param result
	 */
	public void query(DataFilter filter, IItemComparator comparator, IncrementalResult result);
	
	/**
	 * Query for specific items using a list of IDs of those items.
	 * @param ids
	 */
	public void query(List<UniqueId> ids);
	
	/**
	 * Save an item to our source
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
	
	
	public void addDataLoadedListener(IDataLoadedListener listener);
	public void notifyDataItemLoaded(QAModel item);
	
	public void addDataSourceAvailableListener(IDataSourceAvailableListener listener);
	public void notifyDataSourceAvailable();
}