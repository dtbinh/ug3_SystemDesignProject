package Planning;

import JavaVision.*;

import java.awt.geom.QuadCurve2D;
public class Vectors {

	public static void planAvoidance(Position ourCoords, Position theirCoords, Position endPoint ) {
		int ourX = ourCoords.getX();
		int ourY = ourCoords.getY();
		int endX = endPoint.getX();
		int endY = endPoint.getY();
		int obsX = theirCoords.getX();
		int obsY = theirCoords.getY();
		
		//vector B
		int starttoendX = endX - ourX;
		int starttoendY = endY - ourY;
		
		//vector A
		int starttoobsX = obsX - ourX;
		int starttoobsY = obsY - ourY;
		
		//magnitudes
		double magA = Math.sqrt((starttoobsX^2)+(starttoobsY^2));
		double magB = Math.sqrt((starttoendX^2)+(starttoendY^2));
		
		//dot product
		int aDotB = starttoendX*starttoobsX + starttoendY*starttoobsY;
		int bDotB = starttoendX*starttoendX + starttoendY*starttoendY;
		
		//angle between B and A
		double thetaOfA = Math.cos(aDotB/(magA*magB));
		double bHatX = starttoendX/magB;
		double bHatY = starttoendY/magB;
		
		double newrejX = 2* (starttoobsX -  ( (magA*Math.cos(thetaOfA) * bHatX)));
		double newrejY = 2* (starttoobsY - ( (magA*Math.cos(thetaOfA) * bHatY)));
		
		double scalar = aDotB/bDotB;
		double scalarbX = scalar * starttoendX;
		double scalarbY = scalar * starttoendY;
		int rejectionX = (int)  (starttoobsX - scalarbX);
		int rejectionY = (int)  (starttoobsY - scalarbY);
		System.out.println("newX: "+ newrejX + " newY: " + newrejY);
		//return (new Position (rejectionX, rejectionY));
		
		
	}
	
	
	public static void main(String[] args) {
		planAvoidance(new Position (168, 269), new Position(327, 266), new Position(464, 262));

	}

}
