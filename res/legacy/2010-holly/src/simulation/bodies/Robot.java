package simulation.bodies;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;

import simulation.Simulation;
//import strategy.MoveCommand;
import strategy.PathSearch;
import strategy.PathSearch_Sim;
import strategy.PathSearch_Simple;
import strategy.GridPoint;


import net.phys2d.raw.Body;
import net.phys2d.raw.BodyList;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;

/**
 * @author: Joe Tam
 **/

public class Robot extends SimBody implements Runnable{
	
	private int kickForce = 1000;
	private Simulation simulation;	
	public ArrayList<int[]> cmds;
	boolean flag = true;
	
	public Robot(float width, float height, float mass, int c, Simulation simulation) {
		super(new Box(width,height),mass,c);
		//setDamping(0.004f);
		setSimulation(simulation);
	}
	
	public Robot(String name,float width, float height, float mass, int c, Simulation simulation) {
		super(name, new Box(width,height),mass,c);
		//setDamping(0.004f);
		setSimulation(simulation);
	}
	
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}
	
	public Simulation getSimulation() {
		return simulation;
	}
	
	public boolean canKick(){
		System.out.println("can kick??");
		return getSimulation().intersect(this);
		
	}
	
	
	
	
	//works with PathSearch 0bsolete
	
	public ArrayList<int[]> getCommands(int x, int y, float z,int left){
		Point ballPosition = new Point(Simulation.adj2X((int) getSimulation().getBall().getPosition().getX()), Simulation.adj2Y((int) getSimulation().getBall().getPosition().getY()));
		Point oppPosition = new Point(x,y);	
		float oppAngle = z;
		Point myPosition = new Point(Simulation.adj2X((int) this.getPosition().getX()),Simulation.adj2Y((int) this.getPosition().getY()));
		
		//ArrayList<int[]> commands = PathSearch.getPath2(ballPosition,myPosition,(int)getRotation(),oppPosition,oppAngle,1 );
		ArrayList<int[]> commands = PathSearch.getPath2(ballPosition,myPosition,adjAngle(getRotation()),oppPosition,adjAngle(oppAngle),left );
//getPath2(Point _ballPosition, Point _ourPosition, int _ourAngle, Point _oppPosition, int _oppAngle, int side) {
		
		// translate commands into commands that simulation understand
		ArrayList<int[]> cmds2 = new ArrayList<int[]>();
		
		for (int k=0; k<commands.size();k++){
			if (commands.get(k)[0]==1){		//forward
				
				if (commands.get(k)[2]<0){	// minus angle - turn left 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 4;
						turnCmd[1] = 1;
						cmds2.add(turnCmd);
					}
					
					
					if(k==commands.size()-2){
						
						for (int l=0;l<commands.get(k)[1]*0.7;l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 1;
						turnCmd2[1] = 220;
						cmds2.add(turnCmd2);
						}
					}
						else{
							for (int l=0;l<commands.get(k)[1];l++){
								int[] turnCmd2 = new int[2];
								turnCmd2[0] = 1;
								turnCmd2[1] = 220;
								cmds2.add(turnCmd2);
								}
					}
					
				} //minus angle
				else if (commands.get(k)[2]>0){	// plus angle - turn right 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 3;
						turnCmd[1] = 1;
						cmds2.add(turnCmd);
					}
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 1;
						turnCmd2[1] = 230;
						cmds2.add(turnCmd2);
					}
				}
				else {		//zero angle
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 1;
						turnCmd[1] = 230;
						cmds2.add(turnCmd);
					}
				}
			}
			
			//backwards
			else if (commands.get(k)[0]==2){
				System.out.println("command 2");
				if (commands.get(k)[2]<0){	// minus angle - turn left 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 4;
						turnCmd[1] = 1;
						cmds2.add(turnCmd);
					}
					
					
					if(k==commands.size()-2){
						
						for (int l=0;l<commands.get(k)[1]*0.7;l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 2;
						turnCmd2[1] = 220;
						cmds2.add(turnCmd2);
						}
					}
						else{
							for (int l=0;l<commands.get(k)[1];l++){
								int[] turnCmd2 = new int[2];
								turnCmd2[0] = 2;
								turnCmd2[1] = 220;
								cmds2.add(turnCmd2);
								}
					}
					
				} //minus angle
				else if (commands.get(k)[2]>0){	// plus angle - turn right 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 3;
						turnCmd[1] = 1;
						cmds2.add(turnCmd);
					}
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 2;
						turnCmd2[1] = 230;
						cmds2.add(turnCmd2);
					}
				}
				else {		//zero angle
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 2;
						turnCmd[1] = 230;
						cmds2.add(turnCmd);
					}
				}
		}
		}
		
		//was meant to add unconditional kick at the end of any command list
		/*
		int[] turnCmd = new int[2];
		turnCmd[0] = 7;
		turnCmd[1] = 1500;
		cmds2.add(turnCmd);
		int[] turnCmd2 = new int[2];
		turnCmd2[0] = 7;
		turnCmd2[1] = 1500;
		cmds2.add(turnCmd2);
		*/
		
		
