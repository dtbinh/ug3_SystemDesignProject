package PitchObject;

public class Ball extends PitchObject {
	public final static int DRIBBLE_DIST = 60;

	public Position intersectinPosition(Robot robot) {
		Position robotCoors = robot.getCoors();
		Position ballCoors = this.getCoors();
		if (robotCoors == null || ballCoors == null) {
			return null;
		}
		return this.getReachableCoors(robotCoors, robot.getSpeed());
	}

	public boolean robotHasBall(Robot robot) {
		return this.robotIsCloseToBall(robot) && this.robotIsFacingBall(robot);
	}

	public boolean robotIsFacingBall(Robot robot) {
		Position ballCoors = this.getCoors();
		if (ballCoors == null) {
			return false;
		}
		return robot.isFacing(ballCoors);
	}

	public boolean robotIsCloseToBall(Robot robot) {
		Position robotCoors = robot.getCoors();
		Position ballCoors = this.getCoors();
		if (robotCoors == null || ballCoors == null) {
			return false;
		}
		double distToBall = robotCoors.euclidDistTo(ballCoors);
		return distToBall <= DRIBBLE_DIST;
	}

	public Position pointBehindBall(Position direction, int distance) {
		Position ballPos = this.getCoors();
		double rvrsBallToGoal = new Robot(direction, 0).getAngleFromRobotToPoint(ballPos);
		Position goPoint = ballPos.projectPoint(rvrsBallToGoal, distance);
		if (!goPoint.withinPitch()){
			goPoint.setX((ballPos.getX()));
		    goPoint.setY((ballPos.getY()));
		}
		return goPoint;
	}
}