/**
 * The Main method. This is where the magic happens.
 */

package Planning;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import Shared.Line;
import Shared.ObjectInfo;
import Shared.RobotDetails;
import Shared.Tools;
import Simulator.Simulator;
import Vision.ImageProcessor;
import Vision.Viewer;

public class Main2 extends Thread {
	// coordinates of the goals
	public static final int GOAL_BOTTOM = 160;
	public static final int GOAL_TOP = 300;
	public static final int GOAL_LEFT = 25;
	public static final int GOAL_RIGHT = 612;

	// coordinates of the walls: image is 640 x 480
	public static final int WALL_TOP = ImageProcessor.ylowerlimit;
	public static final int WALL_BOTTOM = ImageProcessor.yupperlimit;
	public static final int WALL_LEFT = ImageProcessor.xlowerlimit;
	public static final int WALL_RIGHT = ImageProcessor.xupperlimit;

	// other objects
	public Ball ball = new Ball();
	public static Robot2 nxt; // our robot: can do more than just own the
	// details
	public RobotDetails opponentRobot;
	private Viewer vision;
	private Simulator simulator;

	// game flags
	boolean turningToFaceGoal = false;
	boolean usingSimulator = false;
	public static boolean shootingLeft = true;
	public static boolean weAreBlueTeam = true;
	public static boolean robotMoving = false;
	public static boolean blockingMode = false;
	public static final int DEFAULT_SPEED = 350;

	static Main2 instance = null;

	Point goalMiddle;
	public static boolean isInPenaltyMode = false;

	ArrayList<Point> lastpath = new ArrayList<Point>();

	int gear = 12;
	private Point[] goalPoints = new Point[5];
	private Point currentGoal;
	private int side;
	private long lr;
	private int MINYPOS = 98 + 2;
	private int MAXYPOS = 376 - 4;

