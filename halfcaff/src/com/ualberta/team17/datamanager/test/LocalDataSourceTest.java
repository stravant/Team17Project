package com.ualberta.team17.datamanager.test;

import android.test.ActivityTestCase;

import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.UserContext;

public class LocalDataSourceTest extends ActivityTestCase {
	final Integer maxWaitSeconds = 5;
	DataFilter dataFilter;
	IncrementalResult result;
	LocalDataManager dataManager;
	
	public void setUp() {
		dataManager = new LocalDataManager(getActivity(), new UserContext("test_user"));
		dataFilter = new DataFilter();
	}
	
	public void test_SaveLoad_Cycle() {
		
	}
}
