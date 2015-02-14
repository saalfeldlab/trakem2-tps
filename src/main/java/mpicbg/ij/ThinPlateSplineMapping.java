package mpicbg.ij;

import ij.process.ImageProcessor;

import java.util.concurrent.atomic.AtomicInteger;

import jitk.spline.ThinPlateR2LogRSplineKernelTransform;

public class ThinPlateSplineMapping {

	ThinPlateR2LogRSplineKernelTransform xfm;
	public ThinPlateSplineMapping( final ThinPlateR2LogRSplineKernelTransform xfm ){
		this.xfm = xfm;
	}

	public ThinPlateR2LogRSplineKernelTransform getTransform(){
		return xfm;
	}

	public void map(final ImageProcessor source, final ImageProcessor target) {
		ThinPlateSplineMapping.mapInterval(xfm, source, target);
	}

	public void mapInterpolated(final ImageProcessor source, final ImageProcessor target) {
		System.out.println("mapping interpolated");
		ThinPlateSplineMapping.mapInterval(xfm, source, target);
	}

	final static public void mapInterval(
			final ThinPlateR2LogRSplineKernelTransform 	xfm,
			final ImageProcessor 		src,
			final ImageProcessor 		tgt )
	{
		final int w = tgt.getWidth()  - 1;
		final int h = tgt.getHeight() - 1;

		for(int x=0; x<w; x++)for(int y=0; y<h; y++)
		{
			final double[] srcPt  = xfm.apply( new double[]{x, y});
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
			final int k = i.getAndIncrement();
		}

	}


}
