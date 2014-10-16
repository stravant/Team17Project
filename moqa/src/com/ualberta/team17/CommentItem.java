package com.ualberta.team17;

import java.util.Date;

public class CommentItem extends AuthoredItem {
	public String mBody;
	
	public CommentItem(UniqueId id, UniqueId parentId, String author, Date date, String body) {
		super(id, parentId, author, date);
		mBody = body;
	}
	
	public String getBody() {
		return mBody;
	}

	public ItemType getItemType() {
		return ItemType.Comment;
	}
}
