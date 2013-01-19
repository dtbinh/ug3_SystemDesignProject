import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Motor;


public class TachoCount extends Thread {
	public boolean tachoCollided = false;
	private DataOutputStream output;
	private int threshold = 10;
	private int aDifference = 0;
	private int bDifference = 0;
	public void run() {
		aDifference = Motor.A.getSpeed() - Motor.A.getRotationSpeed();
		bDifference = Motor.B.getSpeed() - Motor.B.getRotationSpeed();
		
		if ((aDifference > threshold) && (bDifference > threshold)){
			if((Motor.A.getMode() == 1) || (Motor.B.getMode() == 1)){
				tachoCollided = true;
				Motor.A.setSpeed(900);
				Motor.B.setSpeed(900);
				Motor.A.rotateTo(100,true);
				Motor.B.rotateTo(100,true);
				try {
					output.writeInt(2);
					output.flush();
				} catch (IOException ex) {
				
				}

			} else if ((Motor.A.getMode() == 2) || (Motor.B.getMode() == 2)){
				tachoCollided = true;
				Motor.A.setSpeed(900);
				Motor.B.setSpeed(900);
				Motor.A.rotateTo(-100,true);
				Motor.B.rotateTo(-100, true);
				try {
					output.writeInt(3);
					output.flush();
				} catch (IOException ex) {
	
				}
				
			}
		}
	}
	public TachoCount(DataOutputStream dos){
		output = dos;
	}

}
