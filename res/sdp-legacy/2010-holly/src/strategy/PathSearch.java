package strategy;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import vision.Vision;

/**
 *
 * @author Joe Tam
 * @author Michael Wiseman
 *
 * PathSearch2 implements the use of grids and search for a possible path
 *
 *		34 grids (w) x 17 grids (h)
 *
 *		Each grid:
 *		|-----------|
 *		|			|
 *		|			|
 *		|	21x22	|	22px
 *		|			|
 *		|-----------|
 *			 21px
 *
 * List of available commands
 *	Type 1: forward
 *	Type 2: backward
 *	Type 3: strafe left
 *	Type 4: strafe right
 *	Type 5: rotate
 *	Type 6: adjust forward/backward
 *	Type 7: adjust strafing
 *	Type 8: kick
 *	Type 9: stop
 *
 *	COMMAND format: [command type,command value,angle to turn,way-point x,way-point y, speed]
**/

public class PathSearch {

	private static Dimension robotDimension = new Dimension(54,60);

	public static GridPoint ourGridPosition;
	public static GridPoint oppGridPosition;
	public static GridPoint ballGridPosition;
	public static int ourAngle = 0;
	public static int oppAngle;

	//dimensions from vision are 752 * 418
	//pathsearch uses 760 * 420 for convenient integer divisions
	//Divide pitch into a 38x21 grid (each grid is 20 x 20)

//	public static int PITCH_HEIGHT = 374;
//	public static int PITCH_WIDTH = 714;
	public static int PITCH_WIDTH = Vision.PITCH_END_X - Vision.PITCH_START_X;
	public static int PITCH_HEIGHT = Vision.PITCH_END_Y - Vision.PITCH_START_Y;

	private static int widthInGrids = 38;
	private static int heightInGrids = 21;
	private static int gridWidth = PITCH_WIDTH/widthInGrids;
	private static int gridHeight = PITCH_HEIGHT/heightInGrids;
	private static GridPoint startGridPoint;
	private static GridPoint endGridPoint;
	private static GridPointComparator comparator = new GridPointComparator();

	private static Point startCoorPoint;
	private static Point endCoorPoint;
	private static boolean strafingEnabled;

	private static ArrayList<GridPoint> path;
	private static ArrayList<GridPoint> validGrids;
	private static ArrayList<GridPoint> invalidGrids;

	private final static int LEFT = 0;
	private final static int RIGHT = 1;

	public static int ourSide;

	//return a list of coordinates to go through
	//public static ArrayList<int[]> getPath(int startAngle,Point startPt, Point endPt) {
	public static ArrayList<int[]> getPath2(Point _ballPosition, Point _ourPosition, int _ourAngle, Point _oppPosition, int _oppAngle, int side) {

		ourSide = side;

		//record the start and end points in coordinates for adjusting the path
		startCoorPoint = _ourPosition;
		oppGridPosition = translateCoordinatesToGrid(_oppPosition);
		ourGridPosition = translateCoordinatesToGrid(_ourPosition);
		ballGridPosition = translateCoordinatesToGrid(_ballPosition);
		endCoorPoint = _ballPosition;
		ourAngle = _ourAngle;
		oppAngle = _oppAngle;

		path = new ArrayList<GridPoint>();
		validGrids = new ArrayList<GridPoint>();
		invalidGrids = new ArrayList<GridPoint>();

		startGridPoint = translateCoordinatesToGrid(_ourPosition);
		endGridPoint = translateCoordinatesToGrid(_ballPosition);

		validGrids.add(startGridPoint);
		search(startGridPoint,endGridPoint);

		path = optimisePath(path);

		ArrayList<Point> waypoints = translateGridsToCoordinates(path);
		ArrayList<int[]> commands = waypointsToCommands(waypoints);

		return optimizeCommands(commands);
	}

	private static ArrayList<int[]> waypointsToCommands(ArrayList<Point> waypoints){

		ArrayList<int[]> commands = new ArrayList<int[]>();

		for (int i = 0; i < path.size() - 1; i++) {
			int[] distanceAndAngle = getDistanceAndAngle(waypoints.get(i), waypoints.get(i+1));
			int[] cmd = new int[7];
			cmd[0] = 1;									//command type, set to forward temporarily
			cmd[1] = distanceAndAngle[0];				//command value
			cmd[2] = distanceAndAngle[1];				//angle to turn
			cmd[3] = (int) waypoints.get(i+1).getX();	//way-point x
			cmd[4] = (int) waypoints.get(i+1).getY();	//way-point y
			cmd[5] = 600;								//command speed
			cmd[6] = 600;								//turning speed
			commands.add(cmd);
		}
		return commands;
	}

