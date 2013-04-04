package movement;

import baseSystem.Singleton;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

/**
 * @author Ben Ledbury
 */
public class Movement_New extends Thread {

	private boolean debug;
	boolean open = false;
	DataInputStream dis;
	DataOutputStream dos;
	NXTComm nxtComm;
	final Singleton singleton;
	Point curWaypoint;
	Point robotCoords;
	final int robotColor;
	int curAngle;
	int angleToTurn;
	double distFromGoal;
	boolean waypointChanged;
	int[] curCoords;
	long timeToSleep = 100;
	ArrayList<int[]> commands;
	private Timer t;
	
	int sLeft, sRight, sFront, sBack;

	public Movement_New(int robotColor, boolean debug) {
		this.robotColor = robotColor;
		this.debug = debug;
		this.singleton = Singleton.getSingleton();
	
		while(!open){
			open = connect();
			//open = true;
			if(!open){

			System.out.println("The system is NOT connected");

			try {
					try {
						nxtComm.close();
					} catch (IOException ex) {
						Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
					}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
		}

	}

	@Override
	public void run(){
	
	while(true){
		
		curCoords = singleton.getCoordinates();
		
		if(curWaypoint==null){sendCommand(new int[]{0,0,0,0}); continue;};
		
		if (robotColor == 0) {
			robotCoords = new Point(curCoords[3], curCoords[4]);
			curAngle = curCoords[5];
		} else {
			robotCoords = new Point(curCoords[6], curCoords[7]);
			curAngle = curCoords[8];
		}

		if(curAngle>180){curAngle = curAngle-360;}

		angleToTurn = MFunctions.calculateAngle(robotCoords, curWaypoint, curAngle);

		commands = singleton.getCommands();
		if(commands!=null){
			if(commands.get(0)[0]==1){
				move(1, robotCoords, curWaypoint, angleToTurn, 600);
			}
			else if(commands.get(0)[0]==8){
				moveAndKick();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException ex) {
						Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
					}
			}
			else {sendCommand(commands.get(0), false);}
		}
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
			}
	}
	
}

	public void move(int moveType, Point start, Point end, int angleToRotate, int speed){

		System.out.println("Angle: " + angleToRotate);

		/**
		//If the angle is too far out then rotate to correct it
		if(angleToRotate>40 || angleToRotate<-40){

			System.out.println("Rotating: " + angleToRotate);

			rotate(MFunctions.angleToWheelRotations(angleToRotate), 600);
			return;
			
		}
		 * */
		
		sLeft = speed;
		sRight = speed;

		System.out.println(angleToRotate);
		
		//Positive angles are anti-clockwise so the left wheel should be slowed down
		if(angleToRotate>0){
			sLeft -= (angleToRotate/180.0)*(speed);
		}
		
		//Negative angles are clockwise so the right wheel should be slowed down
		else if(angleToRotate<0){
			sRight += (angleToRotate/180.0)*(speed);
		}

		System.out.println("Sending speeds: ( " + sLeft + " , " + sRight + " )");

		//Send speeds to the robot
		sendCommand(new int[]{sLeft, sRight, sFront, sBack});
		
	}

	public void rotate(int angle, int speed){

		try{
		dos.writeInt(5);
		dos.writeInt(speed);
		dos.writeInt(-angle);
		dos.writeBoolean(true);

		dos.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public void sendCommand(int[] speeds){
		
		//Left wheel speed, right wheel speed
		try {
			
			//Tell the robot to do a new style movement command
			dos.writeInt(42);
			
			//Write the speeds
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

	public void executeArrayList(ArrayList<int[]> commands) {

		/**
		int[] command = commands.get(0);

		if(curWaypoint==null){
			curWaypoint = new Point(command[3], command[4]);
		}

		curWaypoint.x = command[3];
		curWaypoint.y = command[4];
		**/

		for (int i = 0; i < commands.size(); i++) {

			if (debug) {
				System.out.println("STRATEGY: Turning " + commands.get(i)[2]);
				System.out.println("STRATEGY: Move " + commands.get(i)[1]);
			}

			if(commands.get(i)[0]==8){
				moveAndKick();
				return;
			}


			sendCommand(commands.get(i), false);
			
		}

		try {
			dos.flush();
		} catch (IOException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}

	public void moveAndKick(){

		try {
			dos.writeInt(15);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeBoolean(true);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		t.schedule(new TimerTask(){
			public void run(){
				try {
					dos.writeInt(16);
					dos.writeInt(0);
					dos.writeInt(0);
					dos.writeBoolean(true);
					dos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}, 1000);
		
		t.schedule(new TimerTask(){
			public void run(){
				try {
					dos.writeInt(17);
					dos.writeInt(0);
					dos.writeInt(0);
					dos.writeBoolean(true);
					dos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1200);


	}

	private void sendCommand(int[] command, boolean immediateRet) {

		/* COMMAND format: [command type,command value,angle to turn,way-point x,way-point y, speed]*/

		int moveType = command[0], value = command[1], angleToTurn = command[2], waypointx = command[3], waypointy = command[4], speed = command[5];

		curWaypoint = new Point(waypointx, waypointy);

		angleToTurn = MFunctions.angleToWheelRotations(angleToTurn);

		if(!(command[0]==3 || command[0]==4)){
			value = MFunctions.pixelsToWheelRotations(value);
		}

		if (debug) {
			System.out.println("MOVEMENT: Turning " + angleToTurn);
			System.out.println("MOVEMENT: Move " + value);
		}

		try {

			dos.writeInt(5);
			dos.writeInt(speed);
			dos.writeInt(-angleToTurn);
			dos.writeBoolean(true);

			dos.writeInt(moveType);
			dos.writeInt(speed);
			dos.writeInt(value);
			dos.writeBoolean(immediateRet);

			dos.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean connect() {

		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			NXTInfo nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, "Holly", "00:16:53:09:92:F5");
			open = nxtComm.open(nxtInfo, NXTComm.PACKET);
			dis = new DataInputStream(nxtComm.getInputStream());
			dos = new DataOutputStream(nxtComm.getOutputStream());
			DataInputHandler dih = new DataInputHandler(dis, singleton);
			dih.start();
			t = new Timer();

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
	//                  Just for the remote control of the robot                               //
	public void sendControllerCommand(int moveType) {
		try {
			dos.writeInt(moveType);
			dos.writeInt(255);
			dos.writeInt(0);
			dos.writeBoolean(true);
			dos.flush();
		} catch (IOException ex) {
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
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
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
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
			Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public static void main(String[] args) {
		Movement_New m = new Movement_New(0, true);
		m.connect();

		try {
				m.sendCommand(new int[]{4, 200, 0, 0, 0, 255}, false);

				try {
					m.dos.flush();
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				m.sendCommand(new int[]{99, 0, 0, 0, 0, 0}, false);
				
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
					Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
				}
				m.nxtComm.close();
				System.exit(0);
			} catch (IOException ex) {
				Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
			}
	
	}
}
