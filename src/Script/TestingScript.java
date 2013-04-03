package Script;

//import PitchObject.PitchObject;
import PitchObject.Position;

public class TestingScript extends AbstractBaseScript {
	static volatile long startTime = System.currentTimeMillis();
	public static final int STOP_TIME = 200000;
	public static final TestCase DEFAULT_TESTCASE = TestCase.VISION;
	public static enum TestCase {
		VISION,
		GO_STRAIGHT_TO_BALL,
		GO_STRAIGHT_TO_OPPONENT_GOAL,
		ROTATE_TO_BALL,
		MOVE_AND_TURN_TO_BALL,
		MOVE_AND_HARD_TURN_TO_BALL,
		GO_TO_BALL_AND_ROTATE_TO_OPPONENT_GOAL,
		GO_TO_BALL_AND_HARD_ROTATE_TO_OPPONENT_GOAL,
		ROTATE_TO_OPPONENT_GOAL_AND_KICK,
	}

	TestCase[] testCases;

	public static void main(String[] args) {
		TestingScript ts = new TestingScript(args);
		ts.run();
	}

	public TestingScript(String[] args) {
		super(args);

		if (args.length == _argsParsed) {
			testCases = new TestCase[]{ DEFAULT_TESTCASE };
		} else {
			testCases = new TestCase[args.length];
			for (int i = 0; _argsParsed < args.length; i++, _argsParsed++) {
				testCases[i] = TestCase.values()[Integer.parseInt(args[_argsParsed])];
			}
		}
	}

	public void run() {
		for (int i = 0; i < testCases.length; i++) {
			while (!timeOut(startTime, STOP_TIME)) {
				updateWorldState();
				if (ball.getCoors() == null) continue;
				test(testCases[i]);
			}
			sendZeros();
		}
	}

	static void test(TestCase testCase) {
		Position behindBall = new Position(0,0);
		behindBall = ball.pointBehindBall(theirGoal.getCoors(), 100);
		switch (testCase) {
			case VISION:
//				System.out.printf("|  Goal %s  |  Ball %s  |  BehindBall %s  |\n",
//					theirGoal.getCoors(), ball.getCoors(), behindBall);
//				System.out.println(ourRobot.getRobotAngle() + " " + ball.getCoors().getAngleToPosition(ourRobot.getCoors()));
				System.out.println(ball.getCoors());
				break;
	
			case GO_STRAIGHT_TO_BALL:
				planMoveStraight(behindBall);
				break;
	
			case GO_STRAIGHT_TO_OPPONENT_GOAL:
				planMoveStraight(theirGoal.getCoors());
				break;
	
			case ROTATE_TO_BALL:
				planRotate(ball.getCoors());
				break;
	
			case MOVE_AND_TURN_TO_BALL:
				planMoveAndTurn(behindBall, ball.getCoors());
				break;
	
			case MOVE_AND_HARD_TURN_TO_BALL:
				planMoveToFace(ball.getCoors(), ball.getCoors());
				break;
	
			case GO_TO_BALL_AND_ROTATE_TO_OPPONENT_GOAL:
				planMoveAndTurn(behindBall, theirGoal.getCoors());
				break;
	
			case GO_TO_BALL_AND_HARD_ROTATE_TO_OPPONENT_GOAL:
				planMoveToFace(behindBall, theirGoal.getCoors());
				break;
	
			case ROTATE_TO_OPPONENT_GOAL_AND_KICK:
				if (ourRobot.getCoors() == null) {
					return;
				}
				if (ourRobot.isFacing(theirGoal.getCoors())) {
					if (System.currentTimeMillis() > kickTimeOut)
						planKick();
					else return;
				} else {
					planRotate(theirGoal.getCoors());
				}
		}
		playExecute();
	}
}
