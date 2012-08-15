package com.filter.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;

public class MyWebView extends WebView {

	private int currentY = 0;

	public MyWebView(Context context) {
		super(context);
		init(context);
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		initWebSettings();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		currentY = t;
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public int getCurrentY() {
		return currentY;
	}

	public boolean canZoomIn() {
		boolean canZoomIn = true;
		try {
			Field mActualScale = WebView.class.getDeclaredField("mActualScale");
			Field mMaxZoomScale = WebView.class.getDeclaredField("mMaxZoomScale");
			mActualScale.setAccessible(true);
			mMaxZoomScale.setAccessible(true);
			canZoomIn = mActualScale.getFloat(this) < mMaxZoomScale.getFloat(this);
		} catch (Exception e) {
			try {
				Field mZoomManager = WebView.class.getDeclaredField("mZoomManager");
				if (mZoomManager != null) {
					mZoomManager.setAccessible(true);
					Object zoomManager = mZoomManager.get(this);
					if (zoomManager != null) {
						Field mEmbeddedZoomControl = zoomManager.getClass().getDeclaredField("mEmbeddedZoomControl");
						if (mEmbeddedZoomControl != null) {
							mEmbeddedZoomControl.setAccessible(true);
							Object zoomControlEmbedded = mEmbeddedZoomControl.get(zoomManager);
							if (zoomControlEmbedded != null) {
								mZoomManager = zoomControlEmbedded.getClass().getDeclaredField("mZoomManager");
								if(mZoomManager!=null){
									mZoomManager.setAccessible(true);
									zoomManager = mZoomManager.get(zoomControlEmbedded);
									Method canZoomInMethod = zoomManager.getClass().getDeclaredMethod("canZoomIn");
									if(canZoomInMethod!=null){
										canZoomInMethod.setAccessible(true);
										Object canZoomInObj = canZoomInMethod.invoke(zoomManager, new Object[0]);
										if (canZoomInObj != null && canZoomInObj instanceof Boolean) {
											return (Boolean) canZoomInObj;
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e2) {
			}
		}
		return canZoomIn;
	}

	public boolean canZoomOut() {
		boolean canZoomOut = true;
		try {
			Field mActualScale = WebView.class.getDeclaredField("mActualScale");
			Field mMinZoomScale = WebView.class.getDeclaredField("mMinZoomScale");
			Field mInZoomOverview = WebView.class.getDeclaredField("mInZoomOverview");
			mActualScale.setAccessible(true);
			mMinZoomScale.setAccessible(true);
			mInZoomOverview.setAccessible(true);
			canZoomOut = mActualScale.getFloat(this) > mMinZoomScale.getFloat(this)
					&& !mInZoomOverview.getBoolean(this);
		} catch (Exception e) {
			try {
				Field mZoomManager = WebView.class.getDeclaredField("mZoomManager");
				if (mZoomManager != null) {
					mZoomManager.setAccessible(true);
					Object zoomManager = mZoomManager.get(this);
					if (zoomManager != null) {
						Field mEmbeddedZoomControl = zoomManager.getClass().getDeclaredField("mEmbeddedZoomControl");
						if (mEmbeddedZoomControl != null) {
							mEmbeddedZoomControl.setAccessible(true);
							Object zoomControlEmbedded = mEmbeddedZoomControl.get(zoomManager);
							if (zoomControlEmbedded != null) {
								mZoomManager = zoomControlEmbedded.getClass().getDeclaredField("mZoomManager");
								if(mZoomManager!=null){
									mZoomManager.setAccessible(true);
									zoomManager = mZoomManager.get(zoomControlEmbedded);
									Method canZoomOutMethod = zoomManager.getClass().getDeclaredMethod("canZoomOut");
									if(canZoomOutMethod!=null){
										canZoomOutMethod.setAccessible(true);
										Object canZoomOutObj = canZoomOutMethod.invoke(zoomManager, new Object[0]);
										if (canZoomOutObj != null && canZoomOutObj instanceof Boolean) {
											return (Boolean) canZoomOutObj;
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e2) {
			}
		}
		return canZoomOut;
	}

	private void initWebSettings() {
		WebSettings webSettings = getSettings();
		webSettings.setRenderPriority(RenderPriority.HIGH);
		webSettings.setTextSize(TextSize.NORMAL);// 设置字体
		
		webSettings.setJavaScriptEnabled(true);
		// 设置支持缩放
		webSettings.setBuiltInZoomControls(true);// 设置支持缩放
		webSettings.setSupportZoom(true);
		webSettings.setAllowFileAccess(true);// 设置可以访问文件
		webSettings.setDomStorageEnabled(true);
		// 设置屏幕自适应
		webSettings.setUseWideViewPort(true);// 实现双击放大，再次双击显示缩略图的功能。
		if(Integer.parseInt(Build.VERSION.SDK)<=10){
			webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		}else{
			webSettings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		}
		// webSettings.setLoadWithOverviewMode(true); //概览模式

		// 启用插件
		webSettings.setPluginsEnabled(true);

		// 设置缓存
		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webSettings.setAppCacheEnabled(true);
		webSettings.setAppCacheMaxSize(10 * 1204 * 1024);
		webSettings.setDatabaseEnabled(true);

	}

	public interface OnScrollListener {
		void onScroll();
	}

}
