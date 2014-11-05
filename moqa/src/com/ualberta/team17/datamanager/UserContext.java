package com.ualberta.team17.datamanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

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
	 * Construct a user context from the name of a user
	 * @param username The username of the user
	 */
	public UserContext(String username) {
		mUserName = username;
		mUserFavorites = new ArrayList<UniqueId>();
		mUserReplies = new ArrayList<UniqueId>();
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
	
	
	/**
	 * Get the name of the file that we are using to store the
	 * settings in
	 * @return The name of the file
	 */
	private String getDataLocationName() {
		// Use the user's Id as the location
		return "TestFile"; //getUserId().toString();
	}
	
	/**
	 * Get the file a handle to the source of settings data
	 * @param ctx The context to get the source from
	 * @return A file, that is the location to read from
	 *  given the current Context and UserContext. 
	 *  If the user hasn't saved any data yet, return null
	 */
	public FileInputStream getLocalDataSource(Context ctx, String category) {
		try {
			return ctx.openFileInput(getDataLocationName() + "_" + category);
		} catch (FileNotFoundException e) {
			Log.e("app", "Data Source file not found!:" + e.getMessage());
			// File was not found, we return null, letting the caller
			// create a file if they want to.
			return null;
		}
	}
	
	/**
	 * Destination to save changes to, given the current Context and UserContext
	 * @return A file, that is the location to write
	 *  to given the current Context and UserContext
	 */
	public FileOutputStream getLocalDataDestination(Context ctx, String category) {
		try {
			return ctx.openFileOutput(getDataLocationName() + "_" + category, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			throw new Error("Fatal Error: Can't write to application directory");
		}
	}
}
