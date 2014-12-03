package com.ualberta.team17.datamanager;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter.DataFilterType;

/**
 * This filter is used to implement a MoreLikeThis query on the Elastic Search server.
 * It does nothing special in the local data manager.
 * 
 * @author michaelblouin
 */
public class MoreLikeThisFilter extends DataFilter {
	public static final Integer MLTMinDocFreq = 1;
	public static final Integer MLTMinTermFreq = 1;
	private List<String> mMoreLikeThisItems;
	private List<JsonObject> mMoreLikeThisDocs;
	private List<String> mMLTFields = new ArrayList<String>(){{
		add("title");
//		add("body");
	}};

	/**
	 * Adds the given item to the More Like This Query.
	 * @param item
	 */
	public void addMoreLikeThisObject(QAModel item) {
		addMoreLikeThisObject(item.getUniqueId(), item.getItemType());
	}

	/**
	 * Adds the given item to the More Like This Query using just it's UniqueId.
	 * 
	 * Note: The item must already be indexed. Additionally, Elastic Search
	 * occasionally gets moody when a type isn't specified.
	 * 
	 * @param itemId
	 */
	public void addMoreLikeThisObject(UniqueId itemId) {
		if (null == mMoreLikeThisItems) {
			mMoreLikeThisItems = new ArrayList<String>();
		}

		mMoreLikeThisItems.add(itemId.toString());
	}

	/**
	 * Adds the given item to the More Like This Query using it's itemId and Type.
	 * 
	 * Note that if the type does not match the given itemId as it is indexed in elastic search,
	 * you will not receive a result.
	 * 
	 * @param itemId
	 * @param type
	 */
	public void addMoreLikeThisObject(UniqueId itemId, ItemType type) {
		if (null == mMoreLikeThisDocs) {
			mMoreLikeThisDocs = new ArrayList<JsonObject>();
		}

		JsonObject obj = new JsonObject();
		obj.addProperty("_type", type.toString().toLowerCase());
		obj.addProperty("_id", itemId.toString());

		mMoreLikeThisDocs.add(obj);
	}

	/**
	 * Gets the MoreLikeThis document ids to filter on.
	 * @return
	 */
	public List<String> getMoreLikeThisIds() {
		return mMoreLikeThisItems;
	}

	/**
	 * Gets the MoreLikeThis document list to filter on.
	 * @return
	 */
	public List<JsonObject> getMoreLikeThisDocs() {
		return mMoreLikeThisDocs;
	}

	/**
	 * Gets the fields to be considered when search for a match on the server.
	 * @return
	 */
	public List<String> getMLTFields() {
		return mMLTFields;
	}
	
	public DataFilterType getDataFilterType() {
		return DataFilterType.MORE_LIKE_THIS;
	}
}
