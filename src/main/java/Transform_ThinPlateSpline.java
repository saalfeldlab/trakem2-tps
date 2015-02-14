import ij.gui.GenericDialog;

import java.awt.Color;

import jitk.spline.ThinPlateR2LogRSplineKernelTransform;
import mpicbg.ij.InteractiveMapping;
import mpicbg.ij.TransformMeshMapping;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NoninvertibleModelException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;
import mpicbg.models.TransformMesh;

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
	private static double alpha = 1.0;

	protected TransformMesh mesh;
	protected ThinPlateR2LogRSplineKernelTransform tps;

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
	final protected void addHandle( final int x, final int y )
	{
		final double[] l = new double[]{ x, y };
		synchronized ( mesh )
		{
			try
			{
				mesh.applyInverseInPlace( l );
				final Point here = new Point( l );
				final Point there = new Point( l );
				hooks.add( here );
				here.apply( mesh );
				m.add( new PointMatch( there, here, 10f ));

				tps.addMatch( here.getW(), there.getL());
			}
			catch ( final NoninvertibleModelException e ){ e.printStackTrace(); }
		}
	}

	@Override
	final protected void updateHandles( final int x, final int y )
	{
		final double[] l = hooks.get( targetIndex ).getW();

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
		alpha = gd.getNextNumber();

		showPreview = gd.getNextBoolean();

		tps = new ThinPlateR2LogRSplineKernelTransform( 2 );

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
