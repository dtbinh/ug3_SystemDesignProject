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
					if (ball.isMoving()) endPenalty();
					break;
				case PENALTY_ATK:
					penaltyAtkMode();
					if (!ball.robotHasBall(ourRobot)) endPenalty();
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
			int minSpeed = 120; int maxSpeed = 255; 
			int minDist  = 0;   int maxDist  = 150;
			int dist = (int) target.euclidDistTo(ourRobot.getCoors());
			Robot.MAX_SPEED = Math.min(maxSpeed,
					(minSpeed + (dist-minDist) / (maxDist-minDist) * (maxSpeed-minSpeed)));
		}
		else {
			if (wantToKick()) {
				planKick();
				System.out.println("planning to kick");
			}
			else {
				planMoveStraight(theirGoal.getCoors());
				System.out.println("planning to DRIBBLE");
			}
			Robot.MAX_SPEED = 180;
		}

		// !!!! execution phase !!!!
		playExecute();
	}

	static void penaltyDefMode() {
		// !!!! planning phase !!!!
		int defendY = theirRobot.getCoors().projectPoint(theirRobot.getRobotAngle(),
		    (int) ourRobot.getCoors().euclidDistTo(theirRobot.getCoors())).getY();
		
		// if they aren't pointing into the goal
		if (defendY < 115 || defendY > 310) {
			plannedCommands.clear(); sendZeros();
			System.out.println("Currently defending");
			return;
		}
		
		// we'll towards a point (but stop before getting there)
		int targetX = shootingRight ? 60 : 655;
		int targetY = ourRobot.getCoors().getY();
		if (ourRobot.getCoors().getY() - defendY > 20) {
			targetY -= 100;
		} else if (defendY - ourRobot.getCoors().getY() > 20) {
			targetY += 100;
		} else {
			plannedCommands.clear(); sendZeros();
			System.out.println("Currently defending");
			return;
		}
		Position target = new Position(targetX, targetY);
		
		// always point downwards
		Position frontOfUs = ourRobot.getCoors().projectPoint(Math.PI/2, 150);
		
		planMoveToFace(target, frontOfUs);
		System.out.println("defending to " + target);
		Robot.MAX_SPEED = 150;
		
		// !!!! execution phase !!!!
		playExecute();
	}

	static void penaltyAtkMode() {
		sendKickCommand();
	}
}
