/*
 * Copyright 2011-2012 Renren Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.renren;

import java.util.Set;
import java.util.TreeSet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.WebView;


/**
 * 封装对人人的请求，如：显示登录界面、退出登录、请求 人人 APIs等。
 * 
 * @author yong.li@opi-corp.com 
 */
public class Renren implements Parcelable {

	/**
	 * 版本MD5信息
	 */

	private static final String LOG_TAG = "Renren";

	/**
	 * 用来在Activity 等对象传递renren对象时用作读写Renren对象的key
	 */
	public static final String RENREN_LABEL = "Renren";

	/**
	 * 如果服务器redirect该地址，SDK会认为用户做了取消操作。
	 */
	public static final String CANCEL_URI = "rrconnect://cancel";

	/**
	 * 如果服务器redirect该地址，SDK会认为用户做了确认操作。
	 */
	public static final String SUCCESS_URI = "rrconnect://success";

	/**
	 * 人人登录和授权的地址
	 */
	public static final String AUTHORIZE_URL = "https://graph.renren.com/oauth/authorize";

	/**
	 * 接口支持的数据格式
	 */
	public static final String RESPONSE_FORMAT_JSON = "json";

	/**
	 * 接口支持的数据格式
	 */
	public static final String RESPONSE_FORMAT_XML = "xml";

	public static final String DEFAULT_REDIRECT_URI = "http://graph.renren.com/oauth/login_success.html";

	public static final String WIDGET_CALLBACK_URI = "http://widget.renren.com/callback.html";

	private static final String RESTSERVER_URL = "http://api.renren.com/restserver.do";

	private static final String KEY_API_KEY = "api_key";

	private static final String KEY_SECRET = "secret";

	private static final String KEY_APP_ID = "appid";


	/**
	 * 记录请求和响应日志的TAG
	 */
	private static final String LOG_TAG_REQUEST = "Renren-SDK-Request";

	private static final String LOG_TAG_RESPONSE = "Renren-SDK-Response";

	private String apiKey;

	private String secret;

	private String appId;

	private AccessTokenManager accessTokenManager;


	/**
	 * 构造Renren对象，开发者可以使用该类调用人人网提供的接口。
	 * 
	 * @param apiKey
	 * @param secretKey
	 * @param appId
	 */
	public Renren(String apiKey, String secret, String appId, Context context) {
		if (apiKey == null) {
			throw new RuntimeException("apiKey必须提供");
		}
		if (secret == null) {
			throw new RuntimeException("secret必须提供");
		}
		if (appId == null) {
			throw new RuntimeException("appId必须提供");
		}
		this.apiKey = apiKey;
		this.secret = secret;
		this.appId = appId;
		init(context);
	}

