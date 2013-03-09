package Planning;

import JavaVision.Position;

public class ConstantTester {
	public static void main(String args[]){
		Robot current = new Robot();
		current.setCoors(new Position(100,100));
		current.setAngle(0);
		GameConstants.setOurRobot(current);
		String signal = RobotMath.moveStraight(new Position(200,200));
		System.out.println(signal);
		current.setCoors(new Position(300,400));
		GameConstants.setOurRobot(current);
		System.out.println(RobotMath.rotate(new Position(400,400)));
		System.out.println(RobotMath.rotate(new Position(200,400)));
		System.out.println(RobotMath.moveToFace(GameConstants.OUR_ROBOT_COORS, new Position(200,300)));
	}

}
