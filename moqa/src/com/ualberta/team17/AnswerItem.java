package com.ualberta.team17;

import java.util.Date;

public class AnswerItem extends QAItem {
	public AnswerItem(UniqueId id, UniqueId parentId, String author, Date date, String body) {
		super(id, parentId, author, date, body);
	}
	
	@Override
	public ItemType getItemType() {
		return ItemType.Answer;
	}
}
