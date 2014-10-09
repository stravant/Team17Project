package com.ualberta.team17;

public class DataManager {
	/*
	 * Get the full thread, with question, answers, and comments to those items
	 * as an observable incremental result.
	 */
	public QuestionThread requestFullThread(UniqueId id);
	
	/*
	 * Search questions and answers using a given filter, and sorting the results
	 * by a given sort. Return as an observable incremental result.
	 */
	public QASearchResult requestSearch(QASearchFilter filter, QASearchOrder sort);
}
