package com.ualberta.team17;

import java.util.Date;

/*
 * Represents an upvote of a given item
 */
public class UpvoteItem extends AuthoredItem {
	public UpvoteItem(UniqueId id, UniqueId parentId, String author, Date date) {
		super(ItemType.Upvote, id, parentId, author, date);
	}
}
