/*
 * Copy of Main, adapted for the Interception milestone. Not recommended for usage in tournaments.
 */

package Planning;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Line2D;

import Shared.ObjectInfo;
import Shared.RobotDetails;
import Shared.Tools;
import Simulator.Simulator;
import Vision.ImageProcessor;
import Vision.Viewer;

public class intercept extends Thread {
	// coordinates of the goals
	// public static final int GOAL_BOTTOM = 188;
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
	public Viewer vision;
	private Simulator simulator;

	// game flags
	boolean turningToFaceGoal = false;
	boolean usingSimulator = false;
	public static boolean shootingLeft = true;
	public static boolean weAreBlueTeam = false;
	public static boolean robotMoving = false;
	public static final int DEFAULT_SPEED = 350;

	static intercept instance = null;

	boolean shooting = false;
	Point[] goalPoints = new Point[5];
	public static boolean isInPenaltyMode = false;

	boolean stopped = false;
	Point currentGoal;
	
	int speed = 12;
	
	public static boolean trackBall = false;	//TODO: Change this
	boolean showTracking = true;

	// you might want to turn this off if there is no yellow robot
	boolean useObstacleAvoidance = false;

	public static void main(String args[]) {
		instance = new intercept();
	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public intercept() {
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
		int side;
		if (shootingLeft) {
			side = GOAL_LEFT;
		} else {
			side = GOAL_RIGHT;
		}
		goalPoints[0] = new Point(side, mid + sixth * 3);
		goalPoints[1] = new Point(side, mid + sixth * 2);
		goalPoints[2] = new Point(side, mid + sixth * 4);
		goalPoints[3] = new Point(side, mid + sixth);
		goalPoints[4] = new Point(side, mid + sixth * 5);
		// goalPoints[5] = new Point(side, GOAL_TOP);
		// goalPoints[6] = new Point(side, GOAL_BOTTOM);
		theLoop();
	}
	
	Point expectedPos;

	
	/**
	 * Planning loop: every plus second get the latest vision data and implement
	 * the first mode in the action queue
	 */
	private void theLoop() {

		System.out.println("Get Pitch Info");
		getPitchInfo();
		expectedPos = ball.getCoors();
		new Expected(this);
		while (true) {
			while (!robotMoving) {
				Tools.rest(100);
			}

			getPitchInfo();

			if (trackBall)
			{
				
				long startTime;
				
				nxt.changeSpeed(700);
				while (trackBall)
				{
					System.out.println("Matching y");
					getPitchInfo();
					
				
					
					double a = getAngleToFacePoint(new Point(nxt.getCoors().x, 0));
					
					if (Math.abs(Math.toDegrees(a)) > 10)
					{
						nxt.rotateBy(a);
						System.out.println("Waiting to turn");
						Tools.rest(700);
					}
//					
					
					nxt.stop();
					int targetY = ball.getCoors().y;

					System.out.println("YCood " + targetY);
					System.out.println("OurCoord " + nxt.getCoors().y);
	
					if (targetY > WALL_BOTTOM - 40)
						targetY = WALL_BOTTOM - 40;
					if (targetY < WALL_TOP + 40)
						targetY = WALL_TOP + 40;
	
					// If our robot is lower than where the opp are aiming..
					if ((nxt.getCoors().y < targetY)) {
	
						nxt.moveForward();
						startTime = System.currentTimeMillis();
						while ((nxt.getCoors().y < targetY) && System.currentTimeMillis() - startTime < 10000) {
							if (!nxt.moving)
								nxt.moveForward();
							
							getPitchInfo();
							
							targetY = ball.getCoors().y;
							if (targetY > WALL_BOTTOM - 40)
								targetY = WALL_BOTTOM - 40;
							if (targetY < WALL_TOP + 40)
								targetY = WALL_TOP + 40;
							Tools.rest(10);
						}
						nxt.stop();
					}else
					// If our robot is higher than where the opp are aiming..
					if ((nxt.getCoors().y > targetY)) {
						nxt.moveBackward();
						startTime = System.currentTimeMillis();
						while ((nxt.getCoors().y > targetY)&& System.currentTimeMillis() - startTime < 100000) {
							if (!nxt.moving)
								nxt.moveBackward();
							getPitchInfo();
							
							targetY = ball.getCoors().y;
							if (targetY > WALL_BOTTOM - 40)
								targetY = WALL_BOTTOM - 40;
							if (targetY < WALL_TOP + 40)
								targetY = WALL_TOP + 40;
							Tools.rest(10);
						}
						nxt.stop();
					}
			
					int dx = nxt.getCoors().x - ball.getCoors().x;
					int dy = nxt.getCoors().y - ball.getCoors().y;
					dy *= dy;
					dx *= dx;
					int disttoBall = (int) Math.sqrt(dx + dy);
					
					if (disttoBall < 100)
					{
						System.out.println("HAS BALL");
						trackBall = false;
					}
				}
				nxt.stop();
			}
			else
			{
				System.out.println("Other Stuff");
				Point target = findPositionToShootFrom(ball.getCoors());
				
	
	
				// findPositionToShoot returns the ball position if there is
				// no good shooting position
				// we can use this for setting the shooting mode
				if (!ball.getCoors().equals(target))
					shooting = true;
	
	//			vision.dropAllLines();
	
	//			// print possible shooting positions
	//			for (Point point : goalPoints) {
	//				int tmpx = point.x;
	//				int tmpy = 480 - point.y;
	//				vision.addLine(new Point(tmpx - 10, tmpy - 10), new Point(
	//						tmpx + 10, tmpy + 10));
	//				vision.addLine(new Point(tmpx + 10, tmpy - 10), new Point(
	//						tmpx - 10, tmpy + 10));
	//			}
				// print corss on destination
				int tmpx = target.x;
				int tmpy = 480 - target.y;
				
				if (!showTracking)
					vision.dropAllLines();
	//
//				vision.dropLine();
//				vision.dropLine();
				vision.addLine(new Point(tmpx - 10, tmpy - 10), new Point(
						tmpx + 10, tmpy + 10), Color.blue.getRGB());
				vision.addLine(new Point(tmpx + 10, tmpy - 10), new Point(
						tmpx - 10, tmpy + 10), Color.blue.getRGB());
	
				// print cross on expected position
	//			for (int i = 0; i <= 1000; i+=100)
	//			{
	//				Point expected = ball.getExpectedPosition(i);
	////				Tools.printCoors("expected i " + i + "  ", expected);
	//				tmpx = expected.x;
	//				tmpy = 480 - expected.y;
	//	
	//				vision.addLine(new Point(tmpx - 10, tmpy - 10), new Point(
	//						tmpx + 10, tmpy + 10));
	//				vision.addLine(new Point(tmpx + 10, tmpy - 10), new Point(
	//						tmpx - 10, tmpy + 10));
	//			}
	
				// if has ball and is clear to shoot - shoot.
				// commented out for debugging
	
				// if (hasBall()) {
				// System.out.println("hasBall");
				// if (isClearToShoot(ball.getCoors())) {
				// nxt.adjustWheelSpeeds(DEFAULT_SPEED, DEFAULT_SPEED);
				// nxt.kick();
				// Tools.rest(500);
				// }
				// }
	
				int dist = (int) Tools.getDistanceBetweenPoint(nxt.getCoors(),
						target);
	
				speed = dist < 100 ? 5 : 12;
				
				// if its close enough and shooting - adjust and shoot
				if (shooting && dist < 30) {
					double a = getAngleToFacePoint(currentGoal);
					if (hasBall() && Math.toDegrees(a) > 10) {
						nxt.rotateBy(a);
						Tools.rest(900);
					}
					nxt.adjustWheelSpeeds(600, 600);
					nxt.kick();
					Tools.rest(600);
				}
	
				// navigate to target.
	
				useObstacleAvoidance = isInTheWay(nxt.getCoors(), target,
				opponentRobot.getCoors(), 100);
				
				System.out.println("Use obstacle Avod " + useObstacleAvoidance);
				
				int[] s = Position(target);
	
				if (s[0] < 0 || s[1] < 0) {
					double a = getAngleToFacePoint(ball.getCoors());
					
					System.out.println("Rotating by Mi " + Math.toDegrees(a));
//					if (Math.abs(Math.toDegrees(a)) < 10)
//						nxt.moveForward();
//					else
//					{
						nxt.rotateBy(a);
						Tools.rest(900);// TODO: might need adjusting
//					}
				} else {
					// if stopped move forward is hopefully not necessary anymore
					nxt.adjustWheelSpeeds(s[0], s[1]);
				}
				Tools.rest(60);

		}
		}
	}

	int[] Position(Point p) {
		int x = p.x;
		int y = p.y;

		// the code from that pdf on path planning
		int desired_angle = 0, theta_e = 0, d_angle = 0, vl, vr, vc = 70;
		double dx, dy, d_e, Ka = 10.0 / 90.0;
		dx = (x - nxt.getCoors().x);
		dy = (y - nxt.getCoors().y);
		d_e = Math.sqrt(dx * dx + dy * dy);
		if (dx == 0 && dy == 0)
			desired_angle = 90;
		else
			desired_angle = (int) (180. / Math.PI * Math.atan2((double) (dy),
					(double) (dx)));

		/*
		 * this is the smart code for path avoidance. I need sth working quick
		 * so I will use a dumb method for now
		 */
		if (useObstacleAvoidance) {
			d_angle = (int) Math.toDegrees(ObstacleAvoidace(nxt.getCoors(),
					opponentRobot.getCoors(), 65., 20., nxt.getAngle()));

			theta_e = (int) (desired_angle + d_angle - 2 * Math.toDegrees(nxt
					.getAngle()));
		} else {
			theta_e = desired_angle - (int) Math.toDegrees(nxt.getAngle());
		}

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
//		System.out.println("vl:" + vl * m + " vr:" + vr * m);
		return new int[] { vr * speed, vl * speed };
	}

	double ObstacleAvoidace(Point robot, Point obstacle, double ro, double m,
			double theta_d) {
		double x = robot.x;
		double y = robot.y;

		double ox = obstacle.x;
		double oy = obstacle.y;

		double dist, length, angle, diff_angle;
		double tmp_x, tmp_y;
		// distance between robot and obstacle
		dist = Math.sqrt((ox - x) * (ox - x) + (y - oy) * (y - oy));
		//
		length = Math.abs((ox - x) * Math.sin(theta_d) + (y - oy)
				* Math.cos(theta_d));
		angle = Math.atan2(oy - y, ox - x);
		diff_angle = theta_d - angle;
		while (diff_angle > Math.PI)
			diff_angle -= 2. * Math.PI;
		while (diff_angle < -Math.PI)
			diff_angle += 2. * Math.PI;
		if ((length < ro + m) && (Math.abs(diff_angle) < Math.PI / 2)) {
			if (dist <= ro)
				theta_d = angle - Math.PI;
			else if (dist <= ro + m) {
				// modify theta_d to avoid it with CW direction
				if (diff_angle > 0.) {
					// make smooth transition near the obstacle
					// boundary
					tmp_x = ((dist - ro) * Math.cos(angle - 1.5 * Math.PI) + (ro
							+ m - dist)
							* Math.cos(angle - Math.PI))
							/ m;
					tmp_y = ((dist - ro) * Math.sin(angle - 1.5 * Math.PI) + (ro
							+ m - dist)
							* Math.sin(angle - Math.PI))
							/ m;
					theta_d = Math.atan2(tmp_y, tmp_x);
				}
				// modify theta_d to avoid it with CCW
				// direction
				else {
					// make smooth transition near the obstacle
					// boundary
					tmp_x = ((dist - ro) * Math.cos(angle - 0.5 * Math.PI) + (ro
							+ m - dist)
							* Math.cos(angle - Math.PI))
							/ m;
					tmp_y = ((dist - ro) * Math.sin(angle - 0.5 * Math.PI) + (ro
							+ m - dist)
							* Math.sin(angle - Math.PI))
							/ m;
					theta_d = Math.atan2(tmp_y, tmp_x);
				}
			} else {
				// modify theta_d to avoid it with CW direction
				if (diff_angle > 0.) {
					theta_d = Math.abs(Math.atan((ro + m)
							/ Math.sqrt(dist * dist - (ro + m) * (ro + m))))
							+ angle;
				}
				// modify theta_d to avoid it with CCW direction
				else {
					theta_d = -Math.abs(Math.atan((ro + m)
							/ Math.sqrt(dist * dist - (ro + m) * (ro + m))))
							+ angle;
				}
			}
		}
		return theta_d;
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

		// stop if the ball goes in either goal
		// if (ball.getCoors().x <= GOAL_LEFT - 10 || ball.getCoors().x >=
		// GOAL_RIGHT) {
		// nxt.celebrate();
		// System.out.println("Goal scored! Stopping..");
		// robotMoving = false;
		// }

		// get info on robots
		if (weAreBlueTeam) {
			nxt.updateRobotDetails(pitchInfo.getBlueBot());
			opponentRobot = pitchInfo.getYellowBot();
		} else {
			nxt.updateRobotDetails(pitchInfo.getYellowBot());
			opponentRobot = pitchInfo.getBlueBot();
		}

	}

	boolean hasBall() {
		int dist = (int) Tools.getDistanceBetweenPoint(nxt.getCoors(), ball
				.getCoors());
		int a = (int) Math.toDegrees(getAngleToFacePoint(ball.getCoors()));
		return dist < 45 && a < 30 && a > -30;
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
			double distance = 50;
			double xvalue = distance * Math.sqrt(1 + Math.pow(gradient, 2));
			if (!shootingLeft) {
				xvalue = -1 * xvalue;
			}
			c.x = (int) ball.x + (int) xvalue;
			c.y = (int) (ball.y + gradient * xvalue);
		} else {
			return ball;
		}
		if(c.y > WALL_BOTTOM || c.y < WALL_TOP)
			return ball;
			
		return c;

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

	/**
	 * 
	 * @param from
	 * @param to
	 * @param obstacle
	 * @return Returns true if obstacle is in the way between points from and
	 *         point to
	 */
	public boolean isInTheWay(Point from, Point to, Point obstacle) {
		return Line2D.ptLineDist(from.x, from.y, to.x, to.y, obstacle.x,
				obstacle.y) < 50;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param obstacle
	 * @param size
	 *            optional minimum clearance
	 * @return
	 */
	public boolean isInTheWay(Point from, Point to, Point obstacle, int size) {
		return Line2D.ptLineDist(from.x, from.y, to.x, to.y, obstacle.x,
				obstacle.y) < size;
	}
}


class Expected extends Thread
{
	intercept inter;
	public Expected(intercept inter)
	{
		this.inter = inter;
		start();
	}
	
	@Override
	public void run() {
		
		if (inter.showTracking)
		{
		Tools.rest(500);
		
		inter.expectedPos = inter.ball.getExpectedPosition(1000);
		inter.vision.dropAllLines();
		
		int colors[] = new int[] {150,200,250,255};
		
		for (int i = 0; i <= 500; i+=100)
		{
			Point expected = inter.ball.getExpectedPosition(i);
//			Point expected = inter.ball.getAveragePoint().toPoint();
//			Tools.printCoors("expected i " + i + "  ", expected);
			int tmpx = expected.x;
			int tmpy = 480 - expected.y;
			int colorToDraw = colors[(int) Math.floor(i / 1000f)];
			
			
			inter.vision.addLine(new Point(tmpx - 10, tmpy - 10), new Point(
					tmpx + 10, tmpy + 10), new Color(colorToDraw, 0, 0).getRGB());
			inter.vision.addLine(new Point(tmpx + 10, tmpy - 10), new Point(
					tmpx - 10, tmpy + 10), new Color(colorToDraw, 0, 0).getRGB());
		}
		new Expected(inter);
		}
	}
}