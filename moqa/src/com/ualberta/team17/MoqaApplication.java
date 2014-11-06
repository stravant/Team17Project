package com.ualberta.team17;

import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.UserContext;

import android.app.Application;
import android.content.res.Resources;

public class MoqaApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		// Make local data manager, and set the data to the test data set
		LocalDataManager localDataManager = new LocalDataManager(getApplicationContext());
		
		// Make net data manager
		Resources resources = getResources();
		NetworkDataManager netDataManager = new NetworkDataManager(
				resources.getString(R.string.esTestServer),
				resources.getString(R.string.esTestIndex));
		
		// Make the manager
		DataManager dataManager = new DataManager(getApplicationContext(), localDataManager, netDataManager);

		// Finally instantiate the controller
		new QAController(dataManager);
	}
}
