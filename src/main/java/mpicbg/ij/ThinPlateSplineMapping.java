package mpicbg.ij;

import java.util.concurrent.atomic.AtomicInteger;

import jitk.spline.KernelTransformFloat;
import jitk.spline.KernelTransformFloatSeparable;
import ij.process.ImageProcessor;

public class ThinPlateSplineMapping {
	
	KernelTransformFloat xfm; 
	public ThinPlateSplineMapping( KernelTransformFloat xfm ){
		this.xfm = xfm;
	}
	
	public KernelTransformFloat getTransform(){
		return xfm;
	}
	
	public void map(ImageProcessor source, ImageProcessor target) {
		ThinPlateSplineMapping.mapInterval(xfm, source, target);
	}

	public void mapInterpolated(ImageProcessor source, ImageProcessor target) {
		System.out.println("mapping interpolated");
		ThinPlateSplineMapping.mapInterval(xfm, source, target);
	}
	
	final static public void mapInterval(
			final KernelTransformFloat 	xfm,
			final ImageProcessor 		src,
			final ImageProcessor 		tgt )
	{
		final int w = tgt.getWidth()  - 1;
		final int h = tgt.getHeight() - 1;
		
		for(int x=0; x<w; x++)for(int y=0; y<h; y++)
		{
			float[] srcPt  = xfm.transformPoint( new float[]{x, y});
			tgt.putPixel( x, y, 
					src.getPixelInterpolated( srcPt[0], srcPt[1] ));
		}		
	}
	
	final static public void mapInterval(
			final KernelTransformFloatSeparable 	xfm,
			final ImageProcessor 		src,
			final ImageProcessor 		tgt )
	{
		final int w = tgt.getWidth()  - 1;
		final int h = tgt.getHeight() - 1;
		
		for(int x=0; x<w; x++)for(int y=0; y<h; y++)
		{
			float[] srcPt  = xfm.transformPoint( new float[]{x, y});
			tgt.putPixel( x, y, 
					src.getPixelInterpolated( srcPt[0], srcPt[1] ));
		}		
	}
	
	
	// TODO mess around with this...
	final static private class MapThinPlateSplineInterpolatedThread extends Thread
	{
		
		final private AtomicInteger i;
		
		MapThinPlateSplineInterpolatedThread(
				final AtomicInteger i )
		{
			this.i = i;
		}
		
		@Override
		final public void run()
		{
			int k = i.getAndIncrement();
		}
		
	}


}