package net.imglib2.examples;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import net.imagej.overlay.PointOverlay;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

public class Helpers
{
	protected ImageJ ij;

	File localBoats = new File( "resources/boats.tif" );

	public < T extends RealType< T > > void displayImageAndPoint( RandomAccessibleInterval< T > img, RealLocalizable pt, String name )
	{
		ImageDisplay display = ( ImageDisplay ) ij.display().createDisplay( img );
		display.setName( name );
		ij.overlay().addOverlays( display, makePointOverlay( pt ) );
		ij.ui().show( display );
	}
	

	public List< PointOverlay > makePointOverlay( RealLocalizable p )
	{
		double[] y = new double[ p.numDimensions() ];
		p.localize( y );
		PointOverlay overlay = new PointOverlay( ij.getContext(), y );
		return Stream.of( overlay ).collect( Collectors.toList() );
	}


	/**
	 * Read from imagej.nih.gov the first we read the image,
	 * but save it locally so that opening is faster 
	 * on subsequent runs.
	 */
	@SuppressWarnings( "unchecked" )
	public RandomAccessibleInterval< UnsignedByteType > openBoats() throws IOException
	{
		String path = "https://imagej.nih.gov/ij/images/boats.gif";
		if( localBoats.exists())
		{
			Object obj =  ij.io().open( localBoats.getPath() );
			// hyperslice because 'boats' opens as an RGB image, with all channels identical 
			return Views.hyperSlice( (RandomAccessibleInterval<UnsignedByteType>)obj, 2, 0 );
		}
		else
		{
			Object obj = ij.io().open( path );
			ij.io().save( (Dataset)obj, localBoats.getAbsolutePath());

			// hyperslice because 'boats' opens as an RGB image, with all channels identical 
			RandomAccessibleInterval<UnsignedByteType> imgout = Views.hyperSlice( (RandomAccessibleInterval<UnsignedByteType>)obj, 2, 0 );
			return imgout;
		}
	}
	
	public static <T extends NativeType<T> & RealType<T>> RandomAccessibleInterval<T> copy( 
			final RandomAccessibleInterval<T> img )
	{
		ArrayImgFactory<T> factory = new ArrayImgFactory<>(Views.flatIterable(img).firstElement().copy());
		ArrayImg<T, ?> out = factory.create(img);
		LoopBuilder.setImages( img, out ).forEachPixel((x,y) -> y.set(x));
		return out;
	}

}
