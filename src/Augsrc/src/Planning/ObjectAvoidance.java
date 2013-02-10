package Planning;

import JavaVision.Position;

public class ObjectAvoidance{

	public static Position planAvoidance(Robot robot, Position theirCoords, double angle) {
		int obsX = theirCoords.getX();
		int obsY = theirCoords.getY();
		
		double alpha = Runner.getAngleFromRobotToPoint(robot,theirCoords);
		
		//Perpendicular angle to the point from them to us
		double usObsAngle = Math.PI/2 + ((alpha + angle) % 2*Math.PI);
		
		//Variable distance 
		int dist = 50;
		
		int newX =  (int) (obsX + (dist*Math.sin(usObsAngle)));
		int newY =  (int) (obsY + (dist*Math.cos(usObsAngle)));
		
		Position newPoint = new Position(newX, newY);
		return newPoint;
		
	}

}
