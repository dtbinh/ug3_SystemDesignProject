package behaviors;


//TODO: Integrate with planWithBall.


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


import PitchObject.Position;
import PitchObject.Robot;

import lejos.geom.Line;
import lejos.geom.Point;
import lejos.geom.Rectangle;
import lejos.robotics.mapping.LineMap;
import lejos.robotics.navigation.DestinationUnreachableException;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.AstarSearchAlgorithm;
import lejos.robotics.pathfinding.FourWayGridMesh;
import lejos.robotics.pathfinding.NodePathFinder;


public class PathPlanner {
	public LineMap preMap = new LineMap();
	private static Line [] obstacles = new Line[8];
	static Rectangle bounds;
	
	public PathPlanner(){ //TODO : Make these correct and in UNITS (Robot)
		obstacles [0] = new Line(0, 0, 0, 480);
		obstacles [1] = new Line(0, 0, 640, 0);
		obstacles [2] = new Line(640, 0, 640, 480);
		obstacles [3] = new Line(0,480, 640,480);
		bounds = new Rectangle(0, 0, 640, 480);
	}
	
	public void updateEnemyPos(Pose enemy){
		Point oPos = new Point(enemy.getLocation().x - 25, enemy.getLocation().y-25);
		double angle = enemy.getHeading();
		//create a square around the enemy;
		Point f1 = rotateBy(oPos, angle-45 );
		Point f2 =  rotateBy(oPos, angle+45 );
		Point f3 =  rotateBy(oPos, angle-135 );
		Point f4 =  rotateBy(oPos, angle+135 );
		obstacles [4] = new Line(f1.x,f1.y, f2.x,f2.y);
		obstacles [5] = new Line(f2.x,f2.y, f3.x,f3.y);
		obstacles [6] = new Line(f3.x,f3.y, f4.x,f4.y);
		obstacles [7] = new Line(f4.x,f4.y, f1.x,f1.y);
	}
	
	public ArrayList <Waypoint> calculatePath(Pose us, Pose goal){
		
		LineMap preMap = new LineMap(obstacles, bounds);
		//
		FourWayGridMesh grid = new FourWayGridMesh(preMap, 10,50);
		//TODO : make grid a hexagonal or octoginal grid.
		AstarSearchAlgorithm alg = new AstarSearchAlgorithm();
		NodePathFinder pf = new NodePathFinder(alg, grid);
	
		ArrayList <Waypoint> coll = new ArrayList<Waypoint>();
		try {
			coll = pf.findRoute(us, new Waypoint(goal));	
		} catch (DestinationUnreachableException e) {
			System.out.println("Destination blocked or something");
			coll.add(new Waypoint(goal));
		}
		return coll;
	}
	
	public void makeACoolSVG(ArrayList<Waypoint> points){
		ArrayList<Line> newlines = new ArrayList<Line>();
		for (int i = 0; i < obstacles.length; i++){
			newlines.add(obstacles[i]);
		}
		for (int i = 0; i < points.size(); i++){
			float currx = points.get(i).x;
			float curry = points.get(i).y;
			newlines.add(new Line(currx, curry, currx+10, curry+10));
		}
		LineMap preMap = new LineMap(newlines.toArray(new Line []{}), bounds);
		try {
			preMap.createSVGFile("pathmap.svg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Point rotateBy(Point p, double angle){
		float newx = (float) (50*Math.cos(angle) + p.x);
		float newy = (float) (50*Math.sin(angle) + p.y);
		return new Point (newx,newy);
	}
	
//	public ArrayList<Waypoint> reduceNodes(ArrayList<Waypoint> path){
//		float angle = 0;
//		ArrayList<Waypoint> newpath = new ArrayList<Waypoint>();
//		for (int i = 0; i < path.size()-1; i++){
//			System.out.println(path.get(i).toString());
//			//System.out.println(path.get(i).getPose().relativeBearing(path.get(i+1).getPose().getLocation()) );
//			Pose currentPose = path.get(i).getPose();
//			currentPose.setHeading(angle);
//			System.out.println(angle);
//			float newang = angle + currentPose.relativeBearing(path.get(i+1).getPose().getLocation());
//			if (angle != newang){ 
//			newpath.add(new Waypoint(currentPose));
//			}
//			angle = newang;
//		}
//	}
//	return newpath;

}
