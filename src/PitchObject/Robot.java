package PitchObject;


public class Robot extends PitchObject {
	// TODO: test to find good value for this
	public static final double DEFAULT_VELOCITY = 100.0;
	public static int MAX_SPEED = 255;
//	public final static int MAX_SPEED = 255;
	public final static int TURN_SPEED = 56;
	public final static int NEAR_RANGE = 30;
	public final double SHOOTING_RANGE = 100;
//    private boolean wantsToRotate;
	private int rotationSign = 0;
//    private boolean wantsToStop;
	

	public Robot() {
//		this.wantsToRotate = false;
//		this.wantsToStop = false;
	}

	public Robot (Position coors, float angle) {
		this.coors = coors;
		this.angle = angle;
	}

//	public void setWantsToRotate(boolean b) {
//		this.wantsToRotate = b;
//	}
//
//	public void setWantsToStop(boolean b) {
//		this.wantsToStop = b;
//	}

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
         return refitAngle(robAngle);
    }

    public double getAngleFromRobotToPoint(Position point) {
         double angleToPoint = this.getCoors().getAngleToPosition(point);
         return getRotationDifference(angleToPoint);
    }
    
    public double getRotationDifference(double angle) {
    	double result = angle - this.getRobotAngle();
    	return refitAngle(result);
    }

	public boolean isFacing(Position point) {
		double angle = this.getAngleFromRobotToPoint(point);
	    double value = this.getRotationValue(angle);    
	    return value == 0;
	}
	public boolean isFacing(double direction) {
		double angle = this.getRotationDifference(direction);
	    double value = this.getRotationValue(angle);    
	    return value == 0;
	}
	
	   /**
     * getRotationValue gets the rotation vector for holonomic movement.
     * value can be changed closer to 1/-1  to make the rotation faster,
     * or closer to 0 to make the rotation slower.
     * The value currently Math.PI/10 can also be changed.
     * closer to 0 for more precision, or increased for less turning. 
     * 
     * 
     * @param angle 	Should be fed from getAngleBetweenRobotAndPoint, the angle between the robot and the destination.
     *  
     * @return 	+/- double relating to rotation direction, or 0 if no rotation necessary. 	
     * 
     * @see getAngleBetweenRobotAndPoint
     * 
     * @author      Caithan Moore s1024940
     * @author      
     *
     */
    public double getRotationValue(double angle){
         double sign = 0;
         // normalize angle and set sign
         if (angle > Math.PI){
        	 sign = +1;
        	 angle = 2*Math.PI - angle;
         }
         else {
        	 sign = -1;
         }
         // get the value -- tested! don't try larger values
         // with TURN_SPEED = 56 and rotationSign up to 5
         if (angle > Math.PI/7) { return sign * 0.2; }
    	 else                   { return 0; }
    }
    
    public double refitAngle(double angle) {
    	return ((angle + (10*Math.PI)) % (2*Math.PI));
    }

    /**
     * getMotorValues applies holonomic algorithms to generate the correct 
     * motor values for the robot to move at an angle and rotate at the 
     * same time. 
     * The actual values are dependent on the direction of the wheels 
     * and the placement of the motors on the multiplexer.
     * As a note for future teams, 
     * a returned set of values of 
     * -255 0 0 255
     * should make the robot go forward, and
     * 0 -255 255 0 
     * should make the robot strafe right. 
     *   
     * 
     * @param rotationfactor 	Should be fed from getRotationValue, adds the rotation vector
     * @param angle 			The angle of movement relative to the direction of the robot.
     * 							A value of (PI) here would cause the robot to move directly backwards.
     * @param hardRotate		determines whether the robot cares about facing the destination (<code>true</code>),
     * 							or if simply moving to the point is okay (<code>false</code>)
     * 
     * 
     * @return 	an array of motor values, each indice relating to a motor. 
     * 
     * @see getRotationValue
     * 
     * @author      Caithan Moore - S1024940
     *
     */
