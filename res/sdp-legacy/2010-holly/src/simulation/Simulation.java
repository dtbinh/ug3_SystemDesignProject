package simulation;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.awt.geom.Line2D;

import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;


import javax.swing.JFrame;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.BodyList;
import net.phys2d.raw.CollisionEvent;
import net.phys2d.raw.CollisionListener;
import net.phys2d.raw.CollisionSpace;
import net.phys2d.raw.Contact;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.World;
import net.phys2d.raw.collide.BoxCircleCollider;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.strategies.BruteCollisionStrategy;
import net.phys2d.raw.strategies.QuadSpaceStrategy;

import simulation.bodies.Ball;
import simulation.bodies.Robot;
import simulation.bodies.SimBody;
import simulation.bodies.Wall;
import strategy.Strategy;

/**
 * @author Joe Tam
 * @author Ben Ledbury
 **/

public class Simulation extends Canvas implements KeyListener, Runnable{
	
	/*BufferStrategy bs = t5.getBufferStrategy();
	 *		pitch size = 8 feet (w) x 4 feet (h) = 244cm (w) x 122cm(h)
	 *		golf ball  = 4.3cm
	 *		goal	   = 18cm (w) x 60cm (h)
	 *		ratio		 10cm real life: 30pixels on screen
	 */
	
	private static Simulation simulation;
	private Strategy strategy;

	//pitch variables
	public static final int FULL_WIDTH = 822;
	public static final int FULL_HEIGHT = 394;
	public static final int PITCH_WIDTH = 714;
	public static final int PITCH_HEIGHT= 374;
	public static final int GOAL_WIDTH = 54;
	public static final int GOAL_HEIGHT = 180;
	
	public static final int DELAY = 10;

	private static int widthInGrids = 34;
	private static int heightInGrids = 17;
	private int gridWidth = PITCH_HEIGHT/heightInGrids;
	private int gridLength = PITCH_WIDTH/widthInGrids;
	
	//physics
	public static final float BALL_MASS = 0.045f;
	public static final int BALL_RADIUS = 13;
	public static final int GRAVITY = 0;
	public static BruteCollisionStrategy collisionStrategy = new BruteCollisionStrategy();
	public static World world = new World(new Vector2f(GRAVITY, GRAVITY), 20, new QuadSpaceStrategy(20,5));	
	public static Wall[] walls = new Wall[8];
	public static Listener lis = new Listener();
	public static Robot myRobot;
	public static Robot oppRobot;
	public static Ball ball;
	
	
	//game play constants
	private final static int BLUE = 0;
	private final static int YELLOW = 1;
	private final static int LEFT = 0;
	private final static int RIGHT = 1;
	
	public Color[] cols = { Color.blue,Color.yellow, Color.red, Color.black, Color.green};
	
	
	
	public static int[] ids = new int[3];	// for collision listener only
	
	
	
	
	public int counter =-1;	// used for cycle
	
	
	
	
	
	// list of bodies - our robot, opponent robot, ball
	public Simulation( int [] p, int color, int side){
		
	simulation = this;
	strategy = new Strategy(true , false, color, side, 0);
	
	setWalls();
	
	
		
		ball = new Ball("Ball", BALL_RADIUS, BALL_MASS, 2);		
		ids[0] = ball.getID();
		ball.setDamping(0.001f);
        ball.setRotDamping(0.04f);
        ball.setRestitution(1.0f);
		
		setIgnoreRepaint(true);
		
		if (color == 0){
			myRobot = new Robot("myRobot", 54, 60, 1, 0, this);		// name, width, height, mass, color, simulation
			oppRobot = new Robot("oppRobot", 54, 60, 1, 1, this);
		}
		else {
			myRobot = new Robot("myRobot", 54, 60, 1, 1, this);		// name, width, height, mass, color, simulation
			oppRobot = new Robot("oppRobot", 54, 60, 1, 0, this);
		}
		
		myRobot.setPosition(adjX((int)p[0]),adjY((int)p[1]));
	    oppRobot.setPosition(adjX((int)p[3]),adjY((int)p[4]));        
	    ball.setPosition(adjX((int)p[6]),adjY((int)p[7]));
	    
	    myRobot.setRotation((float)Math.toRadians(Robot.adjAngle2(p[2])));
        oppRobot.setRotation((float)Math.toRadians(Robot.adjAngle2(p[5])));
	    
	}
	
