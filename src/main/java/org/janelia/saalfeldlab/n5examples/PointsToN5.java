package org.janelia.saalfeldlab.n5examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.GenericComposite;

	
/**
 * This class shows how to write point coordinates
 * to the n5 format.
 * 
 * @author John Bogovic
 *
 */
public class PointsToN5
{

	public static void main(String[] args) throws IOException
	{
		
		/*
		 * Read a list of points from a csv file
		 * and write the result as an hdf5 filye
		 * (using the n5 api)
		 */

		// use the set of points spanning the whole brain
		String inputCsvRelativePath = "resources/fafb_synapses_small.csv";
		String outputH5RelativePath = "resources/fafb_synapses_small.h5";

		// use the more densely spaced points around the ellipsoid body
//		String inputCsvRelativePath = "resources/fafb_synapses_small.csv";
//		String outputH5RelativePath = "resources/fafb_synapses_small.h5";
		
		FloatType type = new FloatType();
		int[] blockSize = new int[]{ 3, 128 };

		// put the point coordinates into a 2d data container
		Img<FloatType> data = imgFromCsv( inputCsvRelativePath, type );

		// use N5Utils to write the data to an hdf5 file
		// under the dataset "synapse_points"
		N5HDF5Writer writer = new N5HDF5Writer(outputH5RelativePath, blockSize );
		N5Utils.save(data, writer, "/synapse_points", blockSize, new GzipCompression());
		
		// read from the points and make sure we get the right value
		N5HDF5Reader n5 = new N5HDF5Reader(outputH5RelativePath, 3, 128 );
		List<RealPoint> points = pointsFromN5( n5, "/synapse_points");
		System.out.println( points.get(0));
	}
	
	public static List<RealPoint> pointsFromN5( N5Reader n5, String dataset ) throws IOException
	{
		ArrayList<RealPoint> pts = new ArrayList<>();

		// read the data as an "image"
		RandomAccessibleInterval<FloatType> data = N5Utils.open(n5, dataset);

		// resave the image data
		CompositeIntervalView<FloatType, ? extends GenericComposite<FloatType>> composite = Views.collapse( Views.permute( data, 0, 1));
		Cursor<? extends GenericComposite<FloatType>> c = Views.flatIterable( composite ).cursor();
		while( c.hasNext() )
		{
			c.fwd();
			RealPoint pt = new RealPoint(
				c.get().get(0).getRealDouble(),
				c.get().get(1).getRealDouble(),
				c.get().get(2).getRealDouble());
	
			pts.add( pt );
		}

		// challenge: wrap the "data" variable somehow 
		// so that we dont have to copy data
		// Hint: implement the Localizable interface

		return pts;
	}

	public static <T extends NativeType<T> & RealType<T>> Img<T> imgFromCsv(String csvPath, T type) throws IOException {
		// Read the points
		List<String> lines = Files.readAllLines(Paths.get(csvPath));
		long N = lines.size(); // number of points
		long D = -1; // dimensions per point

		ArrayImgFactory<T> factory = new ArrayImgFactory<>(type);
		ArrayImg<T, ?> data = null;
		ArrayRandomAccess<T> ra = null;
		long i = 0; // index over dimensions
		int j = 0;
		for (String line : lines) {
			String[] elems = line.split(",");
			D = elems.length;

			if (data == null) {
				data = factory.create(D, N);
				ra = data.randomAccess();
			}
			ra.setPosition(0, 0); // set row index to zero

			for (j = 0; j < D; j++) {
				ra.get().setReal(Double.parseDouble(elems[j]));
				ra.fwd(0);
			}

			ra.fwd(1);
		}

		return data;
	}

}
