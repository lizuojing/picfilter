package com.filter.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Process;

public class FilterApp extends Application {

	public static ArrayList<Activity> allActivity = new ArrayList<Activity>();

	public static Context appContext;

	@Override
	public void onCreate() {
		super.onCreate();
		appContext = this.getApplicationContext();
	}

	public static boolean isAppOnForeground() {
		ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			// 应用程序位于堆栈的顶层
			if ("com.filter".equals(tasksInfo.get(0).topActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAppOnForeground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			// 应用程序位于堆栈的顶层
			if ("com.filter".equals(tasksInfo.get(0).topActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public static void exitApp() {
		for (Activity activity : allActivity) {
			activity.finish();
		}
		allActivity.clear();
		Process.killProcess(Process.myPid());
		System.exit(0);
	}

	public static Context getAppContext() {
		return appContext;
	}

}
