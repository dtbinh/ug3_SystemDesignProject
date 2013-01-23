package Planning;

import java.awt.Point;
import java.util.ArrayList;

import Vision.ImageProcessor;

/**
 * A* path planner. Partitions the pitch into many grids
 * to find the "least cost" path between two points.
 * 
 * Avoids the opponent.
 * Blocks the opponent.
 * Does not plan points outside pitch.
 * Attempts to avoid own goal.
 * Attempts to align to shoot.
 * 
 * getNextWaypoint returns the next waypoint in the path
 * getPath returns the entire path
 * 
 * Both take as parameter the angle in DEGREES.
 **/

public class PathSearch {

	public static GridPoint ourGridPosition;
	public static GridPoint oppGridPosition;
	public static GridPoint ballGridPosition;
	public static int ourAngle = 0;

	private static int PITCH_WIDTH = ImageProcessor.xupperlimit
			- ImageProcessor.xlowerlimit;
	private static int PITCH_HEIGHT = ImageProcessor.yupperlimit
			- ImageProcessor.ylowerlimit - 25;
	// this above is roughly 630 x 335

	private static int displacementX = ImageProcessor.xlowerlimit;
	private static int displacementY = ImageProcessor.ylowerlimit + 25;

	// ideally, we want squares and we want them relatively small
	// default is 21x21 for fast computation 30 & 16
	private static int widthInGrids = (int) (30 * 1.5);
	private static int heightInGrids = (int) (16 * 1.5);
	private static int gridWidth = (int) Math.round((double) PITCH_WIDTH
			/ widthInGrids);
	private static int gridHeight = (int) Math.round((double) PITCH_HEIGHT
			/ heightInGrids);
	private static GridPoint startGridPoint;
	private static GridPoint endGridPoint;
	private static GridPointComparator comparator = new GridPointComparator();

	private static Point startCoorPoint;
	private static Point endCoorPoint;

	private static ArrayList<GridPoint> path;
	private static ArrayList<GridPoint> validGrids;
	private static ArrayList<GridPoint> invalidGrids;

	private final static int LEFT = 0;
	private final static int RIGHT = 1;

	public static int ourSide;

	// return a list of Points to go through
	public static ArrayList<Point> getPath(Point _aimPosition,
			Point _ourPosition, int _ourAngle, Point _obstaclePosition) {
		if (Main2.shootingLeft)
			ourSide = RIGHT;
		else
			ourSide = LEFT;

		// carve pitch and coordinate system, in order prevent any paths going
		// through a wall
		_ourPosition.x = _ourPosition.x - displacementX;
		_ourPosition.y = _ourPosition.y - displacementY;
		startCoorPoint = _ourPosition;
		_obstaclePosition.x = _obstaclePosition.x - displacementX;
		_obstaclePosition.y = _obstaclePosition.y - displacementY;
		_aimPosition.x = _aimPosition.x - displacementX;
		_aimPosition.y = _aimPosition.y - displacementY;

		// translation from carved pitch coordinate system to grids
		oppGridPosition = translateCoordinatesToGrid(_obstaclePosition);
		ourGridPosition = translateCoordinatesToGrid(_ourPosition);
		ballGridPosition = translateCoordinatesToGrid(_aimPosition);
		endCoorPoint = _aimPosition;
		ourAngle = _ourAngle;

		path = new ArrayList<GridPoint>();
		validGrids = new ArrayList<GridPoint>();
		invalidGrids = new ArrayList<GridPoint>();

		startGridPoint = translateCoordinatesToGrid(_ourPosition);
		endGridPoint = translateCoordinatesToGrid(_aimPosition);

		validGrids.add(startGridPoint);
		search(startGridPoint, endGridPoint);

		path = optimisePath(path);

		ArrayList<Point> waypoints = translateGridsToCoordinates(path);

		return waypoints;
	}

