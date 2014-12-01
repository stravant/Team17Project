package com.ualberta.team17;

import java.io.IOException;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ualberta.team17.datamanager.UserContext;

/**
 * Represents a QAModel that is an answer to a question. Answers are always children of Questions.
 */
public class AnswerItem extends AuthoredTextItem {
	/* Ctor */
	public AnswerItem(UniqueId id, UniqueId parentId, String author, Date date, String body, int upvoteCount) {
		super(ItemType.Answer, id, parentId, author, date, body, upvoteCount);
	}
	
	@Override
	public void addToParentDerivedInfo(UserContext ctx, QAModel parentItem) {
		// Tally replies
		((QuestionItem)parentItem).incrementReplyCount();
	}

	public static class GsonTypeAdapter extends AuthoredTextItem.GsonTypeAdapter<AnswerItem> {
		@Override
		public AnswerItem read(JsonReader reader) throws IOException {
			return readInto(new AnswerItem(null, null, null, null, null, 0), reader);
		}	
	}
}
