package movement;

import java.awt.Point;
import java.io.IOException;

public class Holonomic {

	//If we are moving at speed 700 the speed we must strafe at for a 45 degree angle
	final static int matchedStrafeSpeed = 175;

	//If we are strafing at speed 255 the speed the other motors go at for a 45 degree angle
	final static int matchedSpeedMainMotorsSL = 1094;
	//We seem to strafe faster in one direction
	final static int matchedSpeedMainMotorsSR = 1094;

	int sLeft, sRight, sFront, sBack;

	public static int[] angleToSpeeds(int angle, double correctionRatio, int angleToTurn){

		int[] speeds = new int[4];
		float ratio;

		//Get the ratio of y to x which is tan of the angle
		if(Math.abs(angle)==90){
			ratio = 0;
		}
		else{
			ratio = (float) Math.tan(Math.toRadians(angle));
		}

		float absRatio = Math.abs(ratio);

		System.out.println("Ratio " + ratio);

		//Special cases:

		//Forwards
		if(angle==90){
			speeds[0] = 600;
			speeds[1] = 600;
		}
		else if(angle==-90){
			//Backwards
			if(angle==90){
				speeds[0] = 600;
				speeds[1] = 600;
			}
		}

		//If we are strafing faster than we are moving forwards/backwards:
		else if(ratio<1 && ratio>-1){

			speeds[0] = (int)(700*absRatio);
			speeds[1] = (int)(700*absRatio);
                        if(angleToTurn > 0) {
			speeds[2] = matchedStrafeSpeed;
                            speeds[3] = (int)(matchedStrafeSpeed*correctionRatio);
                        }
                        else {
                            speeds[2] = (int)(matchedStrafeSpeed*correctionRatio);
			speeds[3] = matchedStrafeSpeed;
		}
		}
		//Otherwise forwards/backwards motors are faster
		else{

			speeds[0] = 700;
			speeds[1] = 700;
			if(angleToTurn > 0) {
				speeds[2] = (int)(matchedStrafeSpeed/absRatio);
				speeds[3] = (int)((matchedStrafeSpeed/absRatio)*correctionRatio);
			}
			else {
				speeds[2] = (int)((matchedStrafeSpeed/absRatio)*correctionRatio);
				speeds[3] = (int)(matchedStrafeSpeed/absRatio);
		}
		}

		//if the angle is negative then we're going backwards
		if(angle<0){
			speeds[0] = -speeds[0];
			speeds[1] = -speeds[1];
		}

		//Check if we're going left
		if(angle<-90 || angle>90){
			speeds[2] = -speeds[2];
			speeds[3] = -speeds[3];
		}

	return speeds;

	}



	public static void main(String[] args){
		int[] speeds = Holonomic.angleToSpeeds(135,200,0);

		for(int i=0; i<speeds.length; i++){
			System.out.println(speeds[i]);
		}
	}


}
