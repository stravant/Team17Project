package com.ualberta.team17;

import java.util.Date;

/*
 * Represents an upvote of a given item
 */
public class UpvoteItem extends AuthoredItem {
	public UpvoteItem(UniqueId id, UniqueId parentId, String author, Date date) {
		super(id, parentId, author, date);
	}
	
	@Override
	public ItemType getItemType() {
		return ItemType.Comment;
	}
}
