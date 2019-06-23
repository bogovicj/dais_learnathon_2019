package net.imglib2.examples;

import java.io.IOException;
import java.util.ArrayList;

import net.imagej.ImageJ;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Ex02_ComposingTransforms
{

	ImageJ ij = null;

	Helpers helper;

	long[] translation = new long[] { -15, 15 };

	Point p = new Point( 500, 100 );
	
	AffineTransform2D smallRotation;
	
	public Ex02_ComposingTransforms()
	{
		ij = new ImageJ();
		ij.launch();
		helper = new Helpers();
		helper.ij = ij;
		
		smallRotation = new AffineTransform2D();
		smallRotation.translate( -375, -280 );
		smallRotation.rotate( Math.toRadians( 10 ));
		smallRotation.translate( 375, 280 );
	}

	public static < T extends RealType< T > > void main( String[] args ) throws IOException
	{
		// create and launch an instance of imagej
		Ex02_ComposingTransforms example = new Ex02_ComposingTransforms();

		RandomAccessibleInterval< UnsignedByteType > boatsImage = example.helper.openBoats();

		/*
		 * repeatedly apply a transformation to the image with imglib2
		 */
		example.repeatedRealTransform( boatsImage, example.smallRotation, 36 );

		/*
		 * repeatedly apply a transformation to the image, copying data each time
		 * ... what is different here...?
		 * why the blurring?
		 * and why does the first example not blur the image?
		 */
		example.repeatedRealTransformAndCopy( boatsImage, example.smallRotation, 36 );

		/*
		 *  compose the transformations
		 */
		example.composeAffine( boatsImage, example.smallRotation, 36 );
	}

	/**
	 * Apply a continuous transformation - this let's us rotate and scale
	 * the image by arbitrary amounts.
	 *
	 * Things are more complicated though, since now we need to interpolate between pixel values.
	 * 
	 * 
	 */
	public < T extends RealType< T > > void repeatedRealTransform( RandomAccessibleInterval< T > img,
			final InvertibleRealTransform transform,
			final int N)
	{

		ArrayList<RandomAccessibleInterval<T>> imglist = new ArrayList<>();
		// add the untransformed image
		imglist.add( img );

		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );
		RealRandomAccessible<T> lastTransformedImage = interpolatedImg;

		for( int i = 0; i < N; i++ )
		{
			RealTransformRandomAccessible<T, InverseRealTransform> transformedRealImage = RealViews.transform( lastTransformedImage, transform );

			IntervalView< T > transformedImage = Views.interval( Views.raster( transformedRealImage), img );
			imglist.add( transformedImage );

			lastTransformedImage = transformedRealImage;
		}

		RandomAccessibleInterval<T> imgStack = Views.stack(imglist);
		ij.ui().show(imgStack);

	}

	/**
	 * Apply a continuous transformation - this let's us rotate and scale
	 * the image by arbitrary amounts.
	 *
	 * Things are more complicated though, since now we need to interpolate between pixel values.
	 * 
	 * 
	 */
	public < T extends RealType< T > > void composeAffine( RandomAccessibleInterval< T > img,
			final AffineTransform2D transform,
			final int N)
	{
		ArrayList<RandomAccessibleInterval<T>> imglist = new ArrayList<>();
		// add the untransformed image
		imglist.add( img );
		
		AffineTransform2D currentTransform = new AffineTransform2D();

		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );

		for( int i = 0; i < N; i++ )
		{
			currentTransform = currentTransform.copy().preConcatenate( transform );
			RealTransformRandomAccessible<T, ?> transformedRealImage = 
					RealViews.affine( interpolatedImg, currentTransform );

			IntervalView< T > transformedImage = Views.interval( Views.raster( transformedRealImage), img );
			imglist.add( transformedImage );
		}

		RandomAccessibleInterval<T> imgStack = Views.stack(imglist);
		ij.ui().show(imgStack);

	}

	/**
	 * 
	 * @param img The image
	 * @param transform the transformation
	 * @param N number of times to apply the tranform
	 */
	public <T extends RealType<T> & NativeType<T>> void repeatedRealTransformAndCopy(RandomAccessibleInterval<T> img,
			final InvertibleRealTransform transform,
			final int N)
	{

		ArrayList<RandomAccessibleInterval<T>> imglist = new ArrayList<>();
		// add the untransformed image
		imglist.add( img );

		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );
		RealRandomAccessible<T> lastTransformedImage = interpolatedImg;
		for( int i = 0; i < N; i++ )
		{
			RealTransformRandomAccessible<T, InverseRealTransform> transformedRealImage = RealViews.transform( lastTransformedImage, transform );

			IntervalView< T > transformedImage = Views.interval( Views.raster( transformedRealImage), img );
			RandomAccessibleInterval<T> copy = Helpers.copy(transformedImage);
			imglist.add( copy );

			lastTransformedImage = Views.interpolate( Views.extendZero(copy), new NLinearInterpolatorFactory<>());
		}

		RandomAccessibleInterval<T> imgStack = Views.stack(imglist);
		ij.ui().show(imgStack);
	}

}