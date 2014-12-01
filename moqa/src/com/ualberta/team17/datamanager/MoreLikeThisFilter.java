package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;

public class MoreLikeThisFilter extends DataFilter {
	public static final Integer MLTMinDocFreq = 2;
	public static final Integer MLTMinTermFreq = 1;
	private List<String> mMoreLikeThisItems;
	private List<JsonObject> mMoreLikeThisDocs;
	private List<String> mMLTFields = new ArrayList<String>(){{
		add("title");
		add("body");
	}};

	public void addMoreLikeThisObject(QAModel item) {
		addMoreLikeThisObject(item.getUniqueId(), item.getItemType());
	}

	public void addMoreLikeThisObject(UniqueId itemId) {
		if (null == mMoreLikeThisItems) {
			mMoreLikeThisItems = new ArrayList<String>();
		}

		mMoreLikeThisItems.add(itemId.toString());
	}

	public void addMoreLikeThisObject(UniqueId itemId, ItemType type) {
		if (null == mMoreLikeThisDocs) {
			mMoreLikeThisDocs = new ArrayList<JsonObject>();
		}

		JsonObject obj = new JsonObject();
		obj.addProperty("_type", type.toString().toLowerCase());
		obj.addProperty("_id", itemId.toString());

		mMoreLikeThisDocs.add(obj);
	}

	public List<String> getMoreLikeThisIds() {
		return mMoreLikeThisItems;
	}

	public List<JsonObject> getMoreLikeThisDocs() {
		return mMoreLikeThisDocs;
	}

	public List<String> getMLTFields() {
		return mMLTFields;
	}
}
