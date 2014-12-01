package com.ualberta.team17.datamanager;

import com.ualberta.team17.ItemType;

/**
 * Represents a data filter for a top upvoted items query.
 * 
 * Note that the item type filter must be provided on initialization.
 * 
 * @author michaelblouin
 */
public class TopUpvotedDataFilter extends DataFilter {
	public static final String SCORING_TYPE = "sum";
	public static final Integer SCORING_FACTOR = 10;

	public TopUpvotedDataFilter(ItemType parentType) {
		setTypeFilter(parentType);
	}

	public String getChildFilterType() {
		return "upvote_" + getTypeFilter().toString().toLowerCase();
	}

	public String getScoringType() {
		return SCORING_TYPE;
	}

	public Integer getScoringFactor() {
		return SCORING_FACTOR;
	}
}