//    public int[] getMotorValues(double rotationfactor, double angle, boolean hardRotate){
//         double[] motors = {0,0,0,0};
//         int[] returnvalues = {0,0,0,0};
//         double multfactor = 255;
//         double maxval = 0.0001;
//         if (wantsToRotate) {
//                 for (int i = 0; i<4;i++){
//                         motors[i] += rotationfactor;
//                 }
//         }
//         if  (!(wantsToRotate && hardRotate && wantsToStop))  {
//         //hardRotate tells us that we -need- to be facing the correct way.
//         //so, keep moving all the time if we don't really care about rotation.
//         //may need some testing here
//                 motors[0] -= (Math.cos(angle));
//                 motors[1] += (Math.sin(angle));
//                 motors[2] -= (Math.sin(angle));
//                 motors[3] += (Math.cos(angle));
//         }
//         if (wantsToStop && wantsToRotate && (hardRotate)){
//         //similar here, but reversed.
//         // if you want to stop and rotate, only slow down if you actually care about rotating.
//                 multfactor = (multfactor/Math.abs(rotationfactor))/4;
//         } else {
//                 for (int i = 0; i<4;i++){
//                         if (Math.abs(motors[i]) > maxval) maxval = Math.abs(motors[i]);
//                 }
//                 multfactor = (multfactor/maxval);
//                
//         }
//        
//         for (int i = 0;i<4;i++) {
//                 returnvalues[i] = (int) (motors[i]*multfactor);
//         }
//                        
//         return returnvalues;
//        
// 	}

    public static double[] getMovementRatio(double angle){
    	 double[] motors = {0,0,0,0};
    	 motors[0] -= (Math.cos(angle));
         motors[1] += (Math.sin(angle));
         motors[2] -= (Math.sin(angle));
         motors[3] += (Math.cos(angle));
    	 return motors;
    }

    public String moveStraight(Position destination) {
    	double angle = this.getAngleFromRobotToPoint(destination);
    	double[] motors = getMovementRatio(angle);
    	return normalisedSignal(motors, MAX_SPEED);
    }

    public static String normalisedSignal(double[] motors, int maxspeed){
    	double maxVal = -1;
    	for (int i = 0; i<4;i++){
             if (Math.abs(motors[i]) > maxVal) {
            	 maxVal = Math.abs(motors[i]);
             }
    	}
    	int[] returnvalues = new int[4];
    	if (maxVal != 0) {
	    	maxspeed = (int) (maxspeed/maxVal);
			for (int i = 0;i<4;i++) {
	    		 returnvalues[i] = (int) (motors[i]*maxspeed);
	    	}
    	}
    	return createSignal(returnvalues);
    }

    /**
     * This method checks if we are rotating the same way as 
     * we were when we last checked -- giving us time to recheck 
     * whether we should stop; for this it needs to update a 
     * history variable (rotationSign) 
     * @param value rotation value
     * @return value, possibly zeroed
     */
    private double processRotationValue(double value) {
    	// if rotating same way for too long, stop
		if ((value<0 && rotationSign<0) || (value>0 && rotationSign>0)) {
			if (Math.abs(rotationSign) > 5) { value = 0; }
		}
		// if sudden change in rotation, will stop earlier (slows oscillation)
		if ((value<0 && rotationSign>0) || (value>0 && rotationSign<0)) {
			rotationSign = (value<0) ? -2 : +2;
		}
			
		// update rotation sign
		if      (value < 0) { rotationSign -= 1; } 
		else if (value > 0) { rotationSign += 1; }
		else                { rotationSign  = 0; }
		
		return value;
	}
	public String rotate(double direction){
    	double angle = this.getRotationDifference(direction);
    	double value = getRotationValue(angle);
    	value = processRotationValue(value);
    	double[] motors = { value, value, value, value };
    	return normalisedSignal(motors, TURN_SPEED);    	
    }

    public String moveAndTurn(Position movePos, double direction){	
    	double moveAngle = this.getAngleFromRobotToPoint(movePos);
    	double[] motors = getMovementRatio(moveAngle);    	
    	double rotateAngle = this.getRotationDifference(direction);
    	double value = getRotationValue(refitAngle(rotateAngle));
    	value = processRotationValue(value);
    	for (int i = 0; i < 4; i++) {
   		 	motors[i] += value;
    	}
    	return normalisedSignal(motors, MAX_SPEED);	
    }

    
	public String moveToFace(Position movePos, double direction){
    	if ((this.getCoors().euclidDistTo(movePos) < NEAR_RANGE)
    		 && !this.isFacing(direction)) {
    		System.out.println("ROTATE");
    		return rotate(direction);
    	} else {
    		System.out.println(this.getCoors().euclidDistTo(movePos));
    		return moveAndTurn(movePos, direction);
    	}
    }

    /**
     * createSignal puts motor values into a string that can be used by the IPC
     * @param codes 			an array of motor values
     * @return 	a string of the correct format for IPC 
     * @see getMotorValues
     * @author      Caithan Moore - S1024940
     *
     */
 	public static String createSignal(int[] codes){
 			String sig = "1 "+codes[0]+" "+codes[1]+" "+codes[2]+" "+codes[3];
 			return sig;
 	}
 
 	/**
     * getSigToPoint generates a signal to send to IPC that will give the correct
     * motor values for movement in a given way. 
     * @param Robot			the robot you are moving
     * @param destination 	the point which you want to move to
     * @param rotation		the point which you want to rotate to
     * @param hardRotate	determines whether the robot cares about facing the
     *                      destination (<code>true</code>), or if simply moving 
     *                      to the point is okay (<code>false</code>)
     * @return a string of the correct format for IPC 
     * @see getMotorValues
     * @see createSignal
     * @author      Caithan Moore - S1024940
     */
