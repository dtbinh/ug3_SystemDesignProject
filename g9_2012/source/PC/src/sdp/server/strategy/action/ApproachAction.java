package sdp.server.strategy.action;

import sdp.server.math.Vector2;
import sdp.server.strategy.Strategy;

public class ApproachAction extends DriveAction implements RotationAction {
	private double angle;
	
	public ApproachAction( Vector2 position, double angle ) {
		super( position );
		this.angle = angle;
	}
	
	@Override
	public double getRotation( Strategy strategy ) {
		return angle;
	}
	
	@Override
	public boolean applyAction( Strategy strategy ) {
		super.applyAction( strategy );
		strategy.getControl().setTargetAngle( angle );
		
		return true;
	}

}
