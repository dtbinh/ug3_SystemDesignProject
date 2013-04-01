package behaviors;

import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;

import robot.Robot;

public class CommandsFromServer implements Behavior {
	private boolean suppressed = false;
	private static InputStream dis;
	private static OutputStream dos;
	private Robot superRobot;
	

	public CommandsFromServer(Robot thisRobot) { 
		LCD.clear();
		LCD.drawString("Waiting for", 0, 0);
		LCD.drawString("Bluetooth...", 0, 1);

		NXTConnection connection = Bluetooth.waitForConnection();
		dis = connection.openInputStream();
		dos = connection.openOutputStream();
		Sound.beep();
		LCD.clear();
		LCD.drawString("Connected!", 0, 0);
		superRobot = thisRobot;

	}

	public void suppress() {
		suppressed = true;
	}

	public boolean takeControl() {
		return true;
	}

	public void action() {
		byte[] positions = new byte[3];//us /  them /  goal assumed TODO: Make sense
		// TODO
		try{
			while (true) { //TODO: Fix?
				dis.read(positions);
				
				LCD.drawString("US: " + positions[0] + "GOAL: " + positions[2], 0, 6);
				
				Pose goalReadPose =  makePose(positions[2]);
				if (!goalReadPose.equals(superRobot.goalPose)){
					superRobot.goalPose = goalReadPose; //TODO: SEND THIS SHIT OVER BT
					superRobot.ourPose = makePose(positions[0]);
					superRobot.theirPose = makePose(positions[1]);		
					superRobot.needsNewPath = true;
				}
			}
	
		} catch (Exception e) {
			// Does not wait for user to notice
			LCD.drawString("EXCEPTION1!", 0, 6);
			e.printStackTrace();
		}
		
		while (!suppressed) {
			Thread.yield();
		}
		
	}

	private Pose makePose(byte b) {
		// TODO Auto-generated method stub
		return null;
	}



}
