package strategy;

import baseSystem.Singleton;
import java.awt.Point;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import movement.Movement;
import simulation.Simulation;
import vision.Vision;

/**
 *
 * @author Matt
 * @author Ben Ledbury
 * @author Joe Tam
 */
public class StrategyIntercept extends Thread {

	//variables associated with Simulator
	Simulation simulation;
	boolean simulated;

	//game play variables
	private long refreshInterval = 110;		//5 seconds
	private boolean finished = false;
	private int ourColor;
	private int oppColor;
	private int ourSide;
	private int oppSide;
	//game play constants
	private final static int BLUE = 0;
	private final static int YELLOW = 1;
	private final static int LEFT = 0;
	private final static int RIGHT = 1;
	public static int pitchWidth;
	public static int pitchHeight;
	public static int pitchMidLine;
	//objects coordinates and angles
	private int ballX;
	private int ballY;
	private int blueX;
	private int blueY;
	private int blueAngle;
	private int yellowX;
	private int yellowY;
	private int yellowAngle;
	private int ourX;
	//system integration
	private int debug;
	private Singleton singleton;
	private ArrayList<int[]> commands = null;
	Movement m;
	//variables related to strategy
	private boolean hasBall;
	private boolean intercept = true;
	private Point ballPreviousPosition = null;
	private ArrayList ballPositions;

	//constants related to strategy
	private int HAS_BALL_DISTANCE = 95;
	private boolean interceptFinished = false;

	public StrategyIntercept(boolean simulated, int robotColor, int side, int debug) {

		//m = new Movement(1,true);
		//m.connect();
		pitchWidth = Vision.PITCH_END_X - Vision.PITCH_START_X;
		pitchHeight = Vision.PITCH_END_Y - Vision.PITCH_START_Y;
		pitchMidLine = pitchWidth / 2;

		commands = new ArrayList();

		this.debug = debug;
		if (side == LEFT) {
			ourSide = LEFT;
			oppSide = RIGHT;
		} else {
			ourSide = RIGHT;
			oppSide = LEFT;
		}
		if (robotColor == BLUE) {
			ourColor = BLUE;
			oppColor = YELLOW;
		} else {
			ourColor = YELLOW;
			oppColor = BLUE;
		}
		ballPositions = new ArrayList<Point>();

		this.simulated = simulated;
		if (simulated == false) {
			singleton = Singleton.getSingleton();
		}
		else {
			simulation = Simulation.getSimulation();
		}
	}

