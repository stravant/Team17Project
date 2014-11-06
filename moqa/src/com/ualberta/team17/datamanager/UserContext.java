package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

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
	
	/**
	 * The user's name
	 */
	private String mUserName;
	
	/**
	 * The user's favorites
	 */
	private List<UniqueId> mUserFavorites;
	
	/**
	 * The user's replies to content
	 */
	private List<UniqueId> mUserReplies;
	
	/**
	 * The items that we have stored locally, waiting to be
	 * pushed to the network when we next connect.
	 */
	private List<UniqueId> mLocalOnlyItems;
	
	public UserContext() {
		mUserFavorites = new ArrayList<UniqueId>();
		mUserReplies = new ArrayList<UniqueId>();
		mLocalOnlyItems = new ArrayList<UniqueId>();
	}
	
	/**
	 * Construct a user context from the name of a user
	 * @param username The username of the user
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
		//
		for (JsonElement item: elem.getAsJsonObject().get("favorites").getAsJsonArray()) {
			mUserFavorites.add(UniqueId.fromString(item.getAsString()));
		}
		for (JsonElement item: elem.getAsJsonObject().get("local_only").getAsJsonArray()) {
			mLocalOnlyItems.add(UniqueId.fromString(item.getAsString()));
		}
		for (JsonElement item: elem.getAsJsonObject().get("replies").getAsJsonArray()) {
			mUserReplies.add(UniqueId.fromString(item.getAsString()));
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
		JsonObject obj = new JsonObject();
		obj.add("favorites", favoriteArray);
		obj.add("local_only", localOnlyArray);
		obj.add("replies", repliesArray);
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
		if (!mUserFavorites.contains(itemId))
			mUserFavorites.add(itemId);
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
		if (!mUserReplies.contains(itemId))
			mUserReplies.add(itemId);
	}
}