return cmds2;
}
	
	
	public static int adjAngle(float angle){
		int ang = (int)Math.toDegrees(angle);
		
		//float i;
		while(ang<0){
			ang+=360;
		}
		ang = 360 - ang;
		
		ang +=90;
		
		if(ang>=360){
			ang-=360;
		}
		return ang;
		
	}
	
	public static float adjAngle2(float angle){
		
		
		
		angle +=90;
		
		if(angle>=360){
			angle-=360;
		}
		return angle;
		
	}
	/* obsolete
	public void executeCommands() {
		//TODO: more complex commands to come later
		//At the moment it just keeps navigating to the ball
		Point ballPosition = new Point((int) getSimulation().getBall().getPosition().getX(),
										(int) getSimulation().getBall().getPosition().getY());
		Point myPosition = new Point((int) this.getPosition().getX(),
									(int) this.getPosition().getY());
		ArrayList<int[]> cmds = PathSearch.getPathCommands(getRotation(), myPosition, ballPosition);
		
		//int X = myPosition.x;
		//int Y = myPosition.y;
		//float rot = (float)Math.toDegrees(getRotation());
		
		
		//for (int k = 0; k < cmds.size(); k++) {
			//for (int j = 0; j < 2; j++) {
			System.out.println(cmds.size());
		//}
		//}
		
		
		//translate commands
		for (int i = 0; i < cmds.size(); i++) {
			executeCommand(cmds.get(i));
		}
			//execute it
			//System.out.println("run!! "+ i);
			
			 while (commandDone(cmds.get(i), myPosition, rot) != false) {
				try {
					System.out.println("run!! "+ i);
					//executeCommand(cmds.get(i));
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
		
	} */
	
	//translate the commands
	public void executeCommand(int[] command) {
		int cmdType = command[0];
		int cmdValue = command[1];
		if (cmdType == 1) {
			forward(cmdValue);
		}
		else if (cmdType == 2) {
			backward(cmdValue);
		}
		else if (cmdType == 3) {
			turnLeft(cmdValue);
		}
		else if (cmdType == 4) {
			turnRight(cmdValue);
		}
		else if (cmdType == 5) {
			strafeRight(cmdValue);
		}
		else if (cmdType == 6) {
			strafeRight(cmdValue);
		}
		else if (cmdType == 7) {
			strafeRight(cmdValue);
		}
		else if (cmdType == 8) {
			System.out.println("kick??");
			kick(getSimulation().ball);
		}
	}
	
	
	public ArrayList<int[]> getCommandsstrategy(ArrayList<int[]> commands){
		
		// translate commands into commands that simulation understand
		ArrayList<int[]> cmds = new ArrayList<int[]>();
		
		for (int k=0; k<commands.size();k++){
			if (commands.get(k)[0]==1){
				if (commands.get(k)[2]<0){	// minus angle - turn left 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 4;
						turnCmd[1] = 1;
						cmds.add(turnCmd);
					}
					if(k==commands.size()-2){
						
						for (int l=0;l<commands.get(k)[1]*0.7;l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 1;
						turnCmd2[1] = 220;
						cmds.add(turnCmd2);
						}
					}
						else{
							for (int l=0;l<commands.get(k)[1];l++){
								int[] turnCmd2 = new int[2];
								turnCmd2[0] = 1;
								turnCmd2[1] = 220;
								cmds.add(turnCmd2);
								}
					}
					
				}
				else if (commands.get(k)[2]>0){	// plus angle - turn right 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 3;
						turnCmd[1] = 1;
						cmds.add(turnCmd);
					}
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 1;
						turnCmd2[1] = 230;
						cmds.add(turnCmd2);
					}
				}
				else {
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 1;
						turnCmd[1] = 230;
						cmds.add(turnCmd);
					}
				}
			}
			
			//backwards
			else if (commands.get(k)[0]==2){
				System.out.println("command 2");
				if (commands.get(k)[2]<0){	// minus angle - turn left 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 4;
						turnCmd[1] = 1;
						cmds.add(turnCmd);
					}
					
					
					if(k==commands.size()-2){
						
						for (int l=0;l<commands.get(k)[1]*0.7;l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 2;
						turnCmd2[1] = 220;
						cmds.add(turnCmd2);
						}
					}
						else{
							for (int l=0;l<commands.get(k)[1];l++){
								int[] turnCmd2 = new int[2];
								turnCmd2[0] = 2;
								turnCmd2[1] = 220;
								cmds.add(turnCmd2);
								}
					}
					
				} //minus angle
				else if (commands.get(k)[2]>0){	// plus angle - turn right 
					for (int l=0;l<Math.abs(commands.get(k)[2]);l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 3;
						turnCmd[1] = 1;
						cmds.add(turnCmd);
					}
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd2 = new int[2];
						turnCmd2[0] = 2;
						turnCmd2[1] = 230;
						cmds.add(turnCmd2);
					}
				}
				else {		//zero angle
					for (int l=0;l<commands.get(k)[1];l++){
						int[] turnCmd = new int[2];
						turnCmd[0] = 2;
						turnCmd[1] = 250;
						cmds.add(turnCmd);
					}
				}
		}
		}
		/*
		int[] turnCmd = new int[2];
		turnCmd[0] = 8;
		turnCmd[1] = 1500;
		cmds2.add(turnCmd);
		int[] turnCmd2 = new int[2];
		turnCmd2[0] = 8;
		turnCmd2[1] = 1500;
		cmds2.add(turnCmd2);
		*/
		
		System.out.print("commands received!");
