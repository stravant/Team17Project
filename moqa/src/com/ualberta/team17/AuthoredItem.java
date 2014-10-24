package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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

	public static abstract class GsonTypeAdapter<T extends AuthoredItem> extends QAModel.GsonTypeAdapter<T> {
		@Override
		public boolean parseField(T item, String name, JsonReader reader) throws IOException {
			if (super.parseField(item, name, reader)) {
				return true;
			} else if (name.equals("parent")) {
				item.mParentItem = new UniqueId(reader.nextString());
				return true;
			} else if (name.equals("author")) {
				item.mAuthor = reader.nextString();
				return true;
			} else if (name.equals("date")) {
				item.mDate = new Date(reader.nextLong());
				return true;
			}

			return false;
		}
	}
}
