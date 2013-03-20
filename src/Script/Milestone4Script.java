package Script;

import PitchObject.Position;

public class Milestone4Script extends AbstractBaseScript {
	private int taskNo;
	private static boolean intercept = false;
	private static boolean score = false;
	// TODO: CALIBRATE TODAY BEFORE MILESTONE !!!!!!!!!!!!!
	private static int yLeft = 143;
	private static int yRight = 312;


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
		}

		if (intercept) {
			System.out.print("-----I-----");
			Position retreatPoint = getRetreatPoint(shootingRight);
			if (Math.abs(ourRobot.getCoors().getY() - ball.getCoors().getY()) > 35) {
				// planMoveAndTurn(retreatPoint, theirGoal.getCoors());
				planMoveStraight(retreatPoint);
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
				Position behindBall = ball.pointBehindBall(theirGoal.getCoors(), 25);
				planMoveAndTurn(behindBall, ball.getCoors());
				System.out.println("Planning to go to ball");
			} 
			else {
				if (!ourRobot.isFacing(getOptimalGoal())) {
					planRotate(theirGoal.getOptimalPosition());
					System.out.println("Planning to rotate towards their goal");
				}
				else {
					if (theirGoal.getCoors().euclidDistTo(ball.getCoors()) > 250) {
						planMoveStraight(theirGoal.getOptimalPosition());
						System.out.println("Planning to dribble");
					}
					else {
						if (System.currentTimeMillis() > kickTimeOut) {
							planKick();
							System.out.println("Planning to kick!!!");
						}
						else {
							System.out.println("Would like to kick but no");
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
		int x =  ourGoal.getOptimalPosition().getX() + (shootingRight ? +30 : -30);
		// close to where the ball will be (on y axis)
		// int y = ball.getCoors().getY();
		int y = ball.getPredictedCoors(400).getY();
		return new Position (x, y);
	}

	private static boolean openPlay() {
//		int xDist = Math.abs(ball.getCoors().getX() - ourGoal.getCoors().getX());
//
//		//Reason for not 35 is that goal initialises before coordinate 
//		// goal x is 35 
//		return (xDist <100 && (!(xDist == 35)));
		return Math.abs(ball.getCoors().getX() - ourGoal.getCoors().getX()) < 100;
	}

	private static Position getOptimalGoal(){
		int robotY = theirRobot.getCoors().getY();
		if (robotY > yLeft && robotY < yRight){
			int midY;
			int right = yRight - robotY;
			int left = robotY - yLeft;
			if(left > right ){
				midY = (robotY + yLeft)/2;
			}else {
				midY = (yRight + robotY)/2;
			}	
			if (shootingRight){
				return new Position(theirGoal.getCoors().getX() -150,midY);
			}else{
				return new Position(theirGoal.getCoors().getX() +150,midY);
			}
		}else{	
			return (theirGoal.getCoors());
		}
	}

}