return cmds;
}
	
	
	
	public void kick(Ball ball) {
		System.out.println("try and kick");
		
		if (canKick() && flag==true){
			
			float width = (float) Math.cos(getRotation());
			float length = (float) Math.sin(getRotation()); 
			float constant=1;
			
			/*
			 
			 float width = (float) Math.cos(getRotation());
		float length = (float) Math.sin(getRotation()); 
		float sum = Math.abs(width)+ Math.abs(length);
		setForce(force * (length/sum), -force * (width/sum) );
		*/
			
			//setForce(force * width, force * length);
			float sum = Math.abs(width)+ Math.abs(length);
			
			
			ball.setForce( kickForce*1.4f * (length/sum)*constant,-kickForce*1.4f* (width/sum)*constant);
			System.out.println("done!!");
			flag = false;
		}
		}
		
		/*public void kick2() {
			System.out.println("try and kick");
			
			int[] cmd = new int[6];
			cmd[0] = 1;									//command type, set to forward temporarily
			cmd[1] = distanceAndAngle[0];				//command value
			cmd[2] = distanceAndAngle[1];				//angle to turn
			cmd[3] = (int) coordinates.get(i+1).getX();	//way-point x
			cmd[4] = (int) coordinates.get(i+1).getY();	//way-point y
			cmd[5] = 600;								//speed
			commands.add(cmd);
			
			if (canKick() && flag==true){
				getSimulation().ball.setForce(kickForce*1.4f, 0);
				System.out.println("done!!");
				flag = false;
			}
		//check the object that is touched by the Robot is touching the kicker and not the other 3 sides
			//touchList.get(i).getRotation();
		
		//ball.setForce(kickForce, 0);
	}*/
	
	public void strafeRight(float force) {
		float width = (float) Math.cos(getRotation());
		float length = (float) Math.sin(getRotation()); 
		float constant=1;
		
		
		
		//setForce(force * width, force * length);
		float sum = Math.abs(width)+ Math.abs(length);
		
		if (Math.abs(width)>0.4*sum && Math.abs(width)<0.6*sum){
			constant+=0.4;
		}
		
		setForce(force * (width/sum)*constant, force * (length/sum)*constant);
	}
	
	public void strafeLeft(float force) {
		float width = (float) Math.cos(getRotation());
		float length = (float) Math.sin(getRotation()); 
		float sum = Math.abs(width)+ Math.abs(length);
		setForce(-force * (width/sum), -force * (length/sum));
	}
	
	/**
	 * translates turning angle from degrees to radians for the physics engine.
	 * @param angle		the angle to rotate in degrees
	 */
	public void turnLeft(int angle) {
		
		float newR = (float)Math.toRadians(-angle)+getRotation();
		setRotation(newR%(float)Math.toRadians(360));
	}
	
	/**
	 * translates turning angle from degrees to radians for the physics engine.
	 * @param angle		the angle to rotate in degrees
	 */
	public void turnRight(int angle) {
		//new version keeps rotation in range -6.28318...6.28318 (360degrees in radians)
		
		float newR = (float)Math.toRadians(angle)+getRotation();
		setRotation(newR%(float)Math.toRadians(360));
		
	}
	
	public void forward(float force) {
		float width = (float) Math.cos(getRotation());
		float length = (float) Math.sin(getRotation()); 
		float sum = Math.abs(width)+ Math.abs(length);
		setForce(force * (length/sum), -force * (width/sum) );
	}
	
	public void backward(float force) {
		float width = (float) Math.cos(getRotation());
		float length = (float) Math.sin(getRotation()); 
		float sum = Math.abs(width)+ Math.abs(length);
		setForce(-force * (length/sum), force * (width/sum) );
	}

	@Override
	public void run() {
		
		while (true) {
//			executeCommands();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) {
		//Robot r = new 
		float an = (float)Math.toRadians(90);
		System.out.println(adjAngle(an));
		float an2 = 360;
		System.out.println(adjAngle2(an2));
	}

}
