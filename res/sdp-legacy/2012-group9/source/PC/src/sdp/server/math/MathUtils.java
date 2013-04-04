package sdp.server.math;

import static com.googlecode.javacv.cpp.opencv_core.*;

/** Utility class for math functions that didn't fit anywhere else.
 * 
 * @author Cristian Cobzarenco
 *
 */
public class MathUtils {
	/** Bring an angle into the [0,2*pi) interval.
	 * 
	 * @param angle Original angle
	 * @return Angle in the specified interval. 
	 */
	public static double capAngle( double angle ) {
		double origAngle = angle;
		angle -= ( 2.0 * Math.PI ) * Math.floor( angle / (2.0 * Math.PI) );
		assert Double.isNaN( angle ) || angle >= 0.0 && angle <= Math.PI * 2.0 : origAngle + " " + angle;
		return angle;
	}
	
	/** Computes the shortest angle difference (in the (-pi,pi] interval) between two angles.
	 * Assumes abs( a - b ) <= 2*PI
	 * 
	 * @param a The first angle.
	 * @param b The second angle.
	 * @return The shortest difference between the two angles.
	 */
	public static double angleDiff( double a, double b ) {
		//assert a >= 0.0 && a <= Math.PI * 2.0 : a;
		//assert b >= 0.0 && b <= Math.PI * 2.0 : b;
		//assert Math.abs( a - b ) <= 2.0 * Math.PI;
		
		double r = capAngle( a - b );
		
		if( r < - Math.PI )
			r += 2 * Math.PI;
		else if( r > Math.PI )
			r -= 2 * Math.PI;
		
		assert Double.isNaN(a) || Double.isNaN(b) || r >= -Math.PI : r + ", " + a + ", " + b;
		assert Double.isNaN(a) || Double.isNaN(b) || r <= Math.PI  : r + ", " + a + ", " + b;
		
		return r;
	}
	
	/** Clamps a value between a minimum and a maximum. Assumes min <= max.
	 * 
	 * if( value > max )
	 *		value = max;
	 * else if( value < min )
	 * 		value = min;
	 * 
	 * return value;
	 * 
	 * @param value The value to be clamped.
	 * @param min   The minimum value.
	 * @param max   The maximum value.
	 */
	public static double clamp( double value, double min, double max ) {
		if( value > max )
			value = max;
		else if( value < min ) 
			value = min;
		
		return value;
	}
	
	/** Clamps a value between a minimum and a maximum. Assumes min <= max.
	 * 
	 * if( value > max )
	 *		value = max;
	 * else if( value < min )
	 * 		value = min;
	 * 
	 * return value;
	 * 
	 * @param value The value to be clamped.
	 * @param min   The minimum value.
	 * @param max   The maximum value.
	 */
	public static int clamp( int value, int min, int max ) {
		if( value > max )
			value = max;
		else if( value < min ) 
			value = min;
		
		return value;
	}
	
	public static double linearRegression( Vector2[] points ) {
		int nPoints = 0;
		double angle = 0.0;
		
		// Count the number on non-null points
		for( Vector2 p : points )
			if( p != null )
				nPoints ++;
		
		if( nPoints < 2 )
			angle = Double.NaN;
		else {
			CvMat vT    = CvMat.create( 2, nPoints );
			CvMat r     = CvMat.create( 2, nPoints );
			CvMat vTv   = CvMat.create( 2, 2 );
			CvMat y     = CvMat.create( nPoints, 1 );
			CvMat theta = CvMat.create( 2, 1 );
			Vector2 a = null, b = null;
			
			int mi = 0;
			for( Vector2 p : points ) {
				if( p != null ) {
					if( a == null )
						a = p;
					else if( b == null )
						b = p;
					
					vT.put( 0, mi, p.getX() );
					vT.put( 1, mi, 1.0 );
					y.put( 0, mi, p.getY() );
					mi ++;
				}
			}
			cvMulTransposed( vT, vTv, 0, null, 1.0 );
			cvSolve( vTv, vT, r );
			cvMatMul( r, y, theta );
			angle = MathUtils.capAngle( Math.atan( theta.get( 0, 0 ) ) );
			
			if( Math.abs( MathUtils.angleDiff( b.minus( a ).computeAngle(), angle ) ) > Math.PI / 2.0 )
				angle = MathUtils.capAngle( angle + Math.PI );
		}
		
		return angle;
	}
	
	public static CvRect offsetCvRect( CvRect rect, Vector2 offset ) {
		rect.x( rect.x() + offset.getIntX() );
		rect.y( rect.y() + offset.getIntY() );
		return rect;
	}
	
	public static boolean isInBounds( Vector2 p, Vector2 min, Vector2 max, double radius ) {
		return p.getX() >= min.getX() + radius && p.getY() >= min.getY() + radius &&
		       p.getX() <= max.getX() - radius && p.getY() <= max.getY() - radius;
	}
	
	public static boolean isInBounds( Vector2 p, Vector2 min, Vector2 max ) {
		return p.getX() >= min.getX() && p.getY() >= min.getY() &&
	           p.getX() <= max.getX() && p.getY() <= max.getY();
	}
	
	public static boolean isInBounds( Vector2 p, CvRect rect, double radius ) {
		return isInBounds( p, Vector2.bottomLeftOf( rect ), Vector2.topRightOf( rect ), radius );
	}
	
	public static boolean isInBounds( Vector2 p, CvRect rect ) {
		return isInBounds( p, Vector2.bottomLeftOf( rect ), Vector2.topRightOf( rect ), 0.0 );
	}
	
	public static boolean isInBounds( Vector2 p, Vector2 max ) {
		return isInBounds( p, Vector2.ZERO, max );
	}
	
	public static boolean isInBounds( Vector2 p, Vector2 max, double radius ) {
		return isInBounds( p, Vector2.ZERO, max, radius );
	}
	
	public static boolean isInCenteredBounds( Vector2 p, Vector2 topRight ) {
		return isInBounds( p, topRight.times( -1.0 ), topRight );
	}
	
	public static boolean isInCenteredBounds( Vector2 p, Vector2 topRight, double radius ) {
		return isInBounds( p, topRight.times( -1.0 ), topRight, radius );
	}
}
