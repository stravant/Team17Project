package com.ualberta.team17;

import java.util.Date;

/*
 * A QAItem represents a question or answer in the service.
 * In addition to being an authored item, it has a body text. 
 * Also, can be upvoted.
 */
public abstract class AuthoredTextItem extends AuthoredItem implements IUpvotable {
	private String mBody;
	private transient int mUpvoteCount;
	
	/* Ctor */
	public AuthoredTextItem(ItemType type, UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount) {
		super(type, id, parentId, author, date);
		mBody = body;
		mUpvoteCount = upvoteCount;
	}
	
	/* Getters */
	public String getBody() {
		return mBody;
	}

	@Override
	public int getUpvoteCount() {
		return mUpvoteCount;
	}

	@Override
	public void upvote() {
		mUpvoteCount++;
		notifyViews();
	}	
}
