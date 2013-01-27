import lejos.nxt.*;
import java.lang.*;
import lejos.nxt.Button;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;

// Left wheel (when looked from behind) - Port A
// Right wheel - Port B
// Kicker - Port C
public class M1_drive
{
	public static final int DRIVE_DISTANCE = 2500; // in "ticks"
	public static final int LEFT_DIRECTION = 1; // change to -1, to change direction
	public static final int RIGHT_DIRECTION = -1; // change to 1, to change direction
	public static final int ACCELERATION = 3000; // Default 6000
	
	public static void main (String[] args) {
		// Max speed
		Motor.A.setSpeed(9999);
		Motor.B.setSpeed(9999);
		
		Motor.A.setAcceleration(ACCELERATION);
		Motor.B.setAcceleration(ACCELERATION);
		
		// For countdown
		Sound.setVolume(30);

		while (true) {
			LCD.clear();
			System.out.println("Press to drive...");
			Button.waitForAnyPress();
			
			System.out.println("3");
			Sound.beep();
			try{Thread.sleep(1000);}catch(Exception e) {}
			
			
			System.out.println("2");
			Sound.beep();
			try{Thread.sleep(1000);}catch(Exception e) {}
			
			System.out.println("Drive!");
			
			// Drive
			Motor.B.rotate(DRIVE_DISTANCE * RIGHT_DIRECTION, true);
			Motor.A.rotate(DRIVE_DISTANCE * LEFT_DIRECTION, false);
			
			//try{Thread.sleep(1500);}catch(Exception e) {}
			
			//Motor.A.stop();
			//Motor.B.stop();
		}
	}




}