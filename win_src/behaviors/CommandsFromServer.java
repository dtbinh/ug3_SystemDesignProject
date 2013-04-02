package behaviors;

import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;

import robot.Robot;

public class CommandsFromServer implements Behavior {
	private boolean suppressed = false;
	private static InputStream dis;
	private static OutputStream dos;
	private Robot superRobot;
	

	public CommandsFromServer(Robot thisRobot) { 
		LCD.clear();
		LCD.drawString("Waiting for", 0, 0);
		LCD.drawString("Bluetooth...", 0, 1);

		NXTConnection connection = Bluetooth.waitForConnection();
		dis = connection.openInputStream();
		dos = connection.openOutputStream();
		Sound.beep();
		LCD.clear();
		LCD.drawString("Connected!", 0, 0);
		superRobot = thisRobot;

	}

	//TODO: DE-BEHAVIORALIZE, MAKE WORK. THREAD.
	
	public void suppress() {
		suppressed = true;
	}

	public boolean takeControl() { 
		return (superRobot.needsNewData);
	}

	public void action() {
		byte[] positions = new byte[9*2];
		byte[] opcode = new byte[1];
		try{
			while (!suppressed) { //TODO: Fix?
				dis.read(opcode);
				System.out.println(opcode);
				if (opcode[0] == 10){
					dis.read(positions);
					short ux = bytesToShort(positions[1],positions[0]); 
					short uy = bytesToShort(positions[3], positions[2]);
					short ua = bytesToShort(positions[5], positions[4]);
					short ex = bytesToShort(positions[7], positions[6]);
					short ey = bytesToShort(positions[9], positions[8]);
					short ea = bytesToShort(positions[11], positions[10]);
					short gx = bytesToShort(positions[13], positions[12]);
					short gy = bytesToShort(positions[15], positions[14]);
					short ga = bytesToShort(positions[17], positions[16]);
					dos.write(opcode);
					dos.flush();
					
					System.out.println("recieved some stuff bro");			
					Pose goalReadPose =  new Pose(gx,gy,ga);
					if (!goalReadPose.equals(superRobot.getGoalPose())){
						System.out.println(goalReadPose.toString());
						superRobot.setGoalPose(goalReadPose); 
						superRobot.setOurPose(new Pose(ux,uy,ua));
						superRobot.setTheirPose(new Pose(ex,ey,ea));	
						superRobot.needsNewData = false;
						superRobot.needsNewPath = true;
					}
					
				}
			}
		} catch (Exception e) {
			// Does not wait for user to notice
			LCD.drawString("EXCEPTION1!", 0, 6);
			e.printStackTrace();
		}
		
		while (!suppressed) {
			Thread.yield();
		}
		
	}
	
		
	
		
	
	
	public static short bytesToShort(byte b0, byte b1){
		return (short) ((short)b0 <<8 | (0xFF & (short)b1));
	}
	


}
