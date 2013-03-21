package Script;

import PitchObject.Position;

public class SimpleFriendlyScript extends AbstractBaseScript {

    public static void main(String[] args) {
        SimpleFriendlyScript fs = new SimpleFriendlyScript(args);
        fs.run();
    }

	public SimpleFriendlyScript(String[] args) {
        super(args);
        if (args.length < 3) {
        	robotMode = RobotMode.PLAY;
        } else {
        	if (args[2].equalsIgnoreCase("defendPenalty")) {
        		robotMode = RobotMode.PENALTY_DEF;
        	} else if (args[2].equalsIgnoreCase("shootPenalty")) {
        		robotMode = RobotMode.PENALTY_ATK;
        	} else {
        		System.err.println("ERROR: unhandled mode");
        		System.exit(1);
        	}
        }
    }

    public void run() {
		while (true) {
			updateWorldState();
			switch (robotMode) {
				case PLAY:
					playMode();
					break;
				case PENALTY_DEF:
					System.out.println("********* Start Defend Penalty *********");
					penaltyDefMode();
					if (penaltyTimeUp() || ball.isMoving()) {
						robotMode = RobotMode.PLAY;
					}
					System.out.println("********* End Defend Penalty *********");
					break;
				case PENALTY_ATK:
					System.out.println("********* Start Attack Penalty *********");
					penaltyAtkMode();
					if (penaltyTimeUp() || !ball.robotHasBall(ourRobot)) {
						robotMode = RobotMode.PLAY;
					}
					System.out.println("********* End Attack Penalty *********");
					break;
				default:
	        		System.err.println("ERROR: unhandled mode");
	        		System.exit(1);
	        		break;
			}
		}
    }

	static void playMode() {
		// !!!! planning phase !!!!
		if (!ball.robotHasBall(ourRobot)) {
			Position target = ball.pointBehindBall(theirGoal.getCoors(), BEHIND_BALL_DIST);
			System.out.println("going to X: " + target.getX() + " Y: " + target.getY());
			planMoveToFace(target, theirGoal.getCoors());
		}
		else {
			if (wantToKick()) {
				plannedCommands.pushKickCommand();
				System.out.println("planning to kick");
			}
			else {
				planMoveStraight(theirGoal.getCoors());
				System.out.println("planning to DRIBBLE");
			}
		}

		// !!!! execution phase !!!!
		playExecute();
	}

	static void penaltyDefMode() {
		Position frontOfUs = ourRobot.getCoors().projectPoint(ourRobot.getAngle(), 30);
		Position defendCoors = theirRobot.getCoors().projectPoint(theirRobot.getAngle(),
		    (int) ourRobot.getCoors().euclidDistTo(theirRobot.getCoors()));

		System.out.println("ourRobot " + ourRobot.getCoors());
		System.out.println("frontOfUs " + frontOfUs);
		System.out.println("defendCoors " + defendCoors);

		double angle;
		if (ourRobot.getCoors().getY() - defendCoors.getY() > GOT_THERE_DIST) {
			angle = 0.0;
		} else if (defendCoors.getY() - ourRobot.getCoors().getY() > GOT_THERE_DIST) {
			angle = Math.PI;
		} else {
			return;
		}
		Position target = ourRobot.getCoors().projectPoint(angle, 100);
		planMoveToFace(target, frontOfUs);
	}

	static void penaltyAtkMode() {
		sendKickCommand();
	}
}
