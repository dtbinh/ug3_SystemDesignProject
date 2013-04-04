import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import java.io.DataOutputStream;

public class TouchSensors2 extends Thread {

	DataOutputStream output;
	Motormux m;
	public boolean touchCollided = false;
	private double ratio;
	private int count;
	private double previousRatio = 0;
	public TouchSensors2(DataOutputStream o, Motormux motormux) {
		output = o;
		m = motormux;
	}

	public void run() {
		TouchSensor touch1 = new TouchSensor(SensorPort.S1);
		TouchSensor touch2 = new TouchSensor(SensorPort.S2);
		boolean[] touchStatus = new boolean[4];
		while (true){

			if ((Motor.A.getMode() <3) && (Motor.B.getMode() <3)){
				if ((Motor.A.getMode() == 2) && (Motor.B.getMode() == 2)){
					ratio = ((-Motor.A.getRotationSpeed())/(double)(Motor.A.getSpeed()))*100;
				} else if ((Motor.A.getMode() == 1) && (Motor.B.getMode() == 1)){
					ratio = ((Motor.A.getRotationSpeed())/(double)(Motor.A.getSpeed()))*100;
				}
				
				
				if (ratio < 75){
					count++;
				}
				if (count > 7){
					if ((Motor.A.getMode() == 1) && (Motor.B.getMode() == 1)){
						System.out.println("Backward Collision");
						Motor.A.setSpeed(700);
						Motor.B.setSpeed(700);
						Motor.A.rotate(-400,true);
						Motor.B.rotate(-400,false);
					} else if ((Motor.A.getMode() == 2) && (Motor.B.getMode() == 2)){
						System.out.println("Forward Collision");
						Motor.A.setSpeed(700);
						Motor.B.setSpeed(700);
						Motor.A.rotate(400,true);
						Motor.B.rotate(400,false);
					}


				}
				previousRatio = ratio;
			} else {
				if (count != 0){
					System.out.print("Not Moving - ");
					System.out.print(count);
					System.out.print(" # ");
					System.out.print(previousRatio);
					System.out.println();
				}
				count = 0;
				previousRatio = 0;
				
			}
			
			touchStatus[0] = touch1.isPressed();
			touchStatus[1] = touch2.isPressed();

			if(touchStatus[0]){
				m.strafeR((byte) 255);
				System.out.println("Left Collided");

				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					m.flt();
				}
				m.flt();
			}

			if(touchStatus[1]){
				m.strafeL((byte) 255);
				System.out.println("Right Collided");

				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					m.flt();
				}
				m.flt();
			}
			
			try{
			Thread.sleep(50);
			} catch (InterruptedException ex){
				
			}
		}
	}
}
