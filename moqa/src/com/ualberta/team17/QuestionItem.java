package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ualberta.team17.datamanager.UserContext;

/**
 * A QAModel that represents a user question and all related derived info.
 * 
 * @author michaelblouin
 */
public class QuestionItem extends AuthoredTextItem {	
	public static final String FIELD_TITLE = "title";
	public static final String FIELD_REPLIES = "replies";

	private String mTitle;
	private transient int mReplyCount;
	private transient boolean mIsFavorited = false;
	private transient boolean mShouldViewLater = false;
	private transient boolean mIsAttached = false;
	
	/* Ctor */
	public QuestionItem(UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount, String title) {
		super(ItemType.Question, id, parentId, author, date, body, upvoteCount);
		mTitle = title;
	}
	
	/* Reply count */
	public int getReplyCount() {	
		return mReplyCount;
	}
	
	public void setReplyCount(int replies) {
		mReplyCount = replies;
		notifyViews();
	}
	
	public void incrementReplyCount() {
		++mReplyCount;
		notifyViews();
	}
	
	/* Is favorited? */
	public void setFavorited() {
		if (!mIsFavorited) {
			mIsFavorited = true;
			notifyViews();
		}
	}
	
	public boolean isFavorited() {
		return mIsFavorited;
	}
	
	/* Should view later? */
	public void setViewLater() {
		if (!mShouldViewLater) {
			mShouldViewLater = true;
			notifyViews();
		}
	}
	
	public void clearViewLater() {
		if (mShouldViewLater) {
			mShouldViewLater = false;
			notifyViews();
		}
	}
	
	public boolean getViewLater() {
		return mShouldViewLater;
	}
	
	/* Is attached to? */
	public void setHasAttachments() {
		if (!mIsAttached) {
			mIsAttached = true;
			notifyViews();
		}
	}
	
	public boolean hasAttachments() {
		return mIsAttached;
	}
	
	/* Getters */
	public String getTitle() {
		return mTitle;
	}
	
	@Override
	public void copyDerivedInfo(QAModel other) {
		super.copyDerivedInfo(other);
		QuestionItem otherq = (QuestionItem)other;
		mReplyCount = otherq.mReplyCount;
		mIsFavorited = otherq.mIsFavorited;
		mShouldViewLater = otherq.mShouldViewLater;
		mIsAttached = otherq.mIsAttached;
		notifyViews();
		
	}
	
	/**
	 * Calculate parent derived info
	 */
	public void addToParentDerivedInfo(UserContext ctx, QAModel parentItem) {
		// Will never be called, QuestionItems never have a parent item
	}
	
	@Override
	public Object getField(String fieldName) {
		if (fieldName.equals(FIELD_TITLE)) {
			return getTitle();
		} else if (fieldName.equals(FIELD_REPLIES)) {
			return getReplyCount();
		} else {
			return super.getField(fieldName);
		}
	}

	public static class GsonTypeAdapter extends AuthoredTextItem.GsonTypeAdapter<QuestionItem> {
		@Override
		public boolean parseField(QuestionItem item, String name, JsonReader reader) throws IOException {
			if (super.parseField(item, name, reader)) {
				return true;
			} else if (name.equals(QuestionItem.FIELD_TITLE)) {
				item.mTitle = reader.nextString();
				return true;
			}

			return false;
		}
		
		@Override
		public void writeFields(JsonWriter writer, QuestionItem item) throws IOException {
			super.writeFields(writer, item); 
			writer.name(FIELD_TITLE);
			writer.value(item.getTitle());
		}

		@Override
		public QuestionItem read(JsonReader reader) throws IOException {
			return readInto(new QuestionItem(null, null, null, null, null, 0, null), reader);
		}
	}
}
