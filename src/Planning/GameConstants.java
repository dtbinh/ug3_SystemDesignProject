package Planning;

import JavaVision.Position;

public class GameConstants {
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
	
	
}
