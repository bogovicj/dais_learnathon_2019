package net.imglib2.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.render.KDTreeRenderer;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5DisplacementField;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.KDTree;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Util;

/**
 * In this example, we'll learn how to:
 * 1) Apply an arbitrary transform to an image.
 * 
 *
 * 
 */
public class PointRenderingExample< T extends RealType< T > >
{
	
	public static final ARGBType GREEN = new ARGBType( ARGBType.rgba( 0, 255, 0, 255 ) );
	public static final ARGBType MAGENTA = new ARGBType( ARGBType.rgba( 255, 0, 255, 255 ) );

	Img< UnsignedByteType > targetImage;

	AffineTransform3D affine;

	Interval interval;

	Interval smallInterval;

	RealTransform transform;

	RealTransform inversetransform;

	List< RealPoint > pointList;

	List< T > valueList;

	List< RealPoint > randomPointList;

	List< T > fixedValueList;

	final T type;

	public PointRenderingExample( T type )
	{
		this.type = type;
	}

	public static void main( String[] args ) 
	{
		//PointRenderingExample< DoubleType > ex = initialize( args[ 0 ], args[ 1 ], args[ 2 ] );

		PointRenderingExample< DoubleType > ex = initialize(
				"resources/JRC2018F_small.tif",
				"resources/JRC2018F_FAFB_small.h5",
				"resources/fafb_synapses_small.csv");

		// render
//		ex.renderTest( 5.0, 10.0 );

//		// run and visualize
//		ex.compareRenderMethods( 5.0, 10.0 );

		ex.compareRenderMethodsFAFB( 5.0, 10.0 );
	}

	
	/**
	 * Renders an image ({@link RealRandomAccessible}) from point coordinatesXZ
	 * using a radially symmetric point spread function. Uses a {@link KDTree}
	 * to efficiently find points near a query point.
	 * 
	 * @param radius
	 *            the radius of the psf for each point
	 * @param value
	 *            the peak value for the psf
	 */
	public void renderTest( final double radius, final double value )
	{
		KDTreeRenderer< T, RealPoint > renderer = new KDTreeRenderer<>( fixedValueList, randomPointList, radius, value );
		RealRandomAccessible< T > img = renderer.getRealRandomAccessible( radius, renderer::rbfRadius );

		// visualize
		BdvStackSource< T > bdv = BdvFunctions.show( img, smallInterval, "Render points" );

		// set the display range
		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 100 );
	}

	/**
	 * Renders an image ({@link RealRandomAccessible}) from point coordinates
	 * using a radially symmetric point spread function. Uses a {@link KDTree}
	 * to efficiently find points near a query point.
	 * 
	 * @param radius
	 *            the radius of the psf for each point
	 * @param value
	 *            the peak value for the psf
	 */
	public void renderFAFB( final double radius, final double value )
	{
		KDTreeRenderer< T, RealPoint > renderer = new KDTreeRenderer<>( valueList, pointList, radius, value );
		RealRandomAccessible< T > img = renderer.getRealRandomAccessible( radius, renderer::rbfRadius );
		System.out.println( "interval: " + Util.printInterval( interval ));

		// visualize
		BdvStackSource< T > bdv = BdvFunctions.show( img, interval, "Render points" );

		// set the display range
		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 2000 );
	}
	
	/**
	 * Render transformed points using two different methods:<br>
	 * 	1) Render the image, then transform the image.<br>
	 *  2) Transform the points, render the image from the transformed points.<br>
	 *  
	 * <p>
	 * Observations:<br>
	 * 	1)  We need the "forward" transform when transforming points, and the "inverse" when transforming the image 
	 * 		for the points to be at the same locations.<br>
	 *  2)  The PSF centers are in the same locations for both options.<br>
	 * 	3) 	Rendering the image first means that the PSF is also transformed.<br>
	 * 	4) 	The PSF shape is preserved if we transform the points first.<br>
	 * 
	 * @param radius
	 *            the radius of the psf for each point
	 * @param value
	 *            the peak value for the psf
	 */
	public void compareRenderMethods( double radius, double value )
	{
		BdvOptions opts = BdvOptions.options();

		// render the image
		KDTreeRenderer<T,RealPoint> renderer = new KDTreeRenderer<>( fixedValueList, randomPointList, radius, value );
		RealRandomAccessible< T > rra = renderer.getRealRandomAccessible( radius, renderer::rbfRadius );

		// transform the image
		RealRandomAccessible< T > pointImageTransformed = transform( rra, affine.inverse() );
		// visualize
		BdvStackSource< T > bdv = BdvFunctions.show( pointImageTransformed, smallInterval, "points -> render -> transform", opts );


		// transform the points
		List< RealPoint > transformedPoints = transformPoints( randomPointList, affine );
		// render the image from the transformed points
		KDTreeRenderer<T,RealPoint> renderer2 = new KDTreeRenderer<>( fixedValueList, transformedPoints, radius, value );
		RealRandomAccessible< T > pointsTransformedImage = renderer2.getRealRandomAccessible( radius, renderer::rbfRadius );
		// visualize
		BdvFunctions.show( pointsTransformedImage, smallInterval, "points -> transform -> render", opts.addTo( bdv ) );

		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 200 );
		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 1 ).setRange( 0, 200 );
		
		bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( 0 ).setColor( GREEN );
		bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( 1 ).setColor( MAGENTA );
	}

	/**
	 * Render transformed points using two different methods:<br>
	 * 	1) Render the image, then transform the image.<br>
	 *  2) Transform the points, render the image from the transformed points.<br>
	 *  
	 * <p>
	 * Observations:<br>
	 * 	1)  We need the "forward" transform when transforming points, and the "inverse" when transforming the image 
	 * 		for the points to be at the same locations.<br>
	 *  2)  The PSF centers are in the same locations for both options.<br>
	 * 	3) 	Rendering the image first means that the PSF is also transformed.<br>
	 * 	4) 	The PSF shape is preserved if we transform the points first.<br>
	 * 
	 * @param radius
	 *            the radius of the psf for each point
	 * @param value
	 *            the peak value for the psf
	 */
	public void compareRenderMethodsFAFB( double radius, double value )
	{
		BdvOptions opts = BdvOptions.options();
		

		BdvStackSource< UnsignedByteType > bdv = BdvFunctions.show( targetImage, "target", opts );
		opts = opts.addTo( bdv );

		// render the image
		KDTreeRenderer<T,RealPoint> renderer = new KDTreeRenderer<>( valueList, pointList, radius, value );
		RealRandomAccessible< T > rra = renderer.getRealRandomAccessible( radius, renderer::rbfRadius );

		// transform the image
		RealRandomAccessible< T > pointImageTransformed = transform( rra, transform );
		// visualize
		BdvFunctions.show( pointImageTransformed, interval, "points -> render -> transform", opts );

//
//		// transform the points
//		List< RealPoint > transformedPoints = transformPoints( pointList, transform );
//		// render the image from the transformed points
//		KDTreeRenderer<T,RealPoint> renderer2 = new KDTreeRenderer<>( valueList, transformedPoints, radius, value );
//		RealRandomAccessible< T > pointsTransformedImage = renderer2.getRealRandomAccessible( radius, renderer::rbfRadius );
//		// visualize
//		BdvFunctions.show( pointsTransformedImage, interval, "points -> transform -> render", opts );
//
		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 1 ).setRange( 0, 4000 );
		bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( 1 ).setColor( GREEN );

