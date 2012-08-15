package com.filter.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.filter.log.LogUtils;

public class CompressPic {
	private static final String TAG = "CompressPic";
	private static final long CONSTANCESIZE = 200*1024;


	public static Bitmap compressBitmap(Bitmap bm, final int newW, final int newH, int oriention) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		Matrix matrix = new Matrix();
		LogUtils.i(TAG, "oriention =="+oriention);
		matrix.postRotate(oriention);
		if ((newW < width) || (newH < height)) {
			float scaleWidth = ((float) newW) / width;
			float scaleHeight = ((float) newH) / height;
			float scale = scaleWidth < scaleHeight ? scaleWidth : scaleHeight;
			matrix.postScale(scale, scale);
			Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
			if(bm != null) {
				bm.recycle();
			}
			return bitmap;
		} else {
			return bm;
		}
	}

	public static Bitmap compressPic(String path, int oriention) {
		int newWidth=0,newHeight=0;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bb = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false;
		int formerToScal = 100;
		int be = Math.max(options.outHeight, options.outWidth) / formerToScal;// 应该直接除200的，但这里出20是为了增加一位数的精度
		if (be % 10 != 0)
			be += 10; // 尽量取大点图片，否则会模糊
		be = be / 10;
		if (be <= 0) // 判断200是否超过原始图片高度
			be = 1; // 如果超过，则不进行缩放

		// int be = Math.round( Math.max(options.outHeight,options.outWidth) /
		// toScal);
		options.inSampleSize = be;
		try {
			bb = BitmapFactory.decodeFile(path, options);
		} catch (OutOfMemoryError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			options.inSampleSize = be+2;
			bb = BitmapFactory.decodeFile(path, options);
		}
		
		LogUtils.i(TAG, " options.outHeight :" + options.outHeight + " options.outWidth" + options.outWidth);
		if(options.outHeight<=320&&options.outWidth<=320){
			//不做处理
			LogUtils.i(TAG, "nothing to do ");
		}else {
			if(options.outHeight>options.outWidth) {
				newWidth = 320*options.outWidth/options.outHeight;
				newHeight = 320;
			}else if(options.outHeight<options.outWidth){
				newHeight = 320*options.outHeight/options.outWidth;
				newWidth = 320;
			}
			bb = compressBitmap(bb, newWidth, newHeight,oriention);// width height
//			LogUtils.i(TAG, " be=" + be);
			Log.i(TAG, " compress   H:" + bb.getHeight() + "w" + bb.getWidth());
			LogUtils.i(TAG, "decodeFile is " + bb);
		}
		return bb;
	}

	public static File write2File(Bitmap bitmap, File dir, String name, int quality) {
		LogUtils.i(TAG, " write2File start  ");
		if (!dir.exists()) {
			dir.mkdirs();
			LogUtils.i(TAG, " mSavedDir.mkdirs(); ");
		}
		if(bitmap==null) {
			return null;
		}
		
		File f = new File(dir, name);// 构建文件
		LogUtils.i(TAG, "compress file is " + (f!=null?f.getName():"no file"));
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogUtils.i(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if(bitmap != null) {
				bitmap.recycle();
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return f;
	}

	public static File compressPicAndWrite2File(String path, int toScal, File fileDir, String fileName, int quality,int oriention) {
		Bitmap bitmap = compressPic(path,oriention);
		File file = write2File(bitmap, fileDir, fileName, quality);
		bitmap.recycle();
		bitmap = null;
		return file;
	}
}