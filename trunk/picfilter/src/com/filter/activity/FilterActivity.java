package com.filter.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.filter.R;
import com.filter.api.WeiboApi;
import com.filter.config.CacheConfig;
import com.filter.imagefilters.BitmapFilter;
import com.filter.log.LogUtils;
import com.filter.utils.CompressPic;
import com.filter.utils.ImageCache;
import com.filter.utils.Utils;
import com.filter.view.ClipImageView;
import com.filter.view.GalleryView;
import com.filter.view.SharePopupWindow;

public class FilterActivity extends Activity implements OnClickListener{
	private static final String TAG = "FilterActivity";
	private static final int ID_SHARE = 2012;
	private static final int ID_IMAGE = 2016;
	private Button shareButton;
	private ArrayList<WeiboApi.AccountType> list;
	private String filePath = null;
	private SharePopupWindow sharePopup;
	private int[] str = {R.string.ice_filter,R.string.molten_filter,R.string.comic_filter,R.string.softglow_filter
			,R.string.glowingedge_filter,R.string.feather_filter,R.string.zoomblur_filter,R.string.lomo_filter,R.string.gray_filter,
			R.string.relief_filter,R.string.vague_filter,R.string.oil_filter,R.string.neon_filter,R.string.pixelate_filter,
			R.string.invert_filter,R.string.tv_filter,R.string.block_filter,R.string.old_filter,R.string.sharp_filter,R.string.light_filter};
	private LinearLayout feed_gallery;
	private ClipImageView imageView;
	
