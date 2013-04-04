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
public class Movement {

	BTConnection btc;
	DataInputStream dis;
	DataOutputStream dos;
	Motormux chip = new Motormux(SensorPort.S4);
	private TouchSensors t;
	private double pixelsPerMS_L = 0.2;
	private double pixelsPerMS_R = 0.13;
	boolean waiting = true;

	public Movement() {

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

				while (t.collided()) {
					dis.skip(dis.available());
					waiting = false;
				}

				if (dis.available() > 0) {

					waiting = false;

					int moveType = dis.readInt();
					int int1 = 0;
					int int2 = 0;
					boolean immediateRet = false;

					if (moveType != 42) {
						int1 = dis.readInt();
						int2 = dis.readInt();
						immediateRet = dis.readBoolean();
					}

					switch (moveType) {

						case 1:
							forward(int1, int2, immediateRet);
							break;
						case 2:
							backward(int1, int2, immediateRet);
							break;
						case 3:
							strafeLeft(int1, int2);
							break;
						case 4:
							strafeRight(int1, int2);
							break;
						case 5:
							rotate(int1, int2, immediateRet);
							break;
						case 6:
							correctMove(int1, int2);
							break;
						case 7:
							correctStrafe(int1, int2);
							break;
						case 8:
							kick(int1, int2);
							break;
						case 9:
							flt(int1, int2);
							break;

						//Controller commands
						case 11:
							Motor.A.setSpeed(700);
							Motor.B.setSpeed(700);
							Motor.A.forward();
							Motor.B.forward();
							break;
						case 12:
							Motor.A.setSpeed(700);
							Motor.B.setSpeed(700);
							Motor.A.backward();
							Motor.B.backward();
							break;
						case 13: {

							Motor.A.setSpeed(700);
							Motor.B.setSpeed(700);
							Motor.A.backward();
							Motor.B.forward();
							//chip.rotateLeft((byte)255);
							break;

						}

						case 14: {
							Motor.A.setSpeed(700);
							Motor.B.setSpeed(700);
							Motor.A.forward();
							Motor.B.backward();
							//chip.rotateRight((byte)255);
							break;
						}
						case 15: {
							Motor.C.setSpeed(900);
							Motor.C.forward();
							try {
								Thread.sleep(100);
							} catch (InterruptedException ex) {
							}
							Motor.C.backward();
							break;
						}
						case 16: {
							Motor.C.setSpeed(200);
							Motor.C.forward();
							break;
						}
						case 17: {
							Motor.C.flt();
							break;
						}

						//Diagonal
						case 18: {
							diagonal(int1, int2);
							break;
						}
						//Controller Testing Commands


						case 31:
							continuousForward(int1, int2);
							break;

						case 42:
							//New style move
							int s1 = dis.readInt();
							int s2 = dis.readInt();
							int s3 = dis.readInt();
							int s4 = dis.readInt();

							System.out.println("Setting speeds: (" + s1 + "," + s2 + "," + s3 + "," + s4 + ")");

							setSpeeds(s1, s2, s3, s4);
							break;

						case 99:
							btc.close();
							waitForConnection();
							break;

					}

				} else {
					if (!waiting) {
						System.out.println("Finished");
						dos.writeInt(-5);
						dos.flush();
						waiting = true;
					}
					Thread.yield();
				}

			} catch (IOException e) {
				waitForConnection();
			}
		}


	}

	public void continuousForward(int ratio, int maxPower) {
		int s = ratio * maxPower;
		s = s / 1000;
		Motor.A.setSpeed(s);
		Motor.B.setSpeed(maxPower);
		Motor.A.forward();
		Motor.B.forward();
	}

	public void forward(int speed, int angle, boolean immediateRet) {
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.B.rotate(-angle, true);
		Motor.A.rotate(-angle, immediateRet);
	}

	public void backward(int speed, int angle, boolean immediateRet) {
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.A.rotate(angle, true);
		Motor.B.rotate(angle, immediateRet);
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

	public void rotate(int speed, int angle, boolean immediateRet) {

		//byte oldMotorSpeed = (byte) (speed * 900 / 255);

		//if(angle<0) chip.rotateRight(oldMotorSpeed);
		//else chip.rotateLeft(oldMotorSpeed);

		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.A.rotate(-angle, true);
		Motor.B.rotate(angle, false);

	}

	public void strafeLeft(int speed, int angle) {

		chip.strafeL((byte) speed);
		try {
			Thread.sleep((int) (angle / pixelsPerMS_L * (speed / 255)));
		} catch (InterruptedException ex) {
		}
		chip.flt();
	}

	public void strafeRight(int speed, int angle) {

		chip.strafeR((byte) speed);
		try {
			Thread.sleep((int) (angle / pixelsPerMS_R * (speed / 255)));
		} catch (InterruptedException ex) {
		}

		chip.flt();
	}

	public void correctMove(int speedL, int speedR) {
		Motor.A.setSpeed(speedL);
		Motor.B.setSpeed(speedR);
	}

	public void correctStrafe(int speedF, int speedB) {
		/**
		TODO
		 **/
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

	public void diagonal(int xDifference, int yDifference) {
	}

	public void setSpeeds(int speedLeft, int speedRight, int speedFront, int speedBack) {

		System.out.println("Setting speeds: (" + speedLeft + "," + speedRight + "," + speedFront + "," + speedBack + ")");

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

	public static void main(String[] args) {
		Movement m = new Movement();
	}
}
