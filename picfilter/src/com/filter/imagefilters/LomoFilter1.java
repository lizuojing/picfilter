package com.filter.imagefilters;

import android.graphics.Bitmap;


public class LomoFilter1 implements ImageFilterInterface {

	private BrightContrastFilter contrastFx;
	private GradientMapFilter gradientMapFx;
	private ImageBlender blender = new ImageBlender();
	private VignetteFilter vignetteFX;
	private NoiseFilter noiseFx;

	private ImageData image;
	
	public LomoFilter1(Bitmap bm) {
		image = new ImageData(bm);
		contrastFx = new BrightContrastFilter(image);
		contrastFx.BrightnessFactor = 0.05f;
        contrastFx.ContrastFactor = 0.5f;
     
        blender.Mixture = 0.5f;
        blender.Mode = com.filter.imagefilters.ImageBlender.BlendMode.Multiply;
    
        vignetteFX = new VignetteFilter(image);
        vignetteFX.Size = 0.6f;

	}
	public ImageData imageProcess() {
		// TODO Auto-generated method stub
		 ImageData tempImg = contrastFx.imageProcess();
		 noiseFx = new NoiseFilter(tempImg);
		 noiseFx.Intensity = 0.02f;
         tempImg = noiseFx.imageProcess();
         gradientMapFx = new GradientMapFilter(tempImg);
         image = gradientMapFx.imageProcess();
         image = blender.Blend(image, tempImg);
         vignetteFX = new VignetteFilter(image);
         vignetteFX.Size = 0.6f;
         image = vignetteFX.imageProcess();
         return image;
	}

}
