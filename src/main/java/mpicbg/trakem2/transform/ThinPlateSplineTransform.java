package mpicbg.trakem2.transform;

import ij.IJ;
import jitk.spline.ThinPlateR2LogRSplineKernelTransform;

public class ThinPlateSplineTransform implements CoordinateTransform {

	private ThinPlateR2LogRSplineKernelTransform tps;

	public ThinPlateSplineTransform() {
	}

	public ThinPlateSplineTransform(final ThinPlateR2LogRSplineKernelTransform tps) {
		this.tps = tps;
	}

	@Override
	public float[] apply(final float[] location) {

		final float[] out = new float[location.length];
		for (int i = 0; i < location.length; i++) {
			out[i] = location[i];
		}

		applyInPlace(out);
		return out;
	}

	@Override
	public void applyInPlace(final float[] location) {

		tps.applyInPlace(location);
	}

	@Override
	public void init(final String data) throws NumberFormatException {

		final String[] fields = data.split("\\s+");

		int i = 0;

		IJ.log(fields[ i ]);

		final int ndims = Integer.parseInt(fields[++i]);
		final int nLm = Integer.parseInt(fields[++i]);

		double[][] aMtx = null;
		double[] bVec = null;
		if (fields[i + 1].equals("null") && fields[i + 2].equals("null")) {
			// System.out.println(" No affines " );
			i += 2;
		} else {
			aMtx = new double[ndims][ndims];
			bVec = new double[ndims];

			for (int k = 0; k < ndims; k++)
				for (int j = 0; j < ndims; j++) {
					aMtx[k][j] = Double.parseDouble(fields[++i]);
				}
			for (int j = 0; j < ndims; j++) {
				bVec[j] = Double.parseDouble(fields[++i]);
			}
		}

		// parse control points
		final double[][] srcPts = new double[ndims][nLm];
		for (int l = 0; l < nLm; l++)
			for (int d = 0; d < ndims; d++) {
				srcPts[d][l] = Double.parseDouble(fields[++i]);
			}

		// parse control point coordinates
		int n = 0;
		final double[] dMtxDat = new double[nLm * ndims];
		for (int l = 0; l < nLm; l++)
			for (int d = 0; d < ndims; d++) {
				dMtxDat[n++] = Double.parseDouble(fields[++i]);
			}

		tps = new ThinPlateR2LogRSplineKernelTransform(srcPts, aMtx, bVec, dMtxDat);

	}

	@Override
	public String toXML(final String indent) {
		final StringBuilder xml = new StringBuilder();
		xml.append(indent).append("<ict_transform class=\"")
				.append(this.getClass().getCanonicalName())
				.append("\" data=\"");
		toDataString(xml);
		return xml.append("\"/>").toString();
	}

	@Override
	public String toDataString() {
		final StringBuilder data = new StringBuilder();
		toDataString(data);
		return data.toString();
	}

	@Override
	public CoordinateTransform copy() {
		return new ThinPlateSplineTransform(tps);
	}

	private final void toDataString(final StringBuilder data) {

		data.append("ThinPlateSplineR2LogR");

		final int ndims = tps.getNumDims();
		final int nLm = tps.getNumLandmarks();

		data.append(' ').append(ndims); // dimensions
		data.append(' ').append(nLm); // landmarks

		if (tps.getAffine() == null) {
			data.append(' ').append("null"); // aMatrix
			data.append(' ').append("null"); // bVector
		} else {
			final double[][] aMtx = tps.getAffine();
			final double[] bVec = tps.getTranslation();
			for (int i = 0; i < ndims; i++)
				for (int j = 0; j < ndims; j++) {
					data.append(' ').append(aMtx[i][j]);
				}
			for (int i = 0; i < ndims; i++) {
				data.append(' ').append(bVec[i]);
			}

		}

		final double[][] srcPts = tps.getSourceLandmarks();
		for (int l = 0; l < nLm; l++)
			for (int d = 0; d < ndims; d++) {
				data.append(' ').append(srcPts[d][l]);
			}

		final double[] dMtxDat = tps.getKnotWeights();
		for (int i = 0; i < ndims * nLm; i++) {
			data.append(' ').append(dMtxDat[i]);
		}

	}

}
