package strategy;

import java.awt.Point;
import java.util.ArrayList;

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

public class PathSearch_Simple {

	public static float startAngle = 10;
	public static int blah = 1;
	int ballX;
	int ballY;
	int opponentX;
	int opponentY;
	int opponentAngle;
	int robotX;
	int robotY;
	static int robotAngle;
	static PathSearch_Simple instance;
	
    public static PathSearch_Simple getInstance() {
        if(instance == null) {
            instance = new PathSearch_Simple();
        }
        return instance;
    }
    
	public ArrayList<int[]> getPathCommands(float startAng, Point startPoint, Point endPoint) {
		
		//ArrayList<MoveCommand> cmds = new ArrayList<MoveCommand>();
		ArrayList<int[]> cmds = new ArrayList<int[]>();
		
		//search for possible solution to problem and produce list of commands
		//only makes use of turning, forward and backward at the moment (no strafing)
		//float angle = (float) ;
		
		float angle;
		//deal with exceptional cases, zero division, invalid cos/sin etc.
		if (startPoint.x == endPoint.x) {
			if (startPoint.y < endPoint.y)
				angle = 90;
			else
				angle = 180;
		} else {
			angle = (float) (Math.toDegrees((Math.atan2((endPoint.y-startPoint.y),(endPoint.x-startPoint.x)))));
		}

		System.out.println("angle between start and end point " + angle);
		startAngle = robotAngle;				//replace with a method to retrieve the angle from vision
		
		float turnAngle = angle - startAngle;
		
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
		Point startPoint = new Point(100,20);
		Point endPoint = new Point(100,80);
                PathSearch_Simple s = new PathSearch_Simple();

		ArrayList<int[]> cmds = s.getPathCommands(0,startPoint,endPoint);
		for (int i = 0; i < cmds.size(); i++) {
			System.out.println(cmds.get(i)[0]);
			System.out.println(cmds.get(i)[1]);
		}
	}
	
    public void updateBallPosition(int x, int y, int angle) {
        //TODO: are we going to store angle info or leave it out?
        ballX = x;
        ballY = y;
    }

    public void updateOpponentPosition(int x, int y, int angle) {
        opponentX = x;
        opponentY = y;
        opponentAngle = angle;
    }

    public void updateRobotPosition(int x, int y, int angle) {
        robotX = x;
        robotY = y;
        robotAngle = angle;
    }
}
