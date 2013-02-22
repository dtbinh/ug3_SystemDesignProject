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
		
		
		System.out.println("Distance from obs to line" + dist);
		
		System.out.println("Us: " + ourX + " ," + ourY);
		System.out.println("Them " + obsX + " , " + obsY);
		System.out.println("Ball: " + destX + " , " + destY);
		
		if((dist > 100)){
			return false;
		}else if((dist <100) && (mag2 > mag1)){
			return false;
		}else if((dist < 100) && (mag3 > mag1)){
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
		int obsX = opposition.getCoors().getX();
		int obsY = opposition.getCoors().getY();
		int ballX = ball.getCoors().getX();
		int ballY = ball.getCoors().getY();
		//ArrayList<Position> pointList = new ArrayList<Position>();

		int dist = 50; //Variable distance 
		Position ballPoint;

		Position endPoint; //Uses the point behind the ball method
		Position initial;
		
		if(getDist(robot.getCoors(),opposition.getCoors()) < 60){
			initial =  RobotMath.pointBehindBall(opposition, robot.getCoors());
		}else{
			initial = null;
		}
		
		double alpha = RobotMath.getAngleFromRobotToPoint(robot,opposition.getCoors());
		
		//Perpendicular angle to the point from them to us
		double usObsAngle = Math.PI/2 + ((alpha + angle) % 2*Math.PI);
		
		//Gets reflective angle
		double usObsAngle2 = 2*Math.PI -(Math.PI/2 + ((alpha + angle) % 2*Math.PI));
		
		//If the end point is the ball set coordinates
		if(endBall){
			//Using the point behind the ball as the end point
			int endX = ballX;
			int endY = ballY;
			ballPoint = new Position(endX,endY);
			endPoint = RobotMath.pointBehindBall(goal, ballPoint);
		}else{
			int endX = (int) (2 * (dist*Math.sin(usObsAngle)));
			int endY = (int) (2 * (dist*Math.cos(usObsAngle)));
			endPoint = new Position(endX,endY);
		}
		
		//Calculates two points perpendicular to obstacle
		int newX =  (int) (obsX + (dist*Math.sin(usObsAngle)));
		int newY =  (int) (obsY + (dist*Math.cos(usObsAngle)));
		
		int new2X =  (int) (obsX + (dist*Math.sin(usObsAngle2)));
		int new2Y =  (int) (obsY + (dist*Math.cos(usObsAngle2)));
	
		
		Position newPoint = new Position(newX, newY);
		Position newPoint2 = new Position(new2X, new2Y);
		
		//This alternative gets total distance the robot will travel 
		//to waypoint then from there to ball
		int totalPoint = getDist(robot.getCoors(),newPoint) + getDist(endPoint, newPoint);
		int totalPoint2 = getDist(robot.getCoors(),newPoint2) + getDist(endPoint, newPoint2);
		
		//Takes shortest path to ball
		if(((totalPoint2 > totalPoint) || (totalPoint == totalPoint2))  && RobotMath.withinPitch(newPoint)){
			plannedCommands.pushMoveCommand(endPoint, endPoint, true);
			plannedCommands.pushMoveCommand(newPoint, endPoint, false);
			if (initial != null) plannedCommands.pushMoveCommand(initial, endPoint, false);;
			return plannedCommands;
		}else if((totalPoint >= totalPoint2) && RobotMath.withinPitch(newPoint2)){
			plannedCommands.pushMoveCommand(endPoint, endPoint, true);
			plannedCommands.pushMoveCommand(newPoint2, endPoint, false);
			if (initial != null) plannedCommands.pushMoveCommand(initial, endPoint, false);;
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