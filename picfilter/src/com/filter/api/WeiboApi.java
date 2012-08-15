package com.filter.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.filter.activity.WeiboActivity;
import com.filter.api.WeiboApi.TokenManager.LoginInfo;
import com.filter.config.ApiConfig;
import com.filter.config.CacheConfig;
import com.filter.config.Config;
import com.filter.log.LogUtils;
import com.filter.utils.CompressPic;
import com.filter.utils.StringUtils;
import com.filter.utils.Utils;
import com.renren.AsyncRenren;
import com.renren.Renren;
import com.renren.ShareSetRequestParam;
import com.tencent.weibo.OAuth;
import com.tencent.weibo.T_API;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

/**
 * 微博相关api
 * @author Administrator
 *
 */
public class WeiboApi extends BaseApi {

    private static final String TAG = "WeiboApi";
    
    public WeiboApi(Context context) {
        super(context);
    }
    
    /**
     * 判断新浪微博是否绑定
     * @return
     */
    public static boolean hasSinaBind(Context context) {
        if (TokenManager.getInstance(context).find(WeiboActivity.SINA_ACCESS_URL) != null) { 
            return true;
        }
        return false;
    }
    
    /**
     * 判断腾讯微博是否绑定
     * @return
     */
    public static boolean hasTencentBind(Context context) {
        if (TokenManager.getInstance(context).find(WeiboActivity.QQ_Auth) != null) { //输入帐号密码
            return true;
        }
        return false;
    }
    
    /**
     * 判断人人是否绑定
     * @return
     */
    public static boolean hasRenrenBind(Context context) {
        Renren renren = new Renren(ApiConfig.RENREN_API_KEY, ApiConfig.RENREN_SECRET_KEY, ApiConfig.RENREN_APP_ID, context);
        if(renren!=null&&renren.getAccessToken()!=null) {
            return true;
        }
        return false;
    }
    
    /**
     * 判断腾讯空间是否绑定 
     * @return
     */
    public static boolean hasQzoneBind(Context context) {
        if (TokenManager.getInstance(context).find(WeiboActivity.Qzone_Auth) != null) { //输入帐号密码
            return true;
        }
        return false;
    }
    
    /**
     * 内容分享
     * @param list
     * @param content
     * @param picUrl
     * @param listener
     */
    public void shareContent(Context context,ArrayList<AccountType> list,String content,String url,String picUrl,ShareListener listener) {
        if(list==null||list.size()==0) {
            return ;
        }
        ShareContentTask task = new ShareContentTask(list, content,url, picUrl, listener);
        task.execute();
        
    }
    

    private void syncToRenren(Context context, String content,String url, String picUrl, ShareListener listener) {
        try {
            Renren renren = new Renren(ApiConfig.RENREN_API_KEY, ApiConfig.RENREN_SECRET_KEY, ApiConfig.RENREN_APP_ID, context);
            ShareSetRequestParam param = new ShareSetRequestParam(picUrl,"6");
            AsyncRenren aRenren = new AsyncRenren(renren);
            aRenren.publishShare(param, listener, true);
            
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        }
    }

