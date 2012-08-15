package com.filter.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.filter.config.Config;
import com.filter.log.LogUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.view.WindowManager;

public class Utils {
	
	public static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

	public static final int byteToShort(byte[] bytes) {
		return (bytes[0] << 8) + (bytes[1] & 0xFF);
	}

	public static boolean isSDCardEnable() {
		String SDState = Environment.getExternalStorageState();
		if (SDState != null && SDState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	public final static class APNData
	{
		// other infos....
		
		public String proxy = null;
		public int port = 0;
		
		public APNData(String proxy, int port)
		{
			this.proxy = proxy;
			this.port = port;
		}
		
		public boolean hasProxy()
		{
			return (proxy != null && !proxy.equals("")) && (port != 0);
		}
	}
	
	public static APNData getPreferAPNData(Context context)
	{
		//判断wifi是否可用
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
    	NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo(); 
    	boolean WifiOK= ( activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI );
		if(!WifiOK){
			Cursor cursor = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
			if (cursor != null && cursor.moveToFirst())
			{
				String proxy = cursor.getString(cursor.getColumnIndex("proxy"));
				int port = cursor.getInt(cursor.getColumnIndex("port"));
				String mcc = cursor.getString(cursor.getColumnIndex("mcc"));   
	            String mnc = cursor.getString(cursor.getColumnIndex("mnc"));
	            if( mcc!=null && mcc.equals("460") && mnc!=null && (mnc.equals("03")|| mnc.equals("05")) ) {
	            	//电信手机， 电信wap接入点get请求返回404
	            	cursor.close();
	            	return null;
	            }
				
				cursor.close();
				return new APNData(proxy, port);
			}
		}
		
		return null;
	}
	
	/**
	 * 图片上传
	 */
	public static String postImage(Context context, InputStream inputSteam)
	{
		String result = null;
		
		if (inputSteam == null)
		{
			return null;
		}
		
		String BOUNDARY = "---------------------------dakslfdafdfafdafdaf";
		
		StringBuffer sb = new StringBuffer();
		sb = sb.append("--");
		sb = sb.append(BOUNDARY);
		sb = sb.append("\r\n");
		sb = sb.append("Content-Disposition: form-data; name=\""+"upload"+"\"; filename=\""+"avatar.jpg"+"\"\r\n");
		sb = sb.append("Content-Type: Content-Type: image/jpeg\r\n\r\n");
		byte[] data = sb.toString().getBytes();
		byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
		
		try
		{			
			byte[] file=new byte[inputSteam.available()];
			inputSteam.read(file);
	        
			URL url = new URL(Config.Image_Post_URL);
			
			HttpURLConnection connection = null;
			
			APNData apn = Utils.getPreferAPNData(context);
        	
        	if (apn != null && apn.hasProxy())
            {
                try
                {
                     Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(apn.proxy, apn.port));
                     connection = (HttpURLConnection) url.openConnection(proxy);
                }
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            } 
        	else
        	{
        		connection = (HttpURLConnection)url.openConnection();
            }
			
			connection.setDoOutput(true);
			connection.setDoInput(true);   
			connection.setRequestMethod("POST");   
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);   
			connection.setRequestProperty("Content-Length", String.valueOf(data.length + file.length + end_data.length));   
	        connection.setUseCaches(false);   
	        connection.connect();   
	   
	        DataOutputStream out = new DataOutputStream(connection.getOutputStream());   
	        out.write(data);   
	        out.write(file);   
	        out.write(end_data);   
	   
	        out.flush();   
	        out.close();
	        
	        if (connection.getResponseCode() == 200) 
	        {
		        InputStream inStream = connection.getInputStream();
		        result = Utils.inputStream2String(inStream);
		        
		        LogUtils.i("upload", "uploaded image ---- " + result);
		        
		        if (result != null && (result.equalsIgnoreCase("3001") || result.equalsIgnoreCase("3002")))
		        {
		        	result = null;
		        }
	        }
	        else
	        {
	        	LogUtils.i("upload", "connection.getResponseCode()=" + connection.getResponseCode());
	        }
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 将dip转换为pix
	 * 
	 * @param context
	 * @param dip
	 * @return
	 */
	public static int dipToPixels(Context context, float dip) {
		return (int) (context.getResources().getDisplayMetrics().density * dip);
	}

	public static String inputStream2String(InputStream inStream) throws IOException
	{
		/*
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		int  i =-1; 
		while((i = inStream.read()) != -1)
		{ 
			baos.write(i);
		}
		
		return baos.toString();*/
		
		if (inStream != null)
		{
			StringWriter writer = new StringWriter();
			char[] buffer = new char[1024];
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
				
			int n;

			while ((n = reader.read(buffer)) != -1)
			{
				writer.write(buffer, 0, n);
			}

			return writer.toString();
		}
		else
		{
			return "";
		}
	}
	/**
	 * 获取屏幕宽度(像素)
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return windowManager.getDefaultDisplay().getWidth();
	}


	/**
	 * 获取屏幕高度(像素)
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return windowManager.getDefaultDisplay().getHeight();
	}

	/**
	 * 根据不同设备的分辨率获取适合的图片尺寸
	 * 
	 * @param url
	 * @return
	 */
	public static String getFitScreenImageUrl(String url, int size) {
		String returnUrl = "";

		int i = url.lastIndexOf(".");
		int length = url.length();
		String urlName = "";
		String suffix = "";
		if (i < length && i > 0) {
			urlName = url.substring(0, i);
			suffix = url.substring(i, length);
		} else {
			urlName = url;
		}
		int imageW = size;
		/**
		 * 锁定比例,按照固定尺寸取小值缩小(只允许缩小) 协议类型 < width >x< height>) 例如：600x200)
		 * 首先该协议转换后的图片不能是放大的，缩小的时候按照比例，取小值进行缩小
		 */
		returnUrl = urlName + "=C" + imageW + "x" + imageW + suffix + "?quality=70";
		return returnUrl;
	}

}
