package mpicbg.ij;

import ij.IJ;
import ij.ImagePlus;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import jitk.spline.ThinPlateR2LogRSplineKernelTransform;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class RunTpsMapping {

	public static Logger logger = LogManager.getLogger(RunTpsMapping.class.getName());
	
    public static float[][] readCsvPts( String fn ){
        CSVReader reader;
        List<String[]> myEntries = new ArrayList<String[]>();
        try {
            reader = new CSVReader(new FileReader(fn));
            myEntries = reader.readAll();
            //              String[] a = reader.readNext();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }   

        int numCols = myEntries.get(0).length;
        float[][] ptList = new float[myEntries.size()][numCols];
        for( int i = 0; i<myEntries.size(); i++ ){
            String[] thisrow = myEntries.get(i);
            for( int j = 0; j<thisrow.length; j++ ){
                ptList[i][j] = Float.parseFloat( thisrow[j]);
            }   
        }   

        return ptList;
    }
    
    public static double[][] readCsvPtsDouble( String fn ){
        CSVReader reader;
        List<String[]> myEntries = new ArrayList<String[]>();
        try {
            reader = new CSVReader(new FileReader(fn));
            myEntries = reader.readAll();
            //              String[] a = reader.readNext();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 

        int numCols = myEntries.get(0).length;
        double[][] ptList = new double[myEntries.size()][numCols];
        for( int i = 0; i<myEntries.size(); i++ ){
            String[] thisrow = myEntries.get(i);
            for( int j = 0; j<thisrow.length; j++ ){
                ptList[i][j] = Double.parseDouble( thisrow[j]);
            }   
        }   

        return ptList;
    }
    
    public static float[][] transpose( float[][] in ){
    	
    	int nrowsIn = in.length;
    	int ncolsIn = in[0].length;
    	int nrowsOut = ncolsIn;
    	int ncolsOut = nrowsIn;
    	
    	float[][] out = new float[nrowsOut][ncolsOut];
    	
    	for( int i=0; i<nrowsIn; i++ ){
    		for( int j=0; j<ncolsIn; j++ ){
    			out[j][i] = in[i][j];
    		}
    	}
    	return out;
    }
    
    public static double[][] transpose( double[][] in ){
    	
    	int nrowsIn = in.length;
    	int ncolsIn = in[0].length;
    	int nrowsOut = ncolsIn;
    	int ncolsOut = nrowsIn;
    	
    	double[][] out = new double[nrowsOut][ncolsOut];
    	
    	for( int i=0; i<nrowsIn; i++ ){
    		for( int j=0; j<ncolsIn; j++ ){
    			out[j][i] = in[i][j];
    		}
    	}
    	return out;
    }
    
	public static void main( String[] args ){
		
		logger.debug("Reading image.");
		String im_path = "";
		String im_out_path = "";
		if( args.length >= 2 ){
			im_path     = args[0];
			im_out_path = args[1];
		}else{
			logger.error("Must pass at least one argument containing valid image path");
			System.exit(1);
		}
		
		File im_file = new File( im_path );
		if( !im_file.exists() ){
			logger.error("Given image path does not exist");
			System.exit(2);
		}
		
		ImagePlus imSrc = null;
		ImagePlus imDst = null;
		try{
			imSrc = IJ.openImage( im_file.getAbsolutePath() );
			imDst = imSrc.duplicate();
		}catch( Exception e ){
			logger.error("Error reading image");
			System.exit(2);
		}
		
		logger.debug("Reading points.");
		String srcPtPath = "";
		String dstPtPath = "";
		if( args.length >= 4 ){
			srcPtPath = args[2];
			dstPtPath = args[3];
		}else{
			logger.error("Must pass at least three arguments containing valid source and destination points");
			System.exit(3);
		}
		
		float[][] srcPts = null;
		float[][] dstPts = null;
//		double[][] srcPts = null;
//		double[][] dstPts = null;
		try{
			srcPts = readCsvPts( srcPtPath );
			dstPts = readCsvPts( dstPtPath );
//			srcPts = readCsvPtsDouble( srcPtPath );
//			dstPts = readCsvPtsDouble( dstPtPath );
		}catch( Exception e ){
			logger.error("Error loading point matches");
			System.exit(4);
		}
		
		
		
		int ndims = -1;
		if( srcPts[0].length != dstPts[0].length || srcPts.length != dstPts.length){
			logger.error("Source and destination points must have the same size");
			System.exit(5);
		}else{
			ndims = srcPts[0].length;
		}
		
		//TODO: check that dimensionality of image matches that of the points
		
		//TODO: make a better decision regarding when to transpose
		if( srcPts.length > srcPts[0].length ){
			logger.warn("Transposing points to: " + srcPts.length + " points and " + srcPts[0].length + " dimensions.");
			srcPts = transpose( srcPts );
			dstPts = transpose( dstPts );
		}
		
		logger.debug("Fitting thin-plate-spline.");
		

		ThinPlateR2LogRSplineKernelTransform tps = 
				new ThinPlateR2LogRSplineKernelTransform( ndims, dstPts, srcPts  );
		
		
		ThinPlateSplineMapping tpsMap = new ThinPlateSplineMapping( tps );
		tpsMap.mapInterpolated( imSrc.getProcessor(), imDst.getProcessor() );
		
		IJ.saveAsTiff( imDst, im_out_path );
		
		System.exit(0);
		
	}
}
