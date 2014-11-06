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
public abstract class RunTaskHelper<Res> {
	/**
	 * The task to be done, to be overridden
	 */
	public abstract Res task();
	
	/**
	 * The process function on the original thread
	 */
	public void done(Res result) {}
	
	/**
	 * Private AsyncTask, used to run the thread
	 */
	private class RunTaskTask extends AsyncTask<Void, Void, Res> {
		/**
		 * Target task
		 */
		RunTaskHelper<Res> mTarget;
		
		/**
		 * Construct a RunTaskTask with the target task to run
		 * @param target
		 */
		public RunTaskTask(RunTaskHelper<Res> target) {
			mTarget = target;
		}
		
		@Override
		protected Res doInBackground(Void... input) {
			return mTarget.task();
		}

		@Override
		protected void onProgressUpdate(Void... progress) {}

		@Override
		protected void onPostExecute(Res result) {
			mTarget.done(result);
		}
	}
	
	/**
	 * Constructor, starts a thread running task
	 */
	public RunTaskHelper() {
		new RunTaskTask(this).execute();
	}
}
