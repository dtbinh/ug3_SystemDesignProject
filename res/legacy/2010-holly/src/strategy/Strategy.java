package strategy;

import baseSystem.Singleton;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import simulation.Simulation;
import vision.Vision;

/**
 * @author Joe Tam
 * @author Ben Ledbury
 * @author Matt
 * @author Lau
 * @author Martin
 *
 * TODO:
 * 1.	handle unknown ball locations, our robot or opponent's robot might be covering the ball (we wont
 * see this at the ends of the pitch because of the camera curvature
 *
 * 2.	"creep mode", keeps blocking opponent's shooting angle using strafing and closing in at the same time
 *
 * 3.	penalties stuff
 *
 * when
 ********** List of available commands
 *	Type 1: forward
 *	Type 2: backward
 *	Type 3: strafe left
 *	Type 4: strafe right
 *	Type 8: kick
 *	Type 9: forward and kick
 *
 *	COMMAND format: [command type,command value,angle to turn,way-point x,way-point y, speed]
 */
public class Strategy extends Thread {

	//variables associated with Simulator
	Simulation simulation;
	boolean simulated;
	//game play variables
	private long refreshInterval = 50;		//50 milliseconds
	private long movementCorrectionInterval = 180;
	private boolean finished = false;
	private int ourColor;
	private int oppColor;
	private int ourSide;
	private int oppSide;
	private boolean stateSwitchable = true;
	public int currentGameState = 101;
	private boolean movementCorrection;
	//game play constants
	public final static int BLUE = 0;
	public final static int YELLOW = 1;
	public final static int LEFT = 0;
	public final static int RIGHT = 1;
	public final static int STATE_OFFENCE = 101;
	public final static int STATE_DEFENCE = 102;
	public final static int STATE_STRAFEGOAL = 103;
	public final static int STATE_PENALTY_OFFENCE = 104;
	public final static int STATE_PENALTY_DEFENCE = 105;
	public final static int STATE_MARKING_OPPONENT = 106;
	public final static int STATE_BACKING_OFF = 107;
	public static int pitchWidth;
	public static int pitchHeight;
	public static int pitchMidLine;
	//objects coordinates and angles
	private int ballX;
	private int ballY;
	private int prevballX;
	private int prevballY;
	private int blueX;
	private int blueY;
	private int blueAngle;
	private int yellowX;
	private int yellowY;
	private int yellowAngle;
	//system integration
	private int debug;
	private Singleton singleton;
	private ArrayList<int[]> commands = null;
	//variables related to strategy
	private boolean intercept = false;
	private Point ballPreviousPosition = null;
	//constants related to strategy
	private boolean interceptFinished = false;
	private boolean running = true;
	//variables for goalMoves
	//Goal Left
	static int goalStartY = 165;
	static int goalEndY = 275;
	static int goalLeftX = 50;
	static int goalRightX = 700;
	static int interval = 20;

	public Strategy(boolean simulated, boolean movementCorrection, int robotColor, int side, int debug) {

		this.movementCorrection = movementCorrection;

		pitchWidth = Vision.PITCH_END_X - Vision.PITCH_START_X;
		pitchHeight = Vision.PITCH_END_Y - Vision.PITCH_START_Y;
		pitchMidLine = pitchWidth / 2;

		commands = new ArrayList<int[]>();

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

		this.simulated = simulated;
		if (simulated == false) {
			singleton = Singleton.getSingleton();
		} else {
			simulation = Simulation.getSimulation();
		}
	}

	/**
	 ************************************************************************************************************
	 ******************** METHODS FOR CONTROLLING GAME FLOW AND INTEGRATION WITH SYSTEM OVERVIEW ****************
	 ************************************************************************************************************
	 */
	public void startMatch() {
		finished = false;
	}

	public void stopMatch() {
		finished = true;
	}

	public void startNormalMode() {
		stateSwitchable = true;
	}

	public void setState_penalty_offence() {
		currentGameState = STATE_PENALTY_OFFENCE;
		stateSwitchable = false;
	}

	public void setState_penalty_defence() {
		currentGameState = STATE_PENALTY_DEFENCE;
		stateSwitchable = false;
	}

	public void startStrategy() {
		running = true;
	}

	public void stopStrategy() {
		running = false;
		sendStop();
	}

	public void setStateSwitchable(boolean value) {
		stateSwitchable = value;
	}

	public int getGameState() {
		return currentGameState;
	}

	public boolean isRunning() {
		return running;
	}

	public void setSideLeft() {
		ourSide = LEFT;
		oppSide = RIGHT;
	}

	public void setSideRight() {
		ourSide = RIGHT;
		oppSide = LEFT;
	}

	public int getOurSide() {
		return ourSide;
	}

	public int getOppSide() {
		return ourSide;
	}

	public void setColorBlue() {
		ourColor = BLUE;
		oppColor = YELLOW;
	}

	public void setColorYellow() {
		ourColor = YELLOW;
		oppColor = BLUE;
	}

	public int getOurColor() {
		return ourColor;
	}

