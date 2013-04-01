package robot;

import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.*;

import behaviors.*;

/**
 * Units are in meters
 */
public class Robot {
	public static final double GEAR_RATIO = 3.0;
	public static final double WHEEL_RADIUS = 0.0816 * GEAR_RATIO;
	public static final double AXLE_LENGTH = 0.155;
	public static final double STEERING_RAD = 1; //Simulated turning circle TODO: find a good value
	
	private static DifferentialPilot pilot;

	public static NXTRegulatedMotor MOTOR_LEFT = Motor.A;
	public static NXTRegulatedMotor MOTOR_RIGHT = Motor.B;
	public static NXTRegulatedMotor MOTOR_KICKER = Motor.C;

	public static SensorPort TOUCH_SENSOR_LEFT = SensorPort.S1;
	public static SensorPort TOUCH_SENSOR_RIGHT = SensorPort.S3;
	
	public static double MAX_SPEED = MOTOR_LEFT.getMaxSpeed();
	
	//TODO: SETTERS AND GETTERS N00B
	public volatile Pose ourPose; //SNG
	public volatile Pose theirPose; //SNG
	public volatile Pose goalPose; //SNG
	
	public boolean needsNewData = true; 
	public volatile boolean needsNewPath = false; //NEVER do a plan first bro //SNG
	
	public volatile Navigator pathNav;
	
	
	
	
	
	
	public Robot() {
		pilot =  new DifferentialPilot(WHEEL_RADIUS,AXLE_LENGTH, MOTOR_LEFT, MOTOR_RIGHT, false);
		pilot.setTravelSpeed(MAX_SPEED);
		pilot.setRotateSpeed(MAX_SPEED);
	
	}

	public static void main(String[] args) {
		// default behaviour: get commands from server
		Robot thisRobot = new Robot();
			Behavior b1 = new CommandsFromServer(thisRobot);
		
		// instinctively back off when hitting something
		SensorPort[] touch_sensors = {TOUCH_SENSOR_LEFT, TOUCH_SENSOR_RIGHT};
		Behavior b2 = new BackOff(touch_sensors);
		Behavior b3 = new PlanWithoutBall(thisRobot);
		Behavior b4 = new PlanWithBall(thisRobot);
		Behavior b5 = new ExecutePlan(thisRobot);
		
		// run the robot
		Behavior[] behaviors = {b1, b2};
		Arbitrator arby = new Arbitrator(behaviors);
		arby.start();
	}
	
	public static double distance2motorangle(double distance_meters) {
		double meters_in_one_rotation = 2 * Math.PI * WHEEL_RADIUS;
		double degrees_for_one_meter = 360.0 / meters_in_one_rotation;
		return distance_meters * degrees_for_one_meter;
	}
	
	public void diffSetup(){
		pilot.setMinRadius(0);
		pathNav = new Navigator(pilot);
		pathNav.clearPath();
	}
	
	public void steerSetup(){
		pilot.setMinRadius(STEERING_RAD);
		pathNav = new Navigator(pilot);
		pathNav.clearPath();
	}

	public Navigator getNav() {
		// TODO Auto-generated method stub
		return this.pathNav;
	}

	public void requestData() {
		this.needsNewPath = true;
		this.needsNewData = true;
		
	}
}
