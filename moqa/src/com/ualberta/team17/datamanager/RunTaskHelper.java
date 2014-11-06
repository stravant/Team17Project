package com.ualberta.team17.datamanager;

import android.os.AsyncTask;

/**
 * A helper class for doing a task on a separate thread
 * Usage: 
 *   new RunTaskHelper() {
 *       void task() {
 *           
 *       };
 *   };
 */
public abstract class RunTaskHelper {
	/**
	 * The task to be done, to be overridden
	 */
	public abstract void task();
	
	/**
	 * Private AsyncTask, used to run the thread
	 */
	private class RunTaskTask extends AsyncTask<Void, Void, Void> {
		/**
		 * Target task
		 */
		RunTaskHelper mTarget;
		
		/**
		 * Construct a RunTaskTask with the target task to run
		 * @param target
		 */
		public RunTaskTask(RunTaskHelper target) {
			mTarget = target;
		}
		
		@Override
		protected Void doInBackground(Void... input) {
			mTarget.task();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {}

		@Override
		protected void onPostExecute(Void result) {}
	}
	
	/**
	 * Constructor, starts a thread running task
	 */
	public RunTaskHelper() {
		new RunTaskTask(this).execute();
	}
}
