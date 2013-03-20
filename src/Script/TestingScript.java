package Script;

//import PitchObject.PitchObject;
import PitchObject.Position;

public class TestingScript extends AbstractBaseScript {
	public static void main(String[] args) {
		TestingScript ts = new TestingScript(args);
		ts.run();
	}

	public TestingScript(String[] args) {
		super(args);
	}

	public void run() {
		int testCase = 0;
		int stopTime = 200000;
		long startTime = System.currentTimeMillis();
		while (!timeOut(startTime, stopTime)) {
			updateWorldState();
			test(testCase);
		}
		sendZeros();
	}

	static void test(int testCase) {
		Position behindBall = new Position(0,0);
		if (ball.getCoors() != null) {
			behindBall = ball.pointBehindBall(theirGoal.getCoors(), 100);
		}
		if (testCase==0) { // JUST VISION
			System.out.println(" / Goal: " + theirGoal.getCoors() +
					"Ball: " + ball.getCoors() + 
					" / BehindBall: " + behindBall);
		} else if (testCase==1) { // GO STRAIGHT TO THA BALL
			planMoveStraight(behindBall);
		} else if (testCase==2) { // GO STRAIGHT TO THEIR GOAL
			planMoveStraight(theirGoal.getCoors());
		} else if (testCase==3) { // ROTATE TO THA BALL
			planRotate(ball.getCoors());
		} else if (testCase==4) { // GO AND TURN TO THA BALL
			planMoveAndTurn(behindBall, ball.getCoors());
		} else if (testCase==5) { // GO AND hard TURN TO THA BALL
			planMoveToFace(ball.getCoors(), ball.getCoors());
		} else if (testCase==6) { // GO TO THA BALL, ROTATE TO THEIR GOAL
			planMoveAndTurn(behindBall, theirGoal.getCoors());
		} else if (testCase==7) { // GO TO THA BALL, hard ROTATE TO THEIR GOAL
			planMoveToFace(behindBall, theirGoal.getCoors());
		} else if (testCase==8) { // ROTATE TO THE GOAL, KICK
			if (ourRobot.getCoors() == null) { return; }
			if (System.currentTimeMillis() > kickTimeOut && 
					ourRobot.isFacing(theirGoal.getCoors())) {
				planKick();
			} else {
				planRotate(theirGoal.getCoors());
			}
		}
		playExecute();
	}

	static boolean timeOut(long startTime, int allowance) {
		return startTime + allowance < System.currentTimeMillis();
	}
}
