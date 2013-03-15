package Planning;

import JavaVision.Position;

public class Robot extends ObjectDetails {
	
	// TODO: test to find good value for this
	public static final double DEFAULT_VELOCITY = 100.0;
	
	public Robot() {
	}
	
	public Robot (Position coors, float angle) {
		this.setCoors(coors);
		this.setAngle(angle);
	}
	
	public Position getReachableCoors() {
		return super.getReachableCoors(this.coors, this.getSpeed());
	}
	
	@Override public Position getReachableCoors(Position coors, double speed) {
		return this.getReachableCoors();
	}
 	
	@Override public double getSpeed() {
		if (this.isMoving()) {
			return super.getSpeed();
		}
		return DEFAULT_VELOCITY;
	}
}