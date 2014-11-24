package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/*
 * A QAItem represents a question or answer in the service.
 * In addition to being an authored item, it has a body text. 
 * Also, can be upvoted.
 */
public abstract class AuthoredTextItem extends AuthoredItem {
	public static final String FIELD_BODY = "body";
	public static final String FIELD_UPVOTES = "upvotes";
	public static final String FIELD_COMMENTS = "comments";

	private String mBody;
	private transient int mUpvoteCount;
	private transient int mCommentCount;
	private transient boolean mHasUpvoted = false;
	
	/* Ctor */
	public AuthoredTextItem(ItemType type, UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount) {
		super(type, id, parentId, author, date);
		mBody = body;
		mUpvoteCount = upvoteCount;
		mCommentCount = 0;
	}
	
	/* Getters */
	public String getBody() {
		return mBody;
	}

	/* Upvote Count */
	public int getUpvoteCount() {
		return mUpvoteCount;
	}
	
	public void setUpvoteCount(int votes) {
		if (mUpvoteCount != votes) {
			mUpvoteCount = votes;
			notifyViews();
		}
	}

	public void upvote() {
		mUpvoteCount++;
		notifyViews();
	}
	
	/* Have upvoted */
	public void setHaveUpvoted() {
		if (!mHasUpvoted) {
			mHasUpvoted = true;
			notifyViews();
		}
	}
	
	public boolean haveUpvoted() {
		return mHasUpvoted;
	}

	/* Comment Count */
	public int getCommentCount() {
		return mCommentCount;
	}
	
	public void incrementCommentCount() {
		++mCommentCount;
		notifyViews();
	}

	
	@Override
	public Object getField(String fieldName) {
		if (fieldName.equals(FIELD_BODY)) {
			return getBody();
		} else if (fieldName.equals(FIELD_UPVOTES)) {
			return Integer.valueOf(getUpvoteCount());
		} else if (fieldName.equals(FIELD_COMMENTS)) {
			return Integer.valueOf(getCommentCount());
		} else {
			return super.getField(fieldName);
		}
	}
	public static abstract class GsonTypeAdapter<T extends AuthoredTextItem> extends AuthoredItem.GsonTypeAdapter<T> {
		@Override
		public boolean parseField(T item, String name, JsonReader reader) throws IOException {
			if (super.parseField(item, name, reader)) {
				return true;
			} else if (name.equals(AuthoredTextItem.FIELD_BODY)) {
				item.mBody = reader.nextString();
				return true;
			}

			return false;
		}

		@Override
		public void writeFields(JsonWriter writer, T item) throws IOException {
			super.writeFields(writer, item);
			writer.name(FIELD_BODY);
			writer.value(item.getBody());
		}
	}
}