//		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 2 ).setRange( 0, 4000 );
//		bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( 2 ).setColor( MAGENTA );
	}
	
	
	/**
	 * Transform The source using the provided inverse transform.
	 * 
	 * Question - why can't we use RealViews.transform here?
	 * 
	 * @param source the RealRandomAccessible
	 * @param targetToMovingTransform
	 * @return the transformed RealRandomAccessible
	 */
	public static <T extends RealType<T>> RealTransformRandomAccessible< T, ? > transform( 
			final RealRandomAccessible< T > source, final RealTransform targetToMovingTransform )
	{
		return new RealTransformRandomAccessible<>( source, targetToMovingTransform );
	}

	/**
	 * Transform all points in a list.
	 * 
	 * @param pts a list of points 
	 * @param transform a transformation
	 * @return a list of transformed points
	 */
	public static List< RealPoint > transformPoints( List<RealPoint> pts, RealTransform transform )
	{
		return pts.stream().map( x -> {
			RealPoint y = new RealPoint( x.numDimensions() );
			transform.apply( x, y );
			return y;
		}).collect( Collectors.toList() );
	}

	public static PointRenderingExample<DoubleType> initialize( final String targetImagePath,
			final String transformPath,
			final String pointsPath )
	{
		ImagePlus targetIp = IJ.openImage( targetImagePath );


		AffineTransform3D affine = new AffineTransform3D();
		affine.set( 0.8, 0.2, 0.0, 0.0,
				  	0.1, 0.7, 0.1, 0.0, 
					0.2, -0.1, 0.7, 0.0 );
	
		InvertibleRealTransform transform = null;
		try
		{
			transform = N5DisplacementField.openInvertible( 
					new N5HDF5Reader( transformPath, 32, 32, 32, 3 ));
		}
		catch ( Exception e )
		{
			System.err.println("Could not read transform");
			return null;
		}

		PointRenderingExample< DoubleType > ex = new PointRenderingExample<>( new DoubleType() );

		ex.smallInterval = new FinalInterval( 40, 30, 20 );
		ex.targetImage = ImageJFunctions.wrapByte( targetIp );
		ex.interval = ex.targetImage;
		ex.affine = affine;
		ex.transform = transform;
		ex.inversetransform = transform.inverse();
		ex.randomPointsValues( 20, ex.smallInterval );

		try
		{
			ex.pointList = loadPoints3dCsv( pointsPath );
		}
		catch ( IOException e )
		{
			System.err.println("Could not read points");
			return null;
		}

		ex.valueList = Stream.iterate( new DoubleType( 20 ), x -> x ).limit( ex.pointList.size() ).collect( Collectors.toList() );
		
		ex.randomPointsValues( 20, ex.smallInterval );
		
		return ex;
	}

	public void fixedPoints()
	{
		pointList = new ArrayList<>();
		pointList.add( new RealPoint( 9, 9, 9 ));
		pointList.add( new RealPoint( 6, 9, 9 ));

		valueList = new ArrayList<>();
		T value = type.copy();
		value.setOne();
		valueList.add( value.copy() );
		valueList.add( value.copy() );
	}

	public void randomPointsValues( final int N, final RealInterval interval )
	{
		Random rand = new Random( 60 );

		int nd = interval.numDimensions();
		double[] widths = new double[ nd ];
		double[] offset = new double[ nd ];
		for ( int d = 0; d < nd; d++ )
		{
			offset[ d ] = interval.realMin( d );
			widths[ d ] = interval.realMax( d ) - interval.realMin( d );
		}

		randomPointList = new ArrayList<>();
		fixedValueList = new ArrayList<>();
		for ( int i = 0; i < N; i++ )
		{
			RealPoint p = new RealPoint( nd );
			for ( int d = 0; d < nd; d++ )
				p.setPosition( offset[ d ] + rand.nextDouble() * widths[ d ], d );

			randomPointList.add( p );
			
			T value = type.copy();
			value.setReal( 10.0 );
			fixedValueList.add( value );
		}
	}

	public static List<RealPoint> loadPoints3dCsv( String csvPath ) throws IOException
	{
		List< String > lines = Files.readAllLines( Paths.get( csvPath ) );
		ArrayList<RealPoint> pts = new ArrayList<>();

		for( String line : lines )
		{
			String[] elems = line.split( "," );
			RealPoint p = new RealPoint( Double.parseDouble( elems[ 0 ] ), Double.parseDouble( elems[ 1 ] ), Double.parseDouble( elems[ 2 ] ) ) ;
			pts.add( p );
		}
		return pts;
	}

}
