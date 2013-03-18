package PitchObject;

import JavaVision.*;
import Script.AbstractBaseScript;	

public abstract class PitchObject {
	protected Position coors;
	protected float angle;
	
	private static Position coors2 = null;
	private static Position coors3 = null;
	static long time1;
	static long time2;
	static long time3;
	public static int count = 0;
	public static int count2 = 0;
	static long difference;
	static double distance;
	static double speed1;
	static double speed2;
	private Vector lastVelocity = null;
	
	public Position getCoors() {
		return coors;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
	
    public void setCoors(Position current){
		lastVelocity = this.getVelocity();
		coors3 = coors2;
		coors2 = this.coors;
		this.coors = current;
		time3 = time2;
		time2 = time1;
		time1 = System.currentTimeMillis();
	}

	/**
	* @return	2-D vector representing velocity of ball.
	*/
	/**
	* Note from Sarah
	* Why don't we just get the initial velocity, between times 1 and 2,
	* then get the current velocity at times 3 and 4. So two types of get velocity methods.
	* Makes acceleration accurate?
	*/
	public Vector getVelocity() {
		if (this.coors == null || coors2 == null || coors3 == null) {
			return new Vector(0, 0);
		}
		double interval = (double) Math.abs(time1 - time2);
		double speedX = (coors2.getX() - this.coors.getX()) / interval;
		double speedY = (coors2.getY() - this.coors.getY()) / interval;
		double interval2 = (double) Math.abs(time2 - time3);
		speedX = 0.7 * speedX + (0.3 * (coors3.getX() - coors2.getX()) / interval2);
		speedY = 0.7 * speedY + (0.3 * (coors3.getY() - coors2.getY()) / interval2);
		return new Vector(speedX, speedY);
	}
	
	public boolean isMoving() {
		return this.getVelocity().getMagnitude() > 0.05;
	}

	/**
	* @return	2-D vector representing acceleration of ball. 
	* Acceleration = change in velocity over change in time
	*/
	public Vector getAcceleration() {
		if (lastVelocity == null) {
			lastVelocity = this.getVelocity();
			return new Vector(0, 0);
		} else {
			Vector curVelocity = this.getVelocity();
			double interval = (double) Math.abs(time1 - time2);
			double accelX = (curVelocity.getX() - lastVelocity.getX()) / interval;
			double accelY = (curVelocity.getY() - lastVelocity.getY()) / interval;
			return new Vector(accelX, accelY);
		}
	}
	
	public double getSpeed() {
		return this.getAcceleration().getMagnitude();
	}
	
	/**
	* Method, returns first reachable position of ball. 
	* 
	* Iterates with a 400ms step until ball is reachable or prediction timespan reaches 8s. 
	* 
	* @param robotCoors Position of robot.
	* @param robotSpeed Average speed of robot.
	* @return Position where ball is reachable.
	*/
	public Position getReachableCoors(Position robotCoors, double robotSpeed) {
		// get variables for predicting position
		Position c = this.getCoors();
		Vector v = this.getVelocity();
		Vector a = this.getAcceleration();
		// get variables for keeping position within boundaries
		int minX = AbstractBaseScript.getVision().getMinX(); 
		int maxX = AbstractBaseScript.getVision().getMaxX();
		int minY = AbstractBaseScript.getVision().getMinY(); 
		int maxY = AbstractBaseScript.getVision().getMaxY();
		// main loop that tries to find a reachable position
		for (int t = 400; t < 8001; t+=400) {
			Position ballCoors = getPredictedCoors(t, c, v, a, false); 
			ballCoors = reflectInside(ballCoors, minX, maxX, minY, maxY);
			int timeToBall = (int) (robotCoors.euclidDistTo(ballCoors) / robotSpeed);
			if (timeToBall <= t) return ballCoors;
		}
		return null;
	}
	
	/**
	* Method, uses info about speed and acceleration of ball to 
	* predict where it will be after the given time.
	* 
	* <p><b>*** Not sure whether useful (delete this comment if useful) ***</b>
	* 
	* @param timespan Time span to consider.
	* @return Predicted position of ball.
	*/
	public Position getPredictedCoors(int timespan) {
		return getPredictedCoors(timespan, this.getCoors(), 
		this.getVelocity(), this.getAcceleration(), true);
	}
	
	/**
	* Function, predicts where the object will be after the given time. 
	* @param timespan Timespan to consider.
	* @param objCoors Initial position of object.
	* @param speed Velocity of moving object.
	* @param accel Acceleration of moving object.
	* @return Predicted position of object.
	*/
	public static Position getPredictedCoors(int timespan, Position objCoors, 
			Vector velocity, Vector acceler, boolean reflect) {
		// if object velocity reaches 0 within timespan
		if (Math.abs(velocity.getX()) < Math.abs(acceler.getX() * timespan)) {
			// reduce time span to "when the object stops"
			timespan = (int) (velocity.getX() / acceler.getX()); 
		}
		int x = objCoors.getX() + getDisplacement(timespan, velocity.getX(), acceler.getX());
		int y = objCoors.getY() + getDisplacement(timespan, velocity.getY(), acceler.getY());
		
		Position predictedCoors = new Position(x, y);
		if (reflect) { 
			predictedCoors = reflectInside(predictedCoors, AbstractBaseScript.getVision()); 
		}
		return predictedCoors;
	}

	/**
	* Function, applies formula for covered distance.
	* <p> Premises: <ul>
	* <li> we only care about <b>covered</b> distance (initial position is 0) </li>
	* <li> movement is <b>linear</b> for simplicity (only one direction) </li> </ul>
	* <p> Formula is well known in equations of motion with constant linear acceleration. 
	* <p> r = r_0 + v_0 * t + a * t^2 / 2
	* @param speed Velocity (can be negative)
	* @param accel Acceleration (probably against speed, i.e. deceleration) 
	* @param timespan Time span of motion (should be positive)
	* @return Covered distance.
	*/
	public static int getDisplacement(int timespan, double speed, double accel) {
		return (int) (speed * timespan + accel * timespan * timespan / 2);
	}

	/**
	* Method to reflect positions inside the pitch as if walls were mirrors.
	* @param coors Coordinates to reflect.
	* @param vision VisionReader that can get boundaries.
	* @return Coordinates of inside "image" of position.
	*/
	public static Position reflectInside(Position coors, VisionReader vision) {
		return reflectInside(coors, vision.getMinX(), vision.getMaxX(), 
		vision.getMinY(), vision.getMaxY());
	}

	/**
	* Method to reflect positions inside the pitch as if walls were mirrors.
	* @param coors	Coordinates to reflect.
	* @param minX Boundary (left).
	* @param maxX Boundary (right).
	* @param minY Boundary (top).
	* @param maxY Boundary (bottom).
	* @return Coordinates of inside "image" of position.
	*/
	public static Position reflectInside(Position coors, int minX, int maxX, int minY, int maxY) {
		Position rCoors = new Position(coors.getX(), coors.getY());
		while (rCoors.getX() < minX || rCoors.getX() > maxX) {
			if (rCoors.getX() < minX) {
				rCoors.setX(2*minX - rCoors.getX());
			} else {
				rCoors.setX(2*maxX - rCoors.getX());
			}
		}
		while (rCoors.getY() < minY || rCoors.getY() > maxY) {
			if (rCoors.getY() < minY) {
				rCoors.setY(2*minY - rCoors.getY());
			} else {
				rCoors.setY(2*maxY - rCoors.getY());
			}
		}
		return rCoors;
	}
}
