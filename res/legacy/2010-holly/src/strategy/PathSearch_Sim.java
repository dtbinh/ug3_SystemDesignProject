package strategy;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * 
 * @author Joe
 * PathSearch2 implements the use of grids and search for a possible path
 *
 *
 *		|-----------------------------------------------|
 *		|		|		|		|		|		|		|
 *		| (1,1)	| (2,1)	| (3,1)	| (4,1)	| (5,1)	| (6,1)	|
 *		|		|		|		|		|		|		|
 *		|-------|-------|-------|-------|-------|-------|
 *		|		|		|		|		|		|		|
 *		| (1,2)	| (2,2)	| (3,2)	| (4,2)	| (5,2)	| (6,2)	|
 *		|		|		|		|		|		|		|
 *		|-------|-------|-------|-------|-------|-------|
 *		|		|		|		|		|		|		|
 *		| (1,3)	| (2,3)	| (3,3)	| (4,3)	| (5,3)	| (6,3)	|
 *		|		|		|		|		|		|		|
 *		|-----------------------------------------------|
 *
 * List of available commands
 *	Type 1: forward
 *	Type 2: backward
 *	Type 3: turn left
 *	Type 4: turn right
 *	Type 5: strafe left
 *	Type 6: strafe right
**/

public class PathSearch_Sim implements Comparator{
	
	static float startAngle = 10;
	
	static Point ballPosition;
	static Point ourRobotPosition;
	static Point oppRobotPosition;
	static Dimension robotDimension = new Dimension(54,60);
	
	static int ourRobotAngle = 0;
	static int oppRobotAngle;

	public final int PITCH_WIDTH = 366;
	public final int PITCH_LENGTH = 732;
	
	//Divide pitch into a 3x6 grid (each grid is 122 x 122)
	public static int lengthInGrids = 8;
	public static int widthInGrids = 4;
	public int gridWidth = PITCH_WIDTH/widthInGrids;
	public int gridLength = PITCH_LENGTH/lengthInGrids;
	public GridPoint startP;
	public GridPoint endP;
	public GridPoint invP;
	
	ArrayList<GridPoint> path;
	ArrayList<GridPoint> validGrids;
	ArrayList<GridPoint> invalidGrids;
	
	
	//returning a list of gridpoints because the coordinate might be over two grids	
	//public ArrayList<GridPoint> translateCoordinatesToGrid(Point pt) {
	public GridPoint translateCoordinatesToGrid(Point pt) {
		//ArrayList<GridPoint> gridPoints = new ArrayList<GridPoint>();
		
		int gridX = (int) Math.ceil(pt.getX() / gridLength);
		int gridY = (int) Math.ceil(pt.getY() / gridWidth);
		GridPoint gridPoint = new GridPoint(gridX,gridY);
		return gridPoint;
	}
	
	//return a list of coordinates to go through
	public ArrayList<int[]> getPath(int startAngle,Point startPt, Point endPt,Point invalid) {
		ourRobotAngle = startAngle;
		
		GridPoint startPoint = translateCoordinatesToGrid(startPt);
		System.out.println("start grid point: " + startPoint);
		GridPoint endPoint = translateCoordinatesToGrid(endPt);
		System.out.println("end grid point: " + endPoint);
		GridPoint invalidPoint = translateCoordinatesToGrid(invalid);
		invP = invalidPoint;
		
		path = new ArrayList<GridPoint>();
		validGrids = new ArrayList<GridPoint>();
		invalidGrids = new ArrayList<GridPoint>();
		this.startP = startPoint;
		this.endP = endPoint;
		
		validGrids.add(startPoint);
		invalidGrids.add(invalidPoint);
//		invalidGrids.add(new GridPoint(3,3));
//		invalidGrids.add(new GridPoint(4,3));
//		invalidGrids.add(new GridPoint(3,2));
		search(startPoint,endPoint);	
		return translateCommands(translateGridToCoordinates(path));
	}
	