	public static void main(String args[]) {
		instance = new Main2();
	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public Main2() {
		try {
			if (usingSimulator)
				simulator = Simulator.startSimulator();
			else
				vision = Viewer.startVision();
		} catch (Exception ex) {

			if (usingSimulator)
				System.out.println("Error starting Simulator");
			else
				System.out.println("Error starting Vision");

			System.out.println(ex.toString());
			ex.printStackTrace();

		}
		nxt = new Robot2(usingSimulator);
		start();
	}

	/**
	 * Planning thread: connect to robot and start planning loop
	 */
	public void run() {
		// blocks until connected
		while (!nxt.startCommunications()) {
			System.out.println("Failed to Connect to Robot");
			Tools.rest(2000);
		}
		// blocks until you start Planning from GUI
		while (!robotMoving) {
			Tools.rest(100);
		}

		/*
		 * this generates the 5 shooting points, 2 more optional for debugging
		 */

		int sixth = (GOAL_TOP - GOAL_BOTTOM) / 6;
		int mid = GOAL_BOTTOM;
		side = shootingLeft ? GOAL_LEFT : GOAL_RIGHT;

		getPitchInfo();

		goalPoints[0] = new Point(side, mid + sixth * 3);
		goalPoints[1] = new Point(side, mid + sixth * 2);
		goalPoints[2] = new Point(side, mid + sixth * 4);
		goalPoints[3] = new Point(side, mid + sixth);
		goalPoints[4] = new Point(side, mid + sixth * 5);

		lr = System.currentTimeMillis();

		theLoop();
	}

	private void theLoop() {
		while (true) {
			// pause if gui asks to pause
			while (!robotMoving) {
				nxt.stop();
				Tools.rest(100);
			}
			while (blockingMode && robotMoving) {
				// this is the penalty mode that returns to normal game
				defend();
				Tools.rest(50);
			}

			// get rid of debug lines
			vision.dropAllLines();

			// update coordinates
			getPitchInfo();

			// if robot is by the wall, try to rotate
			// 97-379 seem to be upper and lower limits for robots y value
			int tmpy = ImageProcessor.barrelCorrected(nxt.getCoors()).y;
			if (tmpy < MINYPOS || tmpy > MAXYPOS) {
				System.out.println("im too close to wall");
				boolean d = false;
				int upordown = 0;
				if (tmpy < MINYPOS) {
					// d = ball.getCoors().x > nxt.getCoors().x;
					d = !shootingLeft;
					// which way should it be facing
					upordown = nxt.getAngle() > 0 && nxt.getAngle() < Math.PI ? 1
							: -1;
					// by which side is it stuck
				}
				if (tmpy > MAXYPOS) {
					// d = ball.getCoors().x < nxt.getCoors().x;
					d = shootingLeft;
					// which way should it be facing
					upordown = nxt.getAngle() > 0 && nxt.getAngle() < Math.PI ? -1
							: 1;
					// by which side is it stuck
				}

				if (d)
					Main2.nxt.adjustWheelSpeeds(0, upordown * 600);
				else
					Main2.nxt.adjustWheelSpeeds(upordown * 600, 0);
				Tools.rest(600);
				Main2.nxt.stop();

				continue;
			}

			// target point is the shooting position
			// returns ball coors if there is no shooting position
			Point target = findPositionToShootFrom(ball.getCoors());

			// if the other robot is less than 300 away, go for the ball
			if (ball.getCoors().distance(opponentRobot.getCoors()) < 300)
				target = ball.getCoors();

			// distance between the robot and the ball
			int dist = (int) Tools.getDistanceBetweenPoint(nxt.getCoors(), ball
					.getCoors());

			// if we are close to the ball AND it is not behind us, go for the
			// ball
			if (shootingLeft && (ball.getCoors().x - 30) < nxt.getCoors().x
					&& dist < 70) {
				target = ball.getCoors();
			}
			if (!shootingLeft && (ball.getCoors().x + 30) > nxt.getCoors().x
					&& dist < 70) {
				target = ball.getCoors();
			}

			// if we are close to the ball: slow down
			// gear = dist < 100 ? (dist < 70 ? 4 : 7) : 12;

			// if has ball and can shoot -> shoot
			if (hasBall()) {
				// if pointing to the other half -> shoot (also, don't kick
				// 10cm~30px from the wall)
				// 10cm = by how much does the kicker stick our when it kicks
				if (isPointingTheOtherWay()
						&& !(tmpy < MINYPOS + 30 && nxt.getAngle() > Math.PI && nxt
								.getAngle() < 2 * Math.PI)
						&& !(tmpy > MAXYPOS - 30 && nxt.getAngle() > 0 && nxt
								.getAngle() < Math.PI)) {
					nxt.kick();

					nxt.adjustWheelSpeeds(600, 600);

					Tools.rest(50);
					continue;
				}

				System.out.println("hasBall");
				if (isClearToShoot(ball.getCoors())) {
					System.out.println("isClearToShoot");
					nxt.adjustWheelSpeeds(600, 600);
					nxt.kick();
					Tools.rest(50);
					continue;
				} else {
					// if has ball but cannot shoot: just go for goal
					target = ball.getCoors();// goalPoints[0];
				}
			}

			// print the possible goals.
			for (int i = 0; i < goalPoints.length; i++) {
				printOnVisionFix(goalPoints[i], Color.red);
			}

			// mark blue possible shoothig position
			printOnVisionFix(currentGoal, Color.blue);

			// if close enough, rotate.
			{
				if (dist < 50 && !ball.getCoors().equals(target)) {
					double a = getAngleToFacePoint(currentGoal);
					if (Math.toDegrees(a) > 10) {
						long l = System.currentTimeMillis();
						if (l - lr > 1000) {
							nxt.rotateBy(a);
							lr = l;
						}
						Tools.rest(50);
						continue;
					}
				}
			}

			// Get a path from the path planner, fiddle with it to get the correct waypoint, move, iterate
				ArrayList<Point> path = PathSearch.getPath(new Point(ball
						.getCoors().x, 480 - ball.getCoors().y), new Point(nxt
						.getCoors().x, 480 - nxt.getCoors().y), (int) Math
						.toDegrees(nxt.getAngle()), new Point(opponentRobot
						.getCoors().x, 480 - opponentRobot.getCoors().y));
				for (int j = 0; j < path.size(); j++) {
					printOnVision(path.get(j), Color.red);
				}
				if (path.size() > 2)
					target = path.remove(2);
				else if (path.size() > 1)
					target = path.remove(1);
				else
					target = path.remove(0);

				target.y = 480 - target.y;

			printOnVisionFix(target, Color.red);

			moveTo(target);

			Tools.rest(50);
		}
	}

	public void defend() {

		getPitchInfo();

		Line oppShootLine = new Line(ball.getCoors(), opponentRobot.getAngle());

		int targetY = oppShootLine.getYfromX(nxt.getCoors().x);

		System.out.println("YCood " + targetY);
		System.out.println("OurCoord " + nxt.getCoors().y);

		if (targetY > GOAL_TOP - 30)
			targetY = GOAL_TOP - 30;
		if (targetY < GOAL_BOTTOM + 30)
			targetY = GOAL_BOTTOM + 30;

		int targetX = shootingLeft ? GOAL_RIGHT - 30 : GOAL_LEFT + 30;

		Point target = new Point(targetX, targetY);

		printOnVisionFix(target, Color.CYAN);

		int d = (int) Math.abs(target.y - nxt.getCoors().y);

		int facingupordown = nxt.getAngle() > 0 && nxt.getAngle() < Math.PI ? 1
				: -1;

		int goingupordown = targetY > nxt.getCoors().y ? 1 : -1;

		int s = 0;
		if (d < 20)
			nxt.stop();
		else if (d < 60) {
			s = 200;
		} else if (d < 90) {
			s = 400;
		} else {
			s = 400;
		}

		nxt.adjustWheelSpeeds(facingupordown * goingupordown * s,
				facingupordown * goingupordown * s);

	}

	/**
	 * Adding a new method for clarity.
	 * 
	 * @return true if the robot is pointing anywhere the other half
	 */
	private boolean isPointingTheOtherWay() {
		if (shootingLeft)
			return nxt.getAngle() > Math.PI / 2
					&& nxt.getAngle() < 3 * Math.PI / 2;
		else
			return nxt.getAngle() < Math.PI / 2
					|| nxt.getAngle() > 3 * Math.PI / 2;
	}

	private void moveTo(Point target) {

		int[] s = Position(target);
		if (s[0] < 0 && s[1] < 0) {
			double a1 = getAngleToFacePoint(target);
			long l = System.currentTimeMillis();
			if ((l - lr) > 1000) {
				nxt.rotateBy(a1);
				lr = l;
			}
			// Tools.rest(900);
		} else {
			nxt.adjustWheelSpeeds(s[0], s[1]);
		}

		// }
	}
	
	// You are not meant to understand this 
	int[] Position(Point p) {
		int x = p.x;
		int y = p.y;

		int desired_angle = 0, theta_e = 0, vl, vr, vc = 70;
		double dx, dy, d_e, Ka = 10.0 / 90.0;
		dx = (x - nxt.getCoors().x);
		dy = (y - nxt.getCoors().y);
		d_e = Math.sqrt(dx * dx + dy * dy);
		if (dx == 0 && dy == 0)
			desired_angle = 90;
		else
			desired_angle = (int) (180. / Math.PI * Math.atan2((double) (dy),
					(double) (dx)));

		theta_e = desired_angle - (int) Math.toDegrees(nxt.getAngle());

		desired_angle = 0;
		while (theta_e > 180)
			theta_e -= 360;
		while (theta_e < -180)
			theta_e += 360;
		if (d_e > 100.)
			Ka = 17. / 90.;
		else if (d_e > 50)
			Ka = 19. / 90.;
		else if (d_e > 30)
			Ka = 21. / 90.;
		else if (d_e > 20)
			Ka = 23. / 90.;
		else
			Ka = 25. / 90.;
		if (theta_e > 95 || theta_e < -95) {
			theta_e += 180;
			if (theta_e > 180)
				theta_e -= 360;
			if (theta_e > 80)
				theta_e = 80;
			if (theta_e < -80)
				theta_e = -80;
			if (d_e < 5.0 && Math.abs(theta_e) < 40)
				Ka = 0.1;
			vr = (int) (-vc * (1.0 / (1.0 + Math.exp(-3.0 * d_e)) - 0.3) + Ka
					* theta_e);
			vl = (int) (-vc * (1.0 / (1.0 + Math.exp(-3.0 * d_e)) - 0.3) - Ka
					* theta_e);
		} else if (theta_e < 85 && theta_e > -85) {
			if (d_e < 5.0 && Math.abs(theta_e) < 40)
				Ka = 0.1;
			vr = (int) (vc * (1.0 / (1.0 + Math.exp(-3.0 * d_e)) - 0.3) + Ka
					* theta_e);
			vl = (int) (vc * (1.0 / (1.0 + Math.exp(-3.0 * d_e)) - 0.3) - Ka
					* theta_e);
		} else {
			vr = (int) (+.17 * theta_e);
			vl = (int) (-.17 * theta_e);
		}
		// System.out.println("vl:" + vl * m + " vr:" + vr * m);
		// HINT: who decides what is "left" and what is "right"?
		return new int[] { vr * gear, vl * gear };
	}

	public Point findPositionToShootFrom(Point goal, Point ball) {
		int height = Math.abs(goal.y - ball.y);
		int width = Math.abs(goal.x - ball.x);
		double angle = Math.toDegrees(Math.tan(height / width));
		Point c = new Point((int) (50 * Math.cos(angle)), (int) (50 * Math
				.sin(angle)));
		return c;
	}

	/**
	 * Returns the angle needed to rotate the robot so that it is facing towards
	 * the given point.
	 */
	private double getAngleToFacePoint(Point target) {

		// first I want to find where the target is in relation to our robot
		Point targetRelativePos = Tools.getRelativePos(nxt.getCoors(), target);
		// now find direction of target from nxt
		double targetFromNxt = Tools.getAngleFrom0_0(targetRelativePos);

		if (targetFromNxt < 0)
			targetFromNxt = 2 * Math.PI + targetFromNxt;

		// now find how much our robot has to turn to face target
		// (turning by negative getAngle returns it to face 0 then add on ball
		// Angle
		double howMuchToTurn = nxt.getAngle() - targetFromNxt;

		// now adjust it so that it turns in the shortest direction (clockwise
		// or counter clockwise)
		if (howMuchToTurn < -Math.PI)
			howMuchToTurn = 2 * Math.PI + howMuchToTurn;
		else if (howMuchToTurn > Math.PI)
			howMuchToTurn = -(2 * Math.PI - howMuchToTurn);

		return howMuchToTurn;

	}

	private boolean isClearToShoot(Point from) {

		// if facing the other way, return false
		if (shootingLeft
				&& (nxt.getAngle() < Math.PI / 2 || nxt.getAngle() > 3 * Math.PI / 2)) {
			// System.out.println("ERROR 1");
			return false;
		}

		if (!shootingLeft && nxt.getAngle() > Math.PI / 2
				&& nxt.getAngle() < 3 * Math.PI / 2) {
			// System.out.println("ERROR 2");
			return false;
		}

		// if not pointing to the goal, return false
		if (shootingLeft) {
			int y = new Line(from, nxt.getAngle()).getYfromX(GOAL_LEFT);
			if (y > GOAL_TOP || y < GOAL_BOTTOM) {
				// System.out.println("ERROR 3");
				return false;
			}
		} else {
			int y = new Line(from, nxt.getAngle()).getYfromX(GOAL_RIGHT);
			if (y > GOAL_TOP || y < GOAL_BOTTOM) {
				// System.out.println("ERROR 4");
				return false;
			}
		}

		// if the other robot is behind just return true
		if (shootingLeft && opponentRobot.getCoors().x > nxt.getCoors().x)
			return true;
		if (!shootingLeft && opponentRobot.getCoors().x < nxt.getCoors().x)
			return true;

		// 30 is size of the robot, sth larger should work better
		if (new Line(from, nxt.getAngle())
				.distanceBetweenPointAndALine(opponentRobot.getCoors()) > 50)
			return true;

		// System.out.println("ERROR 5");
		return false;
	}

	/**
	 * Get the most recent information from vision
	 */
	public void getPitchInfo() {

		ObjectInfo pitchInfo;

		if (usingSimulator)
			pitchInfo = simulator.getObjectInfos();
		else
			pitchInfo = vision.getObjectInfos();

		// get ball info
		ball.setCoors(pitchInfo.getBallCoors());

		// get info on robots
		if (weAreBlueTeam) {
			nxt.updateRobotDetails(pitchInfo.getBlueBot());
			opponentRobot = pitchInfo.getYellowBot();
		} else {
			nxt.updateRobotDetails(pitchInfo.getYellowBot());
			opponentRobot = pitchInfo.getBlueBot();
		}

	}

	public Point findPositionToShootFrom(Point ball) {

		Point goal = findPointToShootAt();
		currentGoal = goal;
		Point c = new Point();
		// if one of the five points is ok to shoot at then find the point to
		// navigate to shoot from
		// else give the position of the ball
		if (goal.y != 0) {
			double gradient = (double) (ball.y - goal.y)
					/ (double) (ball.x - goal.x);
			double distance = 70;
			double xvalue = distance / Math.sqrt(1 + Math.pow(gradient, 2));
			if (!shootingLeft) {
				xvalue = -1 * xvalue;
			}
			c.x = (int) ball.x + (int) xvalue;
			c.y = (int) (ball.y + gradient * xvalue);
			if (c.y > WALL_TOP && c.y < WALL_BOTTOM)
				return c;
		}
		return ball;
	}

	public Point findPointToShootAt() {
		// checks if it is clear to shoot at any of the points in the goal
		for (Point point : goalPoints) {
			if (!isInTheWay(ball.getCoors(), point, opponentRobot.getCoors()))
				return point;
		}
		return new Point(0, 0); // function that uses the point returned needs
		// to check that it is not (0,0)
	}

	public boolean isInTheWay(Point from, Point to, Point obstacle) {
		return isInTheWay(from, to, obstacle, 50);
	}

	public boolean isInTheWay(Point from, Point to, Point obstacle, int size) {

		double a = from.distance(obstacle);
		double b = to.distance(obstacle);

		double dist = from.distance(to);

		if (a < dist && b < dist) {
			return Line2D.ptLineDist(from.x, from.y, to.x, to.y, obstacle.x,
					obstacle.y) < size;
		}
		return false;
	}

	boolean hasBall() {
		int dist = (int) Tools.getDistanceBetweenPoint(nxt.getCoors(), ball
				.getCoors());
		int a = (int) Math.toDegrees(getAngleToFacePoint(ball.getCoors()));
		return dist < 55 && a < 30 && a > -30; // <45
	}

	/**
	 * Finds a point of interception between a line through from, and a circle
	 * around to. Essentially, guides the robot around an object.
	 * 
	 * @param from
	 *            normally our robot
	 * @param to
	 *            normally the ball coordinates
	 * @return the intermediate point
	 */
	Point findIntermediatePoint(Point from, Point to) {

		double gradient = (double) (from.y - to.y) / (double) (from.x - to.x);
		gradient = -1 / gradient;
		double distance = 70;
		double xvalue = distance / Math.sqrt(1 + Math.pow(gradient, 2));
		int x = (int) Math.round(xvalue);
		int y = (int) Math.round(gradient * xvalue);
		return new Point(to.x + x, to.y + y);
	}

	/**
	 * print a cross on vision. use FIX to print non-vision coordinates.
	 * 
	 * @param a
	 * @param color
	 */

	void printOnVisionFix(Point a, Color color) {
		printOnVision(new Point(a.x, 480 - a.y), color);
	}

	void printOnVision(Point a, Color color) {
		int tmpx = a.x;
		int tmpy = a.y;
		vision.addLine(new Point(tmpx - 10, tmpy - 10), new Point(tmpx + 10,
				tmpy + 10), color.getRGB());
		vision.addLine(new Point(tmpx + 10, tmpy - 10), new Point(tmpx - 10,
				tmpy + 10), color.getRGB());
	}

}