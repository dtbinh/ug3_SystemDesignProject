package PitchObject;

public class Constants {
	public final static int MAX_SPEED = 255;
	public final static int TURN_SPEED = 128;
	public final static int NEAR_RANGE = 100;

	public static Robot OUR_ROBOT;
	public static Robot THEIR_ROBOT;
	public static Position OUR_ROBOT_COORS;
	public static Position THEIR_ROBOT_COORS;

	public static void setOurRobot(Robot current){
		OUR_ROBOT = current;
		OUR_ROBOT_COORS = OUR_ROBOT.getCoors();
	}

	public static void setTheirRobot(Robot current){
		THEIR_ROBOT = current;
		THEIR_ROBOT_COORS = OUR_ROBOT.getCoors();
	}

	/**
	 * Run some sanity checks
	 * @param args
	 */
	public static void main(String args[]){
		Robot current = new Robot();
		current.setCoors(new Position(100,100));
		current.setAngle(0);
		Constants.setOurRobot(current);
		String signal = RobotMath.moveStraight(new Position(200,200));
		System.out.println(signal);
		current.setCoors(new Position(300,400));
		Constants.setOurRobot(current);
		System.out.println(RobotMath.rotate(new Position(400,400)));
		System.out.println(RobotMath.rotate(new Position(200,400)));
		System.out.println(RobotMath.moveToFace(Constants.OUR_ROBOT_COORS, new Position(200,300)));
	}
}