	public void search(GridPoint currentPoint, GridPoint endPoint) {
		
//		//DEBUG
//		System.out.println("search start: parent of endpoint is: "+endPoint.getParent());
//		System.out.println("open list:");
//		System.out.println(validGrids);
//		for (int i = 0; i < validGrids.size(); ++i) {
//			System.out.println(validGrids.get(i).getTotalCost() + "," + validGrids.get(i).getMovementCost() + "," + validGrids.get(i).heuristicCost);
//		}
//		System.out.println("closed list:");
//		System.out.println(invalidGrids);
//		for (int i = 0; i < invalidGrids.size(); ++i) {
//			System.out.println(invalidGrids.get(i).getTotalCost() + "," + invalidGrids.get(i).getMovementCost() + "," + invalidGrids.get(i).heuristicCost);
//		}
//		
		//go through each of the 8 adjacent squares to the currentPoint
		for (int x = currentPoint.x-1; x < currentPoint.x + 2; x++) {
			for (int y = currentPoint.y-1;y< currentPoint.y + 2; y++) {

				GridPoint pt = new GridPoint(x,y);
				//check whether grid is on the "blacklist"
				if (!invalidGrids.contains(pt)) {
					//check in range of grids
					if (x > 0 && y > 0 && x <= lengthInGrids && y <= widthInGrids) {
						//if it's not already on check list, add it
						if (!validGrids.contains(pt)) {
							validGrids.add(pt);
							pt.setParent(currentPoint);
							if((pt.x==endP.x)&&(pt.y==endP.y)){
								endP.setParent(currentPoint);
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
		
		Collections.sort(validGrids, this);
		System.out.println("sorted list: " + validGrids);
		GridPoint closestPt = validGrids.get(0);
		System.out.println("closest pt based on Total Cost is " + closestPt);
		validGrids.remove(closestPt);
		if((closestPt.x==endP.x)&&(closestPt.y==endP.y)){
			System.out.println("found path!!");
			System.out.println("parent of endpoint is: "+endP.getParent());
			tracePath(startP,endP);
//		} else if (validGrids.size() == 0){				//WRONG
//			System.out.println("no path found!!!!!!!!!!");
//			System.out.println("no path found!!!!!!!!!!");
//			System.out.println("no path found!!!!!!!!!!");
//			System.out.println("no path found!!!!!!!!!!");
//			System.out.println("no path found!!!!!!!!!!");
		} else {
			invalidGrids.add(closestPt);
			System.out.println("another try: closest found: "+closestPt+"parent: "+closestPt.getParent()+"   edn:"+endPoint);
			
			search(closestPt,endPoint);
		}
	}
	
	public int calcMovementCost(GridPoint currentPoint, GridPoint newPoint) {
		if (Math.abs(newPoint.x - currentPoint.x) + Math.abs(newPoint.y - currentPoint.y) == 1) {
			System.out.println("horizontal/vertical movement");
			return 10;
		}
		if (Math.abs(newPoint.x - currentPoint.x) + Math.abs(newPoint.y - currentPoint.y) == 2) {
			if(   ((Math.abs(newPoint.x - invP.x))+(Math.abs(newPoint.y - invP.y))==1)&&
				( ((Math.abs(currentPoint.x - invP.x))+(Math.abs(currentPoint.y - invP.y))==1) )){
				return 21;
			}
				
				//Math.abs((foundPath.get(i).x - foundPath.get(i+1).x)) +
				//Math.abs((foundPath.get(i).y - foundPath.get(i+1).y));
			System.out.println("diagonal movement");
			return 14;
		}
		System.out.println("can't obtain movement cost between " + currentPoint + "&" + newPoint);
		return 0;
	}
	
	//Manhattan distance
	public int calcHeuristicCost(GridPoint currentPoint, GridPoint endPoint) {
		return 10 * (Math.abs(endPoint.x - currentPoint.x) + Math.abs(endPoint.y - currentPoint.y));
	}
	
	public void tracePath(GridPoint startPoint,GridPoint endPoint) {
		//System.out.println("item"+endPoint);
		path.add(0,endPoint);
		if (endPoint.getParent() != null) {
			tracePath(startPoint,endPoint.getParent());
		}
	}


	public int compare(Object o1, Object o2) {
		if (((GridPoint) o1).getTotalCost() < ((GridPoint) o2).getTotalCost())
			return -1;
		else
			return 1;
	}
	
	public ArrayList<int[]> translateCommandsList(ArrayList<GridPoint> foundPath) {
		ArrayList<int[]> commands = new ArrayList<int[]>();
		for (int i = 0; i < foundPath.size() - 1; i++) {
			//translate individual command
			
			int manhattanDist = Math.abs((foundPath.get(i).x - foundPath.get(i+1).x)) +
								Math.abs((foundPath.get(i).y - foundPath.get(i+1).y));
			
			//translateGridCoordinate()
//			int currentX = (int) foundPath.get(i).getX();
//			int currentY = (int) foundPath.get(i).getY();
//			int nextX = (int) foundPath.get(i+1).getX();
//			int nextY = (int) foundPath.get(i+1).getY();
//			
//			
//			if (currentY == nextY) {
//				if (nextX > currentX) {
//					
//				}
//			}
		}
		return commands;
	}
	
	public ArrayList<int[]> translateCommands(ArrayList<Point> coordinates) {
		ArrayList<int[]> commands = new ArrayList<int[]>();
		for (int i = 0; i < coordinates.size() - 1; i++) {
			Point currentCoord = coordinates.get(i);
			Point nextCoord = coordinates.get(i+1);
			
			float angle;
			//deal with exceptional cases, zero division, invalid trigonometry values etc.
			if (currentCoord.x == nextCoord.x) {
				if (currentCoord.y < nextCoord.y)
					angle = 90;
				else
					angle = 180;
			} else {
				angle = (float) (Math.toDegrees((Math.atan2((nextCoord.y-currentCoord.y),(nextCoord.x-currentCoord.x)))));
			}

			System.out.println("angle between start and end point " + angle);
			float turnAngle = angle - ourRobotAngle;
			
			
			if (turnAngle < 0)
				turnAngle = 360 + turnAngle;
			
			ourRobotAngle = (int) angle;
			
			//TURNING
			//only turn if the required turning angle is more than 1 degree
			if (Math.abs(turnAngle) > 1) {
				int[] turnCmd = new int[2];
				if (turnAngle < 180) {
					//turn right
					turnCmd[0] = 4;
					turnCmd[1] = (int) turnAngle;
				} else {
					//turn left
					turnCmd[0] = 3;
					turnCmd[1] = 360 - (int) turnAngle;
				}
				commands.add(turnCmd);
			}
			
			
			
			/*
			//TURNING
			//only turn if the required turning angle is more than 1 degree
			if (Math.abs(turnAngle) > 1) {
				int[] turnCmd = new int[2];
				if (angle < 0) {
					//turn left
					turnCmd[0] = 3;
					turnCmd[1] = Math.abs((int)turnAngle);
				} else {
					//turn right
					turnCmd[0] = 4;
					turnCmd[1] = Math.abs((int)turnAngle);
				}
				commands.add(turnCmd);
			}
			*/
			
			
			//FORWARD
			int distance = (int) currentCoord.distance(nextCoord);
			if (distance > 1) {
				int[] fwdCmd = new int[2];
				fwdCmd[0] = 1;
				fwdCmd[1] = distance;
				commands.add(fwdCmd);
			}
			
		}
		return commands;
	}
	
	public ArrayList<Point> translateGridToCoordinates(ArrayList<GridPoint> foundPath) {
		ArrayList<Point> coordinateList = new ArrayList<Point>();
		
		for (int i = 0; i < foundPath.size(); ++i) {
			GridPoint gp = foundPath.get(i);
			Point gridMidPoint = new Point();
			System.out.println(gp.getX() - 1);
			System.out.println(gridWidth);
			System.out.println(gridWidth/2);
			System.out.println((gp.getX()-1 * gridWidth) + gridWidth/2);
			gridMidPoint.x = (int) ((gp.getX()-1) * gridWidth) + gridWidth/2;
			gridMidPoint.y = (int) ((gp.getY()-1) * gridLength) + gridLength/2;
			coordinateList.add(gridMidPoint);
		}
		return coordinateList;
	}
	
	public static void main(String args[]) {
		PathSearch ps = new PathSearch();
		Point startPoint = new Point(10,30);
		Point endPoint = new Point(155,300);
		//ArrayList<int[]> commands = ps.getPath(0,startPoint, endPoint);
//		ArrayList<Point> coordinates = ps.translateGridToCoordinates(foundPath);
//		ArrayList<int[]> commands = ps.translateCommands(coordinates);
		//System.out.println(foundPath);
		//System.out.println("COORDINATES: " + coordinates);
		//for (int i = 0; i < commands.size(); i++) {
		//	System.out.println("COMMAND" + i + ": " + commands.get(i)[0] + " ,value: " + commands.get(i)[1]);
		//}
		//System.out.println("COMMANDS: " + commands);
		//System.out.println(ps.translateCommands(ps.translateGridToCoordinates(ps.getPath(startPoint, endPoint))));
	}
}
