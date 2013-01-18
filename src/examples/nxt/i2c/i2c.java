import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;

public class i2c {
	
	public static void main (String[] args) {
		Motormux robot = new Motormux(SensorPort.S4);
		
		System.out.println("Press to start..");
		Button.waitForAnyPress();
		
		robot.set_speed(0, -255);
		robot.set_speed(1, 255);
		robot.set_speed(2, -255);
		robot.set_speed(3, 255);
		
		System.out.println("Press to end..");
		Button.waitForAnyPress();
		robot.flt();
		
		
	}
}