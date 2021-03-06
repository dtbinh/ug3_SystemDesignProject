package sdp.server;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

import sdp.server.math.Vector2;


public class Bluetooth extends Thread {
	private boolean running;
	private World world;
	private Vector2 oldYellowPos = null;
	private Vector2 oldBluePos = null;
	private Vector2 oldBallPos = null;
	private double oldYellowAng = -1;
	private double oldBlueAng = -1;
	private long lastUpdateTime = -1;


	Bluetooth() {	
		this.running = true;
		this.world = null;
	}

	public void dispose() {
		this.running = false;
	}

	public void run() {
		// Init IPC
		Context context = ZMQ.context(1);
		Socket socket = context.socket(ZMQ.REP);

		// Bind
		System.out.println("Binding to port 6666...");
		socket.bind("tcp://127.0.0.1:6666");
		socket.setReceiveTimeOut(500);

		String request = null;
		String reply;
		while (running) {
			request = null;

			// Wait for request
			//System.out.println("Waiting for request");
			try {
				request = socket.recvStr();
			} catch (Exception e) {
				//System.out.println("Timeout...");
			}
			
			
			// Reply
			if (request != null && this.world != null) {
				RobotState[] rs = this.world.getRobotStates();
				BallState bs = this.world.getBallState();
				ScreenProjection sp = world.getScreenProjection();
				Vector2 ballPosition = sp.projectPosition(bs.getPosition());
				Vector2 yellowPosition;
				double yellowAngle;
				Vector2 bluePosition;
				double blueAngle;
				
				int yellowIndex = 0, blueIndex = 1;
				
				if (rs[0].getTeam() == RobotState.Team.BLUE) {
					yellowIndex = 1;
					blueIndex = 0;
				}
				
				
				yellowPosition = sp.projectPosition(rs[yellowIndex].getPosition());
				yellowAngle = Math.toDegrees(rs[yellowIndex].getRotation());
				
				bluePosition = sp.projectPosition(rs[blueIndex].getPosition());
				blueAngle = Math.toDegrees(rs[blueIndex].getRotation());	
				
				// Update time only
				if (this.lastUpdateTime == -1 || 
						!this.oldYellowPos.equals(rs[yellowIndex].getPosition()) || 
						!this.oldBluePos.equals(rs[blueIndex].getPosition()) || 
						!this.oldBallPos.equals(bs.getPosition()) ||
						this.oldYellowAng != yellowAngle ||
						this.oldBlueAng != blueAngle){
					this.lastUpdateTime = System.currentTimeMillis();
					this.oldYellowPos = rs[yellowIndex].getPosition();
					this.oldBluePos = rs[blueIndex].getPosition();
					this.oldBallPos = bs.getPosition();
					this.oldYellowAng = yellowAngle;
					this.oldBlueAng = blueAngle;
				}
				
				reply = yellowPosition.getIntX() + " " + yellowPosition.getIntY() +
					" " + (int)yellowAngle + 
					" " + bluePosition.getIntX() + " " + bluePosition.getIntY() +
					" " + (int)blueAngle + 
					" " + ballPosition.getIntX() + " " + ballPosition.getIntY() + 
					" " + this.lastUpdateTime;
				
				socket.send(reply);
				
			}
			
		}
	}

	public void updateWorld(World world) {
		this.world = world;		
	}


}
