package Script;

import PitchObject.Position;

public class SimpleFriendlyScript extends AbstractBaseScript {

    public static void main(String[] args) {
        SimpleFriendlyScript fs = new SimpleFriendlyScript(args);
        fs.run();
    }

	public SimpleFriendlyScript(String[] args) {
        super(args);
    }

    public void run() {
		while (true) {
			updateWorldState();
			if (ball.getCoors() == null) continue;
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
			planMoveToFace(target, theirGoal.getCoors());
			System.out.println("planning for " + target);
		}
		else {
			/*if (ourRobot.isFacing(theirRobot.getCoors())) {
				Position target = theirGoal.getOptimalPosition();
				target.setY((theirRobot.getCoors().getY() > 240) ? 215 : 265);
				Position toFace = theirGoal.getCoors();
				toFace.setY(target.getY());
				planMoveToFace(target, toFace);
				System.out.println("planning to GO AROUND");
			}
			else */if (wantToKick()) {
				planKick();
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
