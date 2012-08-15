package com.filter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filter.activity.Avatar;

public class GalleryView extends LinearLayout{
	private Context mContext;
	private Avatar avatar;
	private int defaultImage;
	private ImageView image;
	private TextView decription;
	private int width;

	public GalleryView(Context context, Avatar avatar, int defaultImage, int width) {
		super(context);
		this.mContext = context;
		this.avatar = avatar;
		this.defaultImage = defaultImage;
		this.width = width;
		createLayout();
 		displayImage();
	}

	private void createLayout() {
		this.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams imageLP = new LinearLayout.LayoutParams(width,width);
		imageLP.gravity = Gravity.CENTER_HORIZONTAL;
		ImageView imageView = new ImageView(mContext);
		image = imageView;
		this.addView(imageView,imageLP);
		
		LinearLayout.LayoutParams textLP = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		textLP.gravity = Gravity.CENTER_HORIZONTAL;
		TextView textView = new TextView(mContext);
		decription = textView;
		this.addView(textView,textLP);
	}

	public GalleryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		displayImage();
	}

	public void displayImage() {
		if(null!=avatar) {
			image.setImageResource(avatar.imageId);
			decription.setText(getResources().getString(avatar.textId));
		}else {
			image.setImageResource(defaultImage);
			decription.setText(getResources().getString(avatar.textId));
		}
		
	}

}
