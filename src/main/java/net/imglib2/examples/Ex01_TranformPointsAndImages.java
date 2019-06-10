package net.imglib2.examples;

import java.io.IOException;

import net.imagej.ImageJ;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

public class Ex01_TranformPointsAndImages
{

	ImageJ ij = null;

	Helpers helper;

	long[] translation = new long[] { -15, 15 };

	Point p = new Point( 500, 100 );
	
	public Ex01_TranformPointsAndImages()
	{
		ij = new ImageJ();
		ij.launch();
		helper = new Helpers();
		helper.ij = ij;
	}

	public static < T extends RealType< T > > void main( String[] args ) throws IOException
	{
		// create and launch an instance of imagej
		Ex01_TranformPointsAndImages example = new Ex01_TranformPointsAndImages();

		RandomAccessibleInterval< UnsignedByteType > boatsImage = example.helper.openBoats();
		example.helper.displayImageAndPoint( boatsImage, example.p, "Boats" );

		// translate the image by a fixed number of pixels
		example.simpleImageTransform( boatsImage );

		// do it again, but now learn to apply the transform to points	
		example.discreteTransformImageAndPoint( boatsImage );

		// build a more complex, continuous transformation
		// and apply it to the image, and to a point
		example.realTransform( boatsImage );
	}

	/**
	 * Use Views to transform an image with a discrete transform.
	 */
	public < T extends RealType< T > > void simpleImageTransform( RandomAccessibleInterval< T > img )
	{
		ij.ui().show( "Translated boats", Views.interval( Views.translate( Views.extendZero( img ), translation ), img ) );
	}


	/**
	 * What do we do if we'd like to apply the discrete transformation to a
	 * region of interest (ROI), such as point locations? Just create a
	 * transform object
	 * 
	 * 
	 * Observe that the transform used for images is the "inverse" of that use
	 * for points, and that the transformed image here is the same as for
	 * simpleImageTransform
	 */
	public < T extends RealType< T > > void discreteTransformImageAndPoint( RandomAccessibleInterval< T > img )
	{
		MixedTransform inverseTransform = new MixedTransform( 2, 2 );
		// why do we need to set the inverse translation?
		// see this blog post: TODO
		inverseTransform.setInverseTranslation( translation );

		MixedTransform forwardTransform = new MixedTransform( 2, 2 );
		forwardTransform.setTranslation( translation );

		// transform the value in p and store the result in q
		Point q = new Point( 2 );
		forwardTransform.apply( p, q );
		System.out.println( "" + p + " is transformed to " + q );

		IntervalView< T > transformedImg = Views.interval( new MixedTransformView<>( Views.extendZero( img ), inverseTransform ), img );
		helper.displayImageAndPoint( transformedImg, q, "Translated boats, with a point roi" );
	}

	/**
	 * Apply a continuous transformation - this let's us rotate and scale
	 * the image by arbitrary amounts.
	 *
	 * Things are more complicated though, since now we need to interpolate between pixel values.
	 * 
	 * 
	 */

	public < T extends RealType< T > > void realTransform( RandomAccessibleInterval< T > img )
	{
		AffineTransform2D affine = new AffineTransform2D();
		affine.scale( 0.9 );
		affine.translate( 99.5, -57.8 );
		affine.rotate( Math.PI / 8 );

		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );
		IntervalView< T > transformedImage = Views.interval( Views.raster( RealViews.affine( interpolatedImg, affine ) ), img );

		RealPoint q = new RealPoint( 2 );
		affine.apply( p, q );
		System.out.println( "" + p + " is transformed to " + q );

		helper.displayImageAndPoint( transformedImage, q, "Affine transformed boats, with a point roi" );
	}


}