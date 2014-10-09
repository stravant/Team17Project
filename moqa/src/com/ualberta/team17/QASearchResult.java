package com.ualberta.team17;

import java.util.Observable;

/*
 * An incremental list of results from doing a search for questions 
 * and answers with a given result order.
 * Notifies when new data has arrived.
 */
public class QASearchResult implements Observable {
	public List<QAItem> getCurrentResult();
}
