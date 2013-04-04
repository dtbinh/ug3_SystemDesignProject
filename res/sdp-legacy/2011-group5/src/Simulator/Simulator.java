package Simulator;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import Shared.ObjectInfo;
import Shared.ObjectInfo_Editable;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.Arbiter;
import net.phys2d.raw.ArbiterList;
import net.phys2d.raw.Body;
import net.phys2d.raw.BodyList;
import net.phys2d.raw.Contact;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;
import net.phys2d.raw.shapes.Line;
import net.phys2d.raw.strategies.QuadSpaceStrategy;

public class Simulator {
	
	public static int boardWidth = 630;
	public static int boardHeight = 330;
	public static int padding = 100;
	public static int wallThickness = 20;
	public static int goalWidth = 144;
	public static int goalThickness = 50;
	public static int robotStartX = padding + wallThickness;
	public static int oppRobotStartX = padding + boardWidth - wallThickness;
	public static int penaltyStartX = padding + boardWidth/2;
	public static int robotStartY = boardHeight/2 + padding;
	public static int ballStartX = boardWidth/2 + padding;
	public static int ballStartY = boardHeight/2 + padding;
	public static int goalPost1y = (boardHeight-goalWidth)/2 + padding - wallThickness/2;
	public static int goalPost2y = (boardHeight+goalWidth)/2 + padding + wallThickness/2;
	
	private static Robot robot;
	private static Robot oppRobot;
	private static Ball ball;
	private static Body leftGoalLine;
	private static Body rightGoalLine;
	private static int score1 = 0;
	private static int score2 = 0;
	private boolean goalScored = false;
	private static boolean penaltyMode = false;
	private static boolean takingPenalty = true;
	
	// used to control robot threads
	volatile Boolean moveForwardStop = true;
	volatile int turning = 0; // 0 for not turning, 1 for left, -1 for right
	volatile Boolean moveBackwardStop = true;
	char incCommand;
	volatile Boolean arcStop = true;
	volatile int radius = 0; // used for arc movement, determines direct of the arc, > 1000 it turns left otherwise right
	volatile int sweeping = 0; // 0 for stop, 1 for left, -1 for right
	Thread actionThread = new Thread() {
		public void run() {
			while(true){
				int temp = 0;
				while (!moveForwardStop)
					robot.moveForward(world, ball.getBody());
				while (!moveBackwardStop)
					robot.moveBackward(world, ball.getBody());	
				while (turning != 0) {
					if(temp<15){
						robot.turn(turning);
							temp++;
					} else {
						turning=0;
					}
					try {
						Thread.sleep(20);// higher amount of sleep gives better response from the planner
					} catch (Exception e) {
						System.out.println("Exception when trying to sleep: " + e.toString());
					}
				}
				while (!arcStop)
					robot.moveInArch(world, ball.getBody(), radius);
				while (sweeping != 0)
					robot.sweep(sweeping);
			}
		}
	};
	
	/** The frame displaying the simulation */
	private Frame frame;
	/** The title of the simulation */
	private String title;
	/** The world containing the physics model */
	private World world = new World(new Vector2f(0.0f, 10.0f), 10, new QuadSpaceStrategy(20,5));
	/** True if the simulation is running */
	private boolean running = true;
	/** The rendering strategy */
	private BufferStrategy strategy;
	/** True if we should reset the simulation on the next loop */
	private boolean needsReset;
	/** True if we should render normals */
	private boolean normals = true;
	/** True if we should render contact points */
	private boolean contacts = true;

	// things needed for listening to commands from the planner
	static Socket socket;
	static InputStreamReader is;
	static BufferedReader reader;
//	static PrintWriter writer;
	String sendThis = "";
	
	ObjectInfo_Editable objectInfo = new ObjectInfo_Editable();

	/**
	 * Create a new simulation
	 * 
	 * @param title The title of the simulation
	 */
	public Simulator(String title, Robot robot, Robot oppRobot, Ball ball) {
		this.title = title;
		Simulator.robot = robot;
		Simulator.oppRobot = oppRobot;
		Simulator.ball = ball;
	}

