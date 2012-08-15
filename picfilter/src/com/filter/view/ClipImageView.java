package com.filter.view;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.filter.R;
import com.filter.utils.Utils;

public class ClipImageView extends View 
{
	private Paint mPaint = null;
	private float mRound = 0;
	
	
	private Bitmap mBitmap = null;
	
	private Drawable mShapeMask = null;
	
	
	public ClipImageView(Context context, int round, String imagePath, int maskShapeRes)
	{
		super(context);
		
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(0);
		
		mRound =  Utils.dipToPixels(getContext(), round);
		
		if (imagePath == null)
		{
			InputStream is = getContext().getResources().openRawResource(R.drawable.ic_launcher);
			mBitmap = BitmapFactory.decodeStream(is);
		}
		else
		{
			File file = new File(imagePath);
			if (file.exists())
			{
				//BitmapFactory.Options options=new BitmapFactory.Options(); 
				//options.inSampleSize = 4;
				
				mBitmap = BitmapFactory.decodeFile(imagePath, null);
			}
			else
			{
				InputStream is = getContext().getResources().openRawResource(R.drawable.ic_launcher);
				mBitmap = BitmapFactory.decodeStream(is);
			}
		}
		
		
		mShapeMask = getResources().getDrawable(maskShapeRes);
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		// TODO Auto-generated method stub
		super.draw(canvas);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		
		RectF backgroundRoundRect = new RectF(0, 0, getWidth(), getHeight());
		
		canvas.save();
		
		int lineWidth = Utils.dipToPixels(getContext(), 3);
		RectF pathRect = new RectF(lineWidth, lineWidth, getWidth()-lineWidth, getHeight()-lineWidth); 
		
		Path path = new Path();
		path.addRoundRect(pathRect, mRound, mRound, Path.Direction.CCW);
		
		canvas.clipPath(path);
		
		if (mBitmap != null)
		{
			//int srcLeft = (mBitmap.getWidth()-getWidth())/2;
			//int srcRight = (mBitmap.getHeight()-getHeight())/2;
			//Rect srcRect = new Rect(srcLeft, srcRight, srcLeft+getWidth(), srcRight+getHeight()); 
			Rect srcRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
			canvas.drawBitmap(mBitmap, srcRect, backgroundRoundRect, mPaint);
		}
		
		canvas.restore();
		
		mShapeMask.setBounds(0, 0, getRight()-getLeft(), getBottom()-getTop());
		mShapeMask.draw(canvas);
		
	}
	
	//
	public void rotateImage(int rad)
	{
		Matrix matrix = new Matrix();  
		
	    matrix.postRotate(rad);
	    
	    mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), 
	    		mBitmap.getHeight(), matrix, true);
	    
	    invalidate();
	}
	
	public Bitmap getContentBitmap()
	{
		return mBitmap;
	}
	
	public void setContentBitmap(Bitmap bitmap)
	{
		this.mBitmap = bitmap;
		invalidate();
	}


	
}
