package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class CommentItem extends AuthoredTextItem {
	/* Ctor */
	public CommentItem(UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount) {
		super(ItemType.Comment, id, parentId, author, date, body, upvoteCount);
	}

	public static class GsonTypeAdapter extends AuthoredTextItem.GsonTypeAdapter<CommentItem> {
		@Override
		public CommentItem read(JsonReader reader) throws IOException {
			return readInto(new CommentItem(null, null, null, null, null, 0), reader);
		}
	}
}
