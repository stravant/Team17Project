package com.ualberta.team17.test.utility;

import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.UserContext;

/**
 * A QAModel object to be used for testing ordered results and
 * similar operations. Extends QAModel with a user-provided identifier
 * that can be used to sort the items. 
 */
public class OrderedQAItem extends QAModel {
	private int mSeq;
	
	/**
	 * Constructor, with a given item type, and a given identifier
	 * @param type The type to make the item
	 * @param seq The orderable identifier of this item
	 */
	public OrderedQAItem(ItemType type, int seq) {
		super(type, new UniqueId());
		mSeq = seq;
	}
	
	/**
	 * Get the orderable identifier of this item
	 * @return The orderable identifier
	 */
	public int getSeq() {
		return mSeq;
	}

	@Override
	public void addToParentDerivedInfo(UserContext ctx, QAModel parentItem) {
		// nothing to do
	}
}
