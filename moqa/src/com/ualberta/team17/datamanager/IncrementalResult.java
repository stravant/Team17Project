package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.ItemType;

/*
 * A synchronization / collection object that represents the results of a query.
 * The worker tasks involved in the query will add results to the IncrementalResult
 * as they are found. When results are added in this way, the a set of observers will
 * be notified.
 */
public class IncrementalResult {
	/* ReentrantLock to avoid problems with an Observer calling back 
	 * to getCurrentResult/s within the notify call.
	 * Possibly can just be a normal lock.
	 */
	private ReentrantLock mModifyMutex = new ReentrantLock();
	
	// The list of items that have been added to this result.
	private ArrayList<QAModel> mResultList = new ArrayList<QAModel>();
	
	// Comparator to use on the items when inserting them.
	private IItemComparator mSort;
	
	// List of observers
	private ArrayList<ObserverEntry> mObserverList = new ArrayList<ObserverEntry>();
	
	/*
	 * Internal class to attach a desired item type to be notified on to a
	 * IIncrementalObserver.
	 */
	private class ObserverEntry {
		private IIncrementalObserver mObserver;
		private ItemType mTargetType; // null -> accept any
		
		// Construct with a filter
		public ObserverEntry(IIncrementalObserver observer, ItemType targetType) {
			mObserver = observer;
			mTargetType = targetType;
		}
		
		// Construct without a type filter
		public ObserverEntry(IIncrementalObserver observer) {
			this(observer, null);
		}
		
		// Try to notify the observer
		public void tryNotify(List<QAModel> entries, int index) {
			if (mTargetType != null) {
				// If we have a type of interest, then we have to notify on
				// The view of this result as a list of that type only.
				
				// Shift the index over to be an index into the list of only items
				// of the type we're interested in
				for (int i = 0; i < index; ++i)
					if (mResultList.get(i).getItemType() != mTargetType)
						--index;
				
				// Create a list of only the added entries of the type we want
				List<QAModel> notifyList = new ArrayList<QAModel>();
				for (QAModel entry: entries) {
					if (entry.getItemType() == mTargetType) {
						notifyList.add(entry);
					}
				}
				
				// Notify on that filtered list
				mObserver.itemsArrived(notifyList, index);
			} else {
				// Otherwise, just notify on the entries that we got
				mObserver.itemsArrived(entries, index);
			}
		}
	}
	
	/*
	 * Internal sort wrapper class that disambiguates non-reference equal items
	 * when the comparator that we are passed returns "equal" for them.
	 */
	private class DisambiguatingComparator implements IItemComparator {
		private IItemComparator mBaseComparator;
		
		public DisambiguatingComparator(IItemComparator sort) {
			mBaseComparator = sort;
		}

		@Override
		public int compare(QAModel lhs, QAModel rhs) {
			// First, for reference equality, return equal
			if (lhs == rhs)
				return 0;
			
			// Otherwise, compare them with the comparator, and 
			int result = mBaseComparator.compare(lhs, rhs);
			if (result == 0) {
				if (lhs.hashCode() == rhs.hashCode()) throw new AssertionError("IncrementalResult can't handle on-reference equal objects with identical hashCodes.");
				return (lhs.hashCode() < rhs.hashCode()) ? -1 : 1;	
			} else {
				return result;
			}
		}
	}
	
	// Constructor
	public IncrementalResult(IItemComparator sort) {
		mSort = new DisambiguatingComparator(sort);
	}
	
	// Helper, used by addObserver
	private void notifyOfCurrentResults(ObserverEntry entry) {
		if (!mResultList.isEmpty())
			entry.tryNotify(mResultList, 0);
	}
	
	/*
	 * Adds an observer, and calls it's notify method immediately for
	 * every currently present result.
	 */
	public void addObserver(IIncrementalObserver observer) {
		mModifyMutex.lock();
		ObserverEntry entry = new ObserverEntry(observer);
		mObserverList.add(entry);
		notifyOfCurrentResults(entry);
		mModifyMutex.unlock();
	}
	public void addObserver(IIncrementalObserver observer, ItemType type) {
		mModifyMutex.lock();
		ObserverEntry entry = new ObserverEntry(observer, type);
		mObserverList.add(entry);
		notifyOfCurrentResults(entry);
		mModifyMutex.unlock();
	}
	
	// Notify the observers that a(n) item(s) arrived.
	private void notifyObservers(List<QAModel> objects, int atIndex) {
		for (ObserverEntry entry: mObserverList) {
			entry.tryNotify(objects, atIndex);
		}
	}
	
	public void addObjects(List<QAModel> objects) {
		mModifyMutex.lock();
		
		// For each model in the things to add, add it to the list in a sorted position
		// Try to notify in chunks of multiple items where possible (when multiple items
		// are added to consecutive positions in the list)
		
		// Set up tracking for blocks of inserted entries to notify on
		int lastInsertAt = -2;
		int insertListStart = -2;
		ArrayList<QAModel> notifyList = new ArrayList<QAModel>();
		
		// For each item to add
		for (QAModel model: objects) {
			// Find out where to add it via binary search
			int foundIndex = Collections.binarySearch(mResultList, model, mSort);
			//Log.i("test", "FoundIndex: " + foundIndex);
			if (foundIndex >= 0) {
				// Already has the item, nothing to do
			} else {
				int insertIndex = (-foundIndex) - 1;
				
				// If it follows the last item inserted in a logical block, then add it to the notify list
				if (insertIndex == lastInsertAt + 1) {
					++lastInsertAt;
					notifyList.add(model);
				} else {
					// Otherwise, notify on the current notify list, and start a new notify list with
					// the current item in it.
					if (!notifyList.isEmpty())
						notifyObservers(notifyList, insertListStart);
					notifyList.clear();
					notifyList.add(model);
					lastInsertAt = insertIndex;
					insertListStart = lastInsertAt;
				}
				mResultList.add(insertIndex, model);
			}
		}
		
		// Notify on the remaining notify list if there's any remaining items
		if (!notifyList.isEmpty())
			notifyObservers(notifyList, insertListStart);
		
		mModifyMutex.unlock();
	}
	
	// Get a list of the current results of the given type
	public List<QAModel> getCurrentResultsOfType(ItemType type) {
		mModifyMutex.lock();
		ArrayList<QAModel> results = new ArrayList<QAModel>();
		for (QAModel result: mResultList) {
			if (result.getItemType() == type) {
				results.add(result);
			}
		}
		mModifyMutex.unlock();
		return results;
	}
	
	// Gets a copy of the current result list
	public List<QAModel> getCurrentResults() {
		return new ArrayList<QAModel>(mResultList);
	}
}
