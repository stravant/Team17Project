package com.ualberta.team17;

import java.util.Date;

/*
 * An AuthoredItem is a item that a user posted to the service at some date.
 * Optionally, the item was posted in reply to another item on the service.
 */
public abstract class AuthoredItem implements IUnique {
	private UniqueId mItemId;
	private UniqueId mParentItem;
	private String mAuthor;
	private Date mDate;
	
	// Storage policy, to be set 
	private transient StoragePolicy mStoragePolicy;
	
	public AuthoredItem(UniqueId id, UniqueId parentId, String author, Date date) {
		mItemId = id;
		mParentItem = parentId;
		mAuthor = author;
		mDate = date;
		mStoragePolicy = StoragePolicy.Inherit;
	}
	
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
	public StoragePolicy getStoragePolicy() {
		return mStoragePolicy;
	}

	public void setStoragePolicy(StoragePolicy policy) {
		mStoragePolicy = policy;
	}
	
	@Override 
	public UniqueId getUniqueId() {
		return mItemId;
	}
}
