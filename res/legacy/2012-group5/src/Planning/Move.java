package Planning;
import JavaVision.*;

public class Move {

	private int POS;

	/** returns the distance between the robot and the ball **/
	public int getDist(Robot robot, Ball ball) {
		int robotX = robot.getCoors().getX();
		int robotY = robot.getCoors().getY();
		int ballX = ball.getCoors().getX();
		int ballY = ball.getCoors().getY();
		int x = (int) Math.abs(robotX - ballX);
		int y = (int) Math.abs(robotY - ballY);
		int dist = (int) Math.sqrt(x * x + y * y);
		return dist;
	}

	//rotate coordinate system to get the position of the ball with
	//respect to the robot
	public Ball modifyBallCoors(Robot robot, Ball ball) {

		Position p;
		p = translatePoint(robot, ball); // ball relative to robot

		//rotate counter-clockwise
		//coordinates of the ball with respect to the orientation of the robot
		int x = p.getX();
		int y = p.getY();
		float angle = robot.getAngle(); // in radians
		ball.setCoors(new Position( ((int) (Math.cos(angle)*x - Math.sin(angle)*y)), ((int) (Math.sin(angle)*x + Math.cos(angle)*y))));
		return ball;
	}

	public Position translatePoint(Robot robot, ObjectDetails o) {
		int x = o.getCoors().getX() - robot.getCoors().getX();
		int y = -(o.getCoors().getY()) - (-(robot.getCoors().getY())); // negative y to convert into normal cordinate system
		o.setCoors(new Position(x, y));
		return o.coors;
	}


    /** find the position of the ball with respect to the robot **/
	public int getBallPosition(Ball ball) { 		
        // ball is on the right side of the robot
		if(ball.getCoors().getX() > 0) {
            // ball is in top right square
			if(ball.getCoors().getY() > 0) {
				POS = 0;
			}
            // ball is in bottom right square
			else {
                POS = 1;
            }
		}
        // ball is on the left side of the robot
		else {
            // ball is in top left square
			if(ball.getCoors().getY() > 0) {
				POS = 3;
			}
            // ball is in botom left square
			else { 
                POS = 2;
            }
		}
		return POS;
	}

	// returns the angle from the robot to the ball
	public int getAngleToBall(Robot robot, Ball b){

		int dist = getDist(robot, b);
		Ball ball = new Ball();
		ball = modifyBallCoors(robot, b); // ball with the new coordinates
		System.out.println("Ball coors" + ball.getCoors().getX() + " " + ball.getCoors().getY());
		float angle = 0;
		int ballPos = getBallPosition(ball);
		System.out.println("POS " + ballPos);
		switch(ballPos) {
		case 0: {
			double a = ((double)ball.getCoors().getX())/((double)dist);	
			a = (a > 0) ? a : -a;		
			System.out.println("Bx : " + ball.getCoors().getX());
			System.out.println("Dist : " + dist);
			System.out.println("A is : " + a);
			angle = (float) -Math.asin(a);
			System.out.println("Aangle in radians : " + angle);
			System.out.println("Aangle in degrees : " + Math.toDegrees(angle));
			break;			// turn 'angle' radians right
		}
		case 1: {
			double a = ((double)ball.getCoors().getY())/((double)dist);	
			a = (a > 0) ? a : -a;
			System.out.println("Bx : " + ball.getCoors().getX());
			System.out.println("Dist : " + dist);
			System.out.println("A is : " + a);
			angle = (float) -(Math.asin(a) + Math.PI/2);
			System.out.println("Aangle in radians : " + angle);
			System.out.println("Aangle in degrees : " + Math.toDegrees(angle));
			break;
			// turn 'angle' radians right
		}
		case 2: {
			double a = ((double)ball.getCoors().getY())/((double)dist);
			a = (a > 0) ? a : -a;
			System.out.println("Bx : " + ball.getCoors().getX());
			System.out.println("Dist : " + dist);
			System.out.println("A is : " + a);
			angle = (float) (Math.asin(a) + Math.PI/2); 
			System.out.println("Aangle in radians : " + angle);
			System.out.println("Aangle in degrees : " + Math.toDegrees(angle));
			break;
			// turn 'angle' radians left
		}	
		case 3: {
			double a = ((double)ball.getCoors().getX())/((double)dist);
			a = (a > 0) ? a : -a;
			System.out.println("Bx : " + ball.getCoors().getX());
			System.out.println("Dist : " + dist);
			System.out.println("A is : " + a);
			angle = (float) Math.asin(a); 
			System.out.println("Aangle in radians : " + angle);
			System.out.println("Aangle in degrees : " + Math.toDegrees(angle));
			break;			// turn 'angle' radians left
		}
		}
		return (int) Math.toDegrees(angle);
	

	}

}
