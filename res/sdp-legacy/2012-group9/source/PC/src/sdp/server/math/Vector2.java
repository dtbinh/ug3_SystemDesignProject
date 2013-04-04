package sdp.server.math;

import java.lang.Math;

import org.jbox2d.common.Vec2;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;

/** Immutable 2D Vector class.
 * 
 * @author Cristian Cobzarenco
 *
 */
public class Vector2 {
	public static final Vector2 ZERO = new Vector2( 0.0, 0.0 );
	public static final Vector2 X    = new Vector2( 1.0, 0.0 );
	public static final Vector2 Y    = new Vector2( 0.0, 1.0 );
	public static final Vector2 NaN  = new Vector2( Double.NaN, Double.NaN );
	
	private double x;
	private double y;
	
	/// Create a new vector with given coordinates.
	public Vector2( double x, double y ) {
		this.x = x;
		this.y = y;
	}
	
	/// Create a unit vector from a given angle with the horizontal axis
	public static Vector2 fromAngle( double angle ) {
		return new Vector2( Math.cos( angle ), Math.sin( angle ) );
	}
	
	/// Create a vector from a given angle with the horizontal axis and a given norm
	public static Vector2 fromAngle( double angle, double norm ) {
		return Vector2.fromAngle( angle ).times( norm );
	}
	
	/// Create a vector from the top right position of CvRect
	public static Vector2 topRightOf( CvRect rect ) {
		return new Vector2( rect.x() + rect.width(), rect.y() + rect.height() );
	}
	
	/// Create a vector from the bottom left position of a CvRect
	public static Vector2 bottomLeftOf( CvRect rect ) {
		return new Vector2( rect.x(), rect.y() );
	}
	
	/// Create a new vector from a CvPoint
	public Vector2( CvPoint p ) {
		this( p.x(), p.y() );
	}
	
	/// Create a new vector from a JBox2D Vec2 object.
	public Vector2( Vec2 vec2 ) {
		this( (double) vec2.x, (double) vec2.y );
	}
	
	
	/// Create a CvPoint from a vector
	public CvPoint getCvPoint() {
		return new CvPoint( getIntX(), getIntY() );
	}
	
	/// Create a JBox2D Vec2 from a Vector2
	public Vec2 getVec2() {
		return new Vec2( (float) x, (float) y );
	}
	
	public Vector2 computeNormal() {
		return new Vector2( -y, x );
	}
	
	/// Get the X coordinate of this vector.
	public double getX() {
		return x;
	}
	
	/// Get the Y coordinate of this coordinate.
	public double getY() {
		return y;
	}
	
	/// Get the integer X coordinate of this vector.
	public int getIntX() {
		return (int) Math.round( x );
	}
	
	/// Get the integer Y coordinate of this coordinate.
	public int getIntY() {
		return (int) Math.round( y );
	}
	
	/// Compute the norm of the vector.
	public double computeNorm() {
		return Math.sqrt( computeSquaredNorm() );
	}
	
	/// Compute the square of the vector's norm.
	public double computeSquaredNorm() {
		return x * x + y * y;
	}
	
	/// Subtract another vector from this one.
	public Vector2 minus( Vector2 rhs ) {
		return new Vector2( x - rhs.getX(), y - rhs.getY() );
	}
	
	/// Add another vector to this one.
	public Vector2 plus( Vector2 rhs ) {
		return new Vector2( x + rhs.getX(), y + rhs.getY() );
	}
	
	/// Multiply this vector by a scalar.
	public Vector2 times( double scalar ) {
		return new Vector2( x * scalar, y * scalar );
	}
	
	/// Multiply this vector by another vector element-wise.
	public Vector2 times( Vector2 v ) {
		return new Vector2( x * v.x, y * v.y );
	}
	
	/// Get a unit vector of the same direction.
	public Vector2 computeUnit() {
		double n = computeSquaredNorm();
		if( n == 0.0 )
			return this;
		else {
			return this.times( 1.0 / Math.sqrt( n ) );
		}
	}
	
	/// Compute the dot product with another vector.
	public double dot( Vector2 rhs ) {
		return x*rhs.x + y*rhs.y;
	}
	
	/// Compute the dot product with another vector.
	public double cross( Vector2 rhs ) {
		return x*rhs.y - y*rhs.x;
	}
	
	/// Returns this * -1
	public Vector2 negate() {
		return new Vector2( -x, -y );
	}
		
	/// Compute the dot product with a vector given as components.
	public double dot( double rx, double ry ) {
		return x*rx + y*ry;
	}
	
	/// Check whether this vector is equal to another one within a given tolerance.
	public boolean approxEquals( Vector2 rhs, double tolerance ) {
		assert rhs != null;
		return  Math.abs( x - rhs.getX() ) <= tolerance &&
				Math.abs( y - rhs.getY() ) <= tolerance;
	}
	
	/// Get the angle with the horizontal axis.
	public double computeAngle() {
		return MathUtils.capAngle( Math.atan2( y, x ) );
	}
	
	/// Get distance to another point
	public double computeDistanceTo( Vector2 other ) {
		return minus( other ).computeNorm();
	}
	
	/// Check if either component is NaN
	public boolean isNaN() {
		return Double.isNaN( x ) || Double.isNaN( y );
	}
	
	/// Convert to string.
	@Override
	public String toString() {
		return "[ " + x + ", " + y + "]";
	}
}
