package PitchObject;

/**
 * RobotMath is a class that does all manner of calculations.
 * It has methods that abstract the task of movement given holonomics
 * and has various methods to calculate certain desirable points.
 * 
 * @author      Caithan Moore s1024940
 *
  */
public class RobotMath {
    private static boolean wantsToRotate;
    private boolean wantsToStop;

    public void initLoop() {
       	wantsToRotate = false;
        wantsToStop = false;
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
    public static double getRotationValue(double angle){
         double value = 0;;
         if (angle > (Math.PI) ){
                 if (((Math.PI*2) - angle) > (Math.PI/7)) {
                         value = 0.3;
                 }
                
         } else if (angle > Math.PI/7) {
                 value = -0.3;
         }
         //TODO: Remove.
         wantsToRotate = (!(value == 0)) ;
         return value;
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
    public int[] getMotorValues(double rotationfactor, double angle, boolean hardRotate){
         double[] motors = {0,0,0,0};
         int[] returnvalues = {0,0,0,0};
         double multfactor = 255;
         double maxval = 0.0001;
         if (wantsToRotate) {
                 for (int i = 0; i<4;i++){
                         motors[i] += rotationfactor;
                 }
         }
         if  (!(wantsToRotate && hardRotate && wantsToStop))  {
         //hardRotate tells us that we -need- to be facing the correct way.
         //so, keep moving all the time if we don't really care about rotation.
         //may need some testing here
                 motors[0] -= (Math.cos(angle));
                 motors[1] += (Math.sin(angle));
                 motors[2] -= (Math.sin(angle));
                 motors[3] += (Math.cos(angle));
         }
         
        
         if (wantsToStop && wantsToRotate && (hardRotate)){
         //similar here, but reversed.
         // if you want to stop and rotate, only slow down if you actually care about rotating.
                 multfactor = (multfactor/Math.abs(rotationfactor))/4;
         } else {
                 for (int i = 0; i<4;i++){
                         if (Math.abs(motors[i]) > maxval) maxval = Math.abs(motors[i]);
                 }
                 multfactor = (multfactor/maxval);
                
         }
        
         for (int i = 0;i<4;i++) {
                 returnvalues[i] = (int) (motors[i]*multfactor);
         }
                        
         return returnvalues;
        
 	}
    
    public static double[] getMovementRatio(double angle){
    	 double[] motors = {0,0,0,0};
    	 motors[0] -= (Math.cos(angle));
         motors[1] += (Math.sin(angle));
         motors[2] -= (Math.sin(angle));
         motors[3] += (Math.cos(angle));
    	 return motors;
    }
    
    //TODO: Do constants ASAP
    public static String moveStraight(Position destination){
    	double angle = Constants.OUR_ROBOT.getAngleFromRobotToPoint(destination);
    	double[] motors = getMovementRatio(angle);
    	return (normalisedSignal(motors,Constants.MAX_SPEED));
    }
    
    public static String normalisedSignal(double[] motors,int maxspeed){
    	int maxVal = -1;
    	for (int i = 0; i<4;i++){
             if (Math.abs(motors[i]) > maxVal) {
            	 maxVal = (int) Math.abs(motors[i]);
             }
    	}
    	maxVal = (maxVal == 0) ? 1 : maxVal;
    	maxspeed = (maxspeed/maxVal);
    	int[] returnvalues = new int[4];
		for (int i = 0;i<4;i++) {
    		 returnvalues[i] = (int) (motors[i]*maxspeed);
    	}
            
    	 return createSignal(returnvalues);
    }
    
    public static String rotate(Position toFace){
    	double angle = Constants.OUR_ROBOT.getAngleFromRobotToPoint(toFace);
    	double direction = getRotationValue(angle);
    	double[] motors = {direction, direction, direction,direction};
    	
    	return(normalisedSignal(motors,Constants.TURN_SPEED));    	
    }
    
    public static String moveAndTurn(Position movePos, Position rotatePos){	
    	double moveAngle = Constants.OUR_ROBOT.getAngleFromRobotToPoint(movePos);	
    	double[] motors = getMovementRatio(moveAngle);    	
    	
    	double rotateAngle = Constants.OUR_ROBOT.getAngleFromRobotToPoint(rotatePos);
    	double directionOfRotation = getRotationValue(rotateAngle);
    	
    	for (int i = 0;i<4;i++) {
   		 	motors[i] += directionOfRotation;
    	}
    	return normalisedSignal(motors,Constants.MAX_SPEED);	
    }
    
    public static String moveToFace(Position movePos, Position rotatePos){
    	if ((Constants.OUR_ROBOT_COORS.euclidDistTo(movePos) < Constants.NEAR_RANGE)
    		  && !Constants.OUR_ROBOT.isFacing(rotatePos)) {
    		return rotate(rotatePos);
    	} else {
    		return moveAndTurn(movePos, rotatePos);
    	}
    }
  
    

    
    /**
     *createSignal puts motor values into a string that can be used by the IPC
     *   
     * 
     * @param codes 			an array of motor values
     * 
     * 
     * @return 	a string of the correct format for IPC 
     * 
     * @see getMotorValues
     * 
     * @author      Caithan Moore - S1024940
     *
     */
 	public static String createSignal(int[] codes){
 			String sig = "1 "+codes[0]+" "+codes[1]+" "+codes[2]+" "+codes[3];
 			return sig;
 	}
 	
 	/**
     *getSigToPoint generates a signal to send to IPC that will give the correct
     *motor values for movement in a given way. 
     *   
     * 
     * @param Robot			the robot you are moving
     * @param destination 	the point which you want to move to
     * @param rotation		the point which you want to rotate to
     * @param hardRotate	determines whether the robot cares about facing the destination (<code>true</code>),
     * 						or if simply moving to the point is okay (<code>false</code>)
     * 
     * @return a string of the correct format for IPC 
     * 
     * @see getMotorValues
     * @see createSignal
     * 
     * @author      Caithan Moore - S1024940
     *
     */
	 public String getSigToPoint(Robot robot, Position destination,
			 				     Position rotation, boolean hardRotate){
         double movementangle = robot.getAngleFromRobotToPoint(destination);
         double rotationangle = robot.getAngleFromRobotToPoint(rotation);
         return createSignal(getMotorValues(getRotationValue(rotationangle),
        		                            movementangle, hardRotate));
	}
	 
	public void toggleWantsToStop(){
		wantsToStop = true;
	}
	
	/**
     *Gets the maximum angle the robot could make with the goal
     * <pre>
     *   -------------------------------------
     *   |                                           |
     *   |                                           |
     *   +...........                              +
     *   |    angle ......robot              |
     *   |   ......                                  |
     *   +...                                      +
     *   |                                           |
     *   |                                           |
     *   -------------------------------------
     *   figure: return value when shootingRight is true
     *   // looks wonky in the actual javadoc string because eclipse uses non-monospace fonts when displaying the javadoc tooltip =(
     *   </pre>
     * @param robot			The position of the robot.
     * @param shootingRight <code>true</code> iff you are shooting right. 
     * 
     * @return the maximum angle that the robot could make with the goal (angle in diagram above)
     * 
     * 
     * @author Caithan Moore - s1024940
     * @author Clemens Wolff
     *
     */
	public double getAngleToGoal(Position robot, boolean shootingRight){
		Goal goal = shootingRight ? Goal.goalR() : Goal.goalL();
		double a = goal.getTop().euclidDistTo(robot);
		double c = goal.getBottom().euclidDistTo(robot);
		double b = goal.getTop().euclidDistTo(goal.getBottom());
		return Math.acos((a * a + c * c - b * b) / (2 * a * c));
	}
	
	/**
	 * Returns a number in [0..1] that is proportional to how likely
	 * the robot is to score from his current angle to the goal
	 * 
     * @param robot			The position of the robot.
     * @param shootingRight <code>true</code> iff you are shooting right.
     * 
	 * @return The score of the current angle to the goal (1 = best, 0 = worst)
	 *
	 * @see RobotMath#getAngleToGoal
	 * @see RobotMath#getDistanceScore
	 * @see RobotMath#getPositionScore
	 * 
     * @author Caithan Moore - s1024940
     * @author Clemens Wolff
	 */
	public double getAngleScore(Position robot, boolean shootingRight){
		double angle = getAngleToGoal(robot, shootingRight);
		double minangle = getAngleToGoal(shootingRight ?
											Goal.goalR().getCoors() :
											Goal.goalL().getCoors(),
										 shootingRight);
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
	public double getDistanceScore(Position robot, boolean shootingRight) {
		int xR = Goal.goalR().getCoors().getX();
		int xL = Goal.goalL().getCoors().getX();
		int pitchLen = Math.abs(xR - xL);
		int goalX = shootingRight ? xR : xL;
		int dist = Math.abs(goalX - robot.getX());
		double distRatio = (double) dist / pitchLen;
		double score = Math.cos(distRatio * Math.PI / 2);
		return score;
	}
	
	public double getPositionScore(Position robot, boolean shootingRight) {
		// TODO: find good default value
		return getPositionScore(robot, shootingRight, 0.25);
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
	 * @param preferDistanceBy Multiplier for distance score (in [0..1]) - angle score will be multiplied by one minus this
	 * 
	 * @return The score of the current position of the robot (1 = best, 0 = worst)
	 * 
	 * @see RobotMath#getAngleScore
	 * @see RobotMath#getPositionScore
	 * 
	 * @author Caithan Moore - s1024940
     * @author Clemens Wolff
	 */
	public double getPositionScore(Position robot, boolean shootingRight, double preferDistanceBy) {
		double distanceScore = getDistanceScore(robot, shootingRight);
		double angleScore = getAngleScore(robot, shootingRight);
		double weightedScore = distanceScore * preferDistanceBy + angleScore * (1 - preferDistanceBy);
		return weightedScore;
	}
	
	public double getHitScore(Robot robot, boolean r) {
		Goal goal = r ? Goal.goalR() : Goal.goalL();
		int goalX = goal.getCoors().getX();
		int topY = goal.getTop().getY();
		int botY = goal.getBottom().getY();
		double tan = Math.tan(r ? robot.getAngle() - goal.getAngle() :
								  goal.getAngle() - robot.getAngle());
		int hitY = robot.getCoors().getY() + (int) (Math.abs(robot.getCoors().getX() - goalX)* tan);
		int d1 = botY - hitY;
		int d2 = hitY - topY;
		return Math.min(1.0 * d1 / d2, 1.0 * d2 / d1);
	}
}