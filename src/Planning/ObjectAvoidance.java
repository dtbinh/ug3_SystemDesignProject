
package Planning;

import JavaVision.Position;

public class ObjectAvoidance{
	/*
	 * Gets the line between us and the target (ball)
	 * calculates the distance between us and the obstacle/opposition
	 * if distance is less than 2*(max robot radius) then obstacle in path
	 * otherwise false
	 */
	public static boolean obstacleDetection(Position ourcoors, Position theirCoors, Position ball){
		return obstacleDetection(ourcoors, theirCoors, ball, 50);
	}
	public static boolean obstacleDetection(Position ourcoors, Position theirCoors, Position ball, int width){
		int ourX = ourcoors.getX();
		int ourY = ourcoors.getY();
		int obsX = theirCoors.getX();
		int obsY = theirCoors.getY();
		int destX = ball.getX();
		int destY = ball.getY();
		// http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
		
		int numerator = Math.abs(((destX-ourX)*(ourY-obsY)) - ((ourX-obsX)*(destY-ourY)));
		double denominator = Math.sqrt((Math.pow((double)(destX-ourX), 2)) + ((Math.pow((double)(destY-ourY), 2))));
		double dist = numerator/denominator;
		
		double mag1 = Math.sqrt(Math.pow(ourX-destX,2) + Math.pow(ourY-destY,2));
		double mag2 = Math.sqrt(Math.pow(obsX-destX,2) + Math.pow(obsY-destY,2));
		double mag3 = Math.sqrt(Math.pow(ourX-obsX,2) + Math.pow(ourY-obsY,2));
		
			
		//System.out.println("Us: " + ourX + " ," + ourY);
		//System.out.println("Them " + obsX + " , " + obsY);
		//System.out.println("Ball: " + destX + " , " + destY);
		
		if((dist > width)){
			return false;
		}else if((dist <width) && (mag2 > mag1)){
			return false;
		}else if((dist < width) && (mag3 > mag1)){
			return false;
		}else{
			return true;
		}	
	}
//Dont know why this takes an angle ask Caithan
//Caithan says for later implementations we might want a more refined circle around the obstacle. 
//but we can see what happens eh.
	public static CommandStack planAvoidance(Robot robot, Robot opposition, double angle, 
												boolean endBall, Robot goal, Ball ball,
												CommandStack plannedCommands) {
		RobotMath rmaths = new RobotMath();
		rmaths.init();
		int obsX = opposition.getCoors().getX();
		int obsY = opposition.getCoors().getY();
		int ballX = ball.getCoors().getX();
		int ballY = ball.getCoors().getY();
		Robot obsForAngle = new Robot();
		obsForAngle.setCoors(opposition.getCoors());
		obsForAngle.setAngle(0);

		int dist = 75; //Variable distance 
		Position ballPoint;

		Position endPoint; //Uses the point behind the ball method
		Position initial;
		
		if(getDist(robot.getCoors(),opposition.getCoors()) < 60){
			initial =  rmaths.pointBehindBall(goal.getCoors(), opposition.getCoors());
		}else{
			initial = null;
		}
		
		double alpha = RobotMath.getAngleFromRobotToPoint(obsForAngle,robot.getCoors());
		
		//Perpendicular angle to the point from them to us
		double usObsAngle = (Math.PI/2 + (alpha + angle)) % (2*Math.PI);
		
		//Gets reflective angle
		double usObsAngle2 = (((alpha + angle) - Math.PI/2) + RobotMath.TENPI) % (2*Math.PI);
		
		//If the end point is the ball set coordinates
		if(endBall){
			//Using the point behind the ball as the end point
			int endX = ballX;
			int endY = ballY;
			ballPoint = new Position(endX,endY);
			endPoint = rmaths.pointBehindBall(goal.getCoors(), ballPoint);
		}else{
			//not right
			int endX = (int) (2 * (dist*Math.sin(usObsAngle)));
			int endY = (int) (2 * (dist*Math.cos(usObsAngle)));
			endPoint = new Position(endX,endY);
		}
		
		//Calculates two points perpendicular to obstacle
		Position newPoint = RobotMath.projectPoint(opposition.getCoors(), usObsAngle, dist);

		Position newPoint2 = RobotMath.projectPoint(opposition.getCoors(), usObsAngle2, dist);
		System.out.println(RobotMath.projectPoint(opposition.getCoors(), 0, 100).getX());
		System.out.println(RobotMath.projectPoint(opposition.getCoors(), 0, 100).getY());
		

		
		//This alternative gets total distance the robot will travel 
		//to waypoint then from there to ball
		int totalPoint = getDist(endPoint, newPoint);
		int totalPoint2 = getDist(endPoint, newPoint2);
		
		//Takes shortest path to ball
		if(((totalPoint2 > totalPoint) || (totalPoint == totalPoint2))  && RobotMath.withinPitch(newPoint)){
			plannedCommands.pushMoveCommand(endPoint, endPoint, true);
			plannedCommands.pushMoveCommand(newPoint, endPoint, false);
			if (initial != null) plannedCommands.pushMoveCommand(initial, endPoint, false);;
			System.out.println("X : "+ newPoint.getX()+ " Y: " + newPoint.getY());
			return plannedCommands;
		}else if((totalPoint >= totalPoint2) && RobotMath.withinPitch(newPoint2)){
			plannedCommands.pushMoveCommand(endPoint, endPoint, true);
			plannedCommands.pushMoveCommand(newPoint2, endPoint, false);
			if (initial != null) plannedCommands.pushMoveCommand(initial, endPoint, false);;
			System.out.println("X : "+ newPoint2.getX()+ " Y: " + newPoint2.getY());
			return plannedCommands;
		}else{
			plannedCommands.pushMoveCommand(opposition.getCoors(), endPoint, false);
			plannedCommands.pushMoveCommand(endPoint, endPoint, true);
			return plannedCommands;
		}
	}

	public static int getDist(Position coors1, Position coors2){
		int coor1X = coors1.getX();
		int coor1Y = coors1.getY();
		int coor2X = coors2.getX();
		int coor2Y = coors2.getY();
		
		int x = (int) Math.abs(coor1X - coor2X);
		int y = (int) Math.abs(coor1Y - coor2Y);

		int dist = (int) Math.sqrt(x*x + y*y);
		return dist;
	}
}
