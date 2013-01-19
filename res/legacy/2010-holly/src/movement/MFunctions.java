package movement;

import java.awt.Point;

/**
 * 
 * @author sdp6
 */

public class MFunctions {

	final static double wheelCircumference = 64;
	final static double pitchLengthCM = 244;
	final static double pitchLengthPixels = 712;
	final static double wheelSpan = 140;

	public static int angleToWheelRotations(int angle) {

		return (int) (angle * wheelSpan / wheelCircumference);
	}

	public static int pixelsToWheelRotations(double distance) {

		double MMToRotate = 10 * distance * (pitchLengthCM / pitchLengthPixels);
		int numDegrees = (int) (360 * MMToRotate / (wheelCircumference * Math.PI));

		return numDegrees;
	}

	public static int pixelsToMM(int distance){
		return (int)(10 * distance * (pitchLengthCM / pitchLengthPixels));
	}

	public static int calculateAngle(Point a, Point b, int currentAngle) {

		int angleToTurn = (int) Math.toDegrees(Math.atan2((a.y - b.y),
				(b.x - a.x)))
				- currentAngle;

		if (angleToTurn < -180) {
			angleToTurn = 360 + angleToTurn;
		}
		if (angleToTurn > 180) {
			angleToTurn = angleToTurn - 360;
		}

		return angleToTurn;
	}

	public static int calculateAngle(Point a, Point b) {

		int angleToTurn = (int) Math.toDegrees(Math.atan2((a.y - b.y),
				(b.x - a.x)));

		if (angleToTurn < -180) {
			angleToTurn = 360 + angleToTurn;
		}
		if (angleToTurn > 180) {
			angleToTurn = angleToTurn - 360;
		}

		return angleToTurn;
	}

	public static double turnRate(double radius) {
		int direction;
		double radiusToUse;
		if (radius < 0) {
			direction = -1;
			radiusToUse = -radius;
		} else {
			direction = 1;
			radiusToUse = radius;
		}

		double ratio = (2 * radiusToUse - wheelCircumference)
				/ (2 * radiusToUse + wheelCircumference);

		return (direction * (1 - ratio));
	}

        public static double correctionRatio(int distance, int angle) {
            int wheelSpanMM = pixelsToMM((int)wheelSpan);
            int distanceMM = pixelsToMM(distance);

            return (double)(distanceMM - wheelSpanMM)/(double)(distanceMM + wheelSpanMM)*(angle/5.0);

        }

	public static void main(String[] args) {
		System.out.println(1/turnRate(20));
	}

}
