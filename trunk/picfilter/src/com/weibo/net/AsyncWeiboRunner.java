/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weibo.net;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.filter.api.ShareListener;
import com.filter.api.WeiboApi.AccountType;
import com.filter.log.LogUtils;


import android.content.Context;


/**
 * Encapsulation main Weibo APIs, Include: 1. getRquestToken , 2. getAccessToken, 3. url request.
 * Implements a weibo api as a asynchronized way. Every object used this runner should implement interface RequestListener.
 *
 * @author  ZhangJie (zhangjie2@staff.sina.com.cn)
 */
public class AsyncWeiboRunner {
	
	private Weibo mWeibo;
	
	public AsyncWeiboRunner(Weibo weibo){
		this.mWeibo = weibo;
	}
	
	public void request(final Context context, 
			final String url, 
			final WeiboParameters params, 
			final String httpMethod, 
			final ShareListener listener){
		new Thread(){
			@Override public void run() {
                try {
					String resp = mWeibo.request(context, url, params, httpMethod, mWeibo.getAccessToken());
                    LogUtils.i("WeiboApi", "resp is " + resp);
                    int ret = 0;
                    String msg = null;
                    try {
                        JSONObject json = new JSONObject(resp);
                        ret = json.getInt("error_code");
                        msg = json.getString("error");
                        if(ret!=0) {
                            if(listener!=null) {
                                listener.onReturnFailResult(AccountType.SINA,ret , msg);
                            }
                        }else {
                            if(listener!=null) {
                                listener.onReturnSucceedResult(AccountType.SINA,resp); 
                            }
                        }
                    } catch (JSONException e) {
                        if(listener!=null) {
                            listener.onReturnSucceedResult(AccountType.SINA,resp); 
                        }
                    }
                } catch (WeiboException e) {
                    LogUtils.e("WeiboApi", e);
                    if(listener!=null) {
                        listener.onReturnFailResult(AccountType.SINA,e);
                    }
                }
            }
		}.run();
		
	}
	
	
    public static interface RequestListener {

        public void onComplete(String response);

        public void onIOException(IOException e);

        public void onError(WeiboException e);

    }
	
}
