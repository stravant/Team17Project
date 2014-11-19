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
		//
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("favorites")) {
			mUserFavorites.add(UniqueId.fromString(item.getAsString()));
		}
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("local_only")) {
			mLocalOnlyItems.add(UniqueId.fromString(item.getAsString()));
		}
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("replies")) {
			mUserReplies.add(UniqueId.fromString(item.getAsString()));
		}
		for (JsonElement item: elem.getAsJsonObject().getAsJsonArray("recent")) {
			mRecentlyViewed.add(UniqueId.fromString(item.getAsString()));
		}
	}
	
	/**
	 * Write out the UserContext to a serialized copy
	 * @return
	 */
	public JsonElement saveToJson() {
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
		JsonObject obj = new JsonObject();
		obj.add("favorites", favoriteArray);
		obj.add("local_only", localOnlyArray);
		obj.add("replies", repliesArray);
		obj.add("recent", recentArray);
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
	private void maybeRemoveFromAllItemSet(UniqueId id) {
		if (!mRecentlyViewed.contains(id) &&
			!mUserReplies.contains(id) &&
			!mUserFavorites.contains(id)) 
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
			maybeRemoveFromAllItemSet(removed);
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