	private boolean nearWall(GridPoint robotPosition) {


		return false;
	}

	private static ArrayList<GridPoint> optimisePath(ArrayList<GridPoint> path) {

		ArrayList<GridPoint> newPath = path;
		for (int i = 0; i < newPath.size() - 1; i++) {
			//remove points that are too close to each other
			if (newPath.get(i).distance(newPath.get(i+1)) < 5) {
				newPath.remove(i+1);
			}
		}

		//optimise angles repeatedly 3 times
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < newPath.size() - 2; i++) {
				//remove points that hardly change in gradient
				if (Math.abs(  (SFunctions.getAngle(newPath.get(i), newPath.get(i+1))) -
						(SFunctions.getAngle(newPath.get(i+1), newPath.get(i+2)))) < 30)
					newPath.remove(i+2);
			}
		}

		return newPath;
	}

	private static void search(GridPoint currentPoint, GridPoint endPoint) {

		//go through each of the 8 adjacent squares to the currentPoint
		for (int x = currentPoint.x-1; x < currentPoint.x + 2; x++) {
			for (int y = currentPoint.y-1;y< currentPoint.y + 2; y++) {

				GridPoint pt = new GridPoint(x,y);
				//check whether grid is on the "blacklist"
				if (!invalidGrids.contains(pt)) {
					//check in range of grids
					if (x > 0 && y > 0 && x <= widthInGrids && y <= heightInGrids) {
						//if it's not already on check list, add it
						if (!validGrids.contains(pt)) {
							validGrids.add(pt);
							pt.setParent(currentPoint);
							if((pt.x==endGridPoint.x)&&(pt.y==endGridPoint.y)){
								endGridPoint.setParent(currentPoint);
							}
							pt.setMovementCost(pt.getParent().getMovementCost() + calcMovementCost(currentPoint,pt));
							pt.setHeuristicCost(calcHeuristicCost(pt,endPoint));
							pt.setTotalCost(pt.getMovementCost() + pt.getHeuristicCost());
						}
						if (validGrids.contains(pt)) {
							if (pt.getMovementCost() > calcMovementCost(currentPoint,pt)) {
								pt.setParent(currentPoint);
								pt.setMovementCost(pt.getParent().getMovementCost() + calcMovementCost(currentPoint,pt));
								pt.setTotalCost(pt.getMovementCost() + pt.getHeuristicCost());
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
			if((closestPt.x==endGridPoint.x)&&(closestPt.y==endGridPoint.y)){
				tracePath(startGridPoint,endGridPoint);
			} else {
				invalidGrids.add(closestPt);
				search(closestPt,endPoint);
			}
		} else return;
	}

	private static int calcMovementCost(GridPoint currentPoint, GridPoint newPoint) {
		if (oppGridPosition.distance(newPoint) < 3) {
			//discourage it heavily, to not crash into opponent
			return 500;
		}
		if (ourSide == LEFT) {
			if (Math.abs(newPoint.y - ballGridPosition.y) < 5 && newPoint.x >= ballGridPosition.x)
				return 65;
		}
		if (ourSide == RIGHT) {
			if (Math.abs(newPoint.y - ballGridPosition.y) < 5 && newPoint.x <= ballGridPosition.x)
				return 65;
		}
		if (oppGridPosition.distance(newPoint) < 6) {
			//discourage points that are quite close to the opponent
			return 30;
		}
		if (Math.abs(oppGridPosition.y - newPoint.y) < 5) {
			return 18;
		}
//		if (oppGridPosition.distance(newPoint) < 7 && Math.abs(oppGridPosition.y - newPoint.y) < 5) {
//			return 35;
//		}
		//horizontal and vertical movements
		if (Math.abs(newPoint.x - currentPoint.x) + Math.abs(newPoint.y - currentPoint.y) == 1) {
			return 10;
		}

		//diagonal movements
		if (Math.abs(newPoint.x - currentPoint.x) + Math.abs(newPoint.y - currentPoint.y) == 2) {
			return 14;
		}

		if (strafingEnabled == true) {

		}

		return 0;
	}

	//Manhattan distance
	private static int calcHeuristicCost(GridPoint currentPoint, GridPoint endPoint) {
		return 10 * (Math.abs(endPoint.x - currentPoint.x) + Math.abs(endPoint.y - currentPoint.y));
	}

	private static void tracePath(GridPoint startPoint,GridPoint endPoint) {
		path.add(0,endPoint);
		if (endPoint.getParent() != null) {
			tracePath(startPoint,endPoint.getParent());
		}
	}

	//takes two coordinates and get the distance and angle between them
	private static int[] getDistanceAndAngle(Point currentCoord, Point nextCoord) {
		int[] distanceAndAngle = new int[2];

		int angleToTurn = (int) Math.toDegrees(Math.atan2((currentCoord.y - nextCoord.y), (nextCoord.x - currentCoord.x))) - ourAngle;
        if (angleToTurn < -180) {angleToTurn = 360 + angleToTurn;}
        if (angleToTurn > 180) {angleToTurn = angleToTurn - 360;}
        int distance = (int) currentCoord.distance(nextCoord);

        //only generate a command if the change is significant enough
        if (Math.abs(distance) > 2 || Math.abs(angleToTurn) > 2) {
	        //CAUTION: this line might cause trouble in the real system
	        //this is to calculate the relative angle to turn between each waypoint
			ourAngle = (int) ourAngle + angleToTurn;

	        distanceAndAngle[0] = distance;
	        distanceAndAngle[1] = angleToTurn;
        }
		return distanceAndAngle;
	}

	private static GridPoint translateCoordinatesToGrid(Point pt) {
		int gridX = (int) Math.ceil(pt.getX() / gridWidth);
		int gridY = (int) Math.ceil(pt.getY() / gridHeight);
		GridPoint gridPoint = new GridPoint(gridX,gridY);
		return gridPoint;
	}

	public static ArrayList<Point> translateGridsToCoordinates(ArrayList<GridPoint> foundPath) {
		ArrayList<Point> coordinateList = new ArrayList<Point>();
		//add starting coordinate to the list
		coordinateList.add(0, startCoorPoint);
		//translate every gridpoint except the first and last into coordinates
		if (foundPath.size() > 1) {
			for (int i = 1; i < foundPath.size()-1; ++i) {
				GridPoint gp = foundPath.get(i);
				Point gridMidPoint = new Point();
				gridMidPoint.x = (int) ((gp.getX()-1) * gridWidth) + gridWidth/2;
				gridMidPoint.y = (int) ((gp.getY()-1) * gridHeight) + gridHeight/2;
				coordinateList.add(gridMidPoint);
			}
		}
		//add end coordinate to the list
		coordinateList.add(coordinateList.size(), endCoorPoint);
		return coordinateList;
	}

	private static ArrayList<int[]> optimizeCommands(ArrayList<int[]> oldCommands){
		ArrayList<int[]> newCommands = new ArrayList<int[]>();

		int[] lastCommand = null;
		int[] currentCommand;

		if(oldCommands.size()>1){
			lastCommand = oldCommands.get(0);
			for(int i = 1; i<oldCommands.size(); i++){
				currentCommand = oldCommands.get(i);
				if((currentCommand[0]==lastCommand[0]) && ((currentCommand[0]==1) ||  (currentCommand[0]==2) ||  (currentCommand[0]==3) || (currentCommand[0]==4) ) && (currentCommand[2] ==0 )             ){
					lastCommand[1] = lastCommand[1] + currentCommand[1];
					lastCommand[3] = currentCommand[3];
					lastCommand[4] = currentCommand[4];
				}
				else{
					newCommands.add(lastCommand);
					lastCommand = currentCommand;
				}
			}
			newCommands.add(lastCommand);
			return newCommands;
		}
		//newCommands.add(lastCommand);
		return oldCommands;
	}

	public static ArrayList<GridPoint> getInvalid(){
        return invalidGrids;
    }

	public static void main(String args[]) {

		Point ourPosition = new Point(600,140);
        Point oppPosition = new Point (289,307);
        Point ballPosition = new Point(500,140);
		int ourAngle = 94;
		int oppAngle = 0;
		ArrayList<int[]> commands = getPath2(ballPosition, ourPosition, ourAngle, oppPosition, oppAngle, RIGHT);
		for (int i = 0; i < commands.size(); i++) {
			System.out.println("cmd type:" + commands.get(i)[0] + " cmd value:" + commands.get(i)[1] + " angle:" +  commands.get(i)[2] + " x:" +  commands.get(i)[3] + " y:" + commands.get(i)[4] + " speed:" + commands.get(i)[5] );
		}
	}
}
