package com.ualberta.team17.test.utility;

import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;

public class OrderedQAItem extends QAModel {
	private int mSeq;
	
	public OrderedQAItem(ItemType type, int seq) {
		super(type, new UniqueId());
		mSeq = seq;
	}
	
	public int getSeq() {
		return mSeq;
	}
}
