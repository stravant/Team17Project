package com.ualberta.team17;

public class QASearchFilter {
	// Act on the database side
	public void addTextContains(String words);
	public void addAuthorEquals(String authorName);
	public void addStarredEquals(boolean value);
	
	//??? Do we also need a local filter like this
	public interface FilterPredicate {
		public boolean accept(QAItem item);
	}
	public void addFilter(FilterPredicate filter);
}
