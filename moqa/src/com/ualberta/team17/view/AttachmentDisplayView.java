package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AttachmentItem;
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
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class AttachmentDisplayView extends LinearLayout {
	private List<AttachmentItem> mAttachments = new ArrayList<AttachmentItem>();
	private List<ImageView> mImageViews = new ArrayList<ImageView>();
	
	
	public AttachmentDisplayView(Context context) {
		super(context);
		init(null, 0);
	}

	public AttachmentDisplayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public AttachmentDisplayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}
	
	private void init(AttributeSet attrs, int defStyle) {
		
	}
	
	public void addAttachment(AttachmentItem item) {
		if(!mAttachments.contains(item)) {
			mAttachments.add(item);
			ImageView imageView = new ImageView(getContext());
			Bitmap image = item.getImage();
			
			imageView.setImageBitmap(image);
			imageView.setOnClickListener(new ImageViewOnClickListener());
			this.addView(imageView);
			mImageViews.add(imageView);
			
		}
	}
	
	public void clearAttachments() {
		this.removeAllViews();
		mAttachments.clear();
		mImageViews.clear();
	}
	
	private class ImageViewOnClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			Context baseContext = AttachmentDisplayView.this.getContext();
			if (baseContext instanceof Activity) {
				ImageViewDialog imd = new ImageViewDialog(mAttachments.get(mImageViews.indexOf(view)));
				imd.show(((Activity)baseContext).getFragmentManager(), "Attachment Dialog");
			}
		}
	}

	private class ImageViewDialog extends DialogFragment {
		AttachmentItem mBaseItem;
		ImageView baseView;
		TextView titleView;

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
		    
		    titleView = (TextView) v.findViewById(R.id.imageTitleView);
		    titleView.setText(mBaseItem.getName());
		    Dialog d = builder.create();
		    d.getWindow().setFlags(
		    		android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
		    		android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
		    
		    return d;
		}
	}
}
