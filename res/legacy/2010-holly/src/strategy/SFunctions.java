package strategy;

import java.awt.Point;
import java.util.ArrayList;

public class SFunctions {

        //Goal Left
        static int goalStartY = 100;
        static int goalEndY = 200;
        static int goalLeftX = 50;
        static int goalRightX = 700;
        static int interval = 20;

	//works with GridPoints
	public static boolean withinRadius(GridPoint pt1, GridPoint pt2, int radius) {
		//if ((pt2.y - pt1.y) + (pt2.x - pt1.x) < radius) {
		if (Math.abs(pt2.y - pt1.y) < radius || Math.abs(pt2.x - pt1.x) < radius)
			return true;
		else return false;
	}

    public static double getAngle(Point a, Point b) {
        return Math.toDegrees(Math.atan2((a.y - b.y), (b.x - a.x)));
    }

	public static boolean nearAngle(int currentAngle, int distanceAngle, int threshold) {
		if (Math.abs(distanceAngle - currentAngle) < threshold) {
			return true;
		}
		else return false;
	}
	
    public static int calculateAngle(Point a, Point b, int currentAngle) {

        int angleToTurn = (int) Math.toDegrees(Math.atan2((a.y - b.y), (b.x - a.x))) - currentAngle;

        if (angleToTurn < -180) {angleToTurn = 360 + angleToTurn;}
        if (angleToTurn > 180) {angleToTurn = angleToTurn - 360;}

        return angleToTurn;
    }

   
    
    public static Point intersection(
    		
			double x1,double y1,double x2,double y2,
			double x3, double y3, int robotAngle) {

			double x4 = x3 + 200;
			double y4 = y3 - (200 * Math.tan(Math.toRadians(robotAngle)));

			double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
			if (d == 0) return null;

			int xi = (int) (((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d);
			int yi = (int) (((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d);
			return new Point(xi,yi);
			
	}
    
    public static double calculateAnglePolar(Point r, Point b){
    	double radius = Math.sqrt((r.x-b.x)*(r.x-b.x) + (r.y-b.y)*(r.y-b.y));
        //double angleToTurn = ((Math.acos((b.x - r.x) / radius)));
        double angleToTurn = ((Math.asin((b.y - r.y) / radius)));
        //System.out.println(radius + " angle " + angleToTurn + " b-r/radius " +((b.x-r.x)/radius) );
        return angleToTurn;
    }
    
    public static boolean canStrafeLeft(int angle){
    	return angle < 105 && angle > 75;
    }

    public static boolean canStrafeRight(int angle){
    	return angle < 105 && angle > 75;
    }

    public static boolean withinStrafingDistance(Point ourPosition, Point ballPosition){
    	return false;
    }
    
    public static void main(String[] args){
    	//int[] moves = goalMove(new Point(100,120), 1,0);
    	//System.out.println("Possible move: (" + moves[0] + " , " + moves[1] + ")");

    }

}
