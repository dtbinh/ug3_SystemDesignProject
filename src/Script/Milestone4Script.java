package Script;

import PitchObject.Position;
import PitchObject.Robot;

public class Milestone4Script extends AbstractBaseScript {
	private int taskNo;
	private static boolean intercept = false;
	private static boolean score = false;
	private static int yTop = 160;
	private static int yBottom = 311;


	public static void main(String[] args) {
		Milestone4Script m4 = new Milestone4Script(args);
		m4.run();
	}

	public Milestone4Script(String[] args) {
		super(args);
		taskNo = Integer.parseInt(args[2]);
	}

	public void run() {
		while (true) {
			updateWorldState();
			if (ball.getCoors() == null) continue;
			doTask(this.taskNo);
		}
	}

	static void doTask(int taskNo) {
		
		if (started && ball.isMoving() && !score) {
			intercept = true;
		}
		if (taskNo==3 && started && openPlay()) {
			intercept = false;
			score = true;
			Robot.MAX_SPEED = 212;
		}

		if (intercept) {
			System.out.print("-----I-----");
			Position retreatPoint = getRetreatPoint(shootingRight);
			if (Math.abs(ourRobot.getCoors().getY() - ball.getCoors().getY()) > 35) {
				if (taskNo == 1) {
					planMoveAndTurn(retreatPoint, theirGoal.getCoors());
				}
				else {
					planMoveStraight(retreatPoint);
				}
				System.out.println("Planning moving to retreat point " + retreatPoint);
			}
			else {
				if (!ball.robotIsFacingBall(ourRobot)) {
					planRotate(ball.getCoors());
					System.out.println("Planning to rotate towards ball");
				}
				else {
					plannedCommands.clear();
					sendZeros();
					System.out.println("We done task 1/2 MAN");
				}
			} 
		}
		else if (score) {
			System.out.print("-----S-----");
			if (!ball.robotHasBall(ourRobot)) {
				Position behindBall = ball.pointBehindBall(theirGoal.getCoors(), 50 );
				planMoveToFace(behindBall, ball.getCoors());
				System.out.println("Planning to go to ball");
			} 
			else {
				if (!ourRobot.isFacing(getOptimalGoal())) {
					planRotate(getOptimalGoal());
					System.out.println("Planning to rotate towards their goal");
				}
				else {
					if (theirGoal.getCoors().euclidDistTo(ball.getCoors()) > 180) {
						planMoveStraight(getOptimalKickPosition());
//						planMoveAndTurn(getOptimalKickPosition(), getOptimalGoal());
						System.out.println("Planning to dribble");
					}
					else {
						if (System.currentTimeMillis() > kickTimeOut) {
							planKick();
							System.out.println("Planning to kick!!!");
						}
						else {
							System.out.println("Would like to kick BRO but no");
							return;
						}
					}
				}
			}
		}
		else {
			System.out.println("-----------");
		}
		playExecute();
	}

	private static Position getRetreatPoint(boolean shootingRight) {
		// close to goal (on x axis)
		int x =  ourGoal.getOptimalPosition().getX() + (shootingRight ? +40 : -40);
		// close to where the ball will be (on y axis)
		// int y = ball.getCoors().getY();
		int y = ball.getCoors().getY();
		return new Position (x, y);
	}

	private static boolean openPlay() {
		return Math.abs(ball.getCoors().getX() - ourGoal.getCoors().getX()) < 150;
	}

	private static int getOptimalGoalY() {
		int robotY = theirRobot.getCoors().getY();
		if (robotY > yTop && robotY < yBottom){
			int bottom = yBottom - robotY;
			int top = robotY - yTop;
			if (top > bottom) {
				return (robotY + yTop)/2;
			} else {
				return (yBottom + robotY)/2;
			}	
		}
		else return theirGoal.getCoors().getY();
	}
	
	private static Position getOptimalKickPosition() {
		if (shootingRight){
			return new Position(theirGoal.getCoors().getX() + 120, getOptimalGoalY());
		} else {
			return new Position(theirGoal.getCoors().getX() - 120, getOptimalGoalY());
		}
	}
	
	private static Position getOptimalGoal() {
		return new Position(theirGoal.getCoors().getX(), getOptimalGoalY());
	}

}
