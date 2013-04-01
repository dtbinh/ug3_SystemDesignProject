package behaviors;

import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.robotics.subsumption.Behavior;

import robot.Robot;

public class BackOff implements Behavior {
	public static final double BACKOFF_DISTANCE = 0.2;

	private TouchSensor[] touch_sensors;
	private boolean suppressed = false;

	public BackOff(SensorPort[] ports) {
		touch_sensors = new TouchSensor[ports.length];
		for (int i = 0; i < ports.length; i++) {
			touch_sensors[i] = new TouchSensor(ports[i]);
		}
	}

	public void suppress() {
		suppressed = true;
	}

	public boolean takeControl() {
		for (TouchSensor touch_sensor : touch_sensors) {
			if (touch_sensor.isPressed()) {
				return true;
			}
		}
		return false;
	}

	public void action() {
		suppressed = false;
		
		int motor_angle = (int) Robot.distance2motorangle(BACKOFF_DISTANCE);
		Robot.MOTOR_LEFT.rotate(-motor_angle, true);
		Robot.MOTOR_RIGHT.rotate(-motor_angle, true);
		
		while ((Robot.MOTOR_LEFT.isMoving() ||
				Robot.MOTOR_RIGHT.isMoving()) &&
				!suppressed) {
			Thread.yield();
		}
		
		Robot.MOTOR_LEFT.flt(true);
		Robot.MOTOR_RIGHT.flt(true);
	}
}
