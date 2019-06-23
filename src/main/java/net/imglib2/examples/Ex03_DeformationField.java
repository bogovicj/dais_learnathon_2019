package net.imglib2.examples;

import java.io.IOException;
import java.util.function.BiConsumer;

import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.DeformationFieldTransform;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

public class Ex03_DeformationField
{

	ImageJ ij = null;

	Helpers helper;

	double[] bigTranslation = new double[]{ -50, 50 };
	double[] mediumTranslation = new double[]{ -25, 25 };
	double[] width = new double[]{ 85, 85 };
	double[] center = new double[]{ 500, 100 };

	Point p = new Point( 500, 100 );
	
	public Ex03_DeformationField()
	{
		ij = new ImageJ();
		ij.launch();
		helper = new Helpers();
		helper.ij = ij;
	}

	public static < T extends RealType< T > > void main( String[] args ) throws IOException
	{
		// initialize
		Ex03_DeformationField ex3 = new Ex03_DeformationField();

		RandomAccessibleInterval< UnsignedByteType > boatsImage = ex3.helper.openBoats();

		ex3.simpleDeformationDemo( boatsImage );
		ex3.inverseDeformationField( boatsImage );

//		ex3.addingIsNotTheSameAsComposing( boatsImage );
	}
	
	public < T extends RealType<T>> void simpleDeformationDemo( RandomAccessibleInterval< T > img )
	{
		DeformationFieldTransform<DoubleType> def = buildDeformationField( img, bigTranslation, true );

		RealPoint q = new RealPoint( 2 );
		def.apply( p, q );
		System.out.println( "" + p + " is transformed to " + q );


		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );
		IntervalView< T > transformedImage = Views.interval( Views.raster( new RealTransformRandomAccessible<>( interpolatedImg, def ) ), img );
		helper.ij.ui().show( "Deformed boats", transformedImage );
	}

	public < T extends RealType<T>> void inverseDeformationField( RandomAccessibleInterval< T > img )
	{
		DeformationFieldTransform<DoubleType> def = buildDeformationField( img, bigTranslation, false );

		WrappedIterativeInvertibleRealTransform< DeformationFieldTransform< DoubleType > > invertibleDeformation = new WrappedIterativeInvertibleRealTransform<>( def );
		invertibleDeformation.getOptimzer().setTolerance( 1.5 );
		invertibleDeformation.getOptimzer().setMaxIters( 50 );
		InvertibleRealTransform invdef = invertibleDeformation.inverse();

		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );
		IntervalView< T > transformedImage = Views.interval( Views.raster( new RealTransformRandomAccessible<>( interpolatedImg, invdef ) ), img );
		helper.ij.ui().show( "Deformed boats", transformedImage );
	}
	

	/**
	 * This example demonstrates that adding two displacement fields IS NOT the same
	 * as composing them.
	 * 
	 * Why is that? 
	 * 
	 * @param img an image defining the deformation field bounds
	 */
	public <T extends RealType<T>> void addingIsNotTheSameAsComposing( RandomAccessibleInterval< T > img )
	{
		final DeformationFieldTransform<DoubleType> transform = buildDeformationField( img, mediumTranslation, false );
		final RealRandomAccessible<DoubleType> deformationField = transform.getDefFieldAcess();
		RealRandomAccess<DoubleType> deformationAccess = deformationField.realRandomAccess();

		final RealRandomAccessible<DoubleType> addedDeformations = new FunctionRealRandomAccessible<>( 3, 
				new BiConsumer<RealLocalizable,DoubleType>()
				{
					@Override
					public void accept(RealLocalizable p, DoubleType v) {
						deformationAccess.setPosition(p);
						double value = deformationAccess.get().get();
						v.set( value + value );
					}
				},
				DoubleType::new);
		final DeformationFieldTransform<DoubleType> addedTransform = new DeformationFieldTransform<>( addedDeformations );

		RealTransformSequence composedDeformations = new RealTransformSequence();
		composedDeformations.add(transform);
		composedDeformations.add(transform);

		// lets visualize the difference
		RealPoint transformedAdded = new RealPoint( 2 );
		RealPoint transformedComposed = new RealPoint( 2 );

		// we need to iterate over 2d space but difference is 3d space
		// hence, the gymnastics here
		ArrayImg<DoubleType, DoubleArray> difference = ArrayImgs.doubles(img.dimension(0), img.dimension(1), 2 );
		ArrayRandomAccess<DoubleType> differenceAccess = difference.randomAccess();

		Cursor<T> c = Views.flatIterable(img).cursor();
		while( c.hasNext() )
		{
			c.fwd();
			differenceAccess.setPosition( c.getIntPosition(0), 0);
			differenceAccess.setPosition( c.getIntPosition(1), 1);

			// apply the transforms
			addedTransform.apply(c, transformedAdded);
			composedDeformations.apply(c, transformedComposed);

			// store the difference in the image
			differenceAccess.setPosition( 0, 2);
			differenceAccess.get().set( 
					transformedComposed.getDoublePosition(0) - transformedAdded.getDoublePosition(0));

			differenceAccess.setPosition(1, 2);
			differenceAccess.get().set( 
					transformedComposed.getDoublePosition(1) - transformedAdded.getDoublePosition(1));
		}

		ij.ui().show( "difference between adding and composing", difference );
	}

	public DeformationFieldTransform< DoubleType > buildDeformationField( final Interval imageInterval, 
			final double[] translation, final boolean display )
	{
		FinalInterval displacementInterval = new FinalInterval( 
				new long[]{ imageInterval.min( 0 ), imageInterval.min( 1 ), 0 },
				new long[]{ imageInterval.max( 0 ), imageInterval.max( 1 ), 1 } );
	
		FunctionRandomAccessible<DoubleType> displacement = new FunctionRandomAccessible<>( 
				3, 
				new BiConsumer<Localizable,DoubleType>(){
					@Override
					public void accept( Localizable p, DoubleType v )
					{
						int i = ( p.getDoublePosition( 2 ) < 0.5 ) ? 0 : 1;

						double x = (p.getDoublePosition( 0 ) - center[ 0 ]);
						double y = (p.getDoublePosition( 1 ) - center[ 1 ]);
						v.setReal( translation[ i ] * Math.exp( -( x*x + y*y ) / ( width[i] * width[ i ] )));
					}
				},
				DoubleType::new );
		
		IntervalView< DoubleType > displacementField = Views.interval( displacement, displacementInterval );

		if( display )
			helper.ij.ui().show( "Displacement field", displacementField );

		return new DeformationFieldTransform<>( displacementField );
	}

}