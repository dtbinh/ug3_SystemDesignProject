package Planning;

import JavaVision.Position;

public class Ball extends ObjectDetails {
	public final static int DRIBBLE_DIST = 60;
	
	public Position getReachableCoors(Robot robot) {
		Position robotCoors = robot.getCoors();
		Position ballCoors = this.getCoors();
		if (robotCoors == null || ballCoors == null) {
			return null;
		}
		return this.getReachableCoors(robotCoors, robot.getSpeed());
	}

	public boolean robotHasBall(Robot robot) {
		return this.robotCloseToBall(robot) && this.robotFacingBall(robot);
	}
	
	public boolean robotFacingBall(Robot robot) {
		Position ballCoors = this.getCoors();
		if (ballCoors == null) {
			return false;
		}
		return RobotMath.isTargeting(robot, ballCoors);
	}
	
	public boolean robotCloseToBall(Robot robot) {
		Position robotCoors = robot.getCoors();
		Position ballCoors = this.getCoors();
		if (robotCoors == null || ballCoors == null) {
			return false;
		}
		double distToBall = RobotMath.euclidDist(robotCoors, ballCoors);
		return distToBall <= DRIBBLE_DIST;
	}
}