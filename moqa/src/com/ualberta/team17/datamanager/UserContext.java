package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;

/*
 * The information about a logged in user, that is private to that user, and
 * not public information on the site.
 */
public class UserContext implements Serializable {
	private static final long serialVersionUID = -4375680346934596406L;
	
	public static final int MAX_RECENT = 20;
	
	/**
	 * The user's name
	 */
	private String mUserName;
	
	/**
	 * Is the user context currently tracking a given item
	 * in any of it's lists?
	 */
	private Set<UniqueId> mAllItemSet;
	
	/**
	 * The user's favorites
	 */
	private List<UniqueId> mUserFavorites;
	
	/**
	 * The user's replies to content
	 */
	private List<UniqueId> mUserReplies;
	
	/**
	 * Recently viewed items
	 */
	private List<UniqueId> mRecentlyViewed;
	
	/**
	 * To view later items
	 */
	private List<UniqueId> mViewLater;
	
	/**
	 * The items that we have stored locally, waiting to be
	 * pushed to the network when we next connect.
	 */
	private List<UniqueId> mLocalOnlyItems;
	
	/**
	 * Local only items lock, as LocalOnlyItems may be asynchronously
	 * saved out to the network, causing them to be removed from the
	 * tracking list without a corresponding user action ocurring.
	 */
	private ReadWriteLock mLocalOnlyItemsLock = new ReentrantReadWriteLock();
	
	/**
	 * Constructor, creates a new blank user context
	 */
	public UserContext() {
		mAllItemSet = new HashSet<UniqueId>();
		mUserFavorites = new ArrayList<UniqueId>();
		mUserReplies = new ArrayList<UniqueId>();
		mLocalOnlyItems = new ArrayList<UniqueId>();
		mRecentlyViewed = new ArrayList<UniqueId>();
		mViewLater = new ArrayList<UniqueId>();
	}
	
	/**
	 * Construct a user context from the name of a user
	 * @param username The username of the userobject
	 */
	public UserContext(String username) {
		this();
		mUserName = username;
	}
	
