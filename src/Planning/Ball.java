package Planning;

import JavaVision.Position;

public class Ball extends ObjectDetails {
	public final static int DRIBBLE_DIST = 60;
	
	public Position getReachableCoors(Robot robot) {
		return this.getReachableCoors(robot.getCoors(), robot.getSpeed());
	}
	
	public boolean robotHasIt(Robot robot) {
		double distToBall = RobotMath.euclidDist(robot.getCoors(), this.getCoors());
		boolean closeToBall = distToBall <= DRIBBLE_DIST;
		boolean facingBall = RobotMath.isTargeting(robot, this.getCoors());
		
		return closeToBall && facingBall;
	}
}