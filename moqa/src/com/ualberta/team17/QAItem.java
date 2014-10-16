package com.ualberta.team17;

import java.util.Date;

/*
 * A QAItem represents a question or answer in the service.
 * In addition to being an authored item, it has a body text.
 */
public abstract class QAItem extends AuthoredItem {
	public String mBody;
	
	public QAItem(UniqueId id, UniqueId parentId, String author, Date date, String body) {
		super(id, parentId, author, date);
		mBody = body;
	}
	
	public String getBody() {
		return mBody;
	}
}
