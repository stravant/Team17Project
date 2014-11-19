package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;

public class MoreLikeThisFilter extends DataFilter {
	public static final Integer MLTMinDocFreq = 1;
	public static final Integer MLTMinTermFreq = 1;
	private List<String> mMoreLikeThisItems;
	private List<String> mMLTFields = new ArrayList<String>(){{
		add("title");
		add("body");
	}};

	public void addMoreLikeThisObject(QAModel item) {
		addMoreLikeThisObject(item.getUniqueId());
	}

	public void addMoreLikeThisObject(UniqueId itemId) {
		if (null == mMoreLikeThisItems) {
			mMoreLikeThisItems = new ArrayList<String>();
		}

		mMoreLikeThisItems.add(itemId.toString());
	}

	public List<String> getMoreLikeThisIds() {
		return mMoreLikeThisItems;
	}

	public List<String> getMLTFields() {
		return mMLTFields;
	}
}
