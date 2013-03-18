package PitchObject;

import java.awt.Point;
import java.util.ArrayList;

public class Position {
	private int x;
	private int y;

	@Override public String toString() {
		return new String("(" + this.getX() + ", " + this.getY() + ")");
	}

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Position(Point point) {
		this.x = point.x;
		this.y = point.y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Compares the current x and y co-ordinates to another set
	 * of co-ordinates (usually the previous co-ordinates for the
	 * position), fixing the current co-ordinates based on the
	 * previous ones.
	 * 
	 * @param oldX		The old x-coordinate.
	 * @param oldY		The old y-coordinate.
	 */
	public void fixValues(int oldX, int oldY) {
		
    	/* Use old values if nothing found */
		if (this.getX() == 0) {
			this.setX(oldX);
		}
		if (this.getY() == 0) {
			this.setY(oldY);
		}
    
		
    	/* Use old values if not changed much */
    	if (sqrdEuclidDist(this.getX(), this.getY(), oldX, oldY) < 9) {
    		this.setX(oldX);
    		this.setY(oldY);
    	}
	}
	
	/**
	 * Updates the centre point of the object, given a list of new points
	 * to compare it to. Any points too far away from the current centre are
	 * removed, then a new mean point is calculated and set as the centre
	 * point.
	 * 
	 * @param xs		The new set of x points.
	 * @param ys		The new set of y points.
	 */
    public void filterPoints(ArrayList<Integer> xs, ArrayList<Integer> ys) {
    	
    	if (xs.size() > 0) {
    		
	    	int stdev = 0;
	    	
	    	/* Standard deviation */
	    	for (int i = 0; i < xs.size(); i++) {
	    		int x = xs.get(i);
	    		int y = ys.get(i);
	    		
	    		stdev += Math.pow(Math.sqrt(sqrdEuclidDist(x, y, this.getX(), this.getY())), 2);
	    	}
	    	stdev  = (int) Math.sqrt(stdev / xs.size());
	    	
	    	int count = 0;
	    	int newX = 0;
	    	int newY = 0;
	    	
	    	/* Remove points further than standard deviation */
	    	for (int i = 0; i < xs.size(); i++) {
	    		int x = xs.get(i);
	    		int y = ys.get(i);
	    		if (Math.abs(x - this.getX()) < stdev && Math.abs(y - this.getY()) < stdev) {
	    			newX += x;
	    			newY += y;
	    			count++;
	    		}
	    	}
	    	
	    	int oldX = this.getX();
	    	int oldY = this.getY();
	    	
	    	if (count > 0) {
	    		this.setX(newX / count);
	    		this.setY(newY / count);
	    	}
	    	
	    	this.fixValues(oldX, oldY);
    	}
    }

    public static ArrayList<Point> removeOutliers(ArrayList<Integer> xs,
    		                                      ArrayList<Integer> ys, Point centroid){
    	ArrayList<Point> goodPoints = new ArrayList<Point>();
    	if (xs.size() > 0) {
	    	int stdev = 0;
	    	/* Standard deviation */
	    	for (int i = 0; i < xs.size(); i++) {
	    		int x = xs.get(i);
	    		int y = ys.get(i);
	    		
	    		stdev += Math.pow(Math.sqrt(sqrdEuclidDist(x, y, (int) centroid.getX(), (int)centroid.getY())), 2);
	    	}
	    	stdev  = (int) Math.sqrt(stdev / xs.size());	
	    	/* Remove points further than standard deviation */
	    	for (int i = 0; i < xs.size(); i++) {
	    		int x = xs.get(i);
	    		int y = ys.get(i);
	    		if (Math.abs(x - centroid.getX()) < stdev*1.17 && Math.abs(y - centroid.getY()) < stdev*1.17) {
	    			Point p = new Point(x, y);
	    			goodPoints.add(p);
	    		}
	    	}	
    	}
		return goodPoints;
    }

	public static float sqrdEuclidDist(int x1, int y1, int x2, int y2) {
		int dx = x1 - x2;
		int dy = y1 - y2;
		return (float) (dx * dx + dy * dy);
	}

    public double euclidDistTo(Position other) {
    	int dx = this.getX() - other.getX();
    	int dy = this.getY() - other.getY();
    	return Math.sqrt(dx * dx + dy * dy);
    }

	public Position projectPoint(double ang, int dist){
	 	int newX = (int) (this.getX() + (dist * Math.sin(ang)));
	    int newY = (int) (this.getY() - (dist * Math.cos(ang)));
	    Position goPoint = new Position(newX, newY);
	    return goPoint;
	}

	public boolean withinPitch() {
		int coorX = this.getX();
		int coorY = this.getY();
		if (coorX > 39 && coorX < 602 && coorY > 100 && coorY < 389) {
			return true;
		}
		return false;
	}
	
	public double getAngleToGoal(Goal goal) {
		double a = goal.getTop().euclidDistTo(this);
		double c = goal.getBottom().euclidDistTo(this);
		double b = goal.getTop().euclidDistTo(goal.getBottom());
		return Math.acos((a * a + c * c - b * b) / (2 * a * c));
	}
	
	public double getAngleToPosition(Position position) {
		return Math.atan2(position.getY() - this.getY(),
						  position.getX() - this.getX());
	}
}