	/**
	 * Retrieve the title of the simulation
	 * 
	 * @return The title of the simulation
	 */
	public String getTitle() {
		return title;
	}

public static Simulator startSimulator() {
		BufferedImage blueImage = loadImage("data/blueRobotPhoto.jpeg");
		BufferedImage yellowImage = loadImage("data/yellowRobotPhoto.jpeg");
		
		int newRobotStartX = robotStartX;
		int newOppRobotStartX = oppRobotStartX;
		int newBallStartX = ballStartX;
		if (penaltyMode) {
			if (takingPenalty) { newRobotStartX = penaltyStartX; newBallStartX = ballStartX + 50; }
			  else { newOppRobotStartX = penaltyStartX; newBallStartX = ballStartX - 50; }
		}
				
		final Simulator sim = new Simulator("SDP World",
				new Robot(newRobotStartX, robotStartY+60, 70, 50, Color.BLUE, blueImage, 0),
				new Robot(newOppRobotStartX, robotStartY, 70, 50, Color.YELLOW, yellowImage, 180),
				new Ball(newBallStartX, ballStartY, 9, Color.RED, 0));
		Thread t = new Thread() {
			public void run() {
				setUpConnection();
				sim.start();
			};
		};
		
		t.start();
		
		return sim;
	}

	/**
	 * Notification that a key was pressed
	 * 
	 * Moves the robot if w, a, s or d pressed
	 * 
	 * @param c
	 *            The character of key hit
	 */

	private void keyHit(char c) {
		if (c == 'r') {
			needsReset = true;
		} else if (c == 'w') {
			oppRobot.moveForward(world, ball.getBody());
		} else if (c == 's') {
			oppRobot.moveBackward(world, ball.getBody());
		} else if (c == 'a') { // turn left
			oppRobot.turn(1);
		} else if (c == 'd') { // turn right
			oppRobot.turn(-1);
		} else if (c == 'z'){ // sweep left
			oppRobot.sweep(1);
		} else if (c == 'x'){ // sweep right
			oppRobot.sweep(-1);
		} else if (c == 'k') { // kick
			oppRobot.kick(ball); 
		} else if (c == 'q') { // set higher speed
			oppRobot.setSpeed(51);
		} else if (c == 'e') { // set lower speed
			oppRobot.setSpeed(50);
		}
	}

/**
 * Initialise the simulator - clear the world
 */
public final void initSimulation() {
	world.clear();
		world.setGravity(0, 0);
		
		robot.setAngle(0);
		oppRobot.setAngle(180);
		ball.stop();

		int newRobotStartX = robotStartX;
		int newOppRobotStartX = oppRobotStartX;
		int newBallStartX = ballStartX;
		if (penaltyMode) {
			if (takingPenalty) { newRobotStartX = penaltyStartX; newBallStartX = ballStartX + 50; }
			  else { newOppRobotStartX = penaltyStartX; newBallStartX = ballStartX - 50; }
		}
		robot.setPosition(newRobotStartX, robotStartY);
		oppRobot.setPosition(newOppRobotStartX, robotStartY);
		ball.setPosition(newBallStartX, ballStartY);

		
		System.out.println("Initialising:" + getTitle());
		init(world);
	}

