package com.ualberta.team17.datamanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
		mUserReplies.add(itemId);
	}
}
