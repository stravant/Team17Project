package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/*
 * Represents an upvote of a given item
 */
public class UpvoteItem extends AuthoredItem {
	public UpvoteItem(UniqueId id, UniqueId parentId, String author, Date date) {
		super(ItemType.Upvote, id, parentId, author, date);
	}

	public static class GsonTypeAdapter extends AuthoredItem.GsonTypeAdapter<UpvoteItem> {
		@Override
		public UpvoteItem read(JsonReader reader) throws IOException {
			return readInto(new UpvoteItem(null, null, null, null), reader);
		}

		@Override
		public void write(JsonWriter arg0, UpvoteItem arg1) throws IOException {
			// TODO Auto-generated method stub
			
		}
	}
}