	/**
	 * Adds the robot(s), ball and walls to the simulation
	 * 
	 * @param world
	 *            The world in which the simulation is going to run
	 */
	private void init(World world) {
		world.setGravity(0, 0);

//		Body topWall = new StaticBody("TopWall", new Box(
//				(boardWidth + 2 * wallThickness), wallThickness));
//		topWall.setPosition((boardWidth / 2 + padding),
//				(padding - wallThickness / 2));
//		topWall.setRestitution(1.0f);
//		world.add(topWall);
//		Body bottomWall = new StaticBody("BottomWall", new Box(
//				(boardWidth + 2 * wallThickness), wallThickness));
//		bottomWall.setPosition((boardWidth / 2 + padding), (boardHeight
//				+ padding + wallThickness / 2));
		
		// Set up top and bottom walls
		Body topWall = new StaticBody("TopWall", new Box((boardWidth + 2*wallThickness), wallThickness));
		topWall.setPosition((boardWidth/2 + padding), (padding - wallThickness/2));
		topWall.setRestitution(1.0f);
		world.add(topWall);
		Body bottomWall = new StaticBody("BottomWall", new Box((boardWidth + 2*wallThickness), wallThickness));
		bottomWall.setPosition((boardWidth/2 + padding), (boardHeight + padding + wallThickness/2));
		bottomWall.setRestitution(1.0f);
		world.add(bottomWall);

		// Set up left wall and goal
		Body topLeftWall = new StaticBody("TopLeftWall", new Box(wallThickness, ((boardHeight - goalWidth)/2 + wallThickness)));
		topLeftWall.setPosition((padding - wallThickness/2), ((goalPost1y + padding - (wallThickness/2))/2));
		topLeftWall.setRestitution(1.0f);
		world.add(topLeftWall);
		Body bottomLeftWall = new StaticBody("BottomLeftWall", new Box(wallThickness, ((boardHeight - goalWidth)/2 + wallThickness)));
		bottomLeftWall.setPosition((padding - wallThickness/2), ((padding + boardHeight + (wallThickness/2) + goalPost2y)/2));
		bottomLeftWall.setRestitution(1.0f);
		world.add(bottomLeftWall);
		Body topLeftGoal = new StaticBody("TopLeftGoal", new Box((goalThickness + 2*wallThickness), wallThickness));
		topLeftGoal.setPosition((padding - goalThickness/2 - wallThickness), goalPost1y);
		topLeftGoal.setRestitution(1.0f);
		world.add(topLeftGoal);
		Body bottomLeftGoal = new StaticBody("BottomLeftGoal", new Box((goalThickness + 2*wallThickness), wallThickness));
		bottomLeftGoal.setPosition((padding - goalThickness/2 - wallThickness), goalPost2y);
		bottomLeftGoal.setRestitution(1.0f);
		world.add(bottomLeftGoal);
		Body backLeftGoal = new StaticBody("BackLeftGoal", new Box(wallThickness, (goalWidth + 2*wallThickness)));
		backLeftGoal.setPosition((padding - goalThickness - 3*wallThickness/2), (padding + boardHeight/2));
		backLeftGoal.setRestitution(1.0f);
		world.add(backLeftGoal);
		
		// Set up right wall and goal
		Body topRightWall = new StaticBody("TopRightWall", new Box(wallThickness, ((boardHeight - goalWidth)/2 + wallThickness)));
		topRightWall.setPosition((boardWidth + padding + wallThickness/2), ((goalPost1y + padding - (wallThickness/2))/2));
		topRightWall.setRestitution(1.0f);
		world.add(topRightWall);
		Body bottomRightWall = new StaticBody("BottomRightWall", new Box(wallThickness, ((boardHeight - goalWidth)/2 + wallThickness)));
		bottomRightWall.setPosition((boardWidth + padding + wallThickness/2), ((padding + boardHeight + (wallThickness/2) + goalPost2y)/2));
		bottomRightWall.setRestitution(1.0f);
		world.add(bottomRightWall);
		Body topRightGoal = new StaticBody("TopRightGoal", new Box((goalThickness + 2*wallThickness), wallThickness));
		topRightGoal.setPosition((boardWidth + padding + goalThickness/2 + wallThickness), goalPost1y);
		topRightGoal.setRestitution(1.0f);
		world.add(topRightGoal);
		Body bottomRightGoal = new StaticBody("BottomRightGoal", new Box((goalThickness + 2*wallThickness), wallThickness));
		bottomRightGoal.setPosition((boardWidth + padding + goalThickness/2 + wallThickness), goalPost2y);
		bottomRightGoal.setRestitution(1.0f);
		world.add(bottomRightGoal);
		Body backRightGoal = new StaticBody("BackRightGoal", new Box(wallThickness, (goalWidth + 2*wallThickness)));
		backRightGoal.setPosition((boardWidth + padding + goalThickness + 3*wallThickness/2), (padding + boardHeight/2));
		backRightGoal.setRestitution(1.0f);
		world.add(backRightGoal);		
		
		leftGoalLine = new StaticBody("BackLeftGoal", new Box(1, (goalWidth + 2*wallThickness)));
		leftGoalLine.setPosition((padding), (padding + boardHeight/2));
		leftGoalLine.setRestitution(1.0f);
		world.add(leftGoalLine);
		rightGoalLine = new StaticBody("BackRightGoal", new Box(1, (goalWidth + 2*wallThickness)));
		rightGoalLine.setPosition((boardWidth + padding + 1), (padding + boardHeight/2));
		rightGoalLine.setRestitution(1.0f);
		world.add(rightGoalLine);
		
		ball.setGoalLines(leftGoalLine, rightGoalLine);
		ball.ignoreGoalLines();
		
		// Add ball and robot(s) to world
		world.add(robot.getBody());
		world.add(oppRobot.getBody());
		world.add(ball.getBody());

	}