	public static Point getNextWaypoint(Point _ballPosition,
			Point _ourPosition, int _ourAngle, Point _oppPosition) {
		if (Main2.shootingLeft)
			ourSide = RIGHT;
		else
			ourSide = LEFT;

		// carve pitch and coordinate system, in order prevent any paths going
		// through a wall
		_ourPosition.x = _ourPosition.x - displacementX;
		_ourPosition.y = _ourPosition.y - displacementY;
		startCoorPoint = _ourPosition;
		_oppPosition.x = _oppPosition.x - displacementX;
		_oppPosition.y = _oppPosition.y - displacementY;
		_ballPosition.x = _ballPosition.x - displacementX;
		_ballPosition.y = _ballPosition.y - displacementY;

		// translation from carved pitch coordinate system to grids
		oppGridPosition = translateCoordinatesToGrid(_oppPosition);
		ourGridPosition = translateCoordinatesToGrid(_ourPosition);
		ballGridPosition = translateCoordinatesToGrid(_ballPosition);
		endCoorPoint = _ballPosition;
		ourAngle = _ourAngle;

		path = new ArrayList<GridPoint>();
		validGrids = new ArrayList<GridPoint>();
		invalidGrids = new ArrayList<GridPoint>();

		startGridPoint = translateCoordinatesToGrid(_ourPosition);
		endGridPoint = translateCoordinatesToGrid(_ballPosition);

		validGrids.add(startGridPoint);
		search(startGridPoint, endGridPoint);

		path = optimisePath(path);

		ArrayList<Point> waypoints = translateGridsToCoordinates(path);

		return waypoints.get(1);
	}

