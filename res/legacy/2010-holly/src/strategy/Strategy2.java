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
public class Strategy2 extends Thread {

	//variables associated with Simulator
	Simulation simulation;
	boolean simulated;
	
	//game play variables
	private long refreshInterval = 15000;		//5 seconds
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
	private boolean intercept = false;
	private Point ballPreviousPosition = null;
	private ArrayList ballPositions;
	
	//constants related to strategy
	private int HAS_BALL_DISTANCE = 95;
	private boolean interceptFinished = false;

	public Strategy2(boolean simulated, int robotColor, int side, int debug) {

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
				Logger.getLogger(Strategy2.class.getName()).log(Level.SEVERE, null, ex);
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

	private void computeStrategy() {
		//testCase1();
		//offset is changed dependent on the robot's direction to the opponent's goal
		int ballOffSet_x = 0;
		int ballOffSet_y = 0;

		//for interception task
		if (intercept == true) {
			
			//use two frames in vision system to get angle
			//might need tweaking if testing with hand rolling the ball because it messes up vision
			if (ballX < 200) {
				//discard it altogether for testing, so we can roll the ball from the left side of the pitch
			}
			
			else {
				System.out.println("INTERCEPT: DETECTED BALL");
			
				if (ballPreviousPosition == null) {
					ballPreviousPosition = new Point(ballX,ballY);
				}
				Point currentPosition = new Point(ballX,ballY);
				//no change in the ball's position
				if (currentPosition.distance(ballPreviousPosition) > 1) {
					System.out.println("INTERCEPT: DETECTED ANGLE");
					interceptFinished = true;
					double ballAngle = Angles.getAngle(ballPreviousPosition, currentPosition);
					int deltaX = getOurPosition().x - currentPosition.x;
					int deltaY = getOurPosition().y - currentPosition.y;
					int predictedY = ballY + (int) (deltaX * Math.tan(Math.toRadians(ballAngle)));
					int distanceY = predictedY - getOurPosition().y;
					
					//assume we are facing downwards for now (facing positive direction)
					
					int[] cmd = new int[6];
					if (distanceY > 0)
						cmd[0] = 1;							
					else
						cmd[0] = 2;
					cmd[1] = Math.abs(distanceY);				//command value
					cmd[2] = 0;									//angle to turn
					cmd[3] = getOurPosition().x;				//way-point x
					cmd[4] = predictedY;				//way-point y
					cmd[5] = 100;								//speed
					if (commands.size() > 0) {
						commands.clear();
					}
					commands.add(cmd);
					sendCommands(commands);
				}
				ballPreviousPosition = currentPosition;
			}
			return;
		}
		
		
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
		blueX = coords[2];
		blueY = coords[3];
		blueAngle = coords[4];
		yellowX = coords[5];
		yellowY = coords[6];
		yellowAngle = coords[7];
		ballPositions.add(new Point(ballX,ballY));
		System.out.println(blueX +" x y "+ blueY);
	}

	private void sendCommands(ArrayList<int[]> cmds) {
		System.out.println("size: "+cmds.size());
		if (simulated == false) {
			singleton.sendCommands(cmds);
		} else {
			simulation.sendCommands(cmds);
		}
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
		Strategy2 s = new Strategy2(false,YELLOW, 0, 1);
		s.run();
	}
	
	public void simulate(){
		update();
		computeStrategy();
		
	}
}