	/**
	 * Load in the UserContext from a serialized copy
	 * @param elem
	 */
	public void loadFromJson(JsonElement elem) {
		mUserFavorites.clear();
		mLocalOnlyItems.clear();
		mUserReplies.clear();
		mRecentlyViewed.clear();
		mViewLater.clear();
		
		// Read in sets of items
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("favorites")) {
			final UniqueId id = UniqueId.fromString(item.getAsString());
			mUserFavorites.add(id);
			mAllItemSet.add(id);
		}
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("local_only")) {
			mLocalOnlyItems.add(UniqueId.fromString(item.getAsString()));
		}
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("replies")) {
			final UniqueId id = UniqueId.fromString(item.getAsString());
			mUserReplies.add(id);
			mAllItemSet.add(id);
		}
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("recent")) {
			final UniqueId id = UniqueId.fromString(item.getAsString());
			mRecentlyViewed.add(id);
			mAllItemSet.add(id);
		}
		
		// View later items was added later, outdated test data sets may not have
		// a set of view later items.
		JsonArray laterArray = elem.getAsJsonObject().getAsJsonArray("later");
		if (laterArray != null) {
			for (JsonElement item: laterArray) {
				final UniqueId id = UniqueId.fromString(item.getAsString());
				mViewLater.add(id);
				mAllItemSet.add(id);
			}
		}
	}
	
	/**
	 * Write out the UserContext to a serialized copy
	 * @return
	 */
	public JsonElement saveToJson() {
		// Build arrays
		JsonArray favoriteArray = new JsonArray();
		for (UniqueId item: mUserFavorites) {
			favoriteArray.add(new JsonPrimitive(item.toString()));
		}
		JsonArray localOnlyArray = new JsonArray();
		for (UniqueId item: mLocalOnlyItems) {
			localOnlyArray.add(new JsonPrimitive(item.toString()));
		}
		JsonArray repliesArray = new JsonArray();
		for (UniqueId item: mUserReplies) {
			repliesArray.add(new JsonPrimitive(item.toString()));
		}
		JsonArray recentArray = new JsonArray();
		for (UniqueId item: mRecentlyViewed) {
			recentArray.add(new JsonPrimitive(item.toString()));
		}
		JsonArray laterArray = new JsonArray();
		for (UniqueId item: mViewLater) {
			laterArray.add(new JsonPrimitive(item.toString()));
		}
		
		// Build main object from the arrays
		JsonObject obj = new JsonObject();
		obj.add("favorites", favoriteArray);
		obj.add("local_only", localOnlyArray);
		obj.add("replies", repliesArray);
		obj.add("recent", recentArray);
		obj.add("later", laterArray);
		
		return obj;
	}
	
	/**
	 * Get a unique ID representing this user
	 * @return The Unique ID
	 */
	public UniqueId getUserId() {
		return new UniqueId(mUserName);
	}
	
	/**
	 * Is an item "interesting", that is, is it either
	 * recently viewed, favorited, or an item that we have
	 * posted?
	 */
	public boolean isInteresting(UniqueId id) {
		return mAllItemSet.contains(id);
	}
	
	/**
	 * Get the user's name
	 * @return The user's name
	 */
	public String getUserName() {
		return mUserName;
	}
	
	/**
	 * Get a list of the IDs of the user's favorites
	 * @return List of UniqueIds of the user's favorites
	 */
	public List<UniqueId> getFavorites() {
		return mUserFavorites;
	}
	
	/**
	 * Is a given item favorited?
	 */
	public boolean isFavorited(UniqueId id) {
		return mUserFavorites.contains(id);
	}
	
	/**
	 * Add a favorite to the user's list of favorites by UniqueId
	 * @param itemId
	 */
	public void addFavorite(UniqueId itemId) {
		if (!mUserFavorites.contains(itemId)) {
			mUserFavorites.add(itemId);
			mAllItemSet.add(itemId);
		}
	}
	
	/**
	 * Get view later items
	 * @return The view later items
	 */
	public List<UniqueId> getViewLater() {
		return mViewLater;
	}
	
	/**
	 * Add a view later item. If the item was already marked as
	 * view later, then it will be re-inserted at the start of the
	 * view later list.
	 * @param itemId The item to add
	 */
	public void addViewLater(UniqueId itemId) {
		// Remove the item if it exists
		mViewLater.remove(itemId);
		
		// Insert at start of list since you're probably most interested in
		// the view later items that you saved most recently.
		mViewLater.add(0, itemId);
	}
	
	/**
	 * Getter for view later
	 * @return Whether the item should be viewed later
	 */
	public boolean shouldViewLater(UniqueId item) {
		return mViewLater.contains(item);
	}
	
	/**
	 * Remove a view later item (To be called when it is... well,
	 * viewed later).
	 */
	public void removeViewLater(UniqueId itemId) {
		mViewLater.remove(itemId);
		
		// The view later list may or may not have been the last reference
		// to that itemId, so if there are no more references remove it from
		// the all item set.
		removeFromAllItemsIfNotReferenced(itemId);
	}
	
	/**
	 * Get the replies content that this user has authored
	 * @return
	 */
	public List<UniqueId> getReplies() {
		return mUserReplies;
	}
	
	/**
	 * Add a reply that this user has authored.
	 * @param itemId
	 */
	public void addReply(UniqueId itemId) {
		if (!mUserReplies.contains(itemId)) {
			mUserReplies.add(itemId);
			mAllItemSet.add(itemId);
		}
	}
	
	/**
	 * Check if an item is referenced in any of our lists, and if not
	 * remove it from the allItemSet
	 */
	private void removeFromAllItemsIfNotReferenced(UniqueId id) {
		if (!mRecentlyViewed.contains(id) &&
			!mUserReplies.contains(id) &&
			!mUserFavorites.contains(id) &&
			!mViewLater.contains(id)) 
		{
			mAllItemSet.remove(id);
		}
	}
	
	/**
	 * Mark an item as the most recently viewed one.
	 * @param itemId
	 */
	public void addRecentItem(UniqueId itemId) {
		// Remove the item if it was already in the recently viewed
		Iterator<UniqueId> it = mRecentlyViewed.iterator();
		while (it.hasNext()) {
			UniqueId id = it.next();
			if (id.equals(itemId)) {
				it.remove();
				break;
			}
		}
		
		// Insert the item at the start
		mRecentlyViewed.add(0, itemId);
		mAllItemSet.add(itemId);
		
		// If there are extra items, remove them
		while (mRecentlyViewed.size() > MAX_RECENT) {
			UniqueId removed = mRecentlyViewed.remove(mRecentlyViewed.size() - 1);
			
			// Check if the removed item is still in the all item set
			removeFromAllItemsIfNotReferenced(removed);
		}
	}
	
	/**
	 * Get the recently viewed items
	 */
	public List<UniqueId> getRecentItems() {
		return mRecentlyViewed;
	}
	
	/**
	 * Add a local only item (item that has not been saved yet)
	 * For use by the DataManager
	 */
	public void addLocalOnlyItem(UniqueId id) {
		mLocalOnlyItemsLock.writeLock().lock();
		mLocalOnlyItems.add(id);
		mLocalOnlyItemsLock.writeLock().unlock();
	}
	
	/**
	 * Remove a local only item
	 * For use by the DataManager
	 */
	public void removeLocalOnlyItem(UniqueId id) {
		mLocalOnlyItemsLock.writeLock().lock();
		mLocalOnlyItems.remove(id);
		mLocalOnlyItemsLock.writeLock().unlock();
	}
	
	/**
	 * Get local only items
	 * For use by the DataManager
	 */
	public List<UniqueId> getLocalOnlyItems() {
		mLocalOnlyItemsLock.readLock().lock();
		List<UniqueId> items = new ArrayList<UniqueId>(mLocalOnlyItems);
		mLocalOnlyItemsLock.readLock().unlock();
		return items;
	}
	
	/**
	 * Clear the local only items
	 * For use by the DataManager
	 */
	public void clearLocalOnlyItems() {
		mLocalOnlyItemsLock.writeLock().lock();
		mLocalOnlyItems.clear();
		mLocalOnlyItemsLock.writeLock().unlock();
	}
}
