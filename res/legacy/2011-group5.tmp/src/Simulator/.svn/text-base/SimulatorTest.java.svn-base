package Simulator;

import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

public class SimulatorTest {
	public static int boardWidth = 750;
	public static int boardHeight = 400;
	public static int padding = 100;
	public static int wallThickness = 20;
	public static int goalWidth = boardHeight/2;
	public static int goalThickness = 50;
	public static int robotStartX = padding + wallThickness;
	public static int oppRobotStartX = padding + boardWidth - wallThickness;
	public static int robotStartY = boardHeight/2 + padding;
	public static int ballStartX = boardWidth/2 + padding;
	public static int ballStartY = boardHeight/2 + padding;
	
	Robot robot;

	@Before
	public void setUp() throws Exception {
		robot = new Robot(robotStartX,robotStartY+80, 40, 40, Color.BLUE, null, 0);
	}

	@Test
	public final void testTurnRight() {
		// tests turning in right direction
		double originalAngle = robot.getAngle();
		robot.turn(-1);
		assertTrue(robot.convertAngle((originalAngle + 2)) == robot.getAngle());
	}
	@Test
	public final void testTurnLeft() {
		// tests turning in left direction
		double originalAngle = robot.getAngle();
		robot.turn(1);
		assertTrue(robot.convertAngle((originalAngle - 2)) == robot.getAngle());
	}
	@Test
	public final void testGetRealAngle() {
		// getRealAngle() gets the angle of the body
		// angle can only be between 0 and 359
		robot.getBody().setRotation((float) Math.toRadians(0));
		float test1 = robot.getRealAngle();
		robot.getBody().setRotation((float) Math.toRadians(90));
		float test2 = robot.getRealAngle();
		robot.getBody().setRotation((float) Math.toRadians(180));
		float test3 = robot.getRealAngle();
		robot.getBody().setRotation((float) Math.toRadians(270));
		float test4 = robot.getRealAngle();
		robot.getBody().setRotation((float) Math.toRadians(359));
		float test5 = robot.getRealAngle();
		robot.getBody().setRotation((float) Math.toRadians(17));
		float test6 = robot.getRealAngle();
		robot.getBody().setRotation((float) Math.toRadians(201));
		float test7 = robot.getRealAngle();
		robot.getBody().setRotation((float) Math.toRadians(151));
		float test8 = robot.getRealAngle();
//		System.out.println("test1: "+(int)test1+" test2: "+(int)test2+" test3: "+(int)test3+" test4: "+(int)test4+" test5: "+(int)test5+" test6: "+(int)test6+" test7: "+(int)test7+" test8: "+(int)test8);
		assertTrue(((int)test1 == 0) && ((int)test2 == 270) && ((int)test3 == 180) && ((int)test4 == 90) && ((int)test5 == 1) && ((int)test6 == 343) && ((int)test7 == 159) && ((int)test8 == 209));
	}
	

}
