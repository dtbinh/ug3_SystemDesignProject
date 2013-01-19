package visualisation;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.GradientPaint;


import java.util.ArrayList;

import net.phys2d.raw.Body;
import net.phys2d.raw.BodyList;

import simulation.bodies.SimBody;
import simulation.bodies.Wall;
import strategy.GridPoint;
import strategy.PathSearch;

public class Visualise extends Canvas implements Runnable{
	
	public ArrayList<int[]> cmds;
	
	public static int FULL_WIDTH;
	public static int FULL_HEIGHT;
	public static int PITCH_WIDTH;
	public static int PITCH_HEIGHT;
	public static int GOAL_WIDTH;
	public static int GOAL_HEIGHT;
	public static final int BALL_RADIUS = 13;
	public static Point Robot;
	public static Point Robot2;
	public static Point Ball;
	public static int RotationRobot;
	public static int RotationRobot2;
	public static ArrayList<GridPoint> inv;
	
	
	public static final int DELAY = 10;
	
	private static int widthInGrids = 34;
	private static int heightInGrids = 17;
	private int gridWidth;
	private int gridLength;
	
	private GradientPaint gradient = new GradientPaint(0, 0, Color.RED, 15, 15,
            Color.YELLOW, true);
	
	
public Visualise(int F_WIDTH, int F_HEIGHT, int GOAL_WIDTH2, int GOAL_HEIGHT2,Point R,int r, Point R2,int r2,Point B){
	
		cmds = new ArrayList<int[]>();
		inv =  new ArrayList<GridPoint>();
		
		FULL_WIDTH = F_WIDTH;
		FULL_HEIGHT = F_HEIGHT;
		
		Robot=R;	//just points indicating the middle of the robot. no rotation introdusec
		Robot2=R2;
		Ball=B;
		RotationRobot = r;
		RotationRobot2 = r2;
		
		
		
		PITCH_WIDTH=FULL_WIDTH-2*GOAL_WIDTH2;	//2 walls 54px each
		PITCH_HEIGHT=FULL_HEIGHT-20;	//2 walls 10px each
		GOAL_WIDTH=GOAL_WIDTH2;
		GOAL_HEIGHT=GOAL_HEIGHT2;
		gridWidth = PITCH_HEIGHT/heightInGrids;
		gridLength = PITCH_WIDTH/widthInGrids;
		
		setIgnoreRepaint(true);
}

public void init(){
	draw();		
}


public void cycle() {
	
	while (true) {
		
		draw();	
		
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
	}		              
}

private void draw() {
	

	Graphics g = null;
	
	try {
		g = this.getBufferStrategy().getDrawGraphics();
		
		// It is assumed that mySprite is created somewhere else.
		// This is just an example for passing off the _WIDTHGraphics object.
		paintPitch(g);
		
		
		
	} finally {
		// It is best to dispose() a Graphics object when done with it.
		g.dispose();
	}
	
	// Shows the contents of the backbuffer on the screen.
	this.getBufferStrategy().show();
 
    //Tell the System to do the Drawing now, otherwise it can take a few extra ms until 
    //Drawing is done which looks very jerky
    Toolkit.getDefaultToolkit().sync();	
}


public void run() {
	
	basic();	
}

public void basic(){
	
	cmds = PathSearch.getPath2(Ball, Robot, 180, Robot2, 0, 1);	//rotation is zero, need to find a way of passing the rottions
    inv = PathSearch.getInvalid();
    
    for (int m=0;m<9;m++){
		System.out.println(inv.get(m));
    }
	cycle();
}


private void paintPitch(Graphics g){
	//Draw pitch
	//this.setLayout(null);
	
	Graphics2D g2d = (Graphics2D)g;
	g2d.setColor(new Color(240,240,240));
	g2d.fillRect(0,0,1190,500);
	g2d.setColor(new Color(51,204,0));
	
	g2d.fillRect(GOAL_WIDTH, 10, PITCH_WIDTH, PITCH_HEIGHT); //pitch
	
	
	//Draw centre line
	g2d.setColor(new Color(255,255,255));
	g2d.drawLine(FULL_WIDTH / 2, 0, FULL_WIDTH / 2, FULL_HEIGHT);

	//Draw walls
	g2d.setColor(new Color(0,0,0));	//blk
	g2d.fillRect(0, 0, FULL_WIDTH, 10); //top wall
	g2d.fillRect(0, FULL_HEIGHT-10, FULL_WIDTH,10);	//bottom wall
	
	g2d.fillRect(0,0, GOAL_WIDTH,FULL_HEIGHT-4);	//left wall
	g2d.fillRect(FULL_WIDTH-GOAL_WIDTH,10, GOAL_WIDTH,FULL_HEIGHT-10);	//right wall
	
	
	g2d.setColor(new Color(255,255,255));	//white lines 
	g2d.fillRect(GOAL_WIDTH, 10, PITCH_WIDTH, 3); //top line
	g2d.fillRect(GOAL_WIDTH, FULL_HEIGHT-13, PITCH_WIDTH,3);	//bottom line
	
	g2d.fillRect(54,10, 3,PITCH_HEIGHT);	//left line
	g2d.fillRect(FULL_WIDTH-GOAL_WIDTH-3,10, 3,PITCH_HEIGHT);	//right line
	
	
	//draw grids
	g2d.setColor(new Color(130,130,130));
	for (int l=1;l<widthInGrids+1;l++){
		g2d.drawLine(gridLength*l+GOAL_WIDTH, 10, gridLength*l+GOAL_WIDTH, PITCH_HEIGHT+9);
		}
	
	for (int m=1;m<heightInGrids+1;m++){
		g2d.drawLine(GOAL_WIDTH,gridWidth*m+9,PITCH_WIDTH+GOAL_WIDTH, gridWidth*m+9);
		}
	
	
	//Draw centre line
	g2d.setColor(new Color(255,255,255));
	g2d.drawLine(FULL_WIDTH / 2, 10, FULL_WIDTH / 2, FULL_HEIGHT-11);
	
	//Draw goals
	g2d.setColor(new Color(204,204,204));
	g2d.fillRect(0, (FULL_HEIGHT-GOAL_HEIGHT)/2, GOAL_WIDTH, GOAL_HEIGHT);
	g2d.fillRect(FULL_WIDTH-GOAL_WIDTH, (FULL_HEIGHT-GOAL_HEIGHT)/2, GOAL_WIDTH, GOAL_HEIGHT);

	g2d.setColor(new Color(204,204,204));
	//g2d.fillRect(0, FULL_HEIGHT, FULL_WIDTH, GOAL_HEIGHT);
	
	g2d.setColor(new Color(255,0,200));
	if (inv.size()>0){
	for (int m=0;m<25;m++){
		int x=(int)inv.get(m).getX();
		int y=(int)inv.get(m).getY();
		System.out.println(x+"  dupa  "+y);
		g2d.fillRect(adjX((x-1)*21), adjY((y-1)*22), 21, 22);
	}
	}
	
	
	/*
	int n=2;
	for (int[] i : cmds){
		
		g2d.drawString(i[0]+"  "+i[1]+"  "+i[2]+"  "+i[3]+"  "+i[4]+" "+i[5],10,FULL_HEIGHT+(n*16));
		n++;
		
	}
	*/
	/*for (int m=1;m<cmds.size();m++){
		//if(m>0 && (cmds.get(m-1)[0]!=3 ||cmds.get(m-1)[0]!=4)){
		
			g2d.setColor(new Color(250,0,250));
			g2d.drawLine(cmds.get(m-1)[3],cmds.get(m-1)[4],cmds.get(m)[3],cmds.get(m)[4]);
			//g2d.drawLine(0,0,
			System.out.println(cmds.get(m-1)[3]+" "+cmds.get(m-1)[4]);
		
	}*/
	
	
	//draw robots and ball
	g2d.setPaint(new GradientPaint(0, 0, Color.YELLOW, 33, 33,
            Color.BLUE, true));
	
	//g2d.fillRect(adjX(Robot.x)-32, adjY(Robot.y)-27, 60, 54);
	//g2d.fillRect(adjX(Robot2.x)-32, adjY(Robot2.y)-27, 60, 54);
	g2d.setColor(new Color(255,0,0));
	g2d.fillOval(adjX(Ball.x)-7, adjY(Ball.y)-7, 14, 14);
	
	((Graphics2D) g2d).rotate(-RotationRobot,adjX(Robot.x), adjY(Robot.y));
    
	g2d.setPaint(Color.red);
	g2d.fillRect(adjX(Robot.x)-32, adjY(Robot.y)-27, 60, 54);
	g2d.drawString("T", Robot.x, Robot.y);
	((Graphics2D) g2d).rotate(RotationRobot,adjX(Robot.x), adjY(Robot.y));
	
((Graphics2D) g2d).rotate(RotationRobot2,adjX(Robot2.x), adjY(Robot2.y));
    
	g2d.setPaint(Color.blue);
	g2d.drawString("T", Robot2.x, Robot2.y);
	g2d.fillRect(adjX(Robot2.x)-32, adjY(Robot2.y)-27, 60, 54);
	((Graphics2D) g2d).rotate(-RotationRobot2,adjX(Robot2.x), adjY(Robot2.y));
	
	
	
	
	for (int m=1;m<cmds.size();m++){
		Line2D l= new Line2D.Float();
		l.setLine(adjX(cmds.get(m-1)[3]),adjY(cmds.get(m-1)[4]),adjX(cmds.get(m)[3]),adjY(cmds.get(m)[4]));
		
		//Point d = getRect(cmds.get(m-1)[3],cmds.get(m-1)[4]);
		//System.out.println(l.intersects(d.x, d.y, 21, 22));
		//System.out.println(d.x+"  "+d.y);
		/*if(l.intersects(d.x, d.y, d.x+21, d.x+22)){
			//gradient = new GradientPaint(0, 0, Color.RED, 15, 15,
		          //  Color.YELLOW, true);
			//g2d.setPaint(new GradientPaint(5, 5, new Color(0,155,0), 10, 5,
					//new Color(0,55,0), true));
			//g2d.setColor(new Color(250,0,250));
			g2d.setColor(new Color(51,255,51));
			g2d.fillRect(adjX(d.x), adjY(d.y), 21, 22);
			
			
		}*/
		g2d.setColor(new Color(255,0,255));
		g2d.draw(l);
		
	}
	/*
	if(cmds.size()>0){
		g2d.setPaint(new GradientPaint(5, 5, new Color(0,155,0), 10, 5,
				new Color(0,55,0), true));
	Point d = getRect(cmds.get(cmds.size()-1)[3],cmds.get(cmds.size()-1)[4]);
	g2d.fillRect(adjX(d.x), adjY(d.y), 21, 22);
	}*/
	
	g2d.setColor(new Color(33,33,33));
	g2d.drawString("Commands: ",FULL_WIDTH+10,16);
	
	for (int m=0;m<cmds.size();m++){
		String msg=getMsg(cmds.get(m));
		g2d.drawString(msg,FULL_WIDTH+10,20+((m+2)*16));
	}
	
	g2d.drawString("Commands: ",10,FULL_HEIGHT+16);
	
	

	

	
	
	}

private String getMsg(int[] tbl){
	String s="";
	/*Type 1: forward
	 *	Type 2: backward
	 *	Type 3: strafe left
	 *	Type 4: strafe right
	 *	Type 5: rotate
	 *	Type 6: adjust forward/backward
	 *	Type 7: adjust strafing
	 *	Type 8: kick
	 *	Type 9: stop
	 * */
	if (tbl[0]==1){
		s+="Forward "+tbl[1]+" Angle: "+tbl[2];
	}
	else if (tbl[0]==2){
		s+="backward ";
	}
	else if (tbl[0]==3){
		s+="strafe left "+tbl[1]+" Angle "+tbl[2];
	}
	else if (tbl[0]==4){
		s+="strafe right ";
	}
	else if (tbl[0]==5){
		s+="rotate ";
	}
	else if (tbl[0]==6){
		s+="adjust forward/backward ";
	}
	else if (tbl[0]==7){
		s+="adjust strafing ";
	}
	else if (tbl[0]==8){
		s+="kick ";
	}
	else if (tbl[0]==9){
		s+="stop ";
	}
	else {s="wrong code";}
	return s;
}


private int getLength(Line2D l){
	

	
	return (int)Math.sqrt(((l.getX1()-l.getX2())*(l.getX1()-l.getX2()))+((l.getY1()-l.getY2())*(l.getY1()-l.getY2())));
}

public ArrayList<int[]> getCmds(){
	ArrayList<int[]> cmds2 = new ArrayList<int[]>();
	
	/*for (int l=0;l<10;l++){
		int[] turnCmd = new int[5];
		turnCmd[0] = 1*l;
		turnCmd[1] = 2*l;
		turnCmd[2] = 3*l;
		turnCmd[3] = 4*l;
		turnCmd[4] = 5*l;
		
		cmds2.add(turnCmd);
	}*/
	int[] turnCmd = new int[5];
	turnCmd[0] = 1;
	turnCmd[1] = 50;
	turnCmd[2] = 100;
	turnCmd[3] = 700;
	turnCmd[4] = 200;
	cmds2.add(turnCmd);
	int[] turnCmd2 = new int[5];
	turnCmd2[0] = 3;
	turnCmd2[1] = 50;
	turnCmd2[2] = 100;
	turnCmd2[3] = 500;
	turnCmd2[4] = 360;
	cmds2.add(turnCmd2);
	int[] turnCmd3 = new int[5];
	turnCmd3[0] = 1;
	turnCmd3[1] = 50;
	turnCmd3[2] = 50;
	turnCmd3[3] = 400;
	turnCmd3[4] = 200;
	cmds2.add(turnCmd3);
	int[] turnCmd4 = new int[5];
	turnCmd4[0] = 1;
	turnCmd4[1] = 50;
	turnCmd4[2] = 50;
	turnCmd4[3] = 110;
	turnCmd4[4] = 50;
	cmds2.add(turnCmd4);
	
	//System.out.println(cmds2.size()+"dupaaaaaaa");
	return cmds2;
}


public Point getRect(int x,int y){
	Point p = new Point();
	p.x=x-(x%21);
	p.y=y-(y%22);
	System.out.println(x+"=x, p.x="+p.x+"  y="+y+" p.y="+p.y);
	return p;
}


public static int adjX(int x){
	return x+54;
}

public static int adjY(int y){
	return y+10;
}
}
