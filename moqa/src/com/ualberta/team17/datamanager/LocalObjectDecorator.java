package com.ualberta.team17.datamanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;

/**
 * Tracks local objects and decorates them with derived info.
 * @author stravant
 */
public class LocalObjectDecorator {
	/**
	 * Pointers to the items that we have processed, items which may or may not be
	 * currently locally stored, but which have come our way through saveItem() calls.
	 */
	private HashMap<UniqueId, QAModel> mItemRefById = new HashMap<UniqueId, QAModel>();
	
	private UserContext mUserContext = null;
	
	public LocalObjectDecorator() {
		/* Nothing to do on construction */
	}
	
	/**
	 * Used at startup by the Initial Data Query. Calculates the current 
	 * derived info for all of the {@link QAModel} items that were loaded. 
	 * Examples of derived info: Upvote Count & Reply Count
	 */
	private void calculateInitialDerivedInfo() {
		// Initially all of the items will have UpvoteCount and ReplyCount = 0
		// when they are first constructed.
		// That means that we just have to iterate through all of the items and
		// tally them to their parent item if it exists.
		// That is, we call handleDerivedInfo on each of our items... elegant!
		for (QAModel item: mItemRefById.values()) {
			updateParentDerivedInfoIfHasParent(item);
			item.calculateInitialDerivedInfo(mUserContext);
		}
	}
	
	/**
	 * Apply an item to its parent items derived info, that is, if it is
	 * an upvote, add an upvote to its parent's derived info, and if it is
	 * an answer, add to its parent's reply count.
	 * @param item
	 */
	private void updateParentDerivedInfoIfHasParent(QAModel item) {
		UniqueId parentId = (UniqueId)item.getField(AuthoredTextItem.FIELD_PARENT);
		if (parentId != null) {
			QAModel parentItem = mItemRefById.get(parentId);
			if (parentItem != null) {
				// If the item has a parent, then delegate to it to do the derived info calculation
				item.addToParentDerivedInfo(mUserContext, parentItem);
			}
		}
	}
	
	/**
	 * Handle the derived information related to an item
	 * that has just been added, updating it's parent's info
	 * if it has a parent, and adding already present children's
	 * info to this item.
	 * @param item
	 */
	private void handleDerivedInfo(QAModel item) {
		updateParentDerivedInfoIfHasParent(item);
		
		// If this is the parent of any current items, update this with those children's data
		for (QAModel child: mItemRefById.values()) {
			if (child instanceof AuthoredItem) {
				QAModel parent = mItemRefById.get(((AuthoredItem)child).getParentItem());
				if (parent == item) {
					child.addToParentDerivedInfo(mUserContext, item);
				}
			}
		}
		
		// Check favorited
		if (mUserContext != null) {
			Log.i("save", "Checking derived info <" + item.getUniqueId() + ">: " + 
					mUserContext + ", " + item.getClass().getName() + ", " + mUserContext.isFavorited(item.getUniqueId()));
		}
		if (mUserContext != null && (item instanceof QuestionItem) && mUserContext.isFavorited(item.getUniqueId())) {
			((QuestionItem)item).setFavorited();
		}
	}
	
	/**
	 * Begin tracking a given QAModel item
	 * @param item
	 */
	public void track(QAModel item) {
		if (!mItemRefById.containsKey(item.getUniqueId())) {
			// If not tracked, begin tracking
			mItemRefById.put(item.getUniqueId(), item);
			
			// Apply derived info
			handleDerivedInfo(item);
		} else {
			// Copy derived info
			if (mItemRefById.get(item.getUniqueId()) != item) {
				item.copyDerivedInfo(mItemRefById.get(item.getUniqueId()));
			}
		}
	}
	
	/**
	 * Initial tracking call, done first
	 */
	public void trackInitial(List<QAModel> initialItems) {
		for (QAModel item: initialItems) {
			mItemRefById.put(item.getUniqueId(), item);
		}
		
		// Apply initial derived info
		calculateInitialDerivedInfo();
	}
	
	/**
	 * Get a tracked item with a given Id if it is currently tracked
	 * @param id
	 * @return
	 */
	public QAModel get(UniqueId id) {
		return mItemRefById.get(id);
	}
	
	/**
	 * Get all of the items currently tracked
	 * @return
	 */
	public Collection<QAModel> getAllTrackedItems() {
		return mItemRefById.values();
	}
	
	/**
	 * Set the user context being used
	 */
	public void setUserContext(UserContext ctx) {
		mUserContext = ctx;
		
		// Update all of our current data items to be favorited if they are favorited in the user context
		for (UniqueId id: ctx.getFavorites()) {
			QAModel item = mItemRefById.get(id);
			if (item != null && item instanceof QuestionItem) {
				((QuestionItem)item).setFavorited();
			}
		}
		
		// For each of our upvote items, mark hasUpvoted on it's parent
		for (QAModel item: mItemRefById.values()) {
			if (item instanceof UpvoteItem) {
				UpvoteItem upvote = ((UpvoteItem)item);
				if (upvote.getAuthor().equals(mUserContext.getUserName())) {
					AuthoredTextItem parent = (AuthoredTextItem)mItemRefById.get(upvote.getParentItem());
					if (parent != null) {
						parent.setHaveUpvoted();
					}
				}
			}
		}
	}
}
