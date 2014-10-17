package com.ualberta.team17;

import java.util.Date;

public class CommentItem extends AuthoredTextItem {
	/* Ctor */
	public CommentItem(UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount) {
		super(ItemType.Comment, id, parentId, author, date, body, upvoteCount);
	}
}
