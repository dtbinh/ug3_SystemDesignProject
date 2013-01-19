//Contains all constants related to the current state of the world
package Strategy_devel;

import baseSystem.Singleton;
import java.awt.Point;
import java.util.ArrayList;
import simulation.Simulation;
import vision.Vision;

/**
 *
 * @author sdp6
 */
public class World {

	//variables associated with Simulator
	Simulation simulation;
	boolean simulated;
	//game play variables
	protected long refreshInterval = 30;		//5 seconds
	protected int ourColor;
	protected int oppColor;
	protected int ourSide;
	protected int oppSide;
	//game play constants
	protected final static int BLUE = 0;
	protected final static int YELLOW = 1;
	protected final static int LEFT = 0;
	protected final static int RIGHT = 1;
	public static int pitchWidth;
	public static int pitchHeight;
	public static int pitchMidLine;
	//objects coordinates and angles
	protected int ballX;
	protected int ballY;
	protected int blueX;
	protected int blueY;
	protected int blueAngle;
	protected int yellowX;
	protected int yellowY;
	protected int yellowAngle;
	protected int ourX;
	//system integration
	private Singleton singleton;

	//variables related to strategy
	protected boolean hasBall;
	protected boolean intercept = false;
	protected Point ballPreviousPosition = null;
	protected ArrayList ballPositions;
	//constants related to strategy
	protected int HAS_BALL_DISTANCE = 95;
	protected boolean interceptFinished = false;

	public World(int robotColor, int side) {

		pitchWidth = Vision.PITCH_END_X - Vision.PITCH_START_X;
		pitchHeight = Vision.PITCH_END_Y - Vision.PITCH_START_Y;
		pitchMidLine = pitchWidth / 2;

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
		} else {
			simulation = Simulation.getSimulation();
		}

	}

	public int getState() {
		if(hasBall(ourColor)){
			return 1;
		}
		else if(hasBall(oppColor)){
			return 2;
		}
		else return 0;
	}

	protected void update() {

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
		ballPositions.add(new Point(ballX, ballY));

	}

	protected boolean hasBall(int robotColor) {
		//check the angle and distance between our robot and the ball
		if (facingBall(robotColor)) {
			if (getOurPosition().distance(getBallPosition()) < HAS_BALL_DISTANCE) {

				//read sensor information from movement
				return true;
			}
		}
		return false;
	}

	protected boolean facingBall(int robotColor) {
		//our robot

		if (robotColor == ourColor) {

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

	// -1 means opponent not blocking goal
	protected int oppBlockingGoal() {

		//TODO:

		if (ourSide == LEFT) {
		}
		return -1;
	}

	protected Point getOurPosition() {
		if (ourColor == YELLOW) {
			return new Point(yellowX, yellowY);
		} else {
			return new Point(blueX, blueY);
		}
	}

	protected Point getOppPosition() {
		if (ourColor == YELLOW) {
			return new Point(blueX, blueY);
		} else {
			return new Point(yellowX, yellowY);
		}
	}

	protected int getOurAngle() {
		if (ourColor == YELLOW) {
			return yellowAngle;
		} else {
			return blueAngle;
		}
	}

	protected int getOppAngle() {
		if (oppColor == YELLOW) {
			return yellowAngle;
		} else {
			return blueAngle;
		}
	}

	protected Point getBallPosition() {
		return new Point(ballX, ballY);
	}

	protected boolean inOwnHalf(int robotColor) {
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

	protected boolean canScoreStrafeLeft() {

		if (getOurPosition().y < 228 && getOurPosition().y > 128 && getOurPosition().x < 250) {
			if (ourSide == RIGHT) {
				if (getOurAngle() < 105 && getOurAngle() > 75) {
					if (getOurPosition().distance(getBallPosition()) < 120) {//close enough to the ball
						//ball is on the left of robot
						int angle = (int) Math.abs(Math.toDegrees(Math.atan2(getOurPosition().y - getBallPosition().y, getBallPosition().x - getOurPosition().x)));
						if (angle < 200 && angle > 160) {
							return true;
						}
					}
				}

			}
		}
		return false;
	}

	protected boolean canScoreStrafeRight() {

		if (getOurAngle() < 285 && getOurAngle() > 255) {
			if (getOurPosition().distance(getBallPosition()) < 120) {//close enough to the ball
				//ball is on the right of robot
				int angle = (int) (180 - Math.abs(Math.toDegrees(Math.atan2(getOurPosition().y - getBallPosition().y, getBallPosition().x - getOurPosition().x))));

				if (angle < 20 && angle > -20) {
					return true;
				}
			}
		}
		return false;

	}

	protected boolean canScore(){

		//TODO:

		return false;
	}

	protected boolean betweenOpponentAndGoal(){

		//TODO:

		return false;
	}
}
