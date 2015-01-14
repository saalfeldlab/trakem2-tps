import ij.gui.*;
import mpicbg.ij.InteractiveMapping;
import mpicbg.ij.TransformMeshMapping;
import mpicbg.models.*;
import mpicbg.models.TransformMesh;

import java.awt.Color;

import jitk.spline.ThinPlateR2LogRSplineKernelTransformFloat;

/**
 * Smooth image deformation using landmark based deformation by means
 * of Thin Plate Spline inspired by the implementation of Kitware (ITK).
 * 
 * 
 * @author John Bogovic <bogovicj@janelia.hhmi.org>
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @version 0.2b
 */
public class Transform_ThinPlateSpline extends InteractiveMapping
{
	public static final String NL = System.getProperty( "line.separator" );
	public final static String man =
		"Add some control points with your mouse" + NL +
		"and drag them to deform the image." + NL + " " + NL +
		"ENTER - Apply the deformation." + NL +
		"ESC - Return to the original image." + NL +
		"U - Toggle mesh display.";
	
	/**
	 * number of vertices in horizontal direction
	 */
	private static int numX = 32;
	
	/**
	 * alpha [0 smooth, 1 less smooth ;)]
	 */
	private static float alpha = 1.0f;
	
	protected TransformMesh mesh;
	protected ThinPlateR2LogRSplineKernelTransformFloat tps;
	
	@Override
	final protected void createMapping()
	{
		mapping = new TransformMeshMapping< TransformMesh >( mesh );
	}
	
	@Override
	final protected void updateMapping() throws NotEnoughDataPointsException, IllDefinedDataPointsException
	{
		tps.solve();
		mesh.init( tps );
		updateIllustration();
	}
	
	@Override
	final protected void addHandle( int x, int y )
	{
		float[] l = new float[]{ x, y };
		synchronized ( mesh )
		{
			try
			{
				mesh.applyInverseInPlace( l );
				Point here = new Point( l );
				Point there = new Point( l );
				hooks.add( here );
				here.apply( mesh );
				m.add( new PointMatch( there, here, 10f ));
				
				tps.addMatch( here.getW(), there.getL());
			}
			catch ( NoninvertibleModelException e ){ e.printStackTrace(); }
		}	
	}
	
	@Override
	final protected void updateHandles( int x, int y )
	{
		float[] l = hooks.get( targetIndex ).getW();
	
		l[ 0 ] = x;
		l[ 1 ] = y;
		
		tps.updateTargetLandmark( targetIndex, l );
	}
	
	@Override
	final public void init()
	{
		final GenericDialog gd = new GenericDialog( "Thin Plate Spline Transform" );
		gd.addNumericField( "Vertices_per_row :", numX, 0 );
		gd.addNumericField( "Alpha :", alpha, 2 );
		gd.addCheckbox( "_Interactive_preview", showPreview );
		gd.addMessage( man );
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		numX = ( int )gd.getNextNumber();
		alpha = ( float )gd.getNextNumber();
		
		showPreview = gd.getNextBoolean();
		
		tps = new ThinPlateR2LogRSplineKernelTransformFloat( 2 );
		
		mesh = new TransformMesh( numX, imp.getWidth(), imp.getHeight() );

    }
	
	@Override
	final protected void updateIllustration()
	{
		if ( showIllustration )
			imp.setOverlay( mesh.illustrateMesh(), Color.white, null );
		else
			imp.setOverlay( null );
	}
}
