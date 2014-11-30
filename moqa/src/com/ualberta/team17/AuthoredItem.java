package com.ualberta.team17;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/*
 * An AuthoredItem is a item that a user posted to the service at some date.
 * Optionally, the item was posted in reply to another item on the service.
 */
public abstract class AuthoredItem extends QAModel {
	public static final String FIELD_AUTHOR = "author";
	public static final String FIELD_PARENT = "parent";
	public static final String FIELD_DATE = "date";

	// This is protected because Attachments may need to change it.
	protected UniqueId mParentItem;
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
		mStoragePolicy = StoragePolicy.Transient;
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
	
	@Override
	public Object getField(String fieldName) {
		if (fieldName.equals(FIELD_PARENT)) {
			return getParentItem();
		} else if (fieldName.equals(FIELD_AUTHOR)) {
			return getAuthor();
		} else if (fieldName.equals(FIELD_DATE)) {
			return getDate();
		} else {
			return super.getField(fieldName);
		}
	}

	public static abstract class GsonTypeAdapter<T extends AuthoredItem> extends QAModel.GsonTypeAdapter<T> {
		@Override
		public boolean parseField(T item, String name, JsonReader reader) throws IOException {
			if (super.parseField(item, name, reader)) {
				return true;
			} else if (name.equals(AuthoredItem.FIELD_PARENT)) {
				String parent = reader.nextString();

				if (null != parent && !parent.equals("0")) {
					item.mParentItem = UniqueId.fromString(parent);
				}

				return true;
			} else if (name.equals(AuthoredItem.FIELD_AUTHOR)) {
				item.mAuthor = reader.nextString();
				return true;
			} else if (name.equals(AuthoredItem.FIELD_DATE)) {
				item.mDate = DateStringFormat.parseDate(reader.nextString());
				return true;
			}

			return false;
		}

		@Override
		public void writeFields(JsonWriter writer, T item) throws IOException {
			super.writeFields(writer, item); 
			
			// Rest of fields
			writer.name(FIELD_PARENT);
			if (item.getParentItem() != null) {
				writer.value(item.getParentItem().toString());
			} else {
				writer.value("0");
			}

			writer.name(FIELD_AUTHOR);
			writer.value(item.getAuthor());
			writer.name(FIELD_DATE);
			writer.value(DateStringFormat.formatDate(item.getDate()));
		}
	}
}
