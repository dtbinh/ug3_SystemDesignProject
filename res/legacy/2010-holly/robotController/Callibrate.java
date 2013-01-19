                    //@author Ben Ledbury

/**
 * This is a lejos program to run on the brick so ignore all errors if working with Eclipse/NetBeans
 *
 * To compile and get this onto the brick switch it on and run the following commands:
 *
 * 		$PATH_TO_LEJOS/bin/nxjc	Movement.java
 *		$PATH_TO_LEJOS/bin/nxjlink -o Movement.nxj Movement
 *		$PATH_TO_LEJOS/bin/nxjupload -b -n "Holly" Movement.nxj
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

/*	Movement types:
 *
 * 	Type 1: Forward
 *	Type 2: Backward
 *	Type 3: StrafeL
 *	Type 4: StrafeR
 *	Type 5: Rotate
 *	Type 6: CorrectMove
 *	Type 7: CorrectStrafe
 *	Type 8: Kick
 *	Type 9: Float
 *
 *      Type 11: Forward Continuous
 *      Type 12: Reverse Continuous
 *      Type 13: Rotate Left Continuous
 *      Type 14: Rotate Right Continuous
 */
public class Callibrate {

	Motormux chip = new Motormux(SensorPort.S4);

	public void flt(int int1, int int2) {
		Motor.A.stop();
		Motor.B.stop();
		chip.stop();
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
		}
		Motor.A.flt();
		Motor.B.flt();
		chip.flt();
	}

	public void setSpeeds(int speedLeft, int speedRight, int speedFront, int speedBack) {

		System.out.println("Setting speeds: (" + speedLeft + "," + speedRight + "," + speedFront + "," + speedBack + ")");

		if(speedLeft==0 && speedRight ==0 && speedFront==0 && speedBack==0){
			//STOP
			stop();
			return;
		}

		Motor.A.setSpeed(speedLeft);
		Motor.B.setSpeed(speedRight);

		if (speedLeft < 0) {
			Motor.A.forward();
		} else {
			Motor.A.backward();
		}

		if (speedRight < 0) {
			Motor.B.forward();
		} else {
			Motor.B.backward();
		}

		chip.setSpeeds(speedFront, speedBack);

		boolean flt = false;

		//Check for 0 and set any motors that need it to flt()
		if (speedLeft == 0) {
			flt = true;
		}
		if (speedRight == 0) {
			flt = true;
		}

		if (flt) {

			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
			if (speedLeft == 0) {
				Motor.A.flt();
			}

			if (speedRight == 0) {
				Motor.B.flt();
			}
		}

	}

	private void stop(){
		Motor.A.stop();
		Motor.B.stop();
		chip.stop();
	}

	public static void main(String[] args) {
		Callibrate m = new Callibrate();
		m.setSpeeds(700, 700, 0, 0);

		try{
			Thread.sleep(2000);
		}
		catch(Exception e){
		}

		m.stop();

		try{
			Thread.sleep(2000);
		}
		catch(Exception e){
		}

		m.setSpeeds(0, 0, 255, 255);
		
		try{
			Thread.sleep(2000);
		}
		catch(Exception e){
		}

		m.stop();
	}
}
