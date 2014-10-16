package com.ualberta.team17;

import java.util.Date;

public class AttachmentItem extends AuthoredItem {
	String mName;
	String mData;
	
	public AttachmentItem(UniqueId id, UniqueId parentId, String author, Date date, String name, String data) {
		super(id, parentId, author, date);
		mName = name;
		mData = data;
	}
	
	public String getName() {
		return mName;
	}

	public String getData() {
		return mData;
	}

	@Override
	public ItemType getItemType() {
		return ItemType.Attachment;
	}
}
