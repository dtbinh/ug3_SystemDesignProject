package Script;

public class FriendlyScript extends AbstractBaseScript {

	public FriendlyScript(Stirng[] args) {
		playMode = argc < 3;
		penaltyDefMode = argc == 3 && args[2].equalsIgnoreCase("defendPenalty");
		penaltyAtkMode = argc == 3 && args[2].equalsIgnoreCase("shootPenalty");
    }

    public void run() {
		while (true) {
			updateWorldState();
			if (playMode) {
				playMode();
			} else {
				boolean timeUp = System.currentTimeMillis() > penaltyTimeOut;
				if (penaltyDefMode) {
					System.out.println("********* Start Defend Penalty *********");
					penaltyDefMode();
					if (timeUp || ball.isMoving()) {
						penaltyDefMode = false;
						playMode = true;
						System.out.println("********* End Defend Penalty *********");
					}
				} else {
					System.out.println("********* Start Attack Penalty *********");
					penaltyAtkMode();
					if (timeUp || !haveBall()) {
						penaltyAtkMode = false;
						playMode = true;
						System.out.println("********* End Attack Penalty *********");
					}
				}
			}
		}
	}

	static void playMode() {
		// !!!! planning phase !!!!
		if (!haveBall()) {
			Position target = robotMath.pointBehindBall(theirGoal, ball.getCoors(), BEHIND_BALL_DIST);
			System.out.println("going to X: " + target.getX() + " Y: " + target.getY());
			planMove(target, theirGoal);
		}
		else {
			if (wantToKick()) {
				plannedCommands.pushKickCommand();
				System.out.println("planning to kick");
			}
			else {
				planMove(theirGoal);
				System.out.println("planning to DRIBBLE");
			}
		}

		// !!!! execution phase !!!!
		playExecute();
	}

	static void penaltyDefMode() {
		Position frontOfUs = RobotMath.projectPoint(ourRobot.getCoors(), ourRobot.getAngle(), 30);
		Position defendCoors = RobotMath.projectPoint(
            theirRobot.getCoors(),
            theirRobot.getAngle(),
		    (int) RobotMath.euclidDist(ourRobot.getCoors(),
                                       theirRobot.getCoors()));

		System.out.println("ourRobot x: " + ourRobot.getCoors().getX() + " y: " + ourRobot.getCoors().getY());
		System.out.println("frontOfUs x: " + frontOfUs.getX() + " y: " + frontOfUs.getY());
		System.out.println("defendCoors x: " + defendCoors.getX() + " y: " + defendCoors.getY());

		if (ourRobot.getCoors().getY() - defendCoors.getY() > GOT_THERE_DIST) {
			Position target = RobotMath.projectPoint(ourRobot.getCoors(), 0.0, 100);
			sendMoveCommand(new MoveCommand(target, frontOfUs, false));
		} else if (defendCoors.getY() - ourRobot.getCoors().getY() > GOT_THERE_DIST) {
			Position target = RobotMath.projectPoint(ourRobot.getCoors(), Math.PI, 100);
			sendMoveCommand(new MoveCommand(target, frontOfUs, false));
		}

	}

	static void penaltyAtkMode() {
		sendKickCommand(new KickCommand());
	}

	private static void planMove(Position coors, Robot obstacle, Position coorsToFace) {
		ArrayList<Point> path = PathSearchHolly.getPath2(
				new Point(coors.getX(), coors.getY()),
				new Point(ourRobot.getCoors().getX(), ourRobot.getCoors().getY()),
				(int) Math.toDegrees(ourRobot.getAngle()),
				new Point(obstacle.getCoors().getX(), obstacle.getCoors().getY()),
				(int) Math.toDegrees(obstacle.getAngle()),
				shootingRight ? PathSearchHolly.LEFT : PathSearchHolly.RIGHT);
		        // if we're shootingRight, *our* side is LEFT
		plannedCommands.pushMoveCommand(path, coorsToFace);
	}

	private static void planMove(Position coors, Robot obstacle) {
		planMove(coors, obstacle, coors);
	}

	private static void planMove(Position coors, Position coorsToFace) {
		planMove(coors, theirRobot, coorsToFace);
	}

	private static void planMove(Position coors) {
		planMove(coors, theirRobot);
	}

	static boolean haveBall() {
		double distToBall = RobotMath.euclidDist(ourRobot.getCoors(), ball.getCoors());
		boolean closeToBall = distToBall < DRIBBLE_DIST;
		boolean facingBall = RobotMath.isTargeting(ourRobot, ball.getCoors());

		return closeToBall && facingBall;
	}

	static boolean wantToKick() {
		double positionScore = robotMath.getPositionScore(ourRobot.getCoors(),
													shootingRight, 0.5);
		double hitScore = robotMath.getHitScore(ourRobot, shootingRight);
		boolean kickingAllowed = System.currentTimeMillis() > kickTimeOut;
		return kickingAllowed && (positionScore > 0.0) && (hitScore > 0.32);
	}
}
