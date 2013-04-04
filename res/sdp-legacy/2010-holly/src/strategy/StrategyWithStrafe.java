package strategy;

import baseSystem.Singleton;
import java.awt.Point;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import movement.Movement;
import vision.Vision;

/**
 *
 * @author Matt
 * @author Ben Ledbury
 * @author Joe Tam
 */
public class StrategyWithStrafe extends Thread {

	//game play variables
	private long refreshInterval = 15000;		//10 seconds
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
	private final Singleton singleton;
	private ArrayList<int[]> commands = null;
	Movement m;
	//variables related to strategy
	private boolean hasBall;
	//constants related to strategy
	private int HAS_BALL_DISTANCE = 95;

	public StrategyWithStrafe(int robotColor, int side, int debug) {

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

		singleton = Singleton.getSingleton();
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
				computeStrategy();
				Thread.sleep(refreshInterval);
			} catch (InterruptedException ex) {
				Logger.getLogger(Strategy.class.getName()).log(Level.SEVERE, null, ex);
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


		//STRAFE into goal when we are close enough to the ball, we are roughly at 90 or 270 degrees,
		//and the ball's y position is within the goals boundaries
		if (getOurPosition().y < 228 && getOurPosition().y > 128 && getOurPosition().x < 250) {
			if (ourSide == RIGHT) {
				if (getOurAngle() < 100 && getOurAngle() > 80) {
				//STRAFE left
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

								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");
								System.out.println("STRAFEEEEEEEEEEEE LEFT");

							}
						}
					}
				}
			}

		else {
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

			//commands = PathSearch.turnToGoal(getOurPosition(), getOurAngle(), getBallPosition(), goalPosition);

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

//    	if (ourColor == YELLOW) {    		
//    		commands = PathSearch.getPath(new Point(ballX + ballOffSet_x, ballY + ballOffSet_y), new Point(yellowX,yellowY), yellowAngle, new Point(blueX,blueY), blueAngle);
//        } else if (ourColor == BLUE){
//        	commands = PathSearch.getPath(new Point(ballX + ballOffSet_x ,ballY + ballOffSet_y), new Point(blueX,blueY), blueAngle, new Point(yellowX,yellowY), yellowAngle);
//        }

		if (debug == 1) {
			System.out.println("STRATEGY: Our distance to ball: " + getOurPosition().distance(getBallPosition()));
			System.out.println("STRATEGY: Our angle to ball: " + (Math.abs(Math.toDegrees(Math.atan2((getBallPosition().y - getOurPosition().y),
			        (getOurPosition().x - getBallPosition().x)))) - getOurAngle()));
			if (hasBall(ourColor)) {
				System.out.println("*********** HAS BALL, SHOOT ************");
				System.out.println("*********** HAS BALL, SHOOT ************");
				System.out.println("*********** HAS BALL, SHOOT ************");
			} else {
				System.out.println("Don't have ball, navigate to ball");
			}
			for (int i = 0; i < commands.size(); i++) {
				System.out.println(commands.get(i));
			}
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
			int angle = (int) Math.abs(Math.toDegrees(Math.atan2((getBallPosition().y - getOurPosition().y),
			        (getBallPosition().x - getOurPosition().x))) - getOurAngle());
			angle = Math.abs(angle - 360);
			if (angle < 20) {
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
		int[] coords = singleton.getCoordinates();
		ballX = coords[0];
		ballY = coords[1];
		blueX = coords[3];
		blueY = coords[4];
		blueAngle = coords[5];
		yellowX = coords[6];
		yellowY = coords[7];
		yellowAngle = coords[8];

	}

	private void sendCommands(ArrayList<int[]> cmds) {
		singleton.sendCommands(cmds);
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
		Strategy s = new Strategy(false, false, YELLOW, 0, 1);
		s.run();
	}
}
