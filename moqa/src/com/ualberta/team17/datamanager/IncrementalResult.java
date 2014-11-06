package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.ItemType;

/**
 * A synchronized ordered collection object that represents the results of a
 * query to the application's data source(s).
 * Results may arrive in any order, and the IncrementalResult notifies observers
 * that results have arrived, and in what position in the result when they do
 * arrive.
 * The IncrementalResult also supports type filtering. If a query would result in
 * multiple types of results being returned, both the getter and notification
 * functions of the IncremencalResult come in type-filtered variants as well.
 */
public class IncrementalResult {
	/**
	 * Main Lock used by an IncrementalResult for synchronization purposes.
	 * While locked, no reads or writes can be done to the IncrementalResult. 
	 */
	private ReentrantLock mModifyMutex = new ReentrantLock();
	
	/**
	 * The main list of items, storing what items have been added to this
	 * IncrementalResult.
	 */
	private ArrayList<QAModel> mResultList = new ArrayList<QAModel>();
	
	/**
	 * The comparator that this IncrementalResult should sort it's results
	 * using.
	 */
	private IItemComparator mSort;
	
	/**
	 * The observers of this IncrementalResult, to be notified when items
	 * are added. A ObserverEntries instead of raw IIncrementalObservers are 
	 * used, so as to store both the observer and what type of item it wants 
	 * to observe if any.
	 */
	private ArrayList<ObserverEntry> mObserverList = new ArrayList<ObserverEntry>();
	
	/**
	 * Internal class used to record both an IIncrementalObserver that is
	 * observing us, and if/what items if wants to filter being notified on. 
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
	
	/**
	 * Internal sort wrapper class that disambiguates non-reference equal items
	 * when the comparator that we are passed returns "equal" for them, since
	 * we need them to be non-equal for the purposes of inserting them into our
	 * internal result array. 
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
				// If they are equal, force the items to the end of the array (less than all)
				return 1;
			} else {
				return result;
			}
		}

		@Override
		public void setCompareDirection(SortDirection direction) {
			throw new UnsupportedOperationException("Compare direction not supported on disambiguating comparator.");
		}

		@Override
		public SortDirection getCompareDirection() {
			throw new UnsupportedOperationException("Compare direction not supported on disambiguating comparator.");
		}

		@Override
		public String getFilterField() {
			throw new UnsupportedOperationException("Filter field not supported on disambiguating comparator.");
		}
	}
	
	/**
	 * Constructor, from a comparator.
	 */
	public IncrementalResult(IItemComparator sort) {
		mSort = new DisambiguatingComparator(sort);
	}
	
	/**
	 * Notify an observer of already existing items, to be called when
	 * adding a new observer while we already have results.
	 * @param entry The ObserverEntry to notify of the current results.
	 */
	private void notifyOfCurrentResults(ObserverEntry entry) {
		if (!mResultList.isEmpty())
			entry.tryNotify(mResultList, 0);
	}
	
	/**
	 * Adds an observer, and calls it's notify method immediately for
	 * every currently present result before returning.
	 * Once added, the observer will be notified when any items are added
	 * to the IncrementalResult.
	 * @param observer The observer to notify.
	 */
	public void addObserver(IIncrementalObserver observer) {
		mModifyMutex.lock();
		ObserverEntry entry = new ObserverEntry(observer);
		mObserverList.add(entry);
		notifyOfCurrentResults(entry);
		mModifyMutex.unlock();
	}
	
	/**
	 * Adds an observer, but only notifies it of results with a given item 
	 * type rather than all results. 
	 * @see addObserver(IIncrementalObserver obs)
	 * @param observer The observer to notify
	 * @param type     The type of items to notify the observer on
	 */
	public void addObserver(IIncrementalObserver observer, ItemType type) {
		mModifyMutex.lock();
		ObserverEntry entry = new ObserverEntry(observer, type);
		mObserverList.add(entry);
		notifyOfCurrentResults(entry);
		mModifyMutex.unlock();
	}
	
	// Notify the observers that a(n) item(s) arrived.
	/**
	 * Helper function to notify the observers that a contiguous group of items 
	 * has arrived at a given index in the result list.
	 * @param objects The objects that arrived
	 * @param atIndex What index the block of objects is at in the result list
	 */
	private void notifyObservers(List<QAModel> objects, int atIndex) {
		for (ObserverEntry entry: mObserverList) {
			entry.tryNotify(objects, atIndex);
		}
	}
	
	/**
	 * Main public interface used by producers to add objects to the IncrementalResult.
	 * The objects may be passed in in any order, the IncrementalResult will
	 * handle sorting them and grouping notifications together in a nice way.
	 * @param objects The objects to add
	 */
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
			if (foundIndex >= 0) {
				// Already has the item, nothing to do
			} else {
				int insertIndex = (-foundIndex) - 1;
				
				// Add the item to our result list
				mResultList.add(insertIndex, model);
				
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
			}
		}
		
		// Notify on the remaining notify list if there's any remaining items
		if (!notifyList.isEmpty())
			notifyObservers(notifyList, insertListStart);
		
		mModifyMutex.unlock();
	}
	
	/**
	 * Get the current list of results, only including items of a specific
	 * type, in sorted order.
	 * @param type The type of items to include
	 * @return The items of that type
	 */
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
	
	/**
	 * Get all of the current results, in sorted order.
	 * @return All of the items currently in the result
	 */
	public List<QAModel> getCurrentResults() {
		return new ArrayList<QAModel>(mResultList);
	}
}