	private static ArrayList<GridPoint> optimisePath(ArrayList<GridPoint> path) {

		ArrayList<GridPoint> newPath = path;
		for (int i = 0; i < newPath.size() - 1; i++) {
			// remove points that are too close to each other
			if (newPath.get(i).distance(newPath.get(i + 1)) < 3) {
				newPath.remove(i + 1);
			}
		}

		// optimise angles repeatedly 3 times
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < newPath.size() - 2; i++) {
				// remove points that hardly change in gradient
				if (Math.abs((getAngle(newPath.get(i), newPath.get(i + 1)))
						- (getAngle(newPath.get(i + 1), newPath.get(i + 2)))) < 30)
					newPath.remove(i + 2);
			}
		}

		return newPath;
	}

	private static void search(GridPoint currentPoint, GridPoint endPoint) {

		// go through each of the 8 adjacent squares to the currentPoint
		for (int x = currentPoint.x - 1; x < currentPoint.x + 2; x++) {
			for (int y = currentPoint.y - 1; y < currentPoint.y + 2; y++) {

				GridPoint pt = new GridPoint(x, y);
				// check whether grid is on the "blacklist"
				if (!invalidGrids.contains(pt)) {
					// check in range of grids
					if (x > 0 && y > 0 && x <= widthInGrids
							&& y <= heightInGrids) {
						// if it's not already on check list, add it
						if (!validGrids.contains(pt)) {
							validGrids.add(pt);
							pt.setParent(currentPoint);
							if ((pt.x == endGridPoint.x)
									&& (pt.y == endGridPoint.y)) {
								endGridPoint.setParent(currentPoint);
							}
							pt.setMovementCost(pt.getParent().getMovementCost()
									+ calcMovementCost(currentPoint, pt));
							pt
									.setHeuristicCost(calcHeuristicCost(pt,
											endPoint));
							pt.setTotalCost(pt.getMovementCost()
									+ pt.getHeuristicCost());
						}
						if (validGrids.contains(pt)) {
							if (pt.getMovementCost() > calcMovementCost(
									currentPoint, pt)) {
								pt.setParent(currentPoint);
								pt.setMovementCost(pt.getParent()
										.getMovementCost()
										+ calcMovementCost(currentPoint, pt));
								pt.setTotalCost(pt.getMovementCost()
										+ pt.getHeuristicCost());
							}
						}
					}
				}
			}
		}
		validGrids = comparator.sortGridPoints(validGrids);
		if (validGrids.size() > 0) {
			GridPoint closestPt = validGrids.get(0);

			validGrids.remove(closestPt);
			if ((closestPt.x == endGridPoint.x)
					&& (closestPt.y == endGridPoint.y)) {
				tracePath(startGridPoint, endGridPoint);
			} else {
				invalidGrids.add(closestPt);
				search(closestPt, endPoint);
			}
		} else
			return;
	}

	private static int calcMovementCost(GridPoint currentPoint,
			GridPoint newPoint) {
		if (oppGridPosition.distance(newPoint) < 3) {
			// discourage it heavily, to not crash into opponent
			return 500;
		}

		if (ourSide == LEFT) {
			if (Math.abs(newPoint.y - ballGridPosition.y) < 6
					&& newPoint.x >= ballGridPosition.x)
				return 65;
		}
		if (ourSide == RIGHT) {
			if (Math.abs(newPoint.y - ballGridPosition.y) < 6
					&& newPoint.x <= ballGridPosition.x)
				return 65;
		}
		if (oppGridPosition.distance(newPoint) < 5) {
			// discourage points that are quite close to the opponent
			return 30;
		}
		if (Math.abs(oppGridPosition.y - newPoint.y) < 5) {
			return 18;
		}
		// horizontal and vertical movements
		if (Math.abs(newPoint.x - currentPoint.x)
				+ Math.abs(newPoint.y - currentPoint.y) == 1) {
			return 10;
		}

		// diagonal movements
		if (Math.abs(newPoint.x - currentPoint.x)
				+ Math.abs(newPoint.y - currentPoint.y) == 2) {
			return 14;
		}

		return 0;
	}

	// Diagonal shortcut or Manhattan distance
	// Manhattan distance has the nice side effect of aiming for the goal
	private static int calcHeuristicCost(GridPoint currentPoint,
			GridPoint endPoint) {
		// int xDist = (Math.abs(currentPoint.x - endPoint.x));
		// int yDist = (Math.abs(currentPoint.y - endPoint.y));
		// if (xDist > yDist) {
		// return 14 * yDist + 10*(xDist - yDist);
		// } else {
		// return 14 * xDist + 10*(yDist - xDist);
		// }
		return 10 * (Math.abs(endPoint.x - currentPoint.x) + Math
				.abs(endPoint.y - currentPoint.y));
	}

	private static void tracePath(GridPoint startPoint, GridPoint endPoint) {
		path.add(0, endPoint);
		if (endPoint.getParent() != null) {
			tracePath(startPoint, endPoint.getParent());
		}
	}

	private static GridPoint translateCoordinatesToGrid(Point pt) {
		int gridX = (int) Math.ceil(pt.getX() / gridWidth);
		int gridY = (int) Math.ceil(pt.getY() / gridHeight);
		GridPoint gridPoint = new GridPoint(gridX, gridY);
		return gridPoint;
	}

	public static ArrayList<Point> translateGridsToCoordinates(
			ArrayList<GridPoint> foundPath) {
		ArrayList<Point> coordinateList = new ArrayList<Point>();
		// add starting coordinate to the list
		coordinateList.add(0, new Point(startCoorPoint.x + displacementX,
				startCoorPoint.y + displacementY));
		// translate every gridpoint except the first and last into coordinates
		if (foundPath.size() > 1) {
			for (int i = 1; i < foundPath.size() - 1; ++i) {
				GridPoint gp = foundPath.get(i);
				Point gridMidPoint = new Point();
				gridMidPoint.x = (int) ((gp.getX() - 1) * gridWidth)
						+ gridWidth / 2 + displacementX;
				gridMidPoint.y = (int) ((gp.getY() - 1) * gridHeight)
						+ gridHeight / 2 + displacementY;
				coordinateList.add(gridMidPoint);
			}
		}
		// add end coordinate to the list
		coordinateList.add(coordinateList.size(), new Point(endCoorPoint.x
				+ displacementX, endCoorPoint.y + displacementY));
		return coordinateList;
	}

	public static ArrayList<GridPoint> getInvalid() {
		return invalidGrids;
	}

	private static double getAngle(Point a, Point b) {
		return Math.toDegrees(Math.atan2((a.y - b.y), (b.x - a.x)));
	}
}