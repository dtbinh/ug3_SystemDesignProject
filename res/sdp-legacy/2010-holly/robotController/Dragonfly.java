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
public class Dragonfly {

	BTConnection btc;
	DataInputStream dis;
	DataOutputStream dos;
	Motormux chip = new Motormux(SensorPort.S4);
	private TouchSensors t;
	private double pixelsPerMS_L = 0.2;
	private double pixelsPerMS_R = 0.13;
	boolean waiting = true;
	boolean rotating = false;
	int s1,s2,s3,s4;

	public Dragonfly() {

		Motor.C.smoothAcceleration(false);

		Button.ENTER.addButtonListener(new ButtonListener() {

			public void buttonPressed(Button b) {
			}

			public void buttonReleased(Button b) {
				System.exit(0);
			}
		});

		waitForConnection();
	}

	public void waitForConnection() {

		LCD.drawString("WAITING TO CONNECT", 0, 0);
		LCD.refresh();

		btc = Bluetooth.waitForConnection();
		dis = btc.openDataInputStream();
		dos = btc.openDataOutputStream();

		t = new TouchSensors(chip);
		t.start();

		LCD.drawString("CONNECTED", 0, 0);
		LCD.refresh();

		executeCommands();
	}

	public void executeCommands() {

		while (true) {

			try {

					

				if (dis.available() > 0) {

					int moveType = dis.readInt();
					int int1 = 0;
					int int2 = 0;
					boolean immediateRet = false;

					if (moveType != 42) {
						int1 = dis.readInt();
						int2 = dis.readInt();
						immediateRet = dis.readBoolean();
					}
					else{
						s1 = dis.readInt();
						s2 = dis.readInt();
						s3 = dis.readInt();
						s4 = dis.readInt();
					}

					if(t.collided() || rotating){

						rotating = ((Motor.A).isMoving() | (Motor.B).isMoving());

						continue;
					}

					switch (moveType) {

						case 5:
							rotate(int1, int2);
							break;
						case 8:
							kick(int1, int2);
							break;
						case 9:
							flt(int1, int2);
							break;

						case 42:
							
							//New style move
							

							System.out.println("Setting speeds: (" + s1 + "," + s2 + "," + s3 + "," + s4 + ")");

							setSpeeds(s1, s2, s3, s4);
							break;

						case 99:
							btc.close();
							waitForConnection();
							break;

					}

				}

			} catch (IOException e) {
				waitForConnection();
			}
		}
	}

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

	public void rotate(int speed, int angle) {

		setSpeeds(0,0,0,0);

		rotating = true;

		//byte oldMotorSpeed = (byte) (speed * 900 / 255);

		//if(angle<0) chip.rotateRight(oldMotorSpeed);
		//else chip.rotateLeft(oldMotorSpeed);

		Motor.A.backward();
		Motor.B.backward();
		
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.A.rotate(-angle, true);
		Motor.B.rotate(angle, true);

		try{
			Thread.sleep(100);
		}
		catch(Exception e){}

	}

	public void kick(int speed, int angle) {

		Motor.C.setSpeed(900);
		Motor.C.forward();
		try {
			Thread.sleep(50);
		} catch (InterruptedException ex) {
		}
		Motor.C.backward();
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
		}
		Motor.C.forward();

		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
		}

		Motor.C.flt();

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
		Dragonfly m = new Dragonfly();
	}
}
