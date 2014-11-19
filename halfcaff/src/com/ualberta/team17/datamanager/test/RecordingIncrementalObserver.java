package com.ualberta.team17.datamanager.test;

import java.util.ArrayList;
import java.util.List;


import com.ualberta.team17.QAModel;
import com.ualberta.team17.datamanager.IIncrementalObserver;

/*
 * Dummy Incremental Observer that simply records every call to itemsArrived()
 * and stores those records in an array that can be accessed.
 * For the purposes of testing incremental result notification.
 */
public class RecordingIncrementalObserver implements IIncrementalObserver {
	private List<NotificationRecord> mNotificationRecordList = new ArrayList<NotificationRecord>();
	
	public class NotificationRecord {
		public List<QAModel> List;
		public int Index;
	}
	
	public RecordingIncrementalObserver() {
	}

	@Override
	public void itemsArrived(List<QAModel> itemList, int index) {
		NotificationRecord record = new NotificationRecord();
		record.List = new ArrayList<QAModel>(itemList);
		record.Index = index;
		mNotificationRecordList.add(record);
	}
	
	public boolean wasNotified() {
		return getNotificationCount() > 0;
	}

	public int getNotificationCount() {
		return mNotificationRecordList.size();
	}
	
	public NotificationRecord getNotification(int index) {
		return mNotificationRecordList.get(index);
	}

}
