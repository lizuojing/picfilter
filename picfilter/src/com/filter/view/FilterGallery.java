package com.filter.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class FilterGallery extends Gallery {

	public FilterGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int kEvent;
		if (isScrollingLeft(e1, e2)) {
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return false;
	}
}
