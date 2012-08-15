package com.filter.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.filter.R;
import com.filter.config.CacheConfig;
import com.filter.config.Config;
import com.filter.log.LogUtils;

public class PicTakeActivity extends Activity implements OnClickListener{
	private static final int GET_IMAGE = 2000;
	private static final String TAG = null;
	private Button cameraButton;
	private Button mediaButton;
	private File mCurrentPhotoFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		initComponents();
	}

	private void initComponents() {
		cameraButton = (Button)findViewById(R.id.button1);
		mediaButton = (Button)findViewById(R.id.button2);
		cameraButton.setOnClickListener(this);
		mediaButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			mCurrentPhotoFile = new File(CacheConfig.getCacheDir(),getPhotoFileName());
			Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			captureImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));

			startActivityForResult(captureImage, Activity.DEFAULT_KEYS_DIALER);
			break;
		case R.id.button2:
			Intent getImage = new Intent(Intent.ACTION_GET_CONTENT); 
            getImage.addCategory(Intent.CATEGORY_OPENABLE);
            //getImage.setType("image/jpeg"); 
            getImage.setType("image/*"); 
            getImage.putExtra("crop", "true");
            
            getImage.putExtra("aspectX", 1); 
            getImage.putExtra("aspectY", 1);
            getImage.putExtra("outputX", Config.Post_Image_Width);
            getImage.putExtra("outputY", Config.Post_Image_Height);
            getImage.putExtra("scale",           true);
            getImage.putExtra("noFaceDetection", true);
            getImage.putExtra("setWallpaper",    false);
            getImage.putExtra("return-data", true);
            //getImage.putExtra("output", Uri.fromFile(new File(SettingLoader.getTempAvatarStorePath()))); 
            //getImage.putExtra("outputFormat", "JPEG");
            
            startActivityForResult(Intent.createChooser(getImage, getResources().getString(R.string.pick_image)), GET_IMAGE);
			break;

		default:
			break;
		}
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{ 
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK)
		{
			if (GET_IMAGE == requestCode)
			{
				if (data != null)
				{
					final Bundle extras = data.getExtras();
					if (extras != null)
					{
						Bitmap bitmap = extras.getParcelable("data");
						String  path = (String) extras.get("filePath");
						if(bitmap==null && path!=null) {
							bitmap = BitmapFactory.decodeFile(path);
						}else if(bitmap==null&&path==null){
							if(mCurrentPhotoFile!=null) {
								bitmap = BitmapFactory.decodeFile(mCurrentPhotoFile.getAbsolutePath());
								if( mCurrentPhotoFile.exists() ) {
									mCurrentPhotoFile.delete();
								}
							}
						}
						String NeedPostAvatarPath = new File(getCacheDir(),getPhotoFileName()).getAbsolutePath();
						
						boolean writeResult = writeImage(NeedPostAvatarPath, bitmap);
						
						if (writeResult)
						{
							Intent intent = new Intent(this, FilterActivity.class);
							
							intent.putExtra("path", NeedPostAvatarPath);
				            
							startActivity(intent);
							
//							finish();
							
							return;
						}
					}
					else
					{
						return;
					}
				}
				
				
			}
			else
			{	
				if( mCurrentPhotoFile!=null ) {
					Intent edit = new Intent("com.android.camera.action.CROP");
			        edit.setDataAndType(Uri.fromFile(mCurrentPhotoFile), "image/*");
			        edit.putExtra("crop", "true");
			        edit.putExtra("aspectX", 1);
			        edit.putExtra("aspectY", 1);
			        edit.putExtra("outputX", Config.Post_Image_Width);
			        edit.putExtra("outputY", Config.Post_Image_Width);
			        edit.putExtra("scale",           true);
			        edit.putExtra("noFaceDetection", true);
			        edit.putExtra("setWallpaper",    false);
			        edit.putExtra("return-data", true);

			        startActivityForResult(edit, GET_IMAGE);
				} 
			}
		}

	}
	
	private boolean writeImage(String path, Bitmap bitmap)
	{
		boolean result = false;
		
        try 
        { // catches IOException below 
        	LogUtils.i(TAG,"writeImage");
            File f = new File(path); 
            FileOutputStream osf = new FileOutputStream(f);
            result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, osf);
            osf.flush();
        } 
        catch (IOException ioe) 
        {
        	LogUtils.i(TAG,ioe.toString());
        	result = false;
        }
        
        return result;
    }

	private String getPhotoFileName() {
		 Date date = new Date(System.currentTimeMillis());  
		 SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMddHHmmss");  
		 return dateFormat.format(date) + ".jpg";
	}
}