	public void init(){
		draw();		
	}
	
    public World getWorld() {
    	return world;
    }
    
    public void setWorld(World world) {
    	this.world = world;
    }
    
    public SimBody getBall() {
    	return ball;
    }
    
    public void basic() {
        //basic is run when the button 'run' is clicked
        //give ball a force, simulating being kicked by a robot
        
    	world.add(ball);
        world.add(myRobot);
        world.add(oppRobot);
        
        myRobot.setDamping(0.06f);
        myRobot.setRotDamping(1.0f);
        oppRobot.setDamping(0.060f);
        oppRobot.setRotDamping(1.0f);
           
        world.addListener(lis);
        strategy.start();
       //ball.setForce(500, 500);
        cycle();
    }
    
    
    public void second(){
    	myRobot.setPosition(adjX(100),adjY(PITCH_HEIGHT/2)-10);
    	myRobot.setRotation(0);
        oppRobot.setPosition(adjX(500),adjY(PITCH_HEIGHT/2)-10);        
        ball.setPosition(adjX(PITCH_WIDTH/2),adjY(PITCH_HEIGHT/2));
    }
    
    public void penalty(){
    	myRobot.setPosition(adjX(460),adjY(PITCH_HEIGHT/2)-10);
    	myRobot.setRotation(0);
        oppRobot.setPosition(adjX(100),adjY(100));        
        ball.setPosition(adjX(520),adjY(PITCH_HEIGHT/2)-10);
    }
   
