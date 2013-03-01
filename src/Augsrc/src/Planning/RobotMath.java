package Planning;

import JavaVision.Position;
/**
 * RobotMath is a class that does all manner of calculations.
 * It has methods that abstract the task of movement given holonomics
 * and has various methods to calculate certain desirable points.
 * 
 * @author      Caithan Moore s1024940
 *
  */
public class RobotMath {
	Robot goalL = new Robot();
    Robot goalR = new Robot();
    Robot goalL_top = new Robot();
    Robot goalL_bottom = new Robot();
    Robot goalR_top = new Robot();
    Robot goalR_bottom = new Robot();
    static double TENPI = Math.PI*10;
    static double TWOPI = Math.PI*2;
    private boolean wantsToRotate;
    private boolean wantsToStop;
       
    //Set the values of the goals, init must be called just after we make a new instance
    public void init() {
    	goalR.setAngle((float) (3*Math.PI/2));
    	goalR.setCoors(new Position(603,240));
    	goalL.setAngle((float) Math.PI/2);
    	goalL.setCoors(new Position(35,240));
    	
    	goalR_top.setAngle( (float) (3*Math.PI/2));
    	goalL_top.setCoors(new Position(35,171));
    	goalR_bottom.setAngle((float) (3*Math.PI/2));
    	goalL_bottom.setCoors(new Position(35, 325));
    	
    	goalL_top.setAngle((float) Math.PI/2);
    	goalR_top.setCoors(new Position(603,166));
    	goalL_bottom.setAngle((float) Math.PI/2);
    	goalR_bottom.setCoors(new Position(603, 312));
     }
    
    public void initLoop() {
       	wantsToRotate = false;
        wantsToStop = false;
    }
    
    /**
     * getRobotAngle gives a normalised (between 0 and 2PI) angle 
     * of the robot to the lab doors.
     * 
     * @param robot 	The robot of which to get the angle.
     * 
     * @return 			The normalised angle. 	
     * 
     * @author      Caithan Moore s1024940
     * @author		Ozgur Osman
     * @author		Clemens Wolff
     * 
      */
	
    public static double getRobotAngle(Robot robot){
         // robot.getAngle() returns the angle between the robot and the left bottom
         // corner of the screen
         double robAngle = robot.getAngle();
         // we rotate robAngle by 90 degrees in order to have the angle between the robot and
         // the top left corner of the screen
         // remember: top left corner of screen = (0, 0)
         robAngle = robAngle - Math.PI/2;
         robAngle += TENPI;
         robAngle = robAngle % TWOPI;
        
        
         return robAngle;
        
    }

