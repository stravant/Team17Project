package com.ualberta.team17.datamanager;

import java.io.Serializable;
import java.util.List;

import com.ualberta.team17.UniqueId;

/*
 * The information about a logged in user, that is private to that user, and
 * not public information on the site.
 */
public class UserContext implements Serializable {
	private static final long serialVersionUID = -4375680346934596406L;
	
	private String mUserName;
	private List<UniqueId> mUserFavorites;
	private List<UniqueId> mUserReplies;
	
	public UserContext(String username) {
		mUserName = username;
	}
	
	public String getUserName() {
		return mUserName;
	}
	
	public List<UniqueId> getFavorites() {
		return mUserFavorites;
	}
	
	public void addFavorite(UniqueId itemId) {
		mUserFavorites.add(itemId);
	}
	
	public List<UniqueId> getReplies() {
		return mUserReplies;
	}
	
	public void addReply(UniqueId itemId) {
		mUserReplies.add(itemId);
	}
}
