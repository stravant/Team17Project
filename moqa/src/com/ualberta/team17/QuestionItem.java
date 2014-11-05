package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class QuestionItem extends AuthoredTextItem {	
	public static final String FIELD_TITLE = "title";

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

	public static class GsonTypeAdapter extends AuthoredTextItem.GsonTypeAdapter<QuestionItem> {
		@Override
		public boolean parseField(QuestionItem item, String name, JsonReader reader) throws IOException {
			if (super.parseField(item, name, reader)) {
				return true;
			} else if (name.equals(QuestionItem.FIELD_TITLE)) {
				item.mTitle = reader.nextString();
				return true;
			}

			return false;
		}

		@Override
		public QuestionItem read(JsonReader reader) throws IOException {
			return readInto(new QuestionItem(null, null, null, null, null, 0, null), reader);
		}

		@Override
		public void addValues(JsonWriter writer, QuestionItem model) throws IOException {
			super.addValues(writer, model);

			writer.name(FIELD_TITLE);
			writer.value(model.getTitle());
		}
	}
}