    /**
     * getAngleFromRobotToPoint gets the angle from a robot to a point.
     * 
     * 
     * @param robot 	The robot of which to get the angle from.
     * @param point 	The coordinates of the point to get the angle to.
     * 
     * @return 			The angle (in radians) from robot to point in the positive direction of the X axis. 	
     * 
     * @author      Caithan Moore s1024940
     * @author      Ozgur Osman
     * @author		Clemens Wolff
     */
    public static double getAngleFromRobotToPoint(Robot robot, Position point) {
         // angleToPoint is the angle between the top left corner of the pitch and the point
         // angleToRobot is the angle between the top left corner of the pitch and the robot
         // angleBetweenRobotAndPoint is the clockwise angle between the robot and the point
         double angleToPoint = Math.atan2( point.getY() - robot.getCoors().getY(), point.getX()-robot.getCoors().getX());
         double angleToRobot = getRobotAngle(robot);
         double angleBetweenRobotAndPoint = angleToPoint - angleToRobot;
         //it was giving a weird slightly negative number here in the robot north, ball in q2 case. Resolved.
         angleBetweenRobotAndPoint += TENPI;
         angleBetweenRobotAndPoint = angleBetweenRobotAndPoint % TWOPI;
         return angleBetweenRobotAndPoint;
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
         double value = 0;;
         if (angle > (Math.PI) ){
                 if (((Math.PI*2) - angle) > (Math.PI/5)) {
                         value = 0.3;
                 }
                
         } else if (angle > Math.PI/5) {
                 value = -0.3;
         }
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
                 multfactor = (multfactor/Math.abs(rotationfactor))/3;
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

 	public String createSignal(int[] codes){
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

	 public String getSigToPoint(Robot robot, Position destination, Position rotation, boolean hardRotate){
	         double movementangle = getAngleFromRobotToPoint(robot,destination);
	         double rotationangle = getAngleFromRobotToPoint(robot,rotation);
	         return createSignal(getMotorValues(getRotationValue(rotationangle),movementangle, hardRotate));
	        
	}
		/**
	     *Determines if the robot is facing a point.
	     *   
	     * 
	     * @param robot			the robot in question
	     * @param point	 		the point to check
	     * 
	     * @return true if angle between is less than +/- PI/10 (set in getRotationValues) 
	     * 
	     * @see getRotationValues
	     * 
	     * @author      Caithan Moore - S1024940
	     *
	     */
	 public boolean isFacing(Robot robot, Position point) {
	         double angle = getAngleFromRobotToPoint(robot,point);
	         double value = getRotationValue(angle);
	        
	         return (value == 0);
	}
	 
	 /**
	     *Projects a point behind the ball in the direction of the desired goal.
	     *   
	     * 
	     * @param goal			the goal in question set as a robot (opponent's goal) 
	     * @param ball	 		coordiantes of the ball.
	     * 
	     * @return projected point 100 pixels away. 
	     * 
	     * @see projectPoint
	     * 
	     * @author Caithan Moore - S1024940
	     * @author Jamie Ironside
	     * @author Sarah McGillion
	     *
	     */
	
	 public Position pointBehindBall(Robot goal, Position ball){
	 	    double rvrsBallToGoal = getAngleFromRobotToPoint(goal, ball)+Math.PI;
	         Position goPoint;
	        
	         goPoint = projectPoint(ball, rvrsBallToGoal, 100);
	        
	        
	         if (!withinPitch(goPoint)){
	                 goPoint.setX((ball.getX()));
	                 goPoint.setY((ball.getY()));
	         }
	         return goPoint;
	        
	}
	 
	 /**
	     *Projects a point behind a point at a given angle and distance
	     *   
	     * 
	     * @param pos			point you want to rotate from.
	     * @param ang	 		rotation (in radians) from the lab door
	     * @param dist 			how far you want to project the point.
	     * 
	     * @return projected point <b>dist</b> pixels away at angle of <b>ang</b>. 
	     * 
	     * 
	     * @author Caithan Moore - S1024940
	     * @author Jamie Ironside
	     * @author Sarah McGillion
	     *
	     */
	 public static Position projectPoint(Position pos, double ang, int dist){
	 	int newX = (int) (pos.getX() + (dist*Math.sin(ang)));
	     int newY = (int) (pos.getY() + (dist*Math.cos(ang)));
	     Position goPoint = new Position(newX,newY);
	     return goPoint;
	}
	 /**
	     *Checks if a point is in the pitch
	     *   
	     * 
	     * @param coors			point you want to check.
	     * 
	     * @return <code>true</code> if point is in the pitch, <code>false</code> otherwise.
	     * 
	     * 
	     * @author Jamie Ironside
	     * @author Sarah McGillion
	     *
	     */
	 public static boolean withinPitch(Position coors){
	         int coorX = coors.getX();
	         int coorY = coors.getY();
	        
	         if(coorX > 39 && coorX < 602 && coorY > 100 && coorY < 389) return true;
	         return false;
	}
	 
	public void toggleWantsToStop(){
		wantsToStop = true;
	}
	
	/**
     *Gets the euclidean distance of two positions
     *   
     * 
     * @param a, b			two positions.
     * 
     * @return the euclidean distance between the points.
     * 
     * 
     * @author Caithan Moore - s1024940
     * @author Clemens Wolff
     *
     */
	
	public static double euclidDist(Position a, Position b ){
		double ans = Math.sqrt(squared(a.getX()- b.getX())+ squared(a.getY() - b.getY()));
		return ans;
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
		Position topOfGoal;
		Position bottomOfGoal;
		
		if (shootingRight){
			topOfGoal = goalR_top.getCoors();
			bottomOfGoal = goalR_bottom.getCoors();
		} else {
			topOfGoal = goalL_top.getCoors();
			bottomOfGoal = goalL_bottom.getCoors();
		}
		double a = euclidDist(topOfGoal, robot);
		double c = euclidDist(bottomOfGoal, robot);
		double b = euclidDist(topOfGoal, bottomOfGoal);
		double angle = Math.acos((squared(a)+squared(c)-squared(b))/(2*a*c));
		return angle;
	}
	
	public static double squared(double x){
		return Math.pow(x, 2);
	}
	public static double squared(int x){
		return Math.pow(x, 2);
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
		
		double maxangle = getAngleToGoal(shootingRight ? goalR.getCoors() : goalL.getCoors(), shootingRight);
		double maxa = Math.max(maxangle, angle);
		double mina = Math.min(maxangle, angle);
		
		return 1 - (mina / maxa);
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
		int xR = goalR.getCoors().getX();
		int xL = goalL.getCoors().getX();
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
}