	@Override
	public void run() {

		while (!finished) {
			try {
				update();
				if (debug == 1) {
					System.out.println("STRATEGY: loop starts");
					System.out.println("STRATEGY: Yellow: (" + yellowX + "," + yellowY + "), ball: (" + ballX + "," + ballY + "), blue: (" + blueX + "," + blueY + ")");
				}
				System.out.println("STRATEGY: refresh");
				if (interceptFinished == false) {
					computeStrategy();
				}
				Thread.sleep(refreshInterval);
			} catch (InterruptedException ex) {
				Logger.getLogger(StrategyIntercept.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	//for testing without vision
	private void testCase1() {
		//test case: opponent robot is not in the way
		ourColor = YELLOW;
		ballX = 500;
		ballY = 200;
		yellowX = 100;
		yellowY = 200;
		yellowAngle = 0;
		blueX = 700;
		blueY = 200;
	}

	public void computeStrategy() {
		//testCase1();
		//offset is changed dependent on the robot's direction to the opponent's goal
		int ballOffSet_x = 0;
		int ballOffSet_y = 0;

		/**
		 **********************************************************************************
		 ************************ START OF INTERCEPTION TASK ******************************
		 **********************************************************************************
		 */
		if (intercept) {

			//use two frames in vision system to get angle
			//might need tweaking if testing with hand rolling the ball because it messes up vision
//			if (ballX < 200) {
//				//discard it altogether for testing, so we can roll the ball from the left side of the pitch
//			}
//			else {
				System.out.println("INTERCEPT: DETECTED BALL");

				if (ballPreviousPosition == null) {
					ballPreviousPosition = new Point(ballX,ballY);
				}
				Point currentPosition = new Point(ballX,ballY);
				//no change in the ball's position
				if (currentPosition.distance(ballPreviousPosition) > 5) {

					double ballAngle = Angles.getAngle(ballPreviousPosition, currentPosition);
					System.out.println("INTERCEPT: DETECTED ANGLE " + ballAngle);

					interceptFinished = true;

					Point intersection1 = intersection(ballPreviousPosition.x, ballPreviousPosition.y, currentPosition.x, currentPosition.y,
									getOurPosition().x, getOurPosition().y, getOurAngle());

					Point intersection2 = intersection(ballPreviousPosition.x, ballPreviousPosition.y, currentPosition.x, currentPosition.y,
						getOurPosition().x, getOurPosition().y, getOurAngle() + 90);


//					Point intersection_top = intersection(ballPreviousPosition.x, ballPreviousPosition.y, currentPosition.x, currentPosition.y,
//							0, 0, 0);
					Point intersection_top = getIntersection(new Point(ballPreviousPosition.x, ballPreviousPosition.y),(int) ballAngle,getOurPosition(),getOurAngle());

					System.out.println("Top intersection: " + intersection_top);

					Point intersection_bottom = intersection(ballPreviousPosition.x, ballPreviousPosition.y, currentPosition.x, currentPosition.y,
							0, pitchHeight, 0);

//					int intersectionTop_deltaX = intersection_top.x - ballPreviousPosition.x;

//					Point intersection3 = intersection(intersection_top.x + intersectionTop_deltaX, ballPreviousPosition.y, intersection_top.x, intersection_top.y,
//							getOurPosition().x, getOurPosition().y, getOurAngle());
//
//					Point intersection3 = getIntersection(new Point(ballPreviousPosition.x, ballPreviousPosition.y),(int) -ballAngle,getOurPosition(),getOurAngle());
//
//					Point intersection4 = intersection(intersection_top.x + intersectionTop_deltaX, ballPreviousPosition.y, intersection_top.x, intersection_top.y,
//							getOurPosition().x, getOurPosition().y, getOurAngle() + 90);
//
////					int intersectionBottom_deltaX = intersection_bottom.x - ballPreviousPosition.x;
//
//					Point intersection5 = intersection(intersection_bottom.x + intersectionBottom_deltaX, ballPreviousPosition.y, intersection_bottom.x, intersection_bottom.y,
//							getOurPosition().x, getOurPosition().y, getOurAngle());
//
//					Point intersection6 = intersection(intersection_bottom.x + intersectionBottom_deltaX, ballPreviousPosition.y, intersection_bottom.x, intersection_bottom.y,
//							getOurPosition().x, getOurPosition().y, getOurAngle() + 90);

//					Point[] intersections = {intersection1,intersection2,intersection3,intersection4,intersection5,intersection6};
					Point[] intersections = {intersection1,intersection2};

					System.out.println("Intersection 1:" + intersections[0]);
					System.out.println("Intersection 2:" + intersections[1]);
//					System.out.println("Intersection 3:" + intersections[2]);
//					System.out.println("Intersection 4:" + intersections[3]);
//					System.out.println("Intersection 5:" + intersections[4]);
//					System.out.println("Intersection 6:" + intersections[5]);

					Point intersection;
					int intersection_index = -1;

					intersection = ballPreviousPosition;
					for (int i = 0; i < intersections.length; i++) {
						if (intersections[i].x < 0 || intersections[i].x > pitchWidth || intersections[i].y < 0 || intersections[i].y > pitchHeight) {
							intersections[i] = null;
						} else {
							if (intersections[i].x > intersection.x) {
								intersection = intersections[i];
								intersection_index = i;
							}
						}
					}

					System.out.println("Intersection Point" + intersection );
					System.out.println("Chose intersection point " + (intersection_index+1));
					System.out.println("Pitch height: " + pitchHeight);

					int[] cmd = new int[6];

					int angleToIntersection = (int) SFunctions.getAngle(getOurPosition(), intersection);
					if (angleToIntersection < 0) {
						angleToIntersection = 360 + angleToIntersection;
					}
					System.out.println("angle to intersection: " + angleToIntersection);
					System.out.println("angle to ball:" + Math.abs(getOurAngle()-angleToIntersection));
					if ((SFunctions.nearAngle(Math.abs(getOurAngle()-angleToIntersection), 0, 20) ||
						(SFunctions.nearAngle(Math.abs(getOurAngle()-angleToIntersection), 360, 20))))
						cmd[0] = 1;
					else if (SFunctions.nearAngle((getOurAngle()-angleToIntersection), 90, 20) ||
					        SFunctions.nearAngle((getOurAngle()-angleToIntersection), -270, 20))
						cmd[0] = 4;
					else if (SFunctions.nearAngle(Math.abs(getOurAngle()-angleToIntersection), 180, 20))
						cmd[0] = 2;
					else if (SFunctions.nearAngle((getOurAngle()-angleToIntersection), -90, 20) ||
					        SFunctions.nearAngle((getOurAngle()-angleToIntersection), 270, 20))
						cmd[0] = 3;
					else {
						System.out.println("DONT KNOW HOW TO INTERSECT");
						System.out.println("1");
					}

					System.out.println("INTERSECTION POINT" + intersection);

					int distance = (int) intersection.distance(getOurPosition());

					cmd[1] = distance;					//command value
					cmd[2] = 0;							//angle to turn
					cmd[3] = intersection.x;			//way-point x
					cmd[4] = intersection.y;			//way-point y
					cmd[5] = 700;						//speed
					if (commands.size() > 0) {
						commands.clear();
					}
					commands.add(cmd);
					sendCommands(commands);
				}
				ballPreviousPosition = currentPosition;
			//}
			return;
		}
		/**
		 ********************************************************************************
		 ************************ END OF INTERCEPTION TASK ******************************
		 ********************************************************************************
		 */

		//STRAFE into goal when we are close enough to the ball, we are roughly at 90 or 270 degrees,
		//and the ball's y position is within the goals boundaries
		if (getOurPosition().y < 228 && getOurPosition().y > 128 && getOurPosition().x < 250) {
			if (ourSide == RIGHT) {
				/*
				 *
				 * STRAFE LEFT
				 *
				 */
				if (getOurAngle() < 105 && getOurAngle() > 75) {
					if (getOurPosition().distance(getBallPosition()) < 120) {//close enough to the ball
						//ball is on the left of robot
						int angle = (int) Math.abs(Math.toDegrees(Math.atan2(getOurPosition().y - getBallPosition().y, getBallPosition().x - getOurPosition().x)));
						System.out.println("ANGLE: angle" + angle);
						System.out.println("ANGLE: angle" + angle);
						System.out.println("ANGLE: angle" + angle);
						System.out.println("ANGLE: angle" + angle);
						System.out.println("ANGLE: angle" + angle);
						System.out.println("ANGLE: angle" + angle);
						if (angle < 200 && angle > 160) {
							int[] strafeLeftCmd = new int[6];
							strafeLeftCmd[0] = 3;									//command type, kick
							strafeLeftCmd[1] = 200;								//command value
							strafeLeftCmd[2] = 0;									//angle to turn
							strafeLeftCmd[3] = getOurPosition().x;									//way-point x
							strafeLeftCmd[4] = getOurPosition().y;									//way-point y
							strafeLeftCmd[5] = 100;								//speed
							if (commands.size() > 0) {
								commands.clear();
							}
							commands.add(strafeLeftCmd);
							sendCommands(commands);
							return;
						}
					}
				}
				/*
				 *
				 * STRAFE RIGHT
				 *
				 */
				else if(getOurAngle() < 285 && getOurAngle() > 255){
					if (getOurPosition().distance(getBallPosition()) < 120) {//close enough to the ball
						//ball is on the right of robot
						int angle = (int) (180 - Math.abs(Math.toDegrees(Math.atan2(getOurPosition().y - getBallPosition().y, getBallPosition().x - getOurPosition().x))));
						System.out.println("ANGLE: angle" + angle);
						if (angle < 20 && angle > -20) {
							int[] strafeRightCmd = new int[6];
							strafeRightCmd[0] = 4;									//command type, kick
							strafeRightCmd[1] = 200;								//command value
							strafeRightCmd[2] = 0;									//angle to turn
							strafeRightCmd[3] = getOurPosition().x;					//way-point x
							strafeRightCmd[4] = getOurPosition().y;					//way-point y
							strafeRightCmd[5] = 100;								//speed
							if (commands.size() > 0) {
								commands.clear();
							}
							commands.add(strafeRightCmd);
							sendCommands(commands);
							return;
						}
					}
				}
			}
		}
		if (hasBall(ourColor) == true) {
			if (commands.size() > 0) {
				commands.clear();
			}
			Point goalPosition = null;
			if (ourSide == LEFT) {
				ArrayList<GridPoint> goalPointList = new ArrayList<GridPoint>();
				GridPoint goalGridPoint = new GridPoint(34, 9);
				goalPointList.add(goalGridPoint);
				goalPosition = PathSearch.translateGridsToCoordinates(goalPointList).get(0);
			} else if (ourSide == RIGHT) {
				ArrayList<GridPoint> goalPointList = new ArrayList<GridPoint>();
				GridPoint goalGridPoint = new GridPoint(1, 9);
				goalPointList.add(goalGridPoint);
				goalPosition = PathSearch.translateGridsToCoordinates(goalPointList).get(0);
			}
			int[] kickCmd = new int[6];
			kickCmd[0] = 8;									//command type, kick
			kickCmd[1] = 900;								//command value
			kickCmd[2] = 0;									//angle to turn
			kickCmd[3] = 0;									//way-point x
			kickCmd[4] = 0;									//way-point y
			kickCmd[5] = 600;								//speed

			commands.add(kickCmd);
			//after kicking, it needs to wait for 2 seconds for things to process.
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			if (ourSide == LEFT) {
				ballOffSet_x = -45;
				commands = PathSearch.getPath2(new Point(ballX + ballOffSet_x, ballY + ballOffSet_y), getOurPosition(), getOurAngle(), getOppPosition(), getOppAngle(), LEFT);
//				if (ourColor == YELLOW) {
//					commands = PathSearch.getPath2(new Point(ballX + ballOffSet_x, ballY + ballOffSet_y), new Point(yellowX, yellowY), yellowAngle, new Point(blueX, blueY), blueAngle, LEFT);
//				} else if (ourColor == BLUE) {
//					commands = PathSearch.getPath2(new Point(ballX + ballOffSet_x, ballY + ballOffSet_y), new Point(blueX, blueY), blueAngle, new Point(yellowX, yellowY), yellowAngle, LEFT);
//				}
				} else {
				ballOffSet_x = 45;
				commands = PathSearch.getPath2(new Point(ballX + ballOffSet_x, ballY + ballOffSet_y), getOurPosition(), getOurAngle(), getOppPosition(), getOppAngle(), RIGHT);
//				if (ourColor == YELLOW) {
//					commands = PathSearch.getPath2(new Point(ballX + ballOffSet_x, ballY + ballOffSet_y), new Point(yellowX, yellowY), yellowAngle, new Point(blueX, blueY), blueAngle, RIGHT);
//				} else if (ourColor == BLUE) {
//					commands = PathSearch.getPath2(new Point(ballX + ballOffSet_x, ballY + ballOffSet_y), new Point(blueX, blueY), blueAngle, new Point(yellowX, yellowY), yellowAngle, RIGHT);
//				}
				}
		}

		if (debug == 1) {
			System.out.println("STRATEGY: Our distance to ball: " + getOurPosition().distance(getBallPosition()));
			System.out.println("STRATEGY: Our angle to ball: " + (getOurAngle() - Math.abs(Math.toDegrees(Math.atan2((getBallPosition().y - getOurPosition().y),
			        (getBallPosition().x - getOurPosition().x))))));
			if (hasBall(ourColor)) {
				System.out.println("*********** HAS BALL, SHOOT ************");
				System.out.println("*********** HAS BALL, SHOOT ************");
				System.out.println("*********** HAS BALL, SHOOT ************");
			} else {
				System.out.println("Don't have ball, navigate to ball");
			}
			for (int i = 0; i < commands.size(); i++) {
				System.out.println(commands.get(i)[0]+"  "+commands.get(i)[1]);
			}
		}

		//m.executeArrayList(commands);
		sendCommands(commands);
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


	private static Point getIntersection(Point a, int angleA, Point b, int angleB){

	if(angleA>180){
		angleA = -(360-angleA);
	}
	if(angleB>180){
		angleB = -(360-angleB);
	}

        //Find gradients
        double gradient1 = Math.tan(Math.toRadians(angleA));
        double gradient2 = Math.tan(Math.toRadians(angleB));

        //Get constants
        double const1 = a.y;
        double const2 = -b.y;

        System.out.println(gradient1);
        System.out.println(gradient2);
        System.out.println(const1);
        System.out.println(const2);

        //Find X
        double x =  (gradient1*a.x - gradient2*b.x + const2 - const1) / (gradient1-gradient2);

        //Plug into one of the equations to find y; y= mx + c
        double y = gradient1*(x - a.x) + const1;

        System.out.println((int)x);
        System.out.println((int)y);

        return new Point((int)x, (int)-y);
    }

	// -1 means opponent not blocking goal
	private int oppBlockingGoal() {
		if (ourSide == LEFT) {
		}
		return -1;
	}

	private boolean facingBall(int robotColor) {
		//our robot

		if (robotColor == ourColor) {

			/**
			int angle = (int) Math.abs(Math.toDegrees(Math.atan2((getBallPosition().y - getOurPosition().y),
			        (getBallPosition().x - getOurPosition().x))) - getOurAngle());
			angle = Math.abs(angle - 360);
			if (angle < 20) {
				return true;
			}
			 *
			 **/

			int angle = (int) (getOurAngle() - Math.abs(Math.toDegrees(Math.atan2((getBallPosition().y - getOurPosition().y),
			        (getBallPosition().x - getOurPosition().x)))));

			if (Math.abs(angle) < 20) {
				return true;
			}

		} //opp robot
		else if (robotColor != ourColor) {
			int angle = (int) Math.abs(Math.toDegrees(Math.atan2((getBallPosition().y - getOppPosition().y),
			        (getBallPosition().x - getOppPosition().x))) - getOppAngle());
			angle = Math.abs(angle - 360);
			if (angle < 20) {
				return true;
			}
		}
		return false;
	}

	private boolean hasBall(int robotColor) {
		//check the angle and distance between our robot and the ball
		if (facingBall(robotColor)) {
			if (getOurPosition().distance(getBallPosition()) < HAS_BALL_DISTANCE) {

				//read sensor information from movement
				return true;
			}
		}
		return false;
	}

	private void update() {
		//hasBall = singleton.hasBall();
		//update coordinates
		int[] coords;
		if (simulated == false) {
			coords = singleton.getCoordinates();
		} else {
			coords = simulation.getCoordinates();
		}
		ballX = coords[0];
		ballY = coords[1];
		blueX = coords[3];
		blueY = coords[4];
		blueAngle = coords[5];
		yellowX = coords[6];
		yellowY = coords[7];
		yellowAngle = coords[8];
		ballPositions.add(new Point(ballX,ballY));
		//System.out.println(blueX +" x y "+ blueY);
	}

	private void sendCommands(ArrayList<int[]> cmds) {
		System.out.println("size: "+cmds.size());
		if (simulated == false) {
			singleton.sendCommands(cmds);
		} else {
			simulation.sendCommands(cmds);
		}
		//m.executeArrayList(cmds);
	}

	private boolean inOwnHalf(int robotColor) {
		//our robot
		if (robotColor == ourColor) {
			if (ourSide == LEFT && getOurPosition().x < pitchMidLine) {
				return true;
			} else if (ourSide == RIGHT && getOurPosition().x > pitchMidLine) {
				return true;
			}
		} //opp robot
		else if (robotColor != ourColor) {
			if (oppSide == LEFT && getOppPosition().x < pitchMidLine) {
				return true;
			} else if (oppSide == RIGHT && getOppPosition().x > pitchMidLine) {
				return true;
			}
		}
		return false;
	}

	private Point getOurPosition() {
		if (ourColor == YELLOW) {
			return new Point(yellowX, yellowY);
		} else {
			return new Point(blueX, blueY);
		}
	}

	private Point getOppPosition() {
		if (ourColor == YELLOW) {
			return new Point(blueX, blueY);
		} else {
			return new Point(yellowX, yellowY);
		}
	}

	private int getOurAngle() {
		if (ourColor == YELLOW) {
			return yellowAngle;
		} else {
			return blueAngle;
		}
	}

	private int getOppAngle() {
		if (oppColor == YELLOW) {
			return yellowAngle;
		} else {
			return blueAngle;
		}
	}

	private Point getBallPosition() {
		return new Point(ballX, ballY);
	}

	private void startMatch() {
		finished = false;
	}

	private void stopMatch() {
		finished = true;
	}

	public static void main(String[] args) {
		StrategyIntercept s = new StrategyIntercept(false,YELLOW, 0, 1);
		s.run();
	}

	public void simulate(){
		update();
		computeStrategy();

	}
}