  //The world is updated on each cycle
    public void cycle() {
    	System.out.println("inside cycle!!");
    	
    	while (true) {
    		
    		draw();	
    		ball.setForce(2, 2);
    		
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

    		world.step();
    		if(counter>-1){
    			if (myRobot.cmds!=null){
    			if (counter<myRobot.cmds.size()){
    				myRobot.executeCommand(myRobot.cmds.get(counter));}
    		
    		/*
    		  
    		  if (counter<oppRobot.cmds.size()){
        		oppRobot.executeCommand(oppRobot.cmds.get(counter));}
        		
        		*/
    		}}
    	
    		
    		//System.out.println("counter: " + counter);
    		counter++;
    		
    		
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
	
	private void paintPitch(Graphics g){		//Draw pitch
		
		//this.setLayout(null);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(new Color(240,240,240));
		g2d.fillRect(0,0,850,500);
		
		g2d.setColor(new Color(0,0,0));		//black walls
		g2d.fillRect(0,0,FULL_WIDTH,FULL_HEIGHT);
		
		g2d.setColor(new Color(255,255,255));		
		g2d.fillRect(GOAL_WIDTH, 10, PITCH_WIDTH, PITCH_HEIGHT); // white around pitch
		
		g2d.setColor(new Color(51,204,0));		
		g2d.fillRect(GOAL_WIDTH+3, 10+3, PITCH_WIDTH-6, PITCH_HEIGHT-6); 	//pitch
				
		g2d.setColor(new Color(255,255,255));
		g2d.drawLine(FULL_WIDTH / 2, 10, FULL_WIDTH / 2, FULL_HEIGHT-11);	//centre line
				
		g2d.setColor(new Color(204,204,204));	//Draw goals
		g2d.fillRect(0, (FULL_HEIGHT-GOAL_HEIGHT)/2, GOAL_WIDTH, GOAL_HEIGHT);
		g2d.fillRect(FULL_WIDTH-GOAL_WIDTH, (FULL_HEIGHT-GOAL_HEIGHT)/2, GOAL_WIDTH, GOAL_HEIGHT);
		
		g2d.setColor(new Color(130,130,130));		//draw grids
		for (int l=1;l<widthInGrids;l++){
			g2d.drawLine(gridLength*l+GOAL_WIDTH, 10, gridLength*l+GOAL_WIDTH, PITCH_HEIGHT+9);
			}
		
		for (int m=1;m<heightInGrids;m++){
			g2d.drawLine(GOAL_WIDTH,gridWidth*m+9,PITCH_WIDTH+GOAL_WIDTH-1, gridWidth*m+9);
			}
		
		
		g2d.setColor(new Color(204,204,204));
		g2d.fillRect(0, FULL_HEIGHT, FULL_WIDTH, 100);
		
		g2d.setColor(cols[myRobot.color]);
		
		g2d.drawString("Robot1: "+adj2X((int)myRobot.getPosition().getX())+", "+adj2Y((int)myRobot.getPosition().getY()),10,FULL_HEIGHT+16);
		g2d.setColor(Color.black);
		g2d.drawString("Velocity x: "+roundDecimals(myRobot.getVelocity().getX(),6)+", Velocity y "+roundDecimals(myRobot.getVelocity().getY(),6),10,FULL_HEIGHT+28);
		g2d.drawString("Goals: 0",10,FULL_HEIGHT+40);
		g2d.drawString("Rotation: "+Robot.adjAngle(myRobot.getRotation()),10,FULL_HEIGHT+52);
		g2d.drawString("Force: "+myRobot.getForce().getX()+", "+myRobot.getForce().getY(),10,FULL_HEIGHT+64);
		g2d.drawString("Energy: "+roundDecimals(myRobot.getEnergy(),3),10,FULL_HEIGHT+76);
		
		g2d.drawString("Robot 1 ",FULL_WIDTH+105,20);
		g2d.drawString("Robot 2 ",FULL_WIDTH+105,85);
		g2d.drawString("ball ",FULL_WIDTH+105,170);
		
		g2d.setColor(cols[oppRobot.color]);
		g2d.drawString("Robot2: "+(adj2X((int)oppRobot.getPosition().getX()))+", "+adj2Y((int)oppRobot.getPosition().getY()),540,FULL_HEIGHT+16);
		g2d.setColor(Color.black);
		g2d.drawString("Velocity x: "+roundDecimals(oppRobot.getVelocity().getX(),4)+", Velocity y "+roundDecimals(oppRobot.getVelocity().getY(),4),540,FULL_HEIGHT+28);
		g2d.drawString("Goals: 0",540,FULL_HEIGHT+40);
		g2d.drawString("Rotation: "+Robot.adjAngle(oppRobot.getRotation()),540,FULL_HEIGHT+52);
		g2d.drawString("Force: "+oppRobot.getForce().getX()+", "+oppRobot.getForce().getY(),540,FULL_HEIGHT+64);
		g2d.drawString("Energy: "+roundDecimals(oppRobot.getEnergy(),3),540,FULL_HEIGHT+76);
		
		g2d.drawString("Match, 1st Half",PITCH_WIDTH/2-50,FULL_HEIGHT+16);
		g2d.drawString("00:00",PITCH_WIDTH/2-15,FULL_HEIGHT+28);
		g2d.drawString("Ball: "+adj2X((int)ball.getPosition().getX())+", "+adj2Y((int)ball.getPosition().getY()),PITCH_WIDTH/2-60,FULL_HEIGHT+40);
		g2d.drawString("Ball: "+ball.getVelocity().getX()+", "+ball.getVelocity().getY(),PITCH_WIDTH/2-60,FULL_HEIGHT+52);
		g2d.drawString("Force: "+ball.getForce().getX()+", "+ball.getForce().getY(),PITCH_WIDTH/2-60,FULL_HEIGHT+64);
		
		BodyList bl = world.getBodies();
		
		for(int i=0;i<bl.size();i++){
			//Paint the body
			if (!(bl.get(i) instanceof Wall)){	//we don't draw walls
			
			SimBody sb= (SimBody)bl.get(i);
			//sb.clear(); //
			
			if(sb.isBox()){
				//g2d.setColor(new Color(0,0,255));
				
				//g2d.fillRect(x, y, width, height);
				paintBox(sb,g2d);
			}
			else if(sb.isCircle()){
				
				int x = sb.getGraphicsX();
				int y = sb.getGraphicsY();
				int width = sb.getWidth();
				int height = sb.getHeight();
				
				//g2d.setColor(new Color(255,0,0));
				g2d.setColor(cols[sb.color]);
				g2d.fillOval(x, y, width, height);
			}
			
			//drawLines(g2d,sb);
		}
		}
	}
	
	public void drawLines(Graphics g2d, Body sb){
		if ((sb.getVelocity().getX()!=0)||(sb.getVelocity().getX()!=0)){
			((Graphics2D) g2d).rotate(Math.toDegrees((float)sb.getRotation())*Math.PI / 180.0, sb.getPosition().getX(),sb.getPosition().getY());
	        
	        g2d.setColor(cols[3]);

			g2d.drawLine((int)sb.getPosition().getX(),(int)sb.getPosition().getY(),(int)sb.getPosition().getX()+100,(int)sb.getPosition().getY());
	        ((Graphics2D) g2d).rotate(Math.toDegrees(-(float)sb.getRotation())*Math.PI / 180.0, sb.getPosition().getX(),sb.getPosition().getY());
	        }
	}
	
	
	//used in displaying parameters
	public static float roundDecimals(float d,int i) {
		
		if (i<1){
			return d;}
		
		String template = "#.";
		for (int j = 0; j<i;j++){
				template+="#";
		}
    	DecimalFormat newForm = new DecimalFormat(template);
    	
	return Float.valueOf(newForm.format(d));
	}
	
	public void paintBox2(SimBody sb, Graphics g2d) {
		 Box box = (Box) sb.getShape();
        Vector2f[] points = box.getPoints(sb.getPosition(), sb.getRotation());
        
        int[] xpoints = new int[points.length];
        int[] ypoints = new int[points.length];
        
        for(int c=0;c<points.length;c++){
            xpoints[c] =(int)points[c].x;
            ypoints[c]=(int)points[c].y;
        }
        
        //g2d.setColor(new Color(0,0,255));
        //g2d.setColor(cols[sb.color]);
        //g2d.fillPolygon(xpoints, ypoints, points.length);
        
        g2d.setColor(cols[4]);
        g2d.fillPolygon(xpoints, ypoints, points.length);
        
        
        ((Graphics2D) g2d).rotate(Math.toDegrees((float)sb.getRotation())*Math.PI / 180.0, (int)sb.getPosition().getX(),(int)sb.getPosition().getY());
        
       // g2d.setColor(cols[2]);
        //((Graphics2D) g2d).fillOval((int)sb.getPosition().getX()+20,(int)sb.getPosition().getY()-6, 12, 12);
        g2d.setColor(cols[3]);
        ((Graphics2D) g2d).fillOval((int)sb.getPosition().getX()-30,(int)sb.getPosition().getY()-6, 12, 12);
        
        g2d.setColor(cols[sb.color]);
        ((Graphics2D) g2d).fillRect((int)sb.getPosition().getX()-15,(int)sb.getPosition().getY()-20, 12, 40);
        ((Graphics2D) g2d).fillRect((int)sb.getPosition().getX()-15,(int)sb.getPosition().getY()-6, 40, 12);

        
        ((Graphics2D) g2d).rotate(Math.toDegrees(-(float)sb.getRotation())*Math.PI / 180.0, (int)sb.getPosition().getX(),(int)sb.getPosition().getY());
        
        //g2d.setColor(new Color(255,255,0));
        //g2d.fillPolygon(xpoints, ypoints, points.length);
	}
	
	
	
	public void paintBox(SimBody sb, Graphics g2d) {
		 /*Box box = (Box) sb.getShape();
         Vector2f[] points = box.getPoints(sb.getPosition(), sb.getRotation());
         
         int[] xpoints = new int[points.length];
         int[] ypoints = new int[points.length];
         
         for(int c=0;c<points.length;c++){
             xpoints[c] =(int)points[c].x;
            ypoints[c]= (int)points[c].y;
         }
         
         g2d.setColor(new Color(0,0,255));
         g2d.setColor(cols[sb.color]);
         g2d.fillPolygon(xpoints, ypoints, points.length);
         
         g2d.setColor(cols[4]);
         g2d.fillPolygon(xpoints, ypoints, points.length);
         */
         
         //((Graphics2D) g2d).rotate(sb.getRotation(), sb.getPosition().getX(),sb.getPosition().getY());
         ((Graphics2D) g2d).rotate(Math.toDegrees((float)sb.getRotation())*Math.PI / 180.0, (int)sb.getPosition().getX(),(int)sb.getPosition().getY());

         
         g2d.setColor(cols[4]);
         g2d.fillRect((int)sb.getPosition().getX()-27,(int)sb.getPosition().getY()-30, 54, 60);
        // g2d.setColor(cols[2]);
         //((Graphics2D) g2d).fillOval((int)sb.getPosition().getX()+20,(int)sb.getPosition().getY()-6, 12, 12);
         g2d.setColor(cols[3]);
         ((Graphics2D) g2d).fillOval((int)sb.getPosition().getX()-6,(int)sb.getPosition().getY()+15, 12, 12);
         
         g2d.setColor(cols[sb.color]);
         ((Graphics2D) g2d).fillRect((int)sb.getPosition().getX()-20,(int)sb.getPosition().getY(), 40, 12);
         ((Graphics2D) g2d).fillRect((int)sb.getPosition().getX()-6,(int)sb.getPosition().getY()-27, 12, 30);

         
         //((Graphics2D) g2d).fillRect(adjX((int)sb.getPosition().getX())+34,adjY((int)sb.getPosition().getY())-25, 4, 50);
        // Line2D l2= new Line2D.Float();
     	//l2.setLine(adjX((int)sb.getPosition().getX())+36,adjY((int)sb.getPosition().getY())-25,adjX((int)sb.getPosition().getX())+36,adjY((int)sb.getPosition().getY())+25);
     	//g2d.setColor(new Color(255,0,255));
     	//((Graphics2D) g2d).draw(l2);
     	//if(l2.intersects(adjX((int)ball.getPosition().getX()-7), adjY((int)ball.getPosition().getY()-7), 14, 14)){
         //   ((Graphics2D) g2d).fillRect((int)ball.getPosition().getX()-7, (int)ball.getPosition().getY()-7, 14, 14);
   	//} 
     		//ball.setPosition(adjX(565),adjY(200));  
         
        // ((Graphics2D) g2d).rotate(-sb.getRotation(),(int) sb.getPosition().getX(),(int)sb.getPosition().getY());
         ((Graphics2D) g2d).rotate(-Math.toDegrees((float)sb.getRotation())*Math.PI / 180.0, (int)sb.getPosition().getX(),(int)sb.getPosition().getY());

         //g2d.setColor(new Color(255,255,0));
         //g2d.fillPolygon(xpoints, ypoints, points.length);
	}

	
	public void run() {
		basic();
	} 

	public static int adjX(int x){
		return x+54;
	}
	
	public static int adjY(int y){
		return y+10;
	}
	
	public static int adj2X(int x){
		return x-54;
	}
	
	public static int adj2Y(int y){
		return y-10;
	}
	
	public boolean intersect(Robot r){
		Line2D l2= new Line2D.Float();
     	l2.setLine(adjX((int)r.getPosition().getX())+40,adjY((int)r.getPosition().getY())-25,adjX((int)r.getPosition().getX())+40,adjY((int)r.getPosition().getY())+25);
     	
     	if(l2.intersects(adjX((int)ball.getPosition().getX()-7), adjY((int)ball.getPosition().getY()-7), 20, 20)){
            //((Graphics2D) g2d).fillRect((int)ball.getPosition().getX()-7, (int)ball.getPosition().getY()-7, 14, 14);
     		return true;
     	} else {
     		
     		System.out.println("no:(");
     		return false;}
		
		
	}
	
	

	public void keyPressed(KeyEvent e) {
		
		myRobot.setMaxVelocity(99999, 99999);
		float moveForce = 450;
		int turnAngle = 5;
		
		System.out.println(e.getKeyCode());
		if (e.getKeyCode() == 37) {			//LEFT
			//myRobot.turnLeft(turnAngle);
			myRobot.strafeLeft(moveForce);
		}
		if (e.getKeyCode() == 38) {			//UP
			myRobot.forward(moveForce);
			//System.out.println(myRobot.getPosition());
		}
		if (e.getKeyCode() == 39) {			//RIGHT
			//myRobot.turnRight(turnAngle);
			myRobot.strafeRight(moveForce);
		}
		if (e.getKeyCode() == 40) {			//DOWN
			myRobot.backward(moveForce);
		}
		if (e.getKeyCode() == 44) {		// rotate left <
			myRobot.turnLeft(turnAngle);
		}
		if (e.getKeyCode() == 46) {		// rotate right >
			myRobot.turnRight(turnAngle);
		}
		if (e.getKeyCode() == 65) {			//RIGHT A
			oppRobot.turnLeft(turnAngle);
		}
		if (e.getKeyCode() == 87) {			//UP W
			oppRobot.forward(moveForce);
			
		}
		if (e.getKeyCode() == 68) {			//LEFT D
			oppRobot.turnRight(turnAngle);
			
		}
		if (e.getKeyCode() == 83) {			//DOWN S
			oppRobot.backward(turnAngle);
		}
		
	}

	public void keyReleased(KeyEvent e) {

		if (e.getKeyChar() == 'k') {
			BodyList bodies = this.getWorld().getBodies();
			for (int i = 0; i < bodies.size(); ++i) {
				if (bodies.get(i) instanceof Robot) {
					((Robot) bodies.get(i)).kick(ball);
				}
			}
		}
	
		if (e.getKeyCode() == 38) {			//UP
			myRobot.setMaxVelocity(0, 0);
		}
		
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private void setWalls(){
		walls[0] = new Wall("WallTop", PITCH_WIDTH,10);
		walls[1] = new Wall("WallBottom", PITCH_WIDTH,10);	
		walls[2] = new Wall( "WallLeft1", GOAL_WIDTH, 97);
		walls[3] = new Wall( "WallLeftGoal", GOAL_WIDTH/2, GOAL_HEIGHT);		
		walls[4] = new Wall( "WallLeft3", GOAL_WIDTH, 97);
		walls[5] = new Wall( "WallRight1", GOAL_WIDTH, 97);
		walls[6] = new Wall( "WallRightGoal", GOAL_WIDTH/2, GOAL_HEIGHT);
		walls[7] = new Wall( "WallRight3", GOAL_WIDTH, 97);
					
	    walls[0].setPosition(FULL_WIDTH/2, 5);
	    walls[1].setPosition(FULL_WIDTH/2, FULL_HEIGHT-5);
	    walls[2].setPosition(GOAL_WIDTH/2, 58);
	    walls[3].setPosition(GOAL_WIDTH/4, FULL_HEIGHT/2);
	    walls[4].setPosition(GOAL_WIDTH/2, FULL_HEIGHT-58);
	    walls[5].setPosition(FULL_WIDTH-GOAL_WIDTH/2, (FULL_HEIGHT/2-GOAL_HEIGHT/2)/2);
	    walls[6].setPosition(FULL_WIDTH-GOAL_WIDTH/4, FULL_HEIGHT/2);
	    walls[7].setPosition(FULL_WIDTH-GOAL_WIDTH/2, 10+FULL_HEIGHT-((FULL_HEIGHT/2-GOAL_HEIGHT/2)/2));
	    walls[7].setRestitution(1.0f);
	    //walls[6].setRestitution(0.0f);
	    //walls[6].setFriction(0);
			 
	        for(int i=0 ;i<walls.length ;i++){
	        	world.add(walls[i]);
	        }
	        
		ids[2] = walls[6].getID();	//captures ids used in collision listener
		ids[1]=walls[3].getID();
	}
	
	public void sendCommands(ArrayList<int[]> cmd){
		for (int i =0; i<cmd.get(0).length;i++){
			System.out.print(cmd.get(0)[i]+"  ");
		}
		System.out.println();
		myRobot.cmds = myRobot.getCommandsstrategy(cmd);
		System.out.print("commands received!");
		counter = -1;
	}
	
	
	public int[] getCoordinates() {
    	//return new int[]{ballX,ballY,0,ourX,ourY,ourAngle,oppX,oppY,oppAngle};
		return new int[]{adj2X((int)ball.getPosition().getX()),adj2Y((int)ball.getPosition().getY()),
				0,
				adj2X((int)myRobot.getPosition().getX()), adj2Y((int)myRobot.getPosition().getY()),
				Robot.adjAngle(myRobot.getRotation()),
				adj2X((int)oppRobot.getPosition().getX()), adj2Y((int)oppRobot.getPosition().getY()),
				Robot.adjAngle(oppRobot.getRotation())};
		}	
	
	
	public static Simulation getSimulation(){
		return simulation;
	}
	
	
}