	/**
	 * Initialise the GUI
	 */
	public void initGUI() {
		frame = new Frame(title);
		frame.setResizable(false);
		frame.setIgnoreRepaint(true);
		frame.setSize((boardWidth + 2*padding), (boardHeight + 2*padding));
		
		int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - (boardWidth + 2*padding)) / 2;
		int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - (boardHeight + 2*padding)) / 2;
		
		frame.setLocation(x,y);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				running = false;
				System.exit(0);
			}
		});
		frame.addKeyListener(new KeyAdapter() {
			private final Set<Integer> holonomicKeys = new HashSet<Integer>();
			public void keyTyped(KeyEvent e) {
//				keyHit(e.getKeyChar());
			}
			public void keyPressed(KeyEvent e) {
				keyHit(e.getKeyChar());
				if (e.getKeyCode() == 27) {
					System.exit(0);
				} else if ((e.getKeyCode() == 38) || (e.getKeyCode() == 37) || (e.getKeyCode() == 40) || (e.getKeyCode() == 39)){
					// 38 up, 37 left, 40 down, 39 right
					holonomicKeys.add(e.getKeyCode());
				}
				if (holonomicKeys.size() > 0){
					for (int key : holonomicKeys){
						if (key == 38)// up
							oppRobot.setPosition(oppRobot.getX(), oppRobot.getY()-oppRobot.speed);
						else if (key == 37)// left
							oppRobot.setPosition(oppRobot.getX()-oppRobot.speed, oppRobot.getY());
						else if (key == 40)// down
							oppRobot.setPosition(oppRobot.getX(), oppRobot.getY()+oppRobot.speed);
						else if (key == 39)// right
							oppRobot.setPosition(oppRobot.getX()+oppRobot.speed, oppRobot.getY());
					}
				}
			}
			public void keyReleased(KeyEvent e) {
				holonomicKeys.remove(e.getKeyCode());
			}

		});

		frame.setVisible(true);
		frame.createBufferStrategy(2);

		strategy = frame.getBufferStrategy();
	}

	/**
	 * Start the simulation running
	 */
	public void start() {
		initGUI();
		initSimulation();

		float target = 1000 / 60.0f;
		float frameAverage = target;
		long lastFrame = System.currentTimeMillis();
		float yield = 10000f;
		float damping = 0.1f;

//
		// starts the thread for listening to commands
		Thread incomingCommands = new Thread(new IncomingCommands());
		incomingCommands.start();

		while (running) {

			long timeNow = System.currentTimeMillis();
			frameAverage = (frameAverage * 10 + (timeNow - lastFrame)) / 11;
			lastFrame = timeNow;
			
			yield+=yield*((target/frameAverage)-1)*damping+0.05f;

			for(int i=0;i<yield;i++) {
				Thread.yield();
			}
			yield+=yield*((target/frameAverage)-1)*damping+0.05f;
			
						for(int i=0;i<yield;i++) {
							Thread.yield();
						}
			
			// render
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setColor(Color.GREEN);
			g.fillRect(0,0,(boardWidth + 2*padding),(boardHeight + 2*padding));
			
			draw(g);
			renderGUI(g);
			g.drawString("Team 1     " + score1 + " - " + score2 + "     Team 2", 200, 40);
			g.dispose();
			strategy.show();

			// update data model
			for (int i=0;i<5;i++) {
				world.step();
			}
			
			if (!goalScored) {
				if (ball.getX() < (padding - 5)) {
					ball.stayInGoal();
					++ score2;
					goalScored = true;
				}
				if (ball.getX() > (padding + boardWidth + 5)) {
					ball.stayInGoal();
					++ score1;
					goalScored = true;
				}
			}

			if (needsReset) {
				world.clear();
				ball.ignoreGoalLines();
				goalScored = false;
				initSimulation();
				needsReset = false;
				frameAverage = target;
				yield = 10000f;
			}
			sendInformation();
		}
	}
	/**
	 * Simulation GUI render
	 * 
	 * @param g The graphics context to use for rendering here
	 */
	private void renderGUI(Graphics2D g) {
		g.setColor(Color.black);
		g.drawString("R - Reset ball and robot positions",15,(padding + boardHeight + wallThickness + padding/2));
	}

	/**
	 * Draw the whole simulation
	 * 
	 * @param g
	 *            The graphics context on which to draw
	 */
	protected void draw(Graphics2D g) {
		BodyList bodies = world.getBodies();
		
		for (int i=0;i<bodies.size();i++) {
			Body body = bodies.get(i);

			drawBody(g, body);
		}

		ArbiterList arbs = world.getArbiters();
		
		for (int i=0;i<arbs.size();i++) {
			Arbiter arb = arbs.get(i);

			Contact[] contacts = arb.getContacts();
			int numContacts = arb.getNumContacts();

			for (int j = 0; j < numContacts; j++) {
				drawContact(g, contacts[j]);
			}
		}
	}

	/**
	 * Draw a body
	 * 
	 * @param g The graphics contact on which to draw
	 * @param body The body to be drawn
	 */
	private void drawBody(Graphics2D g, Body body) {
		if (body.getShape() instanceof Box) {
			drawBoxBody(g,body,(Box) body.getShape());
		}
		if (body.getShape() instanceof Circle) {
			drawCircleBody(g,body,(Circle) body.getShape());
		}
		if (body.getShape() instanceof Line) {
			drawLineBody(g,body,(Line) body.getShape());
		}
	}

	/**
	 * Draw a box in the world
	 * 
	 * @param g The graphics contact on which to draw
	 * @param body The body to be drawn
	 * @param box The shape to be drawn
	 */
	private void drawBoxBody(Graphics2D g, Body body, Box box) {
		Vector2f[] pts = box.getPoints(body.getPosition(), body.getRotation());

		Vector2f v1 = pts[0];
		Vector2f v2 = pts[1];
		Vector2f v3 = pts[2];
		Vector2f v4 = pts[3];

		if (body.getUserData() != null) {
			Robot r = (Robot) body.getUserData();
			BufferedImage img = r.getImage();
			if (img != null) {
				AffineTransform at = AffineTransform.getTranslateInstance(r.getX()-r.xSize/2,r.getY()-r.ySize/2);
				at.rotate(Math.toRadians(r.getAngle()), r.xSize/2,r.ySize/2);
		        g.drawImage(img, at, null);
			}
		} else {
			g.setColor(Color.BLACK);
			g.fillRect((int) v1.getX(), (int) v2.getY(),
					(int) (v3.getX() - v1.getX()),
					(int) (v4.getY() - v2.getY()));
		}
	}

	/**
	 * Draw a circle in the world
	 * 
	 * @param g The graphics contact on which to draw
	 * @param body The body to be drawn
	 * @param circle The shape to be drawn
	 */
	private void drawCircleBody(Graphics2D g, Body body, Circle circle) {
		g.setColor(Color.RED);
		float x = body.getPosition().getX();
		float y = body.getPosition().getY();
		float r = circle.getRadius();
		g.fillOval((int) (x-r), (int) (y-r), (int) (r*2), (int) (r*2));
	}

	/**
	 * Draw a line into the simulation
	 * 
	 * @param g The graphics to draw the line onto
	 * @param body The body describing the line's position
	 * @param line The line to be drawn
	 */
	private void drawLineBody(Graphics2D g, Body body, Line line) {
		g.setColor(Color.black);
		Vector2f[] verts = line.getVertices(body.getPosition(), body.getRotation());
		g.drawLine(
				(int) verts[0].getX(),
				(int) verts[0].getY(), 
				(int) verts[1].getX(),
				(int) verts[1].getY());
	}

	/**
	 * Draw a specific contact point determined from the simulation
	 * 
	 * @param g
	 *            The graphics context on which to draw
	 * @param contact
	 *            The contact to draw
	 */
	private void drawContact(Graphics2D g, Contact contact) {
		int x = (int) contact.getPosition().getX();
		int y = (int) contact.getPosition().getY();
		if (contacts) {
			g.setColor(Color.blue);
			g.fillOval(x-3,y-3,6,6);
		}

		if (normals) {
			int dx = (int) (contact.getNormal().getX() * 10);
			int dy = (int) (contact.getNormal().getY() * 10);
			g.setColor(Color.darkGray);
			g.drawLine(x, y, x + dx, y + dy);
		}
	}
	
	public static BufferedImage loadImage(String fileName) {
		BufferedImage img = null;
		if (fileName != null) {
			try {
				img = ImageIO.read(new File(fileName));
			} catch (IOException e) {
				System.out.println("Could not load image " + fileName);
			}
		}
		return img;
	}
	
	public void sendInformation() {
		// send information to the shared objects info class
		try {
			objectInfo.updateBall(new Point((int) ball.getBody().getPosition().getX(), (int) ball.getBody().getPosition().getY()));		
			objectInfo.updateBlueBot(new Point((int) robot.getX(), (int) robot.getY()), (int)robot.getRealAngle());
			objectInfo.updateYellowBot(new Point((int) oppRobot.getX(), (int) oppRobot.getY()), (int)oppRobot.getRealAngle());
		} catch(Exception e){
			e.printStackTrace(); 
		}
	}
	public void pausAllThreads() {
		moveForwardStop = true;
		moveBackwardStop = true;
		turning = 0;
		arcStop = true;
		sweeping = 0;
	}

	public static void setUpConnection() {
		try {
			socket = new Socket(InetAddress.getLocalHost(), 5346);
			is = new InputStreamReader(
					socket.getInputStream());
			reader = new BufferedReader(is);
//			writer = new PrintWriter(socket.getOutputStream());
			System.out.println("Simulator: connection succesful");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// only for commands that take two values
	public void interpretCommandsWithTwoValues(char command, int value){
		if (command == 'R') {
			System.out.println("Simulator: making robot turn");
			double degrees = Math.toDegrees(value);
			degrees = robot.convertAngle(degrees);
			if (degrees > 180)
				turning = -1;
			else
				turning = 1;
		} else if (command == 'S') {
			System.out.println("Simulator: changing speed, value: "+value);
			robot.setSpeed(value);
		}
		else if (command == 'A') {
			System.out.println("Simulator: making robot arc");
			radius = value;
			arcStop = false;
		} else if(command == 'c') {
			// TODO steer with ration
		}
		else {
			// this should not happen, but who knows
			System.out.println("Simulator: Unknown command with two values: " + command);
		} 
	}

	public class IncomingCommands implements Runnable {
		public void run() {
			int intCommand;
			char command;
			char oldCommand = ' ';
			String ett;
			Boolean listeningForValue = false;
			if (!actionThread.isAlive()) {
				System.out.println("Simulator: starting action thread");
				actionThread.start();
			}
			try {
				while (true) {
					intCommand = reader.read();
					ett = reader.readLine(); // does nothing, but without this line it doesn't work
					command = (char) intCommand;
					if (!(command == 'o')) {	
						incCommand = command;
						if (!listeningForValue){
							System.out.println("Simulator: Incoming command: " + command);
							pausAllThreads();
							if (command == 'f') {
								System.out.println("Simulator: making robot move forward");
								moveForwardStop = false;
							} else if (command == 'b') {
								System.out.println("Simulator: making robot move back");
								moveBackwardStop = false;
							} else if (command == 'B') {
								// TODO move back slightly
							} else if (command == ')') {
								System.out.println("Simulator: making robot sweep");
								sweeping = 1;
							} else if (command == '(') {
								System.out.println("Simulator: making robot sweep");
								sweeping = -1;
							}else if (command == 's') {
								System.out.println("Simulator: making robot stop");
							} else if (command == 'K') {
								System.out.println("Simulator: calling kick function");
								robot.kick(ball);
							} else if (command == '#') {
								System.out.println("Simulator: Beep");
							} else if (command == '!') {
								System.out.println("Simulator: Planner thinks that it scored a goal, *celebrating*!!!");
							} else if (command == 'R' || command == 'S' || command == 'A' || command == 'c') {
								oldCommand = command;
								listeningForValue = true;
								continue;
							} else {
								System.out.println("Simulator: Unknown command received: "+ command);
							}
						} else {
							System.out.println("Simulator: Incoming value: " + intCommand);
							listeningForValue = false;
							interpretCommandsWithTwoValues(oldCommand, intCommand);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public ObjectInfo getObjectInfos()
	{
		return objectInfo;
	}
}
