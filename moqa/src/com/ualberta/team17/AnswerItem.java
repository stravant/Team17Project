package com.ualberta.team17;

import java.util.Date;

public class AnswerItem extends AuthoredTextItem {
	/* Ctor */
	public AnswerItem(UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount) {
		super(ItemType.Answer, id, parentId, author, date, body, upvoteCount);
	}
}
