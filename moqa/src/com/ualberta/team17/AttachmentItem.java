package com.ualberta.team17;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class AttachmentItem extends AuthoredItem {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_DATA = "data";

	private String mName;
	private byte[] mData;
	
	private transient Bitmap mImage;
	
	/* Ctor */
	public AttachmentItem(UniqueId id, UniqueId parentId, String author, Date date, String name, byte[] data) {
		super(ItemType.Attachment, id, parentId, author, date);
		mName = name;
		mData = data;
		if (data != null) {
			mImage = decodeBitmap(data);
		}
	}
	
	public AttachmentItem(UniqueId id, UniqueId parentId, String author, Date date, String name, Bitmap image) {
		super(ItemType.Attachment, id, parentId, author, date);
		mName = name;
		if (image != null) {
			mData = encodeBitmap(image);
		}
		mImage = image;
	}
	
	/* Getters */
	public String getName() {
		return mName;
	}
	public byte[] getData() {
		return mData;
	}
	
	/**
	 * Encode a bitmap into a byte array.
	 * @param image The image to encode
	 * @return The encoded image
	 */
	public static byte[] encodeBitmap(Bitmap image) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, out);
		return out.toByteArray();
	}
	
	/**
	 * Decode a byte array into a bitmap image
	 * @param data
	 * @return The decoded image
	 */
	public static Bitmap decodeBitmap(byte[] data) {
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
	
	/**
	 * Encode a byte array as a base 64 string
	 * @return
	 */
	private static String encodeBase64(byte[] data) {
		return Base64.encodeToString(data, Base64.DEFAULT);
	}
	
	/**
	 * Decode a byte array from a base 64 string
	 * @return
	 */
	private static byte[] decodeBase64(String base64) {
		return Base64.decode(base64, Base64.DEFAULT);
	}
	
	/* Image handling */
	public Bitmap getImage() {
		if (mImage == null) {
			if (mData == null) {
				return null;
			} else {
				mImage = decodeBitmap(mData);
			}
		}
		return mImage;
	}
	
	/* Serialization */
	public static class GsonTypeAdapter extends QAModel.GsonTypeAdapter<AttachmentItem> {
		@Override
		public AttachmentItem read(JsonReader reader) throws IOException {
			return readInto(new AttachmentItem(null, null, null, null, null, (Bitmap)null), reader);
		}	

		@Override
		public boolean parseField(AttachmentItem item, String name, JsonReader reader) throws IOException {
			if (super.parseField(item, name, reader)) {
				return true;
			} else if (name.equals(AttachmentItem.FIELD_DATA)) {
				String base64 = reader.nextString();
				item.mData = decodeBase64(base64);
				return true;
			} else if (name.equals(AttachmentItem.FIELD_NAME)) {
				item.mName = reader.nextString();
				return true;
			}

			return false;
		}

		@Override
		public void writeFields(JsonWriter writer, AttachmentItem item) throws IOException {
			super.writeFields(writer, item); 
			
			// Encode 
			writer.name(FIELD_DATA);
			writer.value(encodeBase64(item.mData));
			
			// Rest of fields
			writer.name(FIELD_NAME);
			writer.value(item.getName());
		}
	}
}
