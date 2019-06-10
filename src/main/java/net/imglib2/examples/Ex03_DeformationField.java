package net.imglib2.examples;

import java.io.IOException;
import java.util.function.BiConsumer;

import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.DeformationFieldTransform;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
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

	double[] translation = new double[]{ -50, 50 };
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
	}
	
	public < T extends RealType<T>> void simpleDeformationDemo( RandomAccessibleInterval< T > img )
	{
		DeformationFieldTransform<DoubleType> def = buildDeformationField( img, true );

		RealPoint q = new RealPoint( 2 );
		def.apply( p, q );
		System.out.println( "" + p + " is transformed to " + q );


		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );
		IntervalView< T > transformedImage = Views.interval( Views.raster( new RealTransformRandomAccessible<>( interpolatedImg, def ) ), img );
		helper.ij.ui().show( "Deformed boats", transformedImage );
	}

	public < T extends RealType<T>> void inverseDeformationField( RandomAccessibleInterval< T > img )
	{
		DeformationFieldTransform<DoubleType> def = buildDeformationField( img, false );

		WrappedIterativeInvertibleRealTransform< DeformationFieldTransform< DoubleType > > invertibleDeformation = new WrappedIterativeInvertibleRealTransform<>( def );
		invertibleDeformation.getOptimzer().setTolerance( 1.5 );
		invertibleDeformation.getOptimzer().setMaxIters( 50 );
		InvertibleRealTransform invdef = invertibleDeformation.inverse();

		RealRandomAccessible< T > interpolatedImg = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory< T >() );
		IntervalView< T > transformedImage = Views.interval( Views.raster( new RealTransformRandomAccessible<>( interpolatedImg, invdef ) ), img );
		helper.ij.ui().show( "Deformed boats", transformedImage );
	}
	
	public DeformationFieldTransform< DoubleType > buildDeformationField( final Interval imageInterval, final boolean display )
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