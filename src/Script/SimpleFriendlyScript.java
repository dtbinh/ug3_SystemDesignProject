package Script;

import PitchObject.Position;
import PitchObject.Robot;

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
					penaltyDefMode();
					if (penaltyTimeUp() || ball.isMoving())	endPenalty();
					break;
				case PENALTY_ATK:
					penaltyAtkMode();
					if (penaltyTimeUp() || !ball.robotHasBall(ourRobot)) endPenalty();
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
			int minSpeed = 100; int maxSpeed = 255; 
			int minDist  = 0;   int maxDist  = 150;
			int dist = (int) target.euclidDistTo(ourRobot.getCoors());
			Robot.MAX_SPEED = Math.min(maxSpeed,
					(minSpeed + (dist-minDist) / (maxDist-minDist) * (maxSpeed-minSpeed)));
			System.out.println(Robot.MAX_SPEED);
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
			Robot.MAX_SPEED = 255;
		}

		// !!!! execution phase !!!!
		playExecute();
	}

	static void penaltyDefMode() {
		Position frontOfUs = ourRobot.getCoors().projectPoint(Math.PI/2, 30);
		// !!!! planning phase !!!!
		Position defendCoors = theirRobot.getCoors().projectPoint(theirRobot.getRobotAngle(),
		    (int) ourRobot.getCoors().euclidDistTo(theirRobot.getCoors()));

		System.out.println("ourRobot " + ourRobot.getCoors());
		System.out.println("frontOfUs " + frontOfUs);
		System.out.println("defendCoors " + defendCoors);

		double angle;
		if (ourRobot.getCoors().getY() - defendCoors.getY() > GOT_THERE_DIST) {
			angle = 3*Math.PI/2;
		} else if (defendCoors.getY() - ourRobot.getCoors().getY() > GOT_THERE_DIST) {
			angle = Math.PI/2;
		} else {
			return;
		}
		Position target = ourRobot.getCoors().projectPoint(angle, 100);
		System.out.println("target " + target);
		planMoveToFace(target, frontOfUs);
		Robot.MAX_SPEED = 150;
		
		// !!!! execution phase !!!!
		playExecute();
	}

	static void penaltyAtkMode() {
		sendKickCommand();
	}
}
