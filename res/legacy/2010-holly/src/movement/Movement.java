package movement;

import baseSystem.Singleton;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import strategy.Strategy;

/**
 * @author Ben Ledbury
 */
public class Movement extends Thread {

	private boolean debug;
	boolean open = false;
	private boolean move_diagonally;
	private boolean running = false;
	private boolean lastCommand;
	DataInputStream dis;
	DataOutputStream dos;
	NXTComm nxtComm;
	final Singleton singleton;
	Point curWaypoint;
	Point robotCoords;
	int robotColor;
	int curAngle;
	int angleToWaypoint;
	double distFromWaypoint;
	double distFromGoal;
	boolean waypointChanged;
	int[] curCoords;
	long timeToSleep = 100;
	ArrayList<int[]> commandList;
	private Timer t;
	final int[] stop = new int[]{0, 0, 0, 0};
	int sLeft, sRight, sFront, sBack;
	int[] waitingSpeeds = new int[4];
	boolean turned;
	private Strategy strategy;

	public Movement(int robotColor, boolean debug, boolean movementCorrection, boolean diagonal_movement) {
		this.robotColor = robotColor;
		this.debug = debug;
		move_diagonally = diagonal_movement;
		this.singleton = Singleton.getSingleton();
		this.strategy = singleton.getStrategy();

		while (!open) {
			open = connect(movementCorrection);
			if (!open) {

				System.out.println("The system is NOT connected");

				try {
					try {
						nxtComm.close();
					} catch (IOException ex) {
						Logger.getLogger(Movement.class.getName()).log(
								Level.SEVERE, null, ex);
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void setInPenaltyMode(){
		waitingSpeeds = stop;
	}

	private int getPathDistance(){

		double distance = robotCoords.distance(curWaypoint);

		Point cur = new Point(0,0), next=new Point(0,0);

		for(int i=0; i<commandList.size() - 1; i++){
			cur.x = commandList.get(i)[3]; cur.y = commandList.get(i)[4];
			next.x = commandList.get(i+1)[3]; next.y = commandList.get(i+1)[4];

			distance+=cur.distance(next);
		}

		return (int)distance;
	}

	private void executeDiagonalMovement(ArrayList<int[]> commands, Point goalMidpoint) {
		double ratio;
		int angleToTurn;

		if (running) {

			if (commands.get(0)[0] == 9) {
				sendSpeeds(new int[]{700,700,0,0});
				//kick();
				return;
			}

			if (commands.get(0)[0] == 8) {
				//sendSpeeds(stop);
				kick();
				return;
			}

			if (commands.get(0)[0] == 10) {
				sendSpeeds(stop);
				return;
			}

			if (commands.get(0)[0] == 5) {
				rotate(commands.get(0)[6], commands.get(0)[2]);
				return;
			}
			angleToTurn = MFunctions.calculateAngle(robotCoords, goalMidpoint, curAngle);
//                if(Math.abs(angleToTurn) > 45) {
//                    rotate(200);
//                }
//                else {
//                    ratio = MFunctions.correctionRatio((int)robotCoords.distance(goalMidpoint), angleToTurn);
			moveDiagonally(commands.get(0), 1, angleToTurn);

		}
		
		else{
			angleToTurn = MFunctions.calculateAngle(robotCoords, goalMidpoint, curAngle);
			moveDiagonally(commands.get(0), 1, angleToTurn);
		}


//                }

	}

	public void setRunning(boolean run) {

		this.running = run;

		if (run) {
			sendSpeeds(waitingSpeeds);
		} else {
			sendStop();
		}

	}

	public boolean isRunning() {
		return running;
	}

	public void executeArrayList(ArrayList<int[]> commands, Point goalMidpoint) {

		commandList = commands;

		if (move_diagonally) {
			if (getCurrentCoords()) {
				executeDiagonalMovement(commands, goalMidpoint);
			} else {
				System.out.println("MOVEMENT: Couldn't fetch coordinates from Singleton");
			}

			return;
		}

		int length = 2;
		if (commands.size() < 2) {
			length = commands.size();
		}

		for (int i = 0; i < length; i++) {

			if (debug) {
				System.out.println("STRATEGY: Turning " + commands.get(i)[2]);
				System.out.println("STRATEGY: Move " + commands.get(i)[1]);
			}

			if (commands.get(i)[0] == 9) {

				moveAndKick();
				return;
			}

			if (commands.get(i)[0] == 8) {
				kick();
				return;
			}

			sendCommand(commands.get(i), false);

		}

	}

	public void moveAndKick() {
		try {
			dos.writeInt(1);
			dos.writeInt(700);
			dos.writeInt(1080);
			dos.writeBoolean(true);
			dos.flush();
			Thread.sleep(1000);
		} catch (IOException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		kick();

	}

	public void kick() {

		try {

			dos.writeInt(8);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeBoolean(true);
			dos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendCommand(int[] command, boolean immediateRet) {

		/*
		 * COMMAND format: [command type,command value,angle to turn,way-point
		 * x,way-point y, speed]
		 */

		int moveType = command[0], value = command[1], angleToTurn = command[2], waypointx = command[3], waypointy = command[4], speed = command[5], rotateSpeed = command[6];

		curWaypoint = new Point(waypointx, waypointy);

		angleToTurn = MFunctions.angleToWheelRotations(angleToTurn);

		if (!(command[0] == 3 || command[0] == 4)) {
			value = MFunctions.pixelsToWheelRotations(value);
		}

		if (debug) {
			System.out.println("MOVEMENT: Turning " + angleToTurn);
			System.out.println("MOVEMENT: Move " + value);
		}

		try {

			dos.writeInt(5);
			dos.writeInt(rotateSpeed);
			dos.writeInt(-angleToTurn);
			dos.writeBoolean(false);

			dos.writeInt(moveType);
			dos.writeInt(speed);
			dos.writeInt(value);
			dos.writeBoolean(immediateRet);

			dos.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean connect(boolean movementCorrection) {

		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			NXTInfo nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, "Holly",
					"00:16:53:09:92:F5");
			open = nxtComm.open(nxtInfo, NXTComm.PACKET);
			//dis = new DataInputStream(nxtComm.getInputStream());
			dos = new DataOutputStream(nxtComm.getOutputStream());
			//if (!movementCorrection) {
			//DataInputHandler dih = new DataInputHandler(dis, singleton);
			//dih.start();
			//}
			//t = new Timer();

		} catch (NXTCommException e) {
			return false;
		}

		if (open) {
			return true;
		} else {
			return false;
		}

	}

	/*************************************************************************************************************/
	// Just for the remote control of the robot //
	public void sendControllerCommand(int moveType) {
		try {
			dos.writeInt(moveType);
			dos.writeInt(255);
			dos.writeInt(0);
			dos.writeBoolean(true);
			dos.flush();
		} catch (IOException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null,
					ex);
		}

	}

	public void sendStrafeCommand(int moveType) {
		try {
			dos.writeInt(moveType);
			dos.writeInt(250);
			dos.writeInt(250);
			dos.writeBoolean(false);
			dos.flush();
		} catch (IOException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	public void sendControllerCommand(int moveType, int speed, int angle) {
		try {
			dos.writeInt(moveType);
			dos.writeInt(speed);
			dos.writeInt(angle);
			dos.writeBoolean(true);
			dos.flush();
		} catch (IOException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null,
					ex);
		}

	}

	/**
	 * Change the colour of the robot.
	 * @param value
	 */
	public void setRobotColor(int value) {
		if (value == 0 || value == 1) {
			robotColor = value;
		}
	}

	/********************************************************************************/
	private boolean getCurrentCoords() {

		if (singleton.getCoordinates() == null) {
			return false;
		}
		curCoords = singleton.getCoordinates();

		if (singleton.getCommands().size() == 0) {
			return false;
		}
		curWaypoint = new Point(singleton.getCommands().get(0)[3],
				singleton.getCommands().get(0)[4]);

		if (robotColor == 0) {
			robotCoords = new Point(curCoords[3], curCoords[4]);
			curAngle = curCoords[5];
		} else {
			robotCoords = new Point(curCoords[6], curCoords[7]);
			curAngle = curCoords[8];
		}

		if (curWaypoint.distance(robotCoords) < 100 && singleton.getCommands().size() > 1) {
			curWaypoint = new Point(singleton.getCommands().get(1)[3],
					singleton.getCommands().get(1)[4]);
		}

		angleToWaypoint = MFunctions.calculateAngle(robotCoords, curWaypoint, curAngle);

		return true;

	}

	public void rotate(int rotateSpeed, int angle) {

		/**
		//angleToWaypoint = MFunctions.calculateAngle(robotCoords, curWaypoint, curAngle);
		if(angle<0){
		sendSpeeds(new int[]{rotateSpeed, -rotateSpeed, 0, 0});
		}
		else{
		sendSpeeds(new int[]{-rotateSpeed, rotateSpeed, 0, 0});
		}
		 * */
		angle = MFunctions.angleToWheelRotations(angle);

		try {

			dos.writeInt(5);
			dos.writeInt(rotateSpeed);
			dos.writeInt(-angle);
			dos.writeBoolean(false);

			dos.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void sendSpeeds(int[] speeds) {

		// Left wheel speed, right wheel speed
		try {
			System.out.println("Sending speeds: (" + speeds[0] + ", " + speeds[1] + ", " + speeds[2] + ", " + speeds[3] + ")");
			// Tell the robot to do a new style movement command
			dos.writeInt(42);

			// Write the speeds
			dos.writeInt(speeds[0]);
			dos.writeInt(speeds[1]);
			dos.writeInt(speeds[2]);
			dos.writeInt(speeds[3]);

			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendStop() {

		try {
			Thread.sleep(25);
		} catch (Exception e) {
		}
		// Left wheel speed, right wheel speed
		try {
			System.out.println("Stopping robot");
			// Tell the robot to do a new style movement command
			dos.writeInt(42);

			// Write the speeds
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);

			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	private void move(int moveType, int angleToTurn, int speed, int rotateSpeed) {

	distFromWaypoint = robotCoords.distance(curWaypoint);

	switch(moveType){

	case 1:

	sLeft = speed;
	sRight = speed;
	sFront = 0;
	sBack = 0;

	//Positive angles are anti-clockwise so the left wheel should be slowed down
	if (angleToWaypoint > 0) {

	if(angleToWaypoint > 45){
	rotate(rotateSpeed);
	return;
	}

	double slowDownSpeed = ((angleToWaypoint / 180.0) * speed);

	if(slowDownSpeed>0){
	sLeft -= slowDownSpeed;///(distFromWaypoint/100));
	}
	else{
	sLeft = 0;
	}
	System.out.println("Slowing left wheel down by: " + ((angleToWaypoint / 180.0) * 2*speed));
	} //Negative angles are clockwise so the right wheel should be slowed down
	else if (angleToWaypoint < 0) {

	if(angleToWaypoint < -45){
	rotate(rotateSpeed);
	return;
	}

	double slowDownSpeed = ((angleToWaypoint / 180.0) * 2*speed);

	if(slowDownSpeed<0){
	sRight += slowDownSpeed;///(distFromWaypoint/100));
	}
	else{
	sRight=0;
	}

	System.out.println("Slowing right wheel down by: " + ((angleToWaypoint / 180.0) * speed));
	}
	sendSpeeds(new int[]{sLeft, sRight, sFront, sBack});
	break;

	case 2:

	sLeft = speed;
	sRight = speed;
	sFront = 0;
	sBack = 0;

	//Positive angles are anti-clockwise so the left wheel should be slowed down
	if (angleToTurn > 0) {
	sLeft -= ((angleToTurn / 180.0) * (speed))/distFromWaypoint;
	} //Negative angles are clockwise so the right wheel should be slowed down
	else if (angleToTurn < 0) {
	sRight -= ((-angleToTurn / 180.0) * (speed))/distFromWaypoint;
	}

	//Send speeds to the robot
	sendSpeeds(new int[]{-sLeft, -sRight, sFront, sBack});
	break;

	case 3:

	curAngle += 90;

	//TODO:
	sLeft = 0;
	sRight = 0;
	sFront = speed;
	sBack = speed;

	//If drifting up
	if (angleToTurn > 0) {
	sFront -= ((angleToTurn / 180.0) * (speed))/distFromWaypoint;
	} //If drifting down
	else if (angleToTurn < 0) {
	sBack -= ((-angleToTurn / 180.0) * (speed))/distFromWaypoint;
	}

	sendSpeeds(new int[]{sLeft, sRight, -sFront, -sBack});

	break;

	case 4:

	curAngle -= 90;

	sLeft = 0;
	sRight = 0;
	sFront = speed;
	sBack = speed;

	//If drifting up
	if (angleToTurn > 0) {
	sFront -= ((angleToTurn / 180.0) * (speed))/distFromWaypoint;
	} //If drifting down
	else if (angleToTurn < 0) {
	sBack -= ((-angleToTurn / 180.0) * (speed))/distFromWaypoint;
	}

	//Send speeds to the robot
	sendSpeeds(new int[]{sLeft, sRight, sFront, sBack});
	break;

	case 5:
	rotate(rotateSpeed);
	break;

	case 8:
	kick();
	break;
	case 9:
	moveAndKick();
	break;
	}

	}

	/******************************************************************************************/
	private void moveDiagonally(int[] command, double ratio, int angleToTurn) {
		//Point destination = new Point(command[3], command[4]);
		int[] speeds;
		int angle;
		if (getCurrentCoords()) {

			/*
			System.out.println("Our angle " + curAngle);

			//If we should be facing right
			if(strategy.getOurSide() == Singleton.LEFT){

			//Rotate to be facing right
			if(!(curAngle<45 && curAngle > -45)){
			rotateToParrallel(1);
			return;
			}

			}
			//If we should be facing left
			else{
			//Rotate to be facing left
			if(!(curAngle>135 || curAngle < -135)){
			rotateToParrallel(0);
			return;
			}
			}
			 * */

			angle = MFunctions.calculateAngle(robotCoords, curWaypoint, curAngle - 90);
			System.out.println(angle);
			speeds = Holonomic.angleToSpeeds(angle, ratio, angleToTurn);

			int dist = getPathDistance();

			if (dist < 100) {
				for (int i = 0; i < speeds.length; i++) {
					if (i < 2) {
						if (((speeds[i] / 2) < 70) && ((speeds[i] / 2) > 0)) {
							speeds[i] = 70;
						} else {
							speeds[i] /= 2;
						}
					} else {
						if (((speeds[i] / 2) < 100) && ((speeds[i] / 2) > 0)) {
							speeds[i] = 100;
						} else {
							speeds[i] /= 2;
						}
					}

				}
			}

			if (dist < 50) {

				//rotate(angle, 200);
				//return;

				for (int i = 0; i < speeds.length; i++) {
					speeds[i] /= 4;
				}

			}

			if (!running) {
				waitingSpeeds = speeds;
				return;
			} else {
				sendSpeeds(speeds);
			}

		} else {
			System.out.println("MOVEMENT: Can't move diagonally");
		}
	}

	/**
	private void rotateToParrallel(int side){

	sendSpeeds(stop);

	int angleToRotate;

	//Left
	if(side==0){
	angleToRotate = 180-curAngle;
	if(angleToRotate>180){angleToRotate-=360;}
	if(angleToRotate<-180){angleToRotate=360+angleToRotate;}
	}
	//Right
	else{
	angleToRotate = -curAngle;

	}

	rotate(200);
	}

	/******************************************************************************************/
	public static void main(String[] args) {
		Movement m = new Movement(0, true, true, false);
		m.connect(true);

		Holonomic h = new Holonomic();

		try {
			m.sendSpeeds(h.angleToSpeeds(0, 0.85, 0));
			m.kick();

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			m.sendSpeeds(new int[]{0, 0, 0, 0});
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				Logger.getLogger(Movement.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			m.nxtComm.close();
			System.exit(0);
		} catch (IOException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	}
}
