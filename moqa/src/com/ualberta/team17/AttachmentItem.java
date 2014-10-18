package com.ualberta.team17;

import java.util.Date;

public class AttachmentItem extends AuthoredItem {
	private String mName;
	private byte[] mData;
	
	/* Ctor */
	public AttachmentItem(UniqueId id, UniqueId parentId, String author, Date date, String name, byte[] data) {
		super(ItemType.Attachment, id, parentId, author, date);
		mName = name;
		mData = data;
	}
	
	/* Getters */
	public String getName() {
		return mName;
	}
	public byte[] getData() {
		return mData;
	}
}
