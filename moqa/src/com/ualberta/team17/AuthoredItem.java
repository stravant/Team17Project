package com.ualberta.team17;

import java.util.Date;

/*
 * An AuthoredItem is a item that a user posted to the service at some date.
 * Optionally, the item was posted in reply to another item on the service.
 */
public abstract class AuthoredItem extends QAModel {
	private UniqueId mParentItem;
	private String mAuthor;
	private Date mDate;
	
	// Storage policy, to be set 
	private transient StoragePolicy mStoragePolicy;
	
	/* Ctor */
	public AuthoredItem(ItemType type, UniqueId id, UniqueId parentId, String author, Date date) {
		super(type, id);
		mParentItem = parentId;
		mAuthor = author;
		mDate = date;
		mStoragePolicy = StoragePolicy.Inherit;
	}
	
	/* Getters */
	public String getAuthor() {
		return mAuthor;
	}
	public Date getDate() {
		return mDate;
	}
	public UniqueId getItemId() {
		return mItemId;
	}
	public UniqueId getParentItem() {
		return mParentItem;
	}
	
	/* Storage policy */
	public StoragePolicy getStoragePolicy() {
		return mStoragePolicy;
	}
	public void setStoragePolicy(StoragePolicy policy) {
		mStoragePolicy = policy;
	}
}
