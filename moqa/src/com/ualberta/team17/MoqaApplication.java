package com.ualberta.team17;

import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.UserContext;

import android.app.Application;
import android.content.res.Resources;

/**
 * MoqaApplication is the class that is responsible for setting up the data managers and controller
 * when the application initially launches.
 * 
 * @author michaelblouin
 */
public class MoqaApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		// Make local data manager, and set the data to the test data set
		LocalDataManager localDataManager = new LocalDataManager(getApplicationContext());
		
		// Make net data manager
		Resources resources = getResources();
		NetworkDataManager netDataManager = new NetworkDataManager(
				resources.getString(R.string.esProductionServer),
				resources.getString(R.string.esProductionIndex));
		
		// Make the manager
		DataManager dataManager = new DataManager(getApplicationContext(), localDataManager, netDataManager);

		// Finally instantiate the controller
		new QAController(dataManager);
	}
}
