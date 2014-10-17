package com.ualberta.team17;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.view.IQAView;

/*
 * Abstract base class for all model uniquely IDed model objects that are
 * reflected in the database.
 */
public abstract class QAModel {
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
}
