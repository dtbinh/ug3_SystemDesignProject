package sdp.server.math;

public class Line {
	private Vector2 o, d;
	
	static public Line fromPointAndUnit( Vector2 o, Vector2 d ) {
		return new Line( o, d );
	}
	
	static public Line fromTwoPoints( Vector2 p1, Vector2 p2 ) {
		return new Line( p1, p2.minus(p1).computeUnit() );
	}
	
	public double signedDistanceTo( Vector2 p ) {
		return p.cross( d ) + d.cross( o );
	}
	
	public double distanceTo( Vector2 p ) {
		return Math.abs( signedDistanceTo(p) );
	}
	
	public Vector2 pointAt( double distanceFromOrigin ) {
		return o.plus( d.times( distanceFromOrigin ) );
	}
	
	public Line getNormal( Vector2 newOrigin ) {
		return fromPointAndUnit( newOrigin, new Vector2( - d.getY(), d.getX() ).times( Math.signum( signedDistanceTo( newOrigin ) ) ) );
	}
	
	public Line getNormal() {
		return getNormal( o );
	}
	
	public double intersect( Line other ) {
		return other.o.minus( o ).cross( other.d ) / d.cross( other.d );
	}
	
	public Vector2 getOrigin() {
		return o;
	}
	
	public Vector2 getDirection() {
		return d;
	}
	
	public double intersectCircle( Vector2 centre, double radius ) {
		Vector2 omc  = o.minus( centre );
		double D     = omc.cross( omc.plus( d ) );
		double delta = radius * radius - D * D;
		double t;
		
		if( delta < 0 )
			t = Double.NaN;
		else {
			double t1; 
			double t2; 
			
			if( Math.abs( d.getY() ) >= 1e-6 ) {				
				double y1 = - D * d.getX() + delta;
				double y2 = - D * d.getX() - delta;
				t1 = ( y1 - omc.getY() ) / d.getY();
				t2 = ( y2 - omc.getY() ) / d.getY();
			} else {
				delta = Math.abs( d.getX() ) * Math.sqrt( delta );
				
				double x1 = - D * d.getY() + delta;
				double x2 = - D * d.getY() - delta;
				t1 = ( x1 - omc.getX() ) / d.getX();
				t2 = ( x2 - omc.getX() ) / d.getX();
			}
			
			if( t1 * t2 < 0 ) {
				if( t1 > 0 )
					t = t1;
				else
					t = t2;
			} else if( Math.abs( t1 ) < Math.abs( t2 ) ) {
				t = t1;
			} else {
				t = t2;
			}
		}
		
		return t;
	}
	
	public Vector2 projectPoint( Vector2 p ) {
		return pointAt( intersect( getNormal( p ) ) );
	}
	
	public double solveWithY( double y ) {
		if( d.getY() == 0.0 )
			return Double.NaN;
				
		return ( y - o.getY() ) / d.getY();
	}
	
	public double solveWithX( double x ) {
		if( d.getX() == 0.0 )
			return Double.NaN;
		
		return ( x - o.getX() ) / d.getX();
	}
	
	private Line( Vector2 o, Vector2 d ) {
		this.o = o;
		this.d = d;
	}

	public double computeAngle() {
		return d.computeAngle();
	}
}
