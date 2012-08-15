package com.filter.utils;

import java.util.HashMap;

import com.filter.activity.FilterApp;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class ToastManager {
	private static HashMap<String, Toast> toastMap = new HashMap<String, Toast>();

	/**
	 * 如果第一参数不为null,toast只在该activity在前台时显示时才有效,
	 * 如果第一个参数传null,toast在整个应用中有效,只有在应用可见的时候才显示,否则不显示
	 * 
	 * @param activity
	 * @param text
	 */
	public static void showToast(Activity activity, String text) {
		if ((activity != null && FilterApp.isAppOnForeground(activity))
				|| (activity == null && FilterApp.isAppOnForeground())) {
			if (toastMap.containsKey(text)) {
				toastMap.get(text).show();
			} else {
				Context context = activity == null ? FilterApp.appContext : activity;
				Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
				toastMap.put(text, toast);
				toast.show();
			}
		}
	}

	public static void showToast(Activity activity, int strResId) {
		showToast(activity, activity.getString(strResId));
	}

}