	/**
	 ***********************************************************************************************************
	 ************************************ MAIN STRATEGY CONTROL FLOW *******************************************
	 ***********************************************************************************************************
	 */
	@Override
	public void run() {

		while (!finished) {
			try {
				update();
				if (running) {
					if (!movementCorrection) {
						if (singleton.getWaiting()) {
							computeStrategy();
						}
					} else {
						computeStrategy();
					}
				}
				if (!movementCorrection) {
					Thread.sleep(refreshInterval);
				} else {
					Thread.sleep(movementCorrectionInterval);
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(Strategy.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void computeStrategy() {
		//offset is changed dependent on the robot's direction to the opponent's goal
		int ballOffSet_x = 0;
		int ballOffSet_y = 0;

		print("what state are we in? " + chooseGameState(), 2);

		switch (chooseGameState()) {
			case STATE_OFFENCE:
				print("Are we near ball? " + nearBall(ourColor), 2);
				if (nearBall(ourColor)) {
					int[] canScore = canScore();

					if (canScore[0] == 1) {
						kick(canScore[1], canScore[2], canScore[3]);
					} else if (canScore[0] == 2) {
						strafeLeftGoal(canScore[1], canScore[2], canScore[3]);
					} else if (canScore[0] == 3) {
						strafeRightGoal(canScore[1], canScore[2], canScore[3]);
					} else {
						print("WE DONT KNOW WHAT TO DO, WE ARE NEAR BALL",1);
						int angleToTurn = SFunctions.calculateAngle(getOurPosition(), getOppGoal(), getOurAngle());

						int[] turnToGoalCmd = {5,0,angleToTurn,getOurPosition().x,getOurPosition().y,700,400};
						commands.clear();
						commands.add(turnToGoalCmd);
						//sendCommands(commands);
						if ((angleToTurn<10 && angleToTurn>-10) &&!hasBall_joe()) {
							backoff_joe();
						}
//						if (canTurn()) {
//							int[] move = goalMove(getOurPosition(), getBallPosition(), getOppSide(), getOurAngle());
//							if (Math.abs(move[2]) < 15) {
//								print("Angle smaller than 15 degrees, do nothing", 2);
//							} else {
//								//turnAndkick(move, getOurSide(), getOurAngle());
//							}
//						} else {
//							backOff(getOurPosition(), getBallPosition(), getOurSide(), getOurAngle());
//						}
					}

				} else {
					gotoBall();
				}
				break;

			case STATE_BACKING_OFF:
				if(!nearBall(ourColor)){
					stateSwitchable=true;
				}
				break;

			case STATE_DEFENCE:

				//check if the opponent is closer to our goal than us
				// if closer- we need some immediate movements, preferably only one - strafe, diagonal etc
				boolean oppCloser = false;
				if (ourSide == LEFT) {
					if (getOurPosition().x > getOppPosition().x) {
						oppCloser = true;
					}
				} else if (ourSide == RIGHT) {
					if (getOurPosition().x < getOppPosition().x) {
						oppCloser = true;
					}
				}

				if (!oppCloser) { 	//we are closer to our goal
					stateSwitchable = false;
					currentGameState = STATE_MARKING_OPPONENT;

				}

				break;

			case STATE_MARKING_OPPONENT:
					markOpponent(true);
					break;

			case STATE_STRAFEGOAL:
				break;
			case STATE_PENALTY_OFFENCE:
				commands.clear();
				shootPenalty();
				//shootPenalty2();
				break;
			case STATE_PENALTY_DEFENCE:
				//defendPenalty();
				markOpponent2(false);

				break;
		}

		sendCommands(commands);
		//startNormalMode();
	}

	/**
	 * Should we not see the ball, we assume the opp has it and hence we strafe in front of it to block a possible goal
	 * @param ourSide
	 */

	private boolean hasBall_joe() {
		int sideThr = 30;
		int angleToTurn = Math.abs(SFunctions.calculateAngle(getOurPosition(), getBallPosition(), getOurAngle()));
		int sideDist = (int) (Math.sin(Math.toRadians(angleToTurn)) * getOurPosition().distance(getBallPosition()));
		if (sideDist < sideThr) {
			if (angleToTurn < 90) {
			print ("WE HAVE BALL!!!",1);
			return true;
			}
		}
		print ("WE DONT HAVE BALL!!!",1);
		return false;
	}

	private void backoff_joe() {

		commands.clear();
		int ballOffSet_x = 120;
		if (ourSide == LEFT) {
			//if (getBallPosition().x < getOurPosition().x) {
				int[] bestPoint = goalMove(getOurPosition(), new Point(ballX - ballOffSet_x, ballY), LEFT, getOurAngle());
				Point ballPosition = new Point(bestPoint[0], bestPoint[1]);
				commands = PathSearch.getPath2(ballPosition, new Point(getOurPosition().x-ballOffSet_x,getOurPosition().y), getOurAngle(), getOppPosition(), getOppAngle(), LEFT);
			//}
		} else {
			//if (getBallPosition().x > getOurPosition().x) {
				int[] bestPoint = goalMove(getOurPosition(), new Point(ballX + ballOffSet_x, ballY), RIGHT, getOurAngle());
				Point ballPosition = new Point(bestPoint[0], bestPoint[1]);
				commands = PathSearch.getPath2(ballPosition, new Point(getOurPosition().x+ballOffSet_x,getOurPosition().y), getOurAngle(), getOppPosition(), getOppAngle(), RIGHT);
			//}
		}
		if (commands.size() > 0) {
			commands.get(0)[0] = 1;
		} else {
			//retreat to goal if doesn't know what to do
			if (ourSide == LEFT) {
				Point ballPosition = new Point(10,pitchHeight/2);
				commands = PathSearch.getPath2(ballPosition, new Point(getOurPosition().x-ballOffSet_x,getOurPosition().y), getOurAngle(), getOppPosition(), getOppAngle(), LEFT);
			} else {
				Point ballPosition = new Point(pitchWidth-10,pitchHeight/2);
				commands = PathSearch.getPath2(ballPosition, new Point(getOurPosition().x-ballOffSet_x,getOurPosition().y), getOurAngle(), getOppPosition(), getOppAngle(), RIGHT);				
			}
		}


		currentGameState=STATE_BACKING_OFF;
		stateSwitchable=true;

		print("JOE: BACK OFF!!!", 1);
	}




	public boolean canTurn() {
		return true;
	}

public ArrayList<int[]> goalMoves(Point ball, int side, int ourAngle) {


		int angle;
		int[] vector = new int[2];
		Point p = new Point();

		ArrayList<int[]> moves = new ArrayList<int[]>();

		int x;
		if (side == RIGHT) {
			x = goalLeftX;
		} else {
			x = goalRightX;
		}

		for (int i = goalStartY; i < goalEndY; i += interval) {

			p.x = x;
			p.y = i;
			// this angle is never used ? why ?
			angle = (int) SFunctions.getAngle(p, ball);
			angle = 180 - getOurAngle() - angle;

			vector[0] = (int) (ball.x - p.x);
			vector[1] = (int) (ball.y - p.y);

			print("Vector: " + vector[0] + " , " + vector[1], 2);

			if (vector[1] == 0) {
				continue;
			}

			double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);

			double[] normVector = new double[]{vector[0] / length, vector[1] / length};
			print((ball.x + normVector[0] * 3) + " " + (ball.y + normVector[1] * 3), 2);

			moves.add(new int[]{(int) Math.round(ball.x + normVector[0] * 3), (int) Math.round(ball.y + normVector[1] * 3), angle});
		}

		return moves;
	}

public int[] goalMove(Point robot, Point ball, int side, int ourAngle) {

		ArrayList<int[]> moves = goalMoves(ball, side, ourAngle);

		int[] bestPoint = moves.get(0);

		Point midGoal;

		if (side == 0) {
			midGoal = new Point(goalLeftX, (goalEndY - goalStartY) / 2);
		} else {
			midGoal = new Point(goalRightX, (goalEndY - goalStartY) / 2);
		}

		int minY = Math.abs((bestPoint[1] - midGoal.y));

		for (int i = 1; i < moves.size(); i++) {
			if (Math.abs((moves.get(i)[1] - midGoal.y)) < minY) {
				minY = Math.abs((moves.get(i)[1] - midGoal.y));
				bestPoint = moves.get(i);
			}
		}

		if(ourAngle>180){
			ourAngle-=360;
		}
		/*
		int angleToTurn = (int)(getOurAngle() - SFunctions.getAngle(new Point(bestPoint[0], bestPoint[1]), midGoal));
		if(side == RIGHT)
		{
		if(angleToTurn > 90)
		angleToTurn = 180 - angleToTurn;
		}
		 */
		int angleToTurn = SFunctions.calculateAngle(robot, ball, ourAngle);
		return new int[]{bestPoint[0], bestPoint[1], angleToTurn};

	}

	private boolean withinBoundaries(Point p) {
		if (p.y > 150 && p.y < 265) {
			return true;
		}

		return false;
	}

	private void markOpponent(boolean creep) {

		stateSwitchable=false;

		//Then get the opponents angle and see where it intersects your x
		Point intersectionPoint = SFunctions.intersection(getOurPosition().x, 0, getOurPosition().x, pitchHeight, getOppPosition().x, getOppPosition().y, getOppAngle());
		System.out.println("intersection point " +intersectionPoint);
		int[] cmd = new int[7];

		int angleToIntersect = SFunctions.calculateAngle(getOurPosition(), intersectionPoint, getOurAngle());
		print("angle "+ angleToIntersect,2);
		//If the ball is within the pitch width
		if (withinBoundaries(intersectionPoint)) {

			if (intersectionPoint.y< pitchHeight/2){
				cmd[4] = intersectionPoint.y+25;
				intersectionPoint.y+=25;
			}
			else {
				cmd[4] = intersectionPoint.y-25;
				intersectionPoint.y-=25;
			}
			cmd[1] = 0; //command value
			cmd[2] = 0;							//angle to turn
			cmd[3] = intersectionPoint.x;

			cmd[5] = 255;  						//speed
			cmd[6] = 900;						//turning speed

			if(getOurPosition().distance(intersectionPoint)<40){
					commands.clear();
					commands.add(new int[]{10,0,0,0,0,0,0});
					return;
				}

		} else {

				if(getOurPosition().distance(intersectionPoint)<40){
					commands.clear();
					commands.add(new int[]{10,0,0,0,0,0,0});
					return;
				}

				if (intersectionPoint.y <= 155) {
					cmd[4] = goalStartY; //command value
				} else {
					cmd[4] = goalEndY;
				}

				cmd[0] = 3;
				cmd[1] = 0;
				cmd[2] = 0;							//angle to turn
				cmd[3] = intersectionPoint.x;		//way-point x
				cmd[5] = 255;  						//speed
				cmd[6] = 900;						//turning speed
			}

		commands.clear();
		commands.add(cmd);

	}

	private void markOpponent2(boolean creep) {

		stateSwitchable=false;

		if(!singleton.getMovement().isRunning()){
			return;
		}

		Point ballCurrentPosition = new Point(ballX,ballY);
		ballPreviousPosition = new Point(prevballX,prevballY);
		

		if (prevballX != -1 && ballX != -1 && ballCurrentPosition.distance(ballPreviousPosition) > 10) {	// if prev was -1 and now it's, say 200 - ball suddenly appeared
        stateSwitchable=true;
		}
        //Then get the opponents angle and see where it intersects your x
        Point intersectionPoint = SFunctions.intersection(getOurPosition().x, 0, getOurPosition().x, pitchHeight, getOppPosition().x, getOppPosition().y, getOppAngle());
        System.out.println("intersection point " +intersectionPoint);
		 System.out.println("Pitch height/2 " +pitchHeight/2);
        int[] cmd = new int[7];

        int angleToIntersect = SFunctions.calculateAngle(getOurPosition(), intersectionPoint, getOurAngle());
        print("angle "+ angleToIntersect,2);
        //If the ball is within the goal width
        if (Math.abs(angleToIntersect-180)<30){    //move backwards
            cmd[0] = 2;
        }
        else {
            cmd[0] = 1;
        }
        if (withinBoundaries(intersectionPoint)) {

            if(getOurPosition().distance(intersectionPoint)<40){    // we already cover that point
                commands.clear();
				commands.add(new int[]{10,0,0,0,0,0,0});
                return;
            }


            if (intersectionPoint.y> getOurPosition().y && intersectionPoint.y>210){    // we go down, but 30 px closer than intersection point
                cmd[4] = 220;
                intersectionPoint.y=220;
            }
            else if (intersectionPoint.y< getOurPosition().y && intersectionPoint.y<210){
                cmd[4] = 200;
                intersectionPoint.y=200;
            }

            cmd[1] = 0; //command value
            cmd[2] = 0;                            //angle to turn
            cmd[3] = intersectionPoint.x;

            cmd[5] = 255;                          //speed
            cmd[6] = 900;                        //turning speed
			commands.clear();
           commands.add(cmd);

        } else { //not within boundaries
			commands.clear();
           commands.add(new int[]{10,0,0,0,0,0,0});
        }
    }

	private void turnToParrallel() {

		int[] turnCmd = new int[7];

		int angle = SFunctions.calculateAngle(getOurPosition(), new Point(getOurPosition().x, 0), getOurAngle());

		turnCmd[0] = 5;									//command type
		turnCmd[1] = 0;								    //command value
		turnCmd[2] = angle;							//angle to turn
		turnCmd[3] = getOurPosition().x;				//way-point x
		turnCmd[4] = getOurPosition().y;				//way-point y
		turnCmd[5] = 700;								//speed
		//slowly turn to the ball
		turnCmd[6] = 200;                               //speed for turning
		commands.clear();
		commands.add(turnCmd);

	}

	/**
	 * We are in an unfortunate position and we need to move away so that we are far enough in order to receive new commands. <br/>
	 * Result :  move away from the ball without moving it.
	 * @param r - Point Robot
	 * @param b - Point Ball
	 * @param side - int side
	 * @param ourAngle - int angle
	 *
	 */
	public void backOff2(Point r, Point b, int side, int ourAngle) {
		int backConst = 25;
		double ballRobotAng;
		ballRobotAng = SFunctions.calculateAnglePolar(r, b);
		if (ballRobotAng > 0) {
			//strafe Right();
			move(4, backConst, 0, 300);
			print("angle is " + ballRobotAng + ". I am strafing right", 2);
		} else {
			//strafe Left();
			move(3, backConst, 0, 300);
			print("angle is " + ballRobotAng + ". I am strafing left", 2);
		}
	}

	private void backOff(Point r, Point b, int side, int ourAngle) {
		int angle;
		int backConst = 25;

		int[] movev = goalMove(r, b, side, ourAngle);
		if (Math.abs(movev[2]) < 15) {
			print("Angle (" + movev[2] + ") smaller than 15 degrees, do nothing", 2);
		} else {
			//turnAndkick(move);
		}
		angle = movev[2];

		if (r.x > b.x) {
			//move right, increase x of robot

			if (r.x + backConst < pitchWidth && (ourAngle < 90 || ourAngle > 270)) {
				move(1, backConst, 0, 700);
			} else {
				//angle = 270 - ourAngle + 30;
				move(1, (pitchWidth - r.x + 3), angle, 700);
			}
		} else {
			//move left, decrease x of robot
			if (r.x - backConst > 0 && (ourAngle > 90 || ourAngle < 270)) {
				move(2, backConst, 0, 700);
			} else {
				/*
				if(ourAngle < 90)
				angle = 90 - ourAngle + 30;
				else if(ourAngle > 270)
				angle = 270 - ourAngle - 30;
				 */
				move(2, (r.x - 3), angle, 700);
			}
		}
		print("angle to turn from back off is " + angle, 2);

	}

	private int chooseGameState() {
		if (stateSwitchable == true) {
			if (hasBall(oppColor) || getBallPosition().equals(new Point(-1, -1))) {
				// if the ball is not found or the opponent has the ball
				currentGameState = STATE_DEFENCE;
			} else {
				currentGameState = STATE_OFFENCE;
			}
			currentGameState = STATE_OFFENCE;	// TODO remove when DEFENCE implemented
		}
		return currentGameState;
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

	private boolean nearBall(int robotColor) {
		if (ourColor == robotColor) {
			if (getOurPosition().distance(getBallPosition()) < 100) {
				return true;
			}
		} else if (oppColor == robotColor) {
			if (getOppPosition().distance(getBallPosition()) < 100) {
				return true;
			}
		}
		return false;
	}

	private boolean hasBall(int robotColor) {
		//check the angle and distance between our robot and the ball
		if (ourColor == robotColor) {
			if (facingBall(ourColor)) {
				if (nearBall(ourColor)) {
					return true;
				}
			}
		} else if (oppColor == robotColor) {
			if (facingBall(oppColor)) {
				if (nearBall(oppColor)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Updates object coordinates.
	 */
	private void update() {
		int[] coords;
		if (simulated == false) {
			coords = singleton.getCoordinates();
		} else {
			coords = simulation.getCoordinates();
		}

		// if we don't find the ball assume it's with a robot
		if (coords[0] == -1 && coords[1] == -1) {
			prevballX = ballX;
			prevballY = ballY;
			if (nearBall(oppColor)) {
				ballX = getOppPosition().x;
				ballY = getOppPosition().y;
			} else if (nearBall(ourColor)) {
				ballX = getOurPosition().x;
				ballY = getOurPosition().y;
			} else {
				// don't want to assume anything if it's near
			}
		} else {
			prevballX = ballX;
			prevballY = ballY;
			ballX = coords[0];
			ballY = coords[1];
		}

		blueX = coords[3];
		blueY = coords[4];
		blueAngle = coords[5];
		yellowX = coords[6];
		yellowY = coords[7];
		yellowAngle = coords[8];
	}

	private void sendCommands(ArrayList<int[]> cmds) {
		print("size: " + cmds.size(), 2);
		if (simulated == false) {

			/**
			if(cmds.size()==0 || commands.get(0)[3] == -1 || commands.get(0)[4] == -1){
				int[] cmd = {10,0,0,getOurPosition().x,getOurPosition().y,0,0};
				cmds.add(cmd);
			}
			 * */

			singleton.setWaiting(false);
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

	public void simulate() {
		update();
		computeStrategy();

	}

	private void sendStop(){

		int[] stopCmd = {10,0,0,0,0,0,0};
		commands.clear();
		commands.add(stopCmd);
		sendCommands(commands);

	}

	private void gotoBall() {
		int ballOffSet_x = 65;
		int ballOffSet_y = 0;

		if (ourSide == LEFT) {
			int[] bestPoint = goalMove(getOurPosition(), new Point(ballX - ballOffSet_x, ballY), LEFT, getOurAngle());
			Point ballPosition = new Point(bestPoint[0], bestPoint[1]);
			commands = PathSearch.getPath2(ballPosition, getOurPosition(), getOurAngle(), getOppPosition(), getOppAngle(), LEFT);
		} else {
			int[] bestPoint = goalMove(getOurPosition(), new Point(ballX + ballOffSet_x, ballY), LEFT, getOurAngle());
			Point ballPosition = new Point(bestPoint[0], bestPoint[1]);
			commands = PathSearch.getPath2(ballPosition, getOurPosition(), getOurAngle(), getOppPosition(), getOppAngle(), RIGHT);
		}
		print("Go to ball", 1);
	}

	private void turnToGoal() {
		Point goalPoint = new Point(0, 0);
		Point ourPoint = getOurPosition();
		if (ourSide == LEFT) {
			goalPoint = new Point(pitchWidth - 1, pitchHeight / 2);
		} else {
			goalPoint = new Point(1, pitchHeight / 2);
		}

		int angleToTurn = (int) Math.toDegrees(Math.atan2((ourPoint.y - goalPoint.y), (goalPoint.x - ourPoint.x))) - getOurAngle();
		if (angleToTurn < -180) {
			angleToTurn = 360 + angleToTurn;
		}
		if (angleToTurn > 180) {
			angleToTurn = angleToTurn - 360;
		}

		int[] turnToGoalCmd = {1, 0, angleToTurn, ourPoint.x, ourPoint.y, 100, 100};
		commands.add(turnToGoalCmd);
		print("STRATEGY: TURNING TO GOAL", 1);
	}

	private void turnAndkick(int[] pos, int ourSide, int ourAngle) {

		if (ourSide == RIGHT) {
			// we are facing our own goal
			if (ourAngle <= 90 || ourAngle >= 270) {
				gotoBall();
			} else {
				print("angle to turn from turnAndKick " + pos[2], 2);
				int[] turnCmd = new int[7];
				turnCmd[0] = 1;									//command type, kick
				turnCmd[1] = 0;								    //command value
				turnCmd[2] = pos[2];							//angle to turn
				turnCmd[3] = getOurPosition().x;				//way-point x
				turnCmd[4] = getOurPosition().y;				//way-point y
				turnCmd[5] = 700;								//speed
				//slowly turn to the ball
				turnCmd[6] = 200;                               //speed for turning
				commands.clear();
				commands.add(turnCmd);
				print("turning!!", 2);

				int[] kickCmd = new int[7];
				kickCmd[0] = 9;									//command type, kick
				kickCmd[1] = 900;								//command value
				kickCmd[2] = 0;									//angle to turn
				kickCmd[3] = 0;									//way-point x
				kickCmd[4] = 0;									//way-point y
				kickCmd[5] = 700;								//speed
				kickCmd[6] = 700;								//speed for turning

				commands.add(kickCmd);
				print("kick!!", 2);
			}
			//else do nothing because we are not facing our goal
		} else {
			// our side is left and we are facing it
			if (ourAngle > 90 && ourAngle < 270) {
				gotoBall();
			} else {
				print("angle to turn from turnAndKick " + pos[2], 2);
				int[] turnCmd = new int[7];
				turnCmd[0] = 1;									//command type, kick
				turnCmd[1] = 0;								    //command value
				turnCmd[2] = pos[2];							//angle to turn
				turnCmd[3] = getOurPosition().x;				//way-point x
				turnCmd[4] = getOurPosition().y;				//way-point y
				turnCmd[5] = 700;								//speed
				//slowly turn to the ball
				turnCmd[6] = 200;                               //speed for turning
				commands.clear();
				commands.add(turnCmd);
				print("turning!!", 2);

				int[] kickCmd = new int[7];
				kickCmd[0] = 9;									//command type, kick
				kickCmd[1] = 900;								//command value
				kickCmd[2] = 0;									//angle to turn
				kickCmd[3] = 0;									//way-point x
				kickCmd[4] = 0;									//way-point y
				kickCmd[5] = 700;								//speed
				kickCmd[6] = 700;								//speed for turning

				commands.add(kickCmd);
				print("kick!!", 2);
			}
		}



	}

	/**
	 * Does a kick or move-and-kick based on the distance from the ball.
	 * @param distance
	 */
	private void kick(int dist, int wayX, int wayY) {
		//if (getOurPosition().distance(getBallPosition()) < 10) {
		int moveThr = 50;
		int[] kickCmd = new int[7];

		if (dist > moveThr) {
			kickCmd[0] = 9;		// type
		} else {
			kickCmd[0] = 8;		// type
		}
		kickCmd[1] = dist;		// move distance
		kickCmd[2] = 0;			// rotate angle
		kickCmd[3] = wayX;		// way-point x
		kickCmd[4] = wayY;		// way-point y
		kickCmd[5] = 700;		// move speed
		kickCmd[6] = 0;			// rotate speed

		commands.clear();
		commands.add(kickCmd);
		if (dist > moveThr) {
			print("kick(): moving and kicking!", 2);
		} else {
			print("kick(): kicking!", 2);
		}
		//}
	}

	private void move(int type, int value, int angle, int speed) {
		int[] cmd = new int[7];
		cmd[0] = type;								//command type, kick
		cmd[1] = value;								//command value
		cmd[2] = angle;								//angle to turn
		cmd[3] = 0;									//way-point x
		cmd[4] = 0;									//way-point y
		cmd[5] = speed;								//speed
		cmd[6] = 0;									//speed for turning

		commands.clear();
		commands.add(cmd);
		print("move!!", 2);
	}

	/**
	 * Strafe left to score.
	 * @param dist - distance to move when strafing
	 * @param wayX - X coordinate of waypoint
	 * @param wayY - Y coordinate of waypoint
	 */
	private void strafeLeftGoal(int dist, int wayX, int wayY) {
		int[] cmd = new int[7];
		cmd[0] = 3;		// type
		cmd[1] = dist;	// move distance
		cmd[2] = 0;		// rotate angle
		cmd[3] = wayX;	// way-point x
		cmd[4] = wayY;	// way-point y
		cmd[5] = 255;	// move speed
		cmd[6] = 0;		// rotate speed

		commands.clear();
		commands.add(cmd);
		print("strafeLeftGoal(): strafe left to score!", 2);
	}

	/**
	 * Strafe right to score.
	 * @param dist - distance to move when strafing
	 * @param wayX - X coordinate of waypoint
	 * @param wayY - Y coordinate of waypoint
	 */
	private void strafeRightGoal(int dist, int wayX, int wayY) {
		int[] cmd = new int[7];
		cmd[0] = 4;		// type
		cmd[1] = dist;	// move distance
		cmd[2] = 0;		// rotate angle
		cmd[3] = wayX;	// way-point x
		cmd[4] = wayY;	// way-point y
		cmd[5] = 255;	// move speed
		cmd[6] = 0;		// rotate speed

		commands.clear();
		commands.add(cmd);
		print("strafeRightGoal(): strafe right to score!", 2);
	}

	private boolean intersects(Point ourCoords, Point opponent, int ourAngle){

		Point intersection = SFunctions.intersection(getOppPosition().x, 0, getOppPosition().x, pitchHeight,
				getOurPosition().x, getOurPosition().y, getOurAngle());

		//See if the

		return true;
	}

	private void shootPenalty() {
        // our strategy: wait 3 seconds, then kick with full power in front
        // we assume the opponent will be standing sideways so the ball
        // bounces back, we go to normal mode, have chances to get a ball
        // opponent still needs some time to turn.

		if(!singleton.getMovement().isRunning()){
			return;
		}

		stateSwitchable = false;

        print("shooting penalty", 2);
        commands.clear();
        int[] kickCmd = {8, 900, 0, getOurPosition().x, getOurPosition().y, 700, 0};
        commands.add(kickCmd);


        try {
			 print("IM SLEEPING", 2);
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Strategy.class.getName()).log(Level.SEVERE, null, ex);
        }
        print("kick!!", 2);
        sendCommands(commands);
        commands.clear();

        //wait one second for a ball to bounce off the opponent's side. then we switch to
        // normal mode
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Strategy.class.getName()).log(Level.SEVERE, null, ex);
        }

		stateSwitchable = true;
		
    }

//strafe right, left then kick
	private void shootPenalty2() {
		print("shooting penalty", 1);
		if (commands.size() > 0) {
			commands.clear();
		}

			int[] strCmd = {3, 0, 0, getOurPosition().x, getOurPosition().y-15, 300, 200};
			commands.add(strCmd);
			int[] strCmd2 = {4, 0, -35, getOurPosition().x, getOurPosition().y+15, 0, 200};
			commands.add(strCmd2);

		int[] kickCmd = {8, 900, 0, getOurPosition().x, getOurPosition().y, 700, 900};
		commands.add(kickCmd);
		print("kick!!", 1);
		//pause everything after kicking
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//switches back to normal mode
		System.out.println("Im switching to normal mode");
		stateSwitchable = true;
		System.out.println("Switching done!!");
	}

	// tested on the pitch - some values to be tweaked
	private void defendPenalty() {

				markOpponent(false);
	}


	private void print(String msg, int msgLevel) {
		if (msgLevel <= debug && debug > 0) {
			System.out.println("STRATEGY >> " + msg);
		}
	}

	/**
	 * Checks if scoring is possible in the current state and returns some values which may be
	 * reused in some actions.
	 * @return Array with [outcome, distance, waypointX, waypointY].
	 * Outcome can be '0' for can't score, '1' for can kick, '2' for strafe left,
	 * '3' for strafe right.
	 */
	private int[] canScore() {
		// thresholds
		final int distThr = 200;	// distance from goal for strafing
		final int angleDiffThr = 45;	// max difference between robot-ball/robot-intersection angles for side checking
		final int sideStrafeThr = 30;	// ask Joe/Martin
		final int sideFwdThr = 25;	// ^^
		// other variables
		Point intersection;
		int[] result = new int[4];

		// check front score
		intersection = aimingOppGoal(getOurAngle());
		if (intersection != null) {
			print("canScore(): we are aiming opposing goal for a kick", 2);
			double angleToTurn = Math.abs(SFunctions.calculateAngle(getOurPosition(), getBallPosition(), getOurAngle()));
			int sideDist = (int) (Math.toRadians(angleToTurn) * getOurPosition().distance(getBallPosition()));

			// TODO check for opponent

			if (sideDist < sideFwdThr) {
				print("canScore(): can score with kicker", 1);

				result[0] = 1;
				result[1] = (int) getOurPosition().distance(getBallPosition());
				result[2] = getBallPosition().x;
				result[3] = getBallPosition().y;
				return result;
			}
		}

		// check strafe score
		if (getOurPosition().distance(getOppGoal()) < distThr) {
			print("canScore(): we are within strafe-goal distance threshold", 2);
			// get the angle on the LEFT
			int sideAngle = getOurAngle() + 90;
			if (sideAngle > 360) {
				sideAngle = sideAngle - 360;
			}
			// check if we are aiming at the correct goal
			intersection = aimingOppGoal(sideAngle);
			if (intersection != null) {
				print("canScore(): we are aiming opposing goal for strafe-goal", 2);
				// check if the ball is on the correct side
				int robotBallAngle = (int) SFunctions.getAngle(getOurPosition(), getBallPosition());
				int robotGoalAngle = (int) SFunctions.getAngle(getOurPosition(), intersection);
				// translate the angles to 0-360 representation
				if (robotBallAngle < 0) {
					robotBallAngle = 360 + robotBallAngle;
				}
				if (robotGoalAngle < 0) {
					robotGoalAngle = 360 + robotGoalAngle;
				}
				// check differences between the angles
				if (Math.abs(robotBallAngle - robotGoalAngle) < angleDiffThr) {
					print("canScore(): the ball is on our left side for strafe-goal", 2);
					double angleToTurn = Math.abs(SFunctions.calculateAngle(getOurPosition(), getBallPosition(), sideAngle));
					int sideDist = (int) (Math.toRadians(angleToTurn) * getOurPosition().distance(getBallPosition()));

					if (sideDist < sideStrafeThr) {
						print("canScore(): can strafe-goal left", 1);

						result[0] = 2;
						result[1] = (int) getOurPosition().distance(intersection);
						result[2] = intersection.x;
						result[3] = intersection.y;
						return result;
					}
				}
			}

			// get the angle on the RIGHT
			sideAngle = getOurAngle() - 90;
			if (sideAngle < 0) {
				sideAngle = sideAngle + 360;
			}
			// check if we are aiming at the correct goal
			intersection = aimingOppGoal(sideAngle);
			if (intersection != null) {
				print("canScore(): we are aiming opposing goal for strafe-goal", 2);
				// check if the ball is on the correct side
				int robotBallAngle = (int) SFunctions.getAngle(getOurPosition(), getBallPosition());
				int robotGoalAngle = (int) SFunctions.getAngle(getOurPosition(), intersection);
				// translate the angles to 0-360 representation
				if (robotBallAngle < 0) {
					robotBallAngle = 360 + robotBallAngle;
				}
				if (robotGoalAngle < 0) {
					robotGoalAngle = 360 + robotGoalAngle;
				}
				// check differences between the angles
				if (Math.abs(robotBallAngle - robotGoalAngle) < angleDiffThr) {
					print("canScore(): the ball is on our right side for strafe-goal", 2);
					// check if the ball is on the correct side
					double angleToTurn = Math.abs(SFunctions.calculateAngle(getOurPosition(), getBallPosition(), sideAngle));
					int sideDist = (int) (Math.toRadians(angleToTurn) * getOurPosition().distance(getBallPosition()));

					if (sideDist < sideStrafeThr) {
						print("canScore(): can strafe-goal right", 1);

						result[0] = 3;
						result[1] = (int) getOurPosition().distance(intersection);
						result[2] = intersection.x;
						result[3] = intersection.y;
						return result;
					}
				}
			}
		}

		result[0] = 0;
		return result;
	}

	/**
	 * Given an angle it checks whether we are facing the goal.
	 * @param angle
	 * @return NULL if we are not facing the goal, else returns the intersection.
	 */
	private Point aimingOppGoal(int angle) {
		Point intersection = SFunctions.intersection(getOppGoal().x, getOppGoal().y, getOppGoal().x, getOppGoal().y + 1, getOurPosition().x, getOurPosition().y, angle);

		if (intersection.y > (pitchHeight / 2 - 90) && intersection.y < (pitchHeight / 2 + 90)) {
			// check sides
			return intersection;
//			if (getOurSide() == RIGHT && getOurAngle() > 90 && getOurAngle() < 270) {
//				return intersection;
//			} else if (getOurSide() == LEFT && (getOurAngle() < 90 || getOurAngle() > 270)) {
//				return intersection;
//			}
		}
		return null;
	}

	/**
	 * @return Midpoint of the opponent's goal.
	 */
	public Point getOppGoal() {
		Point goal = new Point();

		if (getOurSide() == RIGHT) {
			goal.setLocation(0, pitchHeight / 2);
		} else {
			goal.setLocation(pitchWidth, pitchHeight / 2);
		}

		return goal;
	}

        public double getDistanceFromGoal() {
            return getOurPosition().distance(getOppGoal());
        }

	public void blockOpp(int ourSide) {

		//check if the opponent is closer to our goal than us
		// if closer- we need some immediate movements, preferably only one - strafe, diagonal etc
		boolean closer = false;
		if (ourSide == LEFT) {
			if (getOurPosition().x > getOppPosition().x) {
				closer = true;
			}
		} else if (ourSide == RIGHT) {
			if (getOurPosition().x < getOppPosition().x) {
				closer = true;
			}
		}

		if (!closer) { 	//we are closer to our goal, there's a bigger chance to defend it!!
			if (facingSide(getOppAngle()) == getOurSide()) {
				//run, we still can defend it!! maybe one move only?
			} else {
				// opponent has a ball, but is not facing our goal, we might have more time to
				// defend it by blocking/strafing
			}
		} else {
			//they're facing the goal and are closer - uupsss
		}
	}

	private Point getIntersectionPoint(int robotColor) {
		Point p;
		Point r;
		int angle;



		if (robotColor == ourColor) {
			p = getOurPosition();
			angle = getOurAngle();

		} else {
			p = getOppPosition();
			angle = getOppAngle();
		}
		//some math, drawing a line from y=ax+b, we have x,y, angle so we have a=tan(angle)
		int b = (int) (p.y - Math.tan(Math.toRadians(angle)) * p.x);
		int b2 = (int) (p.y - Math.tan(Math.toRadians(angle + 90)) * p.x);
		if (b == 0 || b2 == 0) {
			return p;
		}
		//line between the robot and middle of the goal - we would like to cross this line
		Line2D line = new Line2D.Float(p, new Point(pitchWidth, pitchHeight / 2));

		//points to draw 2 lines - along our angle (backwards/forwards and perpendicular - strafe
		Point[] ps = new Point[4];
		ps[0] = new Point(0, b); //y =ax +b
		ps[1] = new Point(pitchWidth, (int) Math.tan(Math.toRadians(angle)) * pitchWidth + b);
		ps[2] = new Point(0, (int) Math.tan(Math.toRadians(angle + 90)) * pitchWidth + b2);
		ps[3] = new Point(pitchWidth, b2);

		//along
		Line2D line2 = new Line2D.Float(ps[0], ps[1]);
		//perpendicular
		Line2D line3 = new Line2D.Float(ps[2], ps[3]);

		if (line.intersectsLine(line3)) {
			//strafe!!
		}
		if (line.intersectsLine(line2)) {
			//forward backward
		}
		//not finished
		//r.x=

		r = new Point(0, 0); 	//to be changed only not to produce error
		return r;
	}
	//given angle check if it's facing left or right

	private int facingSide(int angle) {

		if (angle > 270 || (angle < 90 && angle > -90) || angle < -270) {
			return RIGHT;
		}
		return LEFT;
	}

	public static void main(String[] args) {

		Strategy s = new Strategy(false, false, YELLOW, 0, 2);
		//s.run();

		Point r = new Point(10, 10);
		Point b = new Point(5, 5);
		s.backOff(r, b, LEFT, 45);
	}
}
