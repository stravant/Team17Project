package com.ualberta.team17.datamanager;

/**
 * An IDataItemSavedListener can be used to listen for the completion of a save event on
 * a data source manager.
 * 
 * @author michaelblouin
 */
public interface IDataItemSavedListener {
	public void dataItemSaved(boolean success, Exception e);
}
