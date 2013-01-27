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
public class M1_kick
{
	public static final int KICK_ANGLE = 100; // in "ticks"
	public static final int KICK_DIRECTION = 1; // change to -1, to change direction
	
	public static void main (String[] args) {
		// Max speed
		Motor.C.setSpeed(9999);
		
		// For countdown
		Sound.setVolume(30);

		while (true) {
			LCD.clear();
			System.out.println("Press to kick...");
			Button.waitForAnyPress();
			
			System.out.println("3");
			Sound.beep();
			try{Thread.sleep(1000);}catch(Exception e) {}
			
			
			System.out.println("2");
			Sound.beep();
			try{Thread.sleep(1000);}catch(Exception e) {}
			
			System.out.println("Kick!");
			
			// Kick
			Motor.C.rotate(KICK_ANGLE * -KICK_DIRECTION, true);
			
			try{Thread.sleep(500);}catch(Exception e) {}
			
			// Reset kicker to original position
			Motor.C.rotate(KICK_ANGLE * KICK_DIRECTION, true);
		}
	}




}