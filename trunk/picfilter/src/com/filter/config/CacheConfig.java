package com.filter.config;

import java.io.File;

import android.os.Environment;

/**
 * 缓存的相关配置
 */
public class CacheConfig {
	
	private static final String ROOT_DIR = Environment.getExternalStorageDirectory() + "/filter/";
	private static final String IMG_DIR = CacheConfig.ROOT_DIR + "img/";
	private static final String SAVE_IMG_DIR = CacheConfig.ROOT_DIR + "save_img/";
	private static final String Cache_Dir = "/Filter/cache";

	public static String getSaveImageDir() {
		File file = new File(SAVE_IMG_DIR);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath()+"/";
	}
	
	public static String getRootDir() {
		File file = new File(ROOT_DIR);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath()+"/";
	}

	public static String getImgDir() {
		File file = new File(IMG_DIR);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath()+"/";
	}

	public static File getCacheDir()
	{	
		File cacheDir = new File(Environment.getExternalStorageDirectory(), Cache_Dir);
		if (!cacheDir.exists())
		{
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
	
}
