package com.ualberta.team17;

import java.util.Date;

public class QuestionItem extends QAItem {
	transient int mUpvoteCount;
	
	String mTitle;
	
	public QuestionItem(UniqueId id, UniqueId parentId, String author, Date date, String body, String title, int upvoteCount) {
		super(id, parentId, author, date, body);
		mTitle = title;
		mUpvoteCount = upvoteCount;
	}
	
	public int getUpvoteCount() {
		return mUpvoteCount;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	@Override
	public ItemType getItemType() {
		return ItemType.Question;
	}
}
