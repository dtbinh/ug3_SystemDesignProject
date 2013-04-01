package behaviors;

import robot.Robot;
import lejos.robotics.subsumption.Behavior;

public class DriveForwards implements Behavior {
	private boolean suppressed = false;

	public DriveForwards() { }

	public void suppress() {
		suppressed = true;
	}

	public boolean takeControl() {
		return true;
	}

	public void action() {
		suppressed = false;
		
		Robot.MOTOR_LEFT.forward();
		Robot.MOTOR_RIGHT.forward();
		
		while (!suppressed) {
			Thread.yield();
		}
		
		Robot.MOTOR_LEFT.flt(true);
		Robot.MOTOR_RIGHT.flt(true);
	}
}