	private final int PROGRESS_WAIT_VISIBLE = 1;
	private final int PROGRESS_WAIT_GONE = 2;
	private String[] key = {"IceFilter","MoltenFilter", "ComicFilter", "SoftGlowFilter", "GlowingEdgeFilter"
			,"FeatherFilter", "ZoomBlurFilter", "LomoFilter","GrayFilter","ReliefFilter","VagueFileter","OidFilter",
			"NeonFilter","PixeFilter","InvertFilter","TVFilter","BlockFilter","OldFilter","SharpFilter","LightFilter"};
	private Bitmap mBitmap;
	private ProgressBar mProgressBar;
	private int styleId = 0;
	private FilterTask filterTask;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.i(TAG, "onCreate is running");
		initLayout();
	}

	private List<Map<String, Object>> initPicList() {
		int[] pic = new int[]{R.drawable.ice,R.drawable.molten,R.drawable.comic,R.drawable.softglow,
				R.drawable.glowingedge,R.drawable.feather,R.drawable.zoomblur,R.drawable.lomo,R.drawable.gray,R.drawable.relief,
				R.drawable.vague,R.drawable.oil,R.drawable.neon,R.drawable.pixelate,R.drawable.invert,R.drawable.tv,R.drawable.block,
				R.drawable.old,R.drawable.sharpen,R.drawable.light};
		 //添加图片 
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(int i=0;i<pic.length;i++) {
			Map<String, Object> map = new  HashMap<String,Object>();
			Avatar avatar = new Avatar();
			avatar.imageId = pic[i];
			avatar.textId = str[i];
			map.put("image",avatar);
			list.add(map);
		}
		return list;
	}

	private void initLayout() {
		RelativeLayout mainLayout = new RelativeLayout(this);
		mainLayout.setBackgroundColor(getResources().getColor(R.color.bg_filter));
		
		
		FrameLayout frameLayout = new FrameLayout(this);
		
		String path = getIntent().getStringExtra("path");
		filePath = path;
		FrameLayout.LayoutParams imageLP = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		ClipImageView image = new ClipImageView(this, 2, path, R.drawable.edit_image_mask);
		imageView = image;
		mBitmap = image.getContentBitmap();
		image.setId(ID_IMAGE);
		frameLayout.addView(image,imageLP);
		
		FrameLayout.LayoutParams proLP = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		proLP.gravity = Gravity.CENTER;
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setVisibility(View.GONE);
		mProgressBar = progressBar;
		frameLayout.addView(progressBar,proLP);
		
		RelativeLayout.LayoutParams frameLP = new RelativeLayout.LayoutParams((Utils.getScreenWidth(this)-Utils.dipToPixels(this, 10)),(Utils.getScreenWidth(this)-Utils.dipToPixels(this, 10)));
		frameLP.setMargins(0, Utils.dipToPixels(this, 5), 0, 0);
		frameLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mainLayout.addView(frameLayout,frameLP);
		
		RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		buttonLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		Button button = new Button(this);
		button.setId(ID_SHARE);
		button.setText(getResources().getString(R.string.share_popup_btn_share));
		mainLayout.addView(button,buttonLP);
		shareButton = button;
		button.setOnClickListener(this);
		
		LinearLayout line = new LinearLayout(this);
		line.setBackgroundResource(R.drawable.ic_filter_unit);
		
		LinearLayout.LayoutParams galleryLP = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		HorizontalScrollView scrollView = new HorizontalScrollView(this);
		scrollView.setHorizontalScrollBarEnabled(false);
		LinearLayout.LayoutParams pictureLP = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		LinearLayout pictureView = new LinearLayout(this);
		feed_gallery = pictureView;
		scrollView.addView(pictureView,pictureLP);
		line.addView(scrollView,galleryLP);
		
		RelativeLayout.LayoutParams lineLP = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		lineLP.setMargins(Utils.dipToPixels(this, 5),Utils.dipToPixels(this, 5), Utils.dipToPixels(this, 5), Utils.dipToPixels(this, 5));
		lineLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lineLP.addRule(RelativeLayout.ABOVE,ID_SHARE);
		lineLP.addRule(RelativeLayout.BELOW,ID_IMAGE);
		mainLayout.addView(line,lineLP);
		setContentView(mainLayout);
		
		updateUI();
	}

	private void updateUI() {
		feed_gallery.removeAllViews();
		LinearLayout.LayoutParams pictureLP = new LinearLayout.LayoutParams(Utils.dipToPixels(this, 50),LayoutParams.WRAP_CONTENT);
		pictureLP.setMargins(0, 0, Utils.dipToPixels(this, 2), 0);		
		List<Map<String, Object>> initPicList = initPicList();
		for(int i=0;i<initPicList.size();i++) {
			GalleryView galleryView = new GalleryView(this, (Avatar) initPicList.get(i).get("image"),R.drawable.ic_launcher,Utils.dipToPixels(this, 50));
			feed_gallery.addView(galleryView,pictureLP);
			galleryView.setId(i);
			galleryView.setOnClickListener(this);
		}
	}

	@Override
	protected void onStart() {
		if (sharePopup != null) {
			sharePopup.refreshWeiboButton();
		}
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		LogUtils.i(TAG, "onStop is running");
		ImageCache.clear();
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		LogUtils.i(TAG, "onBackPressed is running");
		ImageCache.clear();
		finish();
		super.onBackPressed();
	}
	
	@Override
	public void onClick(View v) {
		Bitmap tmpBitmap = null;
		switch (v.getId()) {
		case ID_SHARE:
			if(list!=null) {
				list.clear();
			}else {
				list = new ArrayList<WeiboApi.AccountType>();
			}
			showSharePopup();
			break;
			
		case 0:
			styleId = BitmapFilter.ICE_STYLE;//冰冻
			if (ImageCache.get(key[0]) != null) {
				tmpBitmap = ImageCache.get(key[0]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
			
		case 1:
			styleId = BitmapFilter.MOLTEN_STYLE;//熔铸
			if (ImageCache.get(key[1]) != null) {
				tmpBitmap = ImageCache.get(key[1]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
			
		case 2:
			styleId = BitmapFilter.COMIC_STYLE;//连环画
			if (ImageCache.get(key[2]) != null) {
				tmpBitmap = ImageCache.get(key[2]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
			
		case 3:		
			styleId = BitmapFilter.SOFTGLOW_STYLE;//柔化
			if (ImageCache.get(key[3]) != null) {
				tmpBitmap = ImageCache.get(key[3]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 4:
			styleId = BitmapFilter.GLOWINGEDGE_STYLE;//照亮边缘
			if (ImageCache.get(key[4]) != null) {
				tmpBitmap = ImageCache.get(key[4]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 5:	
			styleId = BitmapFilter.FEATHER_STYLE;//羽化
			if (ImageCache.get(key[5]) != null) {
				tmpBitmap = ImageCache.get(key[5]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
			
		case 6:
			styleId = BitmapFilter.ZOOMBLUR_STYLE;//缩放模糊
			if (ImageCache.get(key[6]) != null) {
				tmpBitmap = ImageCache.get(key[6]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 7:
			styleId = BitmapFilter.LOMO_STYLE;
			if (ImageCache.get(key[7]) != null) {
				tmpBitmap = ImageCache.get(key[7]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 8://黑白
			styleId = BitmapFilter.GRAY_STYLE;
			if (ImageCache.get(key[8]) != null) {
				tmpBitmap = ImageCache.get(key[8]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 9://浮雕
			styleId = BitmapFilter.RELIEF_STYLE;
			if (ImageCache.get(key[10]) != null) {
				tmpBitmap = ImageCache.get(key[10]);
				imageView.setContentBitmap(tmpBitmap);
			}
			
			break;
		case 10://模糊
			styleId = BitmapFilter.VAGUE_STYLE;
			if (ImageCache.get(key[11]) != null) {
				tmpBitmap = ImageCache.get(key[11]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 11://油画
			styleId = BitmapFilter.OIL_STYLE;
			if (ImageCache.get(key[9]) != null) {
				tmpBitmap = ImageCache.get(key[9]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 12://霓虹灯
			styleId  = BitmapFilter.NEON_STYLE;
			if (ImageCache.get(key[12]) != null) {
				tmpBitmap = ImageCache.get(key[12]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 13://像素化
			styleId = BitmapFilter.PIXELATE_STYLE;
			if (ImageCache.get(key[13]) != null) {
				tmpBitmap = ImageCache.get(key[13]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 14://反色
			styleId = BitmapFilter.INVERT_STYLE;
			if (ImageCache.get(key[14]) != null) {
				tmpBitmap = ImageCache.get(key[14]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 15://电视
			styleId = BitmapFilter.TV_STYLE;
			if (ImageCache.get(key[15]) != null) {
				tmpBitmap = ImageCache.get(key[15]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 16:;//版画
			styleId = BitmapFilter.BLOCK_STYLE;
			if (ImageCache.get(key[16]) != null) {
				tmpBitmap = ImageCache.get(key[16]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 17://怀旧
			styleId = BitmapFilter.OLD_STYLE;
			if (ImageCache.get(key[17]) != null) {
				tmpBitmap = ImageCache.get(key[17]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 18://锐化
			styleId = BitmapFilter.SHARPEN_STYLE;
			if (ImageCache.get(key[18]) != null) {
				tmpBitmap = ImageCache.get(key[18]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
		case 19://光照
			styleId = BitmapFilter.LIGHT_STYLE;
			if (ImageCache.get(key[19]) != null) {
				tmpBitmap = ImageCache.get(key[19]);
				imageView.setContentBitmap(tmpBitmap);
			}
			break;
			
		default:
			break;
		}
		
		if(filterTask!=null) {
			filterTask.cancel(true);
		}
		filterTask = new FilterTask();
		filterTask.execute();
	}
	
	
	class FilterTask extends AsyncTask<Void, Long, Integer> {

		private Bitmap tmpBitmap = null;

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				tmpBitmap  = BitmapFilter.changeStyle(mBitmap, styleId);
			} catch (OutOfMemoryError e) {
				File tempFile = CompressPic.compressPicAndWrite2File(filePath, 800, CacheConfig.getCacheDir(), System.currentTimeMillis()+".jpg",100,0);
				mBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
				tmpBitmap  = BitmapFilter.changeStyle(mBitmap, styleId);
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}



		@Override
		protected void onPostExecute(Integer result) {
			Message msg = Message.obtain();
			msg.obj = tmpBitmap;
			msg.what = PROGRESS_WAIT_GONE;
			mHandler.sendMessage(msg);
			
			filterTask = null;
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			mHandler.sendEmptyMessage(PROGRESS_WAIT_VISIBLE);
			super.onPreExecute();
		}
		
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case PROGRESS_WAIT_VISIBLE:
					setProgressBarIndeterminateVisibility(true);
					mProgressBar.setVisibility(View.VISIBLE);
					break;
				case PROGRESS_WAIT_GONE:
					setProgressBarIndeterminateVisibility(false);
					mProgressBar.setVisibility(View.GONE);
					Bitmap tmpBitmap =(Bitmap) msg.obj;
					imageView.setContentBitmap(tmpBitmap);
					ImageCache.put(key[msg.arg1], tmpBitmap);
					
					File file = Environment.getExternalStorageDirectory();
					File imageFile = new File(file,"Filter");
					if(imageFile!=null&&!imageFile.exists()) {
						imageFile.mkdirs();
					}
					writeImage(imageFile.getAbsolutePath() + "/" +getPhotoFileName(),tmpBitmap);
					break;
				default:
					break;
			}
		}
	};
	
	private String getPhotoFileName() {  
		 Date date = new Date(System.currentTimeMillis());  
		 SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMddHHmmss");  
		 return dateFormat.format(date) + ".png";
	 }  
	
	private boolean writeImage(String path, Bitmap bitmap)
	{
		if(bitmap==null) {
			return false;
		}
		Log.i("Path", "path is " + path);
		boolean result = false;
		
       try 
       { // catches IOException below 
           File f = new File(path); 
           FileOutputStream osf = new FileOutputStream(f);
           result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, osf);
           osf.flush();
       } 
       catch (IOException ioe) 
       {
       	ioe.printStackTrace();
       }
       
       return result;
   }

	private void showSharePopup() {
		if (sharePopup == null) {
			sharePopup = new SharePopupWindow(this, filePath, R.drawable.bg_share_popup_2);
		}
		int[] btnShareLoc = new int[2];
		shareButton.getLocationInWindow(btnShareLoc);
		int offY = Utils.getScreenHeight(this) - btnShareLoc[1] + Utils.dipToPixels(this, 5);
		int orientation = getResources().getConfiguration().orientation;
		sharePopup.show(shareButton, orientation, Utils.dipToPixels(this, 7), offY);
	}
}

