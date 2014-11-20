package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.QAModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * TODO: document your custom view class.
 */
public class AttachmentView extends HorizontalScrollView {
	private static final int LAYOUT_VIEW_ID = 98169460;
	private List<AttachmentItem> mAttachments = new ArrayList<AttachmentItem>();
	private List<ImageView> mImageViews = new ArrayList<ImageView>();
	private LinearLayout baseLayout;
	private boolean mAddingEnabled = false;
	private QAModel mParentItem;

	public AttachmentView(Context context) {
		super(context);
		init(null, 0);
	}

	public AttachmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public AttachmentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		baseLayout = new LinearLayout(getContext());
		baseLayout.setOrientation(LinearLayout.HORIZONTAL);
		baseLayout.setId(LAYOUT_VIEW_ID);
		this.addView(baseLayout);
	}

	/**
	 * Adds the given attachment image to the current view, resizing it as necessary.
	 * @param item The attachment item to add to the view.
	 */
	public void addAttachment(AttachmentItem item) {
		if (!mAttachments.contains(item)) {
			mAttachments.add(item);
			ImageView imageView = new ImageView(getContext());
			Bitmap image = item.getImage();

			imageView.setImageBitmap(item.getImage());
			imageView.setLayoutParams(
					new LinearLayout.LayoutParams(
							(int)(image.getWidth() * ((float)this.getHeight())/image.getHeight()),
							LayoutParams.MATCH_PARENT));

			baseLayout.addView(imageView);
			mImageViews.add(imageView);
		}
	}

	/**
	 * Sets whether adding attachments is enabled, and if yes, which QAModel should be used as the parent.
	 * @param enabled Whether to enable adding
	 * @param parent The item to attach new items to.
	 */
	public void setAddingEnabled(boolean enabled, QAModel parent) {
		mAddingEnabled = enabled;
		mParentItem = parent;
	}

	/**
	 * Gets whether adding is enabled. Only true if it is both enabled, and the parent object is set.
	 * @return
	 */
	public boolean getAddingEnabled() {
		return mAddingEnabled && null != mParentItem;
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		for (ImageView imageView: mImageViews) {
			Bitmap image = mAttachments.get(mImageViews.indexOf(imageView)).getImage();
			imageView.setLayoutParams(
					new LinearLayout.LayoutParams(
							(int)(image.getWidth() * ((float)this.getHeight())/image.getHeight()),
							LayoutParams.MATCH_PARENT));
		}
	}
}
