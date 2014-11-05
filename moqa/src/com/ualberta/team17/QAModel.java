package com.ualberta.team17;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.ualberta.team17.view.IQAView;

/*
 * Abstract base class for all model uniquely IDed model objects that are
 * reflected in the database.
 */
public abstract class QAModel {
	public static final String FIELD_ID = "id";
	public List<IQAView> mViews = new ArrayList<IQAView>();
	public ItemType mType;
	public UniqueId mUniqueId;
	
	/* Ctor */
	public QAModel(ItemType type, UniqueId id) {
		mType = type;
		mUniqueId = id;
	}
	
	/* Model update notification behavior */
	public final void addView(IQAView view) {
		mViews.add(view);
	}
	public final void deleteView(IQAView view) {
		mViews.remove(view);
	}
	public final void notifyViews() {
		for (IQAView view: mViews) {
			view.update(this);
		}
	}
	
	/* Getters */
	public final UniqueId getUniqueId() {
		return mUniqueId;
	}
	public final ItemType getItemType() {
		return mType;
	}

	@Override
	public boolean equals(Object other) {
		if (null == other || !(other instanceof QAModel)) {
			return false;
		}

		return getUniqueId().equals(((QAModel)other).getUniqueId());
	}

	@Override
	public int hashCode() {
		return getUniqueId().hashCode();
	}

	public static abstract class GsonTypeAdapter<T extends QAModel> extends TypeAdapter<T> {
		public boolean parseField(T item, String name, JsonReader reader) throws IOException {
			if (name.equals(FIELD_ID)) {
				item.mUniqueId = new UniqueId(reader.nextString());
				return true;
			}

			return false;
		}

		public T readInto(T item, JsonReader reader) throws IOException {
			reader.beginObject();

			while (reader.hasNext()) {
				String name = reader.nextName();
				if (parseField(item, name, reader)) {
					continue;
				}

				reader.skipValue();
			}

			reader.endObject();
			return item;
		}
	}
}