    private static void syncToSina(Context context,String content, String imgUrl, ShareListener listener) {
        if (null != TokenManager.getInstance(context).find(WeiboActivity.SINA_ACCESS_URL)) {
            try {
                LoginInfo info = TokenManager.getInstance(context).find(WeiboActivity.SINA_ACCESS_URL);
                if (null != info) {
                    AccessToken accessToken = new AccessToken(info.username, info.password);
                    Weibo weibo = Weibo.getInstance();
                    weibo.setAccessToken(accessToken);
                }
                update(context,Weibo.getInstance(), ApiConfig.Sina_APP_KEY,content,imgUrl, "", "",listener);
            }catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static String update(Context context, Weibo weibo, String source,
            String status, String picUrl, String lon, String lat, ShareListener listener)
            throws MalformedURLException, IOException, WeiboException {
         WeiboParameters bundle = new WeiboParameters();
         String rlt = "";
         if(StringUtils.isNotNullOrEmpty(picUrl)) {
             bundle.add("source", source);
             bundle.add("url", picUrl);
             bundle.add("status", status);
             if (!TextUtils.isEmpty(lon)) {
                 bundle.add("lon", lon);
             }
             if (!TextUtils.isEmpty(lat)) {
                 bundle.add("lat", lat);
             }
//             String url = Weibo.SERVER + "statuses/upload.json";
             String url = Weibo.SERVER + "statuses/upload_url_text.json";
             AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
             weiboRunner.request(context, url, bundle, Utility.HTTPMETHOD_POST, listener);
         }else {
             bundle.add("source", source);
             bundle.add("status", status);
             if (!TextUtils.isEmpty(lon)) {
                 bundle.add("lon", lon);
             }
             if (!TextUtils.isEmpty(lat)) {
                 bundle.add("lat", lat);
             }
             String url = Weibo.SERVER + "statuses/update.json";
             AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
             weiboRunner.request(context, url, bundle, Utility.HTTPMETHOD_POST, listener);
         }
        return rlt;
    }

    private static void syncToTencent(Context context,String content,String imgUrl, ShareListener listener) {
        //检测微博
        if (null != TokenManager.getInstance(context).find(WeiboActivity.QQ_Auth)) {
            T_API tapi = new T_API();
            OAuth oauth = new OAuth(OAuth.URL_ACTIVITY_CALLBACK);
            LoginInfo info = TokenManager.getInstance(context).find(WeiboActivity.QQ_Auth);
            if (null != info) {
                oauth.setOauth_token(info.username);
                oauth.setOauth_token_secret(info.password);
            }
            try {
                String result = null;
                if(imgUrl==null) {
                    result = tapi.add(oauth, "json", content, null);
                }else {
                    result = tapi.add_pic(oauth, "json", content, null,imgUrl);
                }
                LogUtils.i(TAG, "token is " + info.username + " token_secret is " + info.password);
                LogUtils.i(TAG, "addPic is " + result);
                
                JSONTokener tokener = new JSONTokener(result);
                JSONObject input = new JSONObject(tokener);
                int retResult = input.optInt("ret", -1);
                if(retResult == 0){
                    if(listener!=null) {
                        listener.onReturnSucceedResult(AccountType.TENCENT,result);
                    }
                }else if(retResult == 3) {
                    int errcode = input.optInt("errcode", -1);
                    if(listener!=null) {
                        listener.onReturnFailResult(AccountType.TENCENT,errcode,result);
                    }
                }
            } catch (Exception e) {
                LogUtils.e(TAG, e);
                if(listener!=null) {
                    listener.onReturnFailResult(AccountType.TENCENT,e);
                }
            }
        }
    }
    
    protected class ShareContentTask extends AsyncTask<Void, Integer, Long> {
        private ArrayList<AccountType> list;
        private String content;//
        private String picUrl;
        private String url;
        private ShareListener listener;
        
        public ShareContentTask(ArrayList<AccountType> list, String content,String url, String picUrl,
                ShareListener listener) {
            this.list = list;
            this.content = content;
            this.url = url;
            this.picUrl = picUrl;
            this.listener = listener;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            if(list==null) {
                return null;
            }
        	//图片上传
			File tempFile = null;
			FileInputStream fileInputStream = null;
			try 
			{
				tempFile = CompressPic.compressPicAndWrite2File(picUrl, 800, CacheConfig.getCacheDir(), System.currentTimeMillis()+".jpg",100,0);
				if(tempFile!=null&&tempFile.exists()) {
					fileInputStream = new FileInputStream(tempFile);
				}else {
					fileInputStream = new FileInputStream(picUrl);
				}
				
			} 
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			
			picUrl = Config.Image_Server + Utils.postImage(context, fileInputStream);
			LogUtils.i(TAG, "picUrl is " + picUrl);
            
            for(AccountType type : list) {
                if(AccountType.SINA.equals(type)) {
                    syncToSina(context, content, picUrl,listener);
                }else if(AccountType.TENCENT.equals(type)) {
                    syncToTencent(context, content, picUrl,listener);
                }else if(AccountType.RENREN.equals(type)) {
                    syncToRenren(context, content,url, picUrl,listener);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
        }
        
        
    }
    
    
    
    public enum AccountType 
    {
        SINA, 
        TENCENT,
        RENREN,
        QZONE, 
    }
   
    
    /**
     * 解除微博绑定
     * @param type
     */
    public static boolean unBindAccount(AccountType type,Context context) {
        boolean result = false;
        switch (type) {
            case SINA:
                TokenManager.getInstance(context).del(WeiboActivity.SINA_ACCESS_URL);
                break;
            case TENCENT:
                result = TokenManager.getInstance(context).del(WeiboActivity.QQ_Auth);
                break;
            case RENREN:
                Renren renren = new Renren(ApiConfig.RENREN_API_KEY, ApiConfig.RENREN_SECRET_KEY, ApiConfig.RENREN_APP_ID, context);
                renren.logout(context);
                renren.clearAccessToken();
                break;
            case QZONE:
                TokenManager.getInstance(context).del(WeiboActivity.Qzone_Auth);
                break;

            default:
                break;
        }
        return result;
    }
    public static class TokenManager {
        private static final String TAG = "LoginManager";
       /**
        * 登录信息
        * @author Administrator
        *
        */
        public class LoginInfo {
            public String username = "";
            public String password = "";
            public String host = "";

            public LoginInfo(String host, String username, String password) {
                this.host = host;
                this.username = username;
                this.password = password;
            }
        }

        private final static String NAME_EXTERN = "accountlogin.txt";
        private static TokenManager instance = null;
        private static Hashtable<String, LoginInfo> hash = new Hashtable<String, LoginInfo>();
        private static Context mContext;

        private TokenManager() {
            load();
        }

        public void load() {
            LogUtils.d(TAG, "load is running");
            byte[] abytes = read(NAME_EXTERN);
            if (null == abytes) { //文件不存在，返回
                return;
            }
            try {
                String source = new String(abytes, "UTF-8");

                JSONTokener tokener = new JSONTokener(source);
                JSONObject input = new JSONObject(tokener);

                JSONArray ups = input.optJSONArray("login"); //
                int len = ups.length();
                for (int i = 0; i < len; i++) {
                    JSONArray info = ups.getJSONArray(i);
                    String host = info.getString(0);
                    String username = info.getString(1);
                    String password = info.getString(2);
                    LoginInfo li = new LoginInfo(host, username, password);
                    LogUtils.d(TAG, "load host is " + host + " username is " + username + " password is " + password);
                    hash.put(host, li);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        /**
         * 读取数据
         * @param path 相对路径，不可以/开头
         * @return
         */
        synchronized public final static byte[] read(String path) {
            FileInputStream fis = null;
            byte[] data = null;
            try {
                fis = mContext.openFileInput(path);
                data = new byte[(int) fis.available()];
                fis.read(data);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (null != fis) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return data;
        }
        public boolean save() {
            LogUtils.d(TAG, "save is running");
            try {
                JSONStringer json = new JSONStringer();
                json.object();

                //快捷键
                JSONArray ups = new JSONArray();
                Enumeration<String> keys = hash.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    LoginInfo li = hash.get(key);
                    if (null != key && null != li) {
                        JSONArray info = new JSONArray();
                        info.put(li.host);
                        info.put(li.username);
                        info.put(li.password);
                        LogUtils.d(TAG, "save host is " + li.host + " username is " + li.username + " password is " + li.password);
                        ups.put(info);
                    }
                }

                json.key("login").value(ups);
                json.endObject();

                byte[] abytes = json.toString().getBytes("UTF-8");
                write(NAME_EXTERN, abytes);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        /**
         * 将数据写入Chouti目录下
         * @param path 相对路径，不可以/开头
         * @param data
         * @return
         */
        synchronized public final static boolean write(String path, byte[] data) {
            LogUtils.d(TAG, "write is running");
            FileOutputStream fos = null;
            try {
                fos = mContext.openFileOutput(NAME_EXTERN,Context.MODE_PRIVATE);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                return false;
            }catch (IOException e) {
                e.printStackTrace();
                return false;
            }finally {
                if (null != fos) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return true;
        }
        public boolean add(String host, String username, String password) {
            LogUtils.d(TAG, "add is running");
            if (null == host || null == username || null == password) {
                return false;
            }

//          LoginInfo li = new LoginInfo(host, username, password);
            LoginInfo li = new LoginInfo(host, username, password);
            hash.put(host, li);
            return save();
        }
        /**
         * for kaixin
         */
        public boolean add(String host, String username, String password,String verifier) {
            LogUtils.d(TAG, "add is running");
            if (null == host || null == username || null == password) {
                return false;
            }

            LoginInfo li = new LoginInfo(host, username, password);
            hash.put(host, li);
            return save();
        }
        
        public boolean del(String host) {
            LogUtils.d(TAG, "del is running");
            if (null == host) {
                return false;
            }
            
            hash.remove(host);
            return save();
        }

        public final static TokenManager getInstance(Context context) {
            mContext = context;
            if (null == instance) {
                instance = new TokenManager();
            }
            return instance;
        }
        

        /**
         * 查找登录信息
         * @param host
         * @param requireauth true 必须在此次生命周期中，用户提交过登录请求
         * @return
         */
        public LoginInfo find(String host) {
            LogUtils.d(TAG, "find requireauth is running");
            Enumeration<String> keys = hash.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key.indexOf(host) > -1) {
                    LoginInfo ret = hash.get(key);
                    if (null == ret) {
                        return null;
                    }
                    LogUtils.d(TAG, "save host is " + ret.host + " username is " + ret.username + " password is " + ret.password);
                    return ret;
                }
            }
            return null;
        }
    }
}


