package mpicbg.trakem2.transform;

import jitk.spline.ThinPlateR2LogRSplineKernelTransform;
import mpicbg.trakem2.transform.CoordinateTransform;

public class ThinPlateSplineTransform implements CoordinateTransform {

	private ThinPlateR2LogRSplineKernelTransform tps;

	public ThinPlateSplineTransform() {
	}

	public ThinPlateSplineTransform(ThinPlateR2LogRSplineKernelTransform tps) {
		this.tps = tps;
	}

	public float[] apply(float[] location) {

		float[] out = new float[location.length];
		for (int i = 0; i < location.length; i++) {
			out[i] = location[i];
		}

		applyInPlace(out);
		return out;
	}

	public void applyInPlace(float[] location) {

		tps.applyInPlace(location);
	}

	public void init(String data) throws NumberFormatException {

		final String[] fields = data.split("\\s+");

		int i = 0;

		int ndims = Integer.parseInt(fields[i++]);
		int nLm = Integer.parseInt(fields[i++]);

		double[][] aMtx = null;
		double[] bVec = null;
		if (fields[i].equals("null") && fields[i + 1].equals("null")) {
			// System.out.println(" No affines " );
			i += 2;
		} else {
			aMtx = new double[ndims][ndims];
			bVec = new double[ndims];

			for (int k = 0; k < ndims; k++)
				for (int j = 0; j < ndims; j++) {
					aMtx[k][j] = Double.parseDouble(fields[i++]);
				}
			for (int j = 0; j < ndims; j++) {
				bVec[j] = Double.parseDouble(fields[i++]);
			}
		}

		// parse control points
		double[][] srcPts = new double[ndims][nLm];
		for (int l = 0; l < nLm; l++)
			for (int d = 0; d < ndims; d++) {
				srcPts[d][l] = Double.parseDouble(fields[i++]);
			}

		// parse control point coordinates
		int n = 0;
		double[] dMtxDat = new double[nLm * ndims];
		for (int l = 0; l < nLm; l++)
			for (int d = 0; d < ndims; d++) {
				dMtxDat[n++] = Double.parseDouble(fields[i++]);
			}

		tps = new ThinPlateR2LogRSplineKernelTransform(srcPts, aMtx, bVec,
				dMtxDat);

	}

	public String toXML(String indent) {
		final StringBuilder xml = new StringBuilder();
		xml.append(indent).append("<ict_transform class=\"")
				.append(this.getClass().getCanonicalName())
				.append("\" data=\"");
		toDataString(xml);
		return xml.append("\"/>").toString();
	}

	public String toDataString() {
		final StringBuilder data = new StringBuilder();
		toDataString(data);
		return data.toString();
	}

	public CoordinateTransform copy() {
		return new ThinPlateSplineTransform(tps);
	}

	private final void toDataString(final StringBuilder data) {

		if (ThinPlateR2LogRSplineKernelTransform.class.isInstance(tps))
			data.append("ThinPlateSplineR2LogR");

		int ndims = tps.getNumDims();
		int nLm = tps.getNumLandmarks();

		data.append(' ').append(ndims); // dimensions
		data.append(' ').append(nLm); // landmarks

		if (tps.getAffine() == null) {
			data.append(' ').append("null"); // aMatrix
			data.append(' ').append("null"); // bVector
		} else {
			double[][] aMtx = tps.getAffine();
			double[] bVec = tps.getTranslation();
			for (int i = 0; i < ndims; i++)
				for (int j = 0; j < ndims; j++) {
					data.append(' ').append(aMtx[i][j]);
				}
			for (int i = 0; i < ndims; i++) {
				data.append(' ').append(bVec[i]);
			}

		}

		double[][] srcPts = tps.getSourceLandmarks();
		for (int l = 0; l < nLm; l++)
			for (int d = 0; d < ndims; d++) {
				data.append(' ').append(srcPts[d][l]);
			}

		double[] dMtxDat = tps.getKnotWeights();
		for (int i = 0; i < ndims * nLm; i++) {
			data.append(' ').append(dMtxDat[i]);
		}

	}

}
