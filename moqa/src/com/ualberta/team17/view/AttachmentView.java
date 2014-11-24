package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class AttachmentView extends HorizontalScrollView {
	private static final int LAYOUT_VIEW_ID = 98169460;
	private List<AttachmentItem> mAttachments = new ArrayList<AttachmentItem>();
	private List<ImageView> mImageViews = new ArrayList<ImageView>();
	private LinearLayout baseLayout;
	private boolean mAddingEnabled = false;
	private QuestionItem mParentItem;
	private ImageView mAddAttachmentView;
	private IAddAttachmentListener mAddAttachmentListener; 

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
		// Create the linear layout
		baseLayout = new LinearLayout(getContext());
		baseLayout.setOrientation(LinearLayout.HORIZONTAL);
		baseLayout.setId(LAYOUT_VIEW_ID);
		
		// Create the add attachment view
		mAddAttachmentView = new ImageView(getContext());
		mAddAttachmentView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_new_attachment_large));
		mAddAttachmentView.setVisibility(getAddingEnabled() ? ImageView.VISIBLE : ImageView.GONE);
		mAddAttachmentView.setLayoutParams(new LinearLayout.LayoutParams(this.getHeight(), LayoutParams.MATCH_PARENT));
		mAddAttachmentView.setOnClickListener(new AddAttachmentOnClickListener());
		baseLayout.addView(mAddAttachmentView);

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

			imageView.setOnClickListener(new ImageViewOnClickListener());

			baseLayout.addView(imageView);

			mImageViews.add(imageView);
		}
	}

	/**
	 * Sets whether adding attachments is enabled, and if yes, which QAModel should be used as the parent.
	 * @param enabled Whether to enable adding
	 * @param parent The item to attach new items to.
	 */
	public void setAddingEnabled(boolean enabled, QuestionItem parent, IAddAttachmentListener listener) {
		mAddingEnabled = enabled;
		mParentItem = parent;
		mAddAttachmentListener = listener;
		mAddAttachmentView.setVisibility(getAddingEnabled() ? ImageView.VISIBLE : ImageView.GONE);
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
		mAddAttachmentView.setLayoutParams(
				new LinearLayout.LayoutParams(
						this.getHeight(),
						LayoutParams.MATCH_PARENT));

		for (ImageView imageView: mImageViews) {
			Bitmap image = mAttachments.get(mImageViews.indexOf(imageView)).getImage();
			imageView.setLayoutParams(
					new LinearLayout.LayoutParams(
							(int)(image.getWidth() * ((float)this.getHeight())/image.getHeight()),
							LayoutParams.MATCH_PARENT));
		}
	}

	private class AddAttachmentOnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			if (null != mAddAttachmentListener) {
				mAddAttachmentListener.addAttachment();
			}
		}
	}

	private class ImageViewOnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			Context baseContext = AttachmentView.this.getContext();
			if (baseContext instanceof Activity) {
				ImageViewDialog imd = new ImageViewDialog(mAttachments.get(mImageViews.indexOf(arg0)));
				imd.show(((Activity)baseContext).getFragmentManager(), "Attachment Dialog");
			}
		}
	}

	private class ImageViewDialog extends DialogFragment {
		AttachmentItem mBaseItem;
		ImageView baseView;

		public ImageViewDialog(AttachmentItem baseItem) {
			mBaseItem = baseItem;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    LayoutInflater inflater = getActivity().getLayoutInflater();

		    View v = inflater.inflate(R.layout.image_view_dialog, null, false);
		    builder.setView(v);
		    baseView = (ImageView) v.findViewById(R.id.mainImageView);
		    baseView.setImageBitmap(mBaseItem.getImage());
		    baseView.setScaleType(ScaleType.FIT_CENTER);
		    Dialog d = builder.create();
		    d.getWindow().setFlags(
		    		android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
		    		android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
		    
		    return d;
		}
	}
}
