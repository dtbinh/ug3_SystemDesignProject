package strategy;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;

import navigation.Navigation;

//@author Joe Tam

/** List of available commands
*	Type 1: forward
*	Type 2: backward
*	Type 3: turn left
*	Type 4: turn right
*	Type 5: strafe left
*	Type 6: strafe right
**/

//extending JFrame so the path can be visualised
public class PathSearch_old extends JFrame{

	static float startAngle = 10;
	
	static Point ballPosition;
	static Point ourRobotPosition;
	static Point oppRobotPosition;
	static Dimension robotDimension = new Dimension(54,60);
	
	static int ourRobotAngle;
	static int oppRobotAngle;
	
//	int opponentX;
//	int opponentY;
//	int opponentAngle;
//	int robotX;
//	int robotY;
//	static int robotAngle;
	static PathSearch_old instance;
	
	public PathSearch_old() {
		setSize(732,366);
		setVisible(true);
		ballPosition = new Point(100,100);
		ourRobotPosition = new Point(200,100);
		oppRobotPosition = new Point(300,50);
		ourRobotAngle = 30;
		oppRobotAngle = 180;
	}
	
    public static PathSearch_old getInstance() {
        if(instance == null) {
            instance = new PathSearch_old();
        }
        return instance;
    }
    
    public Shape getRobotShape(Point position, int angle) {
    	Rectangle rect = new Rectangle(position.x-(robotDimension.width/2),position.y-(robotDimension.height/2),
    								robotDimension.width,robotDimension.height);
    	AffineTransform t = new AffineTransform();
    	//Draw2D's 0 degree is at North, whereas our physics model is at East, adjust by 90 degrees to compensate
    	t.rotate(Math.toRadians(angle+90),position.x,position.y);
    	Shape newShape = t.createTransformedShape(rect);
    	Area newShapeArea = new Area(newShape);
    	
    	return newShape;
    }
    
	public static ArrayList<int[]> getPathCommands(float startAng, Point startPoint, Point endPoint) {
		
		//ArrayList<MoveCommand> cmds = new ArrayList<MoveCommand>();
		ArrayList<int[]> cmds = new ArrayList<int[]>();
		
		//search for possible solution to problem and produce list of commands
		//only makes use of turning, forward and backward at the moment (no strafing)
		//float angle = (float) ;
		
		//detect intersection with oppRobot
//		Line2D.Float collisionLine = new Line2D.Float(startPoint.x,startPoint.y,endPoint.x,endPoint.y);
//		
//		Rectangle2D.Float ourRobotShape_ = new Rectangle2D.Float(ourRobotPosition.x-(robotDimension.width/2),ourRobotPosition.y-(robotDimension.height/2),
//														robotDimension.width,robotDimension.height);
//		Rectangle2D.Float oppRobotShape_ = new Rectangle2D.Float(oppRobotPosition.x-(robotDimension.width/2),oppRobotPosition.y-(robotDimension.height/2),
//				robotDimension.width,robotDimension.height);
//
//		AffineTransform t = new AffineTransform();
//		t.rotate(Math.toRadians(ourRobotAngle));
//		Shape ourRobotShape = t.createTransformedShape(ourRobotShape_);
//		
//		
//		AffineTransform t2 = new AffineTransform();
//		t2.rotate(Math.toRadians(oppRobotAngle));
//		
//		//ourRobotShape.
//		
//		//TODO: use polygon instead to be able to deal with angles
//		
//		//Graphics2D oppRobotGraphics = new Graphics2D();
//		
//		//int[] xPoints = {oppRobotPosition.x}
//		Polygon oppRobot = new Polygon();
//		
		//collisionLine.intersects(oppRobot)
		
		
		
		////////////			DETECT WHETHER DIRECT PATH TO BALL IS AVAILABLE			////////////
		
		Line2D.Float midLine = new Line2D.Float(startPoint.x,startPoint.y,endPoint.x,endPoint.y);
		//Line2D.Float leftLine = new Line2D.Float(
		int leftStartPointX = ourRobotPosition.x - (int) (robotDimension.width/2 * Math.cos(Math.toRadians(ourRobotAngle)));
		int leftStartPointY = ourRobotPosition.y + (int) (robotDimension.width/2 * Math.sin(Math.toRadians(ourRobotAngle)));
		int leftEndPointX = oppRobotPosition.x - (int) (robotDimension.width/2 * Math.cos(Math.toRadians(ourRobotAngle)));
		int leftEndPointY = oppRobotPosition.y + (int) (robotDimension.width/2 * Math.sin(Math.toRadians(ourRobotAngle)));
		//int[] xPoints = {oppRobotPosition.x - 
		
		
		
		
		/**
		 * Start of Grid implementation
		 */
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		float angle;
		//deal with exceptional cases, zero division, invalid trigonometry values etc.
		if (startPoint.x == endPoint.x) {
			if (startPoint.y < endPoint.y)
				angle = 90;
			else
				angle = 180;
		} else {
			angle = (float) (Math.toDegrees((Math.atan2((endPoint.y-startPoint.y),(endPoint.x-startPoint.x)))));
		}

		System.out.println("angle between start and end point " + angle);
		
		float turnAngle = angle - ourRobotAngle;
		
		if (turnAngle < 0)
			turnAngle = 360 + turnAngle;
		
		System.out.println(turnAngle);
		
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
			cmds.add(turnCmd);
		}
		
		//FORWARD
		int distance = (int) startPoint.distance(endPoint);
		if (distance > 1) {
			int[] fwdCmd = new int[2];
			fwdCmd[0] = 1;
			fwdCmd[1] = distance;
			cmds.add(fwdCmd);
		}
		return cmds;
	}
	
	//TEST
	public static void main(String args[]) {
		PathSearch_old ps = new PathSearch_old();
		Point startPoint = new Point(100,20);
		Point endPoint = new Point(100,80);
		ArrayList<int[]> cmds = getPathCommands(0,startPoint,endPoint);
		for (int i = 0; i < cmds.size(); i++) {
			System.out.println(cmds.get(i)[0]);
			System.out.println(cmds.get(i)[1]);
		}
	}
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(0,0,0));
		paintRobot(g,ourRobotPosition,ourRobotAngle);
		paintRobot(g,oppRobotPosition,oppRobotAngle);
		//g2d.fill(getRobotShape(ourRobotPosition,ourRobotAngle));
	}
	
	public void paintRobot(Graphics g,Point robotPosition,int angle) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.fill(getRobotShape(robotPosition,angle));
		g2d.fillOval(ballPosition.x, ballPosition.y, 13, 13);
//		g2d.setColor(new Color(255,255,255));
//		g2d.fillOval(robotPosition.x + (int)Math.cos(Math.toRadians(angle+90)), robotPosition.y + (int)Math.sin(Math.toRadians(angle+90)), 20, 20);
	}
	

//@Deprecated
//    public void updateBallPosition(int x, int y) {
//    	ballPosition.x = x;
//    	ballPosition.y = y;
//    }
//
//    public void updateOppPosition(int x, int y, int angle) {
//    	oppRobotPosition.x = x;
//    	oppRobotPosition.y = y;
//    	oppRobotAngle = angle;
//    }
//
//    public void updateOurPosition(int x, int y, int angle) {
//    	ourRobotPosition.x = x;
//    	ourRobotPosition.y = y;
//    	ourRobotAngle = angle;    	
//    }
}