//	 public String getSigToPoint(Position destination, Position rotation,
//			                     boolean hardRotate){
//         double movementangle = this.getAngleFromRobotToPoint(destination);
//         double rotationangle = this.getAngleFromRobotToPoint(rotation);
//         return createSignal(getMotorValues(getRotationValue(rotationangle),
//        		                            movementangle, hardRotate));
//	}

	/**
     * Gets the maximum angle the robot could make with the goal
     * @param robot			The position of the robot.
     * @param shootingRight <code>true</code> iff you are shooting right. 
     * @return the maximum angle that the robot could make with the goal (angle in diagram above)
     * @author Caithan Moore - s1024940
     * @author Clemens Wolff
     */
	public double getAngleToGoal(boolean shootingRight) {
		Goal goal = shootingRight ? Goal.goalR() : Goal.goalL();
		return this.getCoors().getAngleToGoal(goal);
	}

	/**
	 * Returns a number in [0..1] that is proportional to how likely
	 * the robot is to score from his current angle to the goal
     * @param robot			The position of the robot.
     * @param shootingRight <code>true</code> iff you are shooting right.
	 * @return The score of the current angle to the goal (1 = best, 0 = worst)
     * @author Caithan Moore - s1024940
     * @author Clemens Wolff
	 */
	public double getAngleScore(boolean shootingRight){
		Goal goal = shootingRight ? Goal.goalR() : Goal.goalL();
		double angle = this.getAngleToGoal(shootingRight);
		double minangle = goal.getOptimalPosition().getAngleToGoal(goal);
		double maxa = Math.max(minangle, angle);
		double mina = Math.min(minangle, angle);
		return mina / maxa;
	}

	/**
	 * Returns a number in [0..1] that is proportional to how likely
	 * the robot is to score from his current distance to the goal
	 * NB: the score is non-linear (follows cos(x))
	 * 
     * @param robot			The position of the robot.
     * @param shootingRight <code>true</code> iff you are shooting right.
     * 
	 * @return The score of the current distance to the goal (1 = best, 0 = worst)
	 * 
	 * @see RobotMath#getAngleScore
	 * @see RobotMath#getPositionScore
	 * 
	 * @author Caithan Moore - s1024940
     * @author Clemens Wolff
	 */
	public double getDistanceScore(boolean shootingRight) {
		int xR = Goal.goalR().getCoors().getX();
		int xL = Goal.goalL().getCoors().getX();
		int pitchLen = Math.abs(xR - xL);
		int goalX = shootingRight ? xR : xL;
		int dist = Math.abs(goalX - this.getCoors().getX());
		double distRatio = (double) dist / pitchLen;
		double score = Math.cos(distRatio * Math.PI / 2);
		return score;
	}

	public double getPositionScore(boolean shootingRight) {
		// TODO: find good default value
		return this.getPositionScore(shootingRight, 0.25);
	}

	/**
	 * Returns a number in [0..1] that is proportional to how likely
	 * the robot is to score from his current position
	 * This number is a linear combination of the likelihood to score from
	 * the current distance to the goal and the current angle to the goal
	 * The parameter |preferDistanceBy| gives more weight to the distance score
	 * 
     * @param robot			The position of the robot.
     * @param shootingRight <code>true</code> iff you are shooting right.
	 * @param preferDistanceBy Multiplier for distance score (in [0..1])
	 *                         - angle score will be multiplied by one minus this
	 * @return The score of the current position of the robot (1 = best, 0 = worst)
	 * 
	 * @see RobotMath#getAngleScore
	 * @see RobotMath#getPositionScore
	 * 
	 * @author Caithan Moore - s1024940
     * @author Clemens Wolff
	 */
	public double getPositionScore(boolean shootingRight, double preferDistanceBy) {
		double distanceScore = this.getDistanceScore(shootingRight);
		double angleScore = this.getAngleScore(shootingRight);
		double weightedScore = distanceScore * preferDistanceBy +
		                       angleScore * (1 - preferDistanceBy);
		return weightedScore;
	}

	public double getHitScore(boolean r) {
		Goal goal = r ? Goal.goalR() : Goal.goalL();
		int goalX = goal.getCoors().getX();
		int topY = goal.getTop().getY();
		int botY = goal.getBottom().getY();
		double tan = Math.tan(r ? this.getAngle() - goal.getAngle() :
								  goal.getAngle() - this.getAngle());
		int hitY = this.getCoors().getY() +
		           (int) (Math.abs(this.getCoors().getX() - goalX) * tan);
		int d1 = botY - hitY;
		int d2 = hitY - topY;
		return Math.min(1.0 * d1 / d2, 1.0 * d2 / d1);
	}
	
	public boolean closeToPoint(Position position){
		return this.getCoors().euclidDistTo(position) < NEAR_RANGE;
	}

	public boolean withinKickingRange(Position theirGoal) {
		Position us = this.getCoors();
		return us.euclidDistTo(theirGoal) < SHOOTING_RANGE;
	}
	
	public Position getTarget(Goal goal){
		int newY = 0;
		int newX = goal.getOptimalPosition().getX();
		int RobotY = this.getCoors().getY();
		int TopGoalY = goal.getTop().getY();
		int BottomGoalY = goal.getBottom().getY();
		
		int difference1 = TopGoalY - RobotY;
		int difference2 = RobotY - BottomGoalY;
		
		if (difference1 > difference2){ 
			newY = TopGoalY + difference1 - 20;
		}
		else{
			newY = BottomGoalY - difference2 + 20;
		}	
		
		return new Position(newX,newY);	
	}
	
}