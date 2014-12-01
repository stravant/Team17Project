package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import com.google.gson.stream.JsonReader;
import com.ualberta.team17.datamanager.UserContext;

/*
 * Represents an upvote of a given item
 */
public class UpvoteItem extends AuthoredItem {
	private transient ItemType mParentType;

	public UpvoteItem(UniqueId id, UniqueId parentId, String author, Date date, ItemType parentType) {
		super(ItemType.Upvote, id, parentId, author, date);
		mParentType = parentType;
	}

	public ItemType getParentType() {
		return mParentType;
	}
	
	@Override
	public void addToParentDerivedInfo(UserContext ctx, QAModel parentItem) {
		AuthoredTextItem parent = (AuthoredTextItem)parentItem;
		
		// Mark I have upvoted if this is an upvote of mine on an item
		if (ctx != null && getAuthor().equals(ctx.getUserName())) {
			parent.setHaveUpvoted();
		}
					
		// Tally upvotes
		parent.upvote();
	}

	public static class GsonTypeAdapter extends AuthoredItem.GsonTypeAdapter<UpvoteItem> {
		@Override
		public UpvoteItem read(JsonReader reader) throws IOException {
			return readInto(new UpvoteItem(null, null, null, null, null), reader);
		}
	}
}
