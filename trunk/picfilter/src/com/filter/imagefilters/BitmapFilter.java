package com.filter.imagefilters;

import android.graphics.Bitmap;


/**
 *  滤镜效果的类，定义了基本的渲染方法
 * @author start
 *
 */
public class BitmapFilter {
	/**
	 * 所有滤镜效果的id
	 */
	public static final int GRAY_STYLE = 1; // 黑白效果
	public static final int RELIEF_STYLE = 2; // 浮雕效果
	public static final int VAGUE_STYLE = 3; // 模糊效果
	public static final int OIL_STYLE = 4; // 油画效果
	public static final int NEON_STYLE = 5; // 霓虹灯效果
	public static final int PIXELATE_STYLE = 6; // 像素化效果
	public static final int TV_STYLE = 7; // TV效果
	public static final int INVERT_STYLE = 8; // 反色效果
	public static final int BLOCK_STYLE = 9; // 版画
	public static final int OLD_STYLE = 10; // 怀旧效果
	public static final int SHARPEN_STYLE = 11; // 锐化效果
	public static final int LIGHT_STYLE = 12; // 光照效果
	public static final int LOMO_STYLE = 13;
	public static final int ZOOMBLUR_STYLE = 14;//缩放模糊
	public static final int FEATHER_STYLE = 15;
	public static final int ICE_STYLE = 16;
	public static final int MOLTEN_STYLE = 17;
	public static final int COMIC_STYLE = 18;
	public static final int SOFTGLOW_STYLE = 19;
	public static final int GLOWINGEDGE_STYLE = 20;
	
	/**
	 * 设置滤镜效果，
	 * @param bitmap
	 * @param styeNo, 效果id
	 */
	public static Bitmap changeStyle(Bitmap bitmap, int styleNo) {
		if (styleNo == GRAY_STYLE) {
			//return changeToGray(bitmap);
			return GrayFilter.changeToGray(bitmap);
		}
		else if (styleNo == RELIEF_STYLE) {
			return ReliefFilter.changeToRelief(bitmap);
		}
		else if (styleNo == VAGUE_STYLE) {
			return VagueFilter.changeToVague(bitmap);
		}
		else if (styleNo == OIL_STYLE) {
			return OilFilter.changeToOil(bitmap);
		}
		else if (styleNo == NEON_STYLE) {
			return NeonFilter.changeToNeon(bitmap);
		}
		else if (styleNo == PIXELATE_STYLE) {
			return PixelateFilter.changeToPixelate(bitmap);
		}
			
		else if (styleNo == TV_STYLE) {
			return TvFilter.changeToTV(bitmap);
		}
		else if (styleNo == INVERT_STYLE) {
			return InvertFilter.chageToInvert(bitmap);
		}
		else if (styleNo == BLOCK_STYLE) {
			return BlockFilter.changeToBrick(bitmap);
		}
		else if (styleNo == OLD_STYLE) {
			return OldFilter.changeToOld(bitmap);
		}
		else if (styleNo == SHARPEN_STYLE) {
			return SharpenFilter.changeToSharpen(bitmap);
		}
		else if (styleNo == LIGHT_STYLE) {
			return LightFilter.changeToLight(bitmap);
		}
		else if (styleNo == LOMO_STYLE) {
			return LomoFilter.changeToLomo(bitmap);
		} 
		else if (styleNo == ZOOMBLUR_STYLE) {
			return new ZoomBlurFilter(bitmap, 30).imageProcess().getDstBitmap();
		}
		else if (styleNo == FEATHER_STYLE) {
			return  new FeatherFilter(bitmap).imageProcess().getDstBitmap();
		} 
		else if (styleNo == ICE_STYLE) {
			return new IceFilter(bitmap).imageProcess().getDstBitmap();
		} 
		else if (styleNo == MOLTEN_STYLE) {
			return new MoltenFilter(bitmap).imageProcess().getDstBitmap();
		} 
		else if (styleNo == COMIC_STYLE) {
			return new ComicFilter(bitmap).imageProcess().getDstBitmap();
		}
		else if (styleNo == SOFTGLOW_STYLE) {
			return new SoftGlowFilter(bitmap,10, 0.1f, 0.1f).imageProcess().getDstBitmap();
		} 
		else if (styleNo == GLOWINGEDGE_STYLE) {
			return new GlowingEdgeFilter(bitmap).imageProcess().getDstBitmap();
		}
			return bitmap;
	}
	
	

}

















