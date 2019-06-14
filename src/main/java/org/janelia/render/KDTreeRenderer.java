package org.janelia.render;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import net.imglib2.KDTree;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.neighborsearch.RBFInterpolator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class KDTreeRenderer<T extends RealType<T>,P extends RealLocalizable>
{

	final double radius;

	final double searchDistSqr;

	final double invSquareSearchDistance;

	final double value;

	final KDTree< T > tree;
	
	public KDTreeRenderer( List<T> vals, List<P> pts,
			final double radius,
			final double value )
	{
		tree = new KDTree< T >( vals, pts );
		this.value = value;

		this.radius = radius;
		this.searchDistSqr = ( radius * radius );
		this.invSquareSearchDistance = 1 / searchDistSqr;
	}

	public RealRandomAccessible<T> getRealRandomAccessible(
			final double searchDist,
			final DoubleUnaryOperator rbf )
	{
		RBFInterpolator.RBFInterpolatorFactory< T > interp = 
				new RBFInterpolator.RBFInterpolatorFactory< T >( 
						rbf, searchDist, false,
						tree.firstElement().copy() );

		return Views.interpolate( tree, interp );
	}
	
	public double rbfRadius( final double rsqr )
	{
		if( rsqr > searchDistSqr )
			return 0;
		else
		{
			return value * ( 1 - ( rsqr * invSquareSearchDistance )); 
		}
	}
}
