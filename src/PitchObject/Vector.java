package PitchObject;

/**
* Class for real 2-D vectors, used e.g. to represent velocity and acceleration.
* @author s1047194
*
*/
public class Vector {
	private final double x;
	private final double y;
	
	public Vector(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getMagnitude()  {
		return Math.sqrt(x * x + y * y);
    }
}
