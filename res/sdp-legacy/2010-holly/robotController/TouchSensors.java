import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import java.io.DataOutputStream;

public class TouchSensors extends Thread {

	DataOutputStream output;
	Motormux chip;

	boolean collided;

	public TouchSensors(Motormux motormux) {
		chip = motormux;
	}

	public void run() {
		TouchSensor left = new TouchSensor(SensorPort.S1);
		TouchSensor right = new TouchSensor(SensorPort.S2);
		TouchSensor front = new TouchSensor(SensorPort.S3);

		while (true) {
			
			if (left.isPressed()) {
				try {
					setSpeeds(0,0,0,0);
					collided = true;
					System.out.println("Moving Right");
					chip.strafeR((byte) 255);
					Thread.sleep(1000);
					chip.flt();
					collided=false;
				} catch (InterruptedException ex) {
				}

			}

			if (right.isPressed()) {
				try {
					setSpeeds(0,0,0,0);
					collided = true;
					System.out.println("Moving Left");
					chip.strafeL((byte) 255);
					Thread.sleep(1000);
					chip.flt();
					collided=false;
				} catch (InterruptedException ex) {
				}
			}
			
			if (front.isPressed() && (Motor.A.isMoving() || Motor.B.isMoving())) {
				
				setSpeeds(0,0,0,0);
				collided = true;

				System.out.println("Reversing");

				Motor.A.setSpeed(700);
				Motor.B.setSpeed(700);
				
				Motor.A.forward();
				Motor.B.forward();

				Motor.A.rotate(360, true);
				Motor.B.rotate(360, false);

				collided=false;
				
			}

		}
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
	
	public boolean collided(){
		return collided;
	}
}
