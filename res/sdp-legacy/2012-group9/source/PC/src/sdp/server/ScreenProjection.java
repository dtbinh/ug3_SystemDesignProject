package sdp.server;

import sdp.server.math.Vector2;

public class ScreenProjection {
	private double  scale        = 0.31 / 90; //0.004153;
	private Vector2 screenCenter = Vector2.ZERO;
	private Vector2 screenSize   = Vector2.ZERO;
	
	public ScreenProjection( Vector2 screenSize ) {
		setScreenSize( screenSize );
	}
	
	public void setScale( double newScale ) {
		scale = newScale;
	}
	
	public double getScale() {
		return scale;
	}
	
	public Vector2 getScreenSize() {
		return screenSize;
	}
	
	public void setScreenSize( Vector2 newScreenSize ) {
		screenSize   = newScreenSize;
		screenCenter = screenSize.times( .5 );
	}
	
	public Vector2 unprojectPosition( Vector2 screenCoords ) {
		Vector2 worldCoords = screenCoords.minus( screenCenter ).times( scale ); 
		return new Vector2( worldCoords.getX(), - worldCoords.getY() );
	}
	
	public Vector2 projectPosition( Vector2 worldCoords ) {
		Vector2 pixelCoords = worldCoords.times( 1.0 / scale ).plus( screenCenter );
		return new Vector2( pixelCoords.getX(), screenSize.getY() - pixelCoords.getY() );
	}
	
	public Vector2 unprojectDifference( Vector2 screenCoords ) {
		Vector2 worldCoords = screenCoords.times( scale ); 
		return new Vector2( worldCoords.getX(), - worldCoords.getY() );
	}
	
	public Vector2 projectDifference( Vector2 worldCoords ) {
		Vector2 pixelCoords = worldCoords.times( 1.0 / scale );
		return new Vector2( pixelCoords.getX(), - pixelCoords.getY() );
	}
	
	public double unprojectAngle( double angle ) {
		return 2.0 * Math.PI - angle;
	}
	
	public double projectAngle( double angle ) {
		return unprojectAngle( angle );
	}
}
