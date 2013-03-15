package Planning;

import JavaVision.Position;

public class Ball extends ObjectDetails {
	public Position getReachableCoors(Robot robot) {
		return this.getReachableCoors(robot.getCoors(), robot.getSpeed());
	}
}