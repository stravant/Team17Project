package com.ualberta.team17;

import java.util.Date;

public class QuestionItem extends AuthoredTextItem {	
	private String mTitle;
	
	/* Ctor */
	public QuestionItem(UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount, String title) {
		super(ItemType.Question, id, parentId, author, date, body, upvoteCount);
		mTitle = title;
	}
	
	/* Getters */
	public String getTitle() {
		return mTitle;
	}
}
