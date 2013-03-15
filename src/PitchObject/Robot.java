package PitchObject;


public class Robot extends PitchObject {
	// TODO: test to find good value for this
	public static final double DEFAULT_VELOCITY = 100.0;

	public Robot() {
	}

	public Robot (Position coors, float angle) {
		this.setCoors(coors);
		this.setAngle(angle);
	}

	public Position getReachableCoors() {
		return super.getReachableCoors(this.coors, this.getSpeed());
	}

	@Override public Position getReachableCoors(Position coors, double speed) {
		return this.getReachableCoors();
	}
 
	@Override public double getSpeed() {
		if (this.isMoving()) {
			return super.getSpeed();
		}
		return DEFAULT_VELOCITY;
	}

	/**
     * getRobotAngle gives a normalised (between 0 and 2PI) angle 
     * of the robot to the lab doors.
     * 
     * @return 			The normalised angle. 	
     */
    public double getRobotAngle(){
         /*
          * robot.getAngle() returns the angle between the robot and the left bottom
          * corner of the screen
          */
         double robAngle = this.getAngle();
         /*
          * we rotate robAngle by 90 degrees in order to have the angle between the robot and
          * the top left corner of the screen
          * remember: top left corner of screen = (0, 0)
          */
         robAngle = robAngle - Math.PI / 2;
         robAngle += (Math.PI * 10);
         robAngle = robAngle % (Math.PI * 2);
         return robAngle;
    }

    public double getAngleFromRobotToPoint(Position point) {
         /*
          * angleToPoint is the angle between the top left corner of the pitch and the point
          * angleToRobot is the angle between the top left corner of the pitch and the robot
          * angleBetweenRobotAndPoint is the clockwise angle between the robot and the point
          */
         double angleToPoint = Math.atan2(point.getY() - this.getCoors().getY(),
        		                          point.getX() - this.getCoors().getX());
         double angleToRobot = this.getRobotAngle();
         double angleBetweenRobotAndPoint = angleToPoint - angleToRobot;
         angleBetweenRobotAndPoint += (Math.PI * 10);
         angleBetweenRobotAndPoint = angleBetweenRobotAndPoint % (Math.PI * 2);
         return angleBetweenRobotAndPoint;
    }

    public boolean isTargeting(Position point) {
        double angle = this.getAngleFromRobotToPoint(point);
        double value = 0;
        if (angle > Math.PI) {
	        if (((Math.PI * 2) - angle) > (Math.PI / 6)) {
	        	value = 0.3;
	        }   
	    } else if (angle > Math.PI / 6) {
	            value = -0.3;
	    }
        return (value == 0);
	}

	public boolean isFacing(Position point) {
		double angle = this.getAngleFromRobotToPoint(point);
	    double value = RobotMath.getRotationValue(angle);    
	    return (value == 0);
	}
}