	public Renren(Parcel in) {
		Bundle bundle = Bundle.CREATOR.createFromParcel(in);
		apiKey = bundle.getString(KEY_API_KEY);
		secret = bundle.getString(KEY_SECRET);
		appId = bundle.getString(KEY_APP_ID);
		this.accessTokenManager = AccessTokenManager.CREATOR.createFromParcel(in);
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
			Log.w(LOG_TAG, "App miss permission android.permission.ACCESS_NETWORK_STATE! "
					+ "Some mobile's WebView don't display page!");
		} else {
			WebView.enablePlatformNotifications();
		}
		this.accessTokenManager = new AccessTokenManager(context);
		this.accessTokenManager.restoreSessionKey();
	}

	/**
	 * 用户授权后更新accessToken和sessionKey。
	 * 
	 * @param accessToken
	 * @throws RenrenError
	 *             换取sessionKey出现错误。
	 * @throws RuntimeException
	 *             出现其他错误。
	 */
	public void updateAccessToken(String accessToken) throws RenrenError, RuntimeException {
		if (accessTokenManager != null) {
			this.accessTokenManager.updateAccessToken(accessToken);
		}
	}

	/**
	 * 尝试读取sessionKey；如果用户在一天内登录过并且没有退出返回true。
	 * 
	 * @param context
	 * @return
	 * @deprecated 用init方法代替
	 */
	public boolean restorSessionKey(Context context) {
		this.init(context);
		if (this.accessTokenManager.isSessionKeyValid()) {
			return true;
		}
		return false;
	}

	/**
	 * 退出登录
	 * 
	 * @param context
	 * @return
	 */
	public String logout(Context context) {
		Util.clearCookies(context);
		this.accessTokenManager.clearPersistSession();
		return "true";
	}

	/**
	 * 调用 人人 APIs
	 * 
	 * @param parameters
	 * @return 返回结果为xml格式
	 */
	public String requestXML(Bundle parameters) {
		return this.request(parameters, "xml");
	}

	/**
	 * 调用 人人 APIs
	 * 
	 * @param parameters
	 * @return 返回结果为Json格式
	 */
	public String requestJSON(Bundle parameters) {
		return this.request(parameters, "json");
	}

	/**
	 * 上传照片到指定的相册。
	 * 
	 * @param albumId
	 * @param photo
	 * @param fileName
	 * @param description
	 * @param format
	 * @return
	 */
	public String publishPhoto(long albumId, byte[] photo, String fileName, String description, String format) {
		Bundle params = new Bundle();
		params.putString("method", "photos.upload");
		params.putString("aid", String.valueOf(albumId));
		params.putString("caption", description);

		String contentType = parseContentType(fileName);
		params.putString("format", format);
		if (isSessionKeyValid()) {
			params.putString("session_key", accessTokenManager.getSessionKey());
		}
		this.prepareParams(params);
		return Util.uploadFile(RESTSERVER_URL, params, "upload", fileName, contentType, photo);
	}


	/**
	 * 发送状态<br>
	 * 同步调用<br>
	 * status.set接口，参见http://wiki.dev.renren.com/wiki/Status.set
	 * 
	 * @param status
	 *          要发送的状态
	 * @return 
	 * 			若成功，返回发送的 {@link StatusSetResponseBean}对象，否则返回null
	 * @thorws RenrenException
	 * @throws Throwable 
	 */
	public StatusSetResponseBean publishStatus(StatusSetRequestParam status) throws RenrenException, Throwable {
		StatusHelper helper = new StatusHelper(this);
		return helper.publish(status);
	}

	/**
	 * 以startActivityForResult方式根据用户传入的
	 * Intent 对象启动相应的AbstractRenrenRequestActivity对象
	 * 
	 * @param activity 
	 * 			当前Activity
	 * @param intent 
	 * 			启动AbstractRenrenRequestActivity对象用到的intent
	 * @param requestCode
	 * 			启动Activity用到的RequestCode
	 */
	public void startRenrenRequestActivity(Activity activity, Intent intent, int requestCode) {
		intent.putExtra(RENREN_LABEL, this);
		activity.startActivityForResult(intent, requestCode);
	}

	private String parseContentType(String fileName) {
		String contentType = "image/jpg";
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".jpg"))
			contentType = "image/jpg";
		else if (fileName.endsWith(".png"))
			contentType = "image/png";
		else if (fileName.endsWith(".jpeg"))
			contentType = "image/jpeg";
		else if (fileName.endsWith(".gif"))
			contentType = "image/gif";
		else if (fileName.endsWith(".bmp"))
			contentType = "image/bmp";
		else
			throw new RuntimeException("不支持的文件类型'" + fileName + "'(或没有文件扩展名)");
		return contentType;
	}

	/**
	 * 调用 人人 APIs
	 * 
	 * @param parameters
	 * @param format
	 *            json or xml
	 * @return
	 */
	private String request(Bundle parameters, String format) {
		parameters.putString("format", format);
		if (isSessionKeyValid()) {
			parameters.putString("session_key", accessTokenManager.getSessionKey());
		}
		this.prepareParams(parameters);
		logRequest(parameters);
		String response = Util.openUrl(RESTSERVER_URL, "POST", parameters);
		logResponse(parameters.getString("method"), response);
		return response;
	}

	/**
	 * 记录请求log
	 */
	private void logRequest(Bundle params) {
		if (params != null) {
			String method = params.getString("method");
			if (method != null) {
				StringBuffer sb = new StringBuffer();
				sb.append("method=").append(method).append("&").append(params.toString());
				Log.i(LOG_TAG_REQUEST, sb.toString());
			} else {
				Log.i(LOG_TAG_REQUEST, params.toString());
			}
		}
	}

	/**
	 * 记录响应log
	 * @param response
	 */
	private void logResponse(String method, String response) {
		if (method != null && response != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("method=").append(method).append("&").append(response);
			Log.i(LOG_TAG_RESPONSE, sb.toString());
		}
	}

	private void prepareParams(Bundle params) {
		params.putString("api_key", apiKey);
		params.putString("v", "1.0");
		params.putString("call_id", String.valueOf(System.currentTimeMillis()));
		params.putString("xn_ss", "1");// sessionSecret作为加密密钥

		StringBuffer sb = new StringBuffer();
		Set<String> keys = new TreeSet<String>(params.keySet());
		for (String key : keys) {
			sb.append(key);
			sb.append("=");
			sb.append(params.getString(key));
		}
		sb.append(accessTokenManager.getSessionSecret());
		params.putString("sig", Util.md5(sb.toString()));
	}

	/**
	 * 判断sessionKey是否有效。
	 * 
	 * @return boolean
	 */
	public boolean isSessionKeyValid() {
		return this.accessTokenManager.isSessionKeyValid();
	}

	public boolean isAccessTokenValid() {
		String at = this.accessTokenManager.getAccessToken();
		if (at != null && at.trim().length() > 0) {
			return true;
		}
		return false;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public String getSecret() {
		return this.secret;
	}

	public String getAppId() {
		return this.appId;
	}

	public String getSessionKey() {
		if (accessTokenManager != null) {
			return accessTokenManager.getSessionKey();
		}
		return null;
	}

	public String getAccessToken() {
		if (accessTokenManager != null) {
			return accessTokenManager.getAccessToken();
		}
		return null;
	}

	/**
	 * 获取当前登录用户的uid
	 * 
	 * @return
	 */
	public long getCurrentUid() {
		return accessTokenManager.getUid();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		if (apiKey != null) {
			bundle.putString(KEY_API_KEY, apiKey);
		}
		if (secret != null) {
			bundle.putString(KEY_SECRET, secret);
		}
		if (appId != null) {
			bundle.putString(KEY_APP_ID, appId);
		}
		bundle.writeToParcel(dest, flags);
		this.accessTokenManager.writeToParcel(dest, flags);
	}

	public static final Parcelable.Creator<Renren> CREATOR = new Parcelable.Creator<Renren>() {
		public Renren createFromParcel(Parcel in) {
			return new Renren(in);
		}

		public Renren[] newArray(int size) {
			return new Renren[size];
		}
	};

    public void clearAccessToken() {
        if (accessTokenManager != null) {
            this.accessTokenManager.clearAccessToken();
        }
    }
}
