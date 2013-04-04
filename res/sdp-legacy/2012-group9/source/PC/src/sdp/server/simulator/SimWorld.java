package sdp.server.simulator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import sdp.server.RobotInterface;
import sdp.server.RobotState;
import sdp.server.RobotState.Team;
import sdp.server.math.Vector2;

/**
 * SimWorld extends World, to provide a simulated environment for the Robot. Rather than using the physical
 * world, SimWorld provides a means for testing various strategies, logic and control of the system through
 * a rudimentary physics simulation.
 * 
 * @author ronan, kieran
 */
public class SimWorld extends sdp.server.World implements ContactListener {
	private static final Vec2 ROBOT_SIZE = new Vec2( 0.2f, 0.13f );
	
	// The Box2D World
	private World world;
	
	// The gravity vector for the world
	private Vec2 gravity = new Vec2( 0f, 0f );
	
	// Both robots and a ball
	private RobotBody robotY;
	private RobotBody robotB;
	private Ball ball;

	// contains the initial positions of movable objects
	private Vec2 ballPos;
	
	private Vec2 robotYPos;
	private float robotYAngle;

	private Vec2 robotBPos;
	private float robotBAngle;
	
	private RobotInterface[] interfaces = { new RobotInterface(), new RobotInterface() };
	
	// Our walls
	private PolygonShape horizWall;
	private PolygonShape vertWall;
	
	// *All* the wall stuff!
	//private Fixture		 upperFixture, lowerFixture, leftFixture, rightFixture;
	private FixtureDef	 upperFixtureDef, lowerFixtureDef, leftFixtureDef, rightFixtureDef;
	private BodyDef	 	 upperDef, 	   lowerDef,     leftDef,     rightDef;
	private Body		 lowerWall,    upperWall,    leftWall,    rightWall;

	// MouseJoint related items
	//private Body 		ground;
	private Fixture		mouse_object;
		
	// Our GUI elements
	private BufferedImage image;
	
	// Stores the last known mouse position - only if we're currently dragging the mouse around,
	// this is reset to null whenever the mouse is released.
	private Vec2 mouse_position = null;
	
	// Determines if the simulation is currently paused (ignoring update requests)
	private boolean isPaused = false;

	//private String mode;

	private int countB = 100;
	private int countY = 100;
	
	/**
	 * This constructs our simulated world and creates a window to view the
	 * simulation in. 
	 * 
	 * @param settings The world settings to use for our environment.
	 */
	public SimWorld( sdp.server.World.Settings settings ) {
		super( settings );
		
		pitchWidth         = (float)settings.pitchTopRight.getX() * 2.0f;
		pitchHeight        = (float)settings.pitchTopRight.getY() * 2.0f;
		scaleFactor        = panelWidth / pitchWidth;
		horizWallThickness = 0.065f;                                
		vertWallThickness  = 0.05f;                                 
		horizWallHalfSize  = pitchWidth * 0.5f - horizWallThickness;
		vertWallHalfSize   = pitchHeight * 0.5f - vertWallThickness;
		
		
		
		image = new BufferedImage( panelWidth, panelHeight, BufferedImage.TYPE_3BYTE_BGR );
				
		initialiseImages();
		setMode("friendly");
		createWorld();
		
		// FIXME: Hard-coded screen projection
		getScreenProjection().setScale( 1.0 / scaleFactor );
		getScreenProjection().setScreenSize( new Vector2( panelWidth, panelHeight ) );
	}
	

	@Override
	public RobotInterface getRobotInterface( Team team ) {
		return interfaces[ team.ordinal() ];
	}
	
	
	/**
	 * Resets the various bodies to their initial positions - essentially 'reseting' the simulation.
	 */
	public void reset() {
		setProperties(ball.getBody(), ballPos, 0, 0, new Vec2(0,0));
		robotY.moveTo( robotYPos );
		robotB.moveTo( robotBPos );
		setProperties(robotY.getBody(), robotYPos, robotYAngle, 0, new Vec2(0, 0));
		setProperties(robotB.getBody(), robotBPos, robotBAngle, 0, new Vec2(0,0));
	}

	/**
 	 * Convenience method used to set all the appropriate properties of a given body in the world to
 	 * the passed values. Used by the reset method to easily reset all bodies in the world.
 	 * 
 	 * @param body 			The body to set details of.
 	 * @param position		The position to assign to the body center.
 	 * @param rotation		The angle of rotation of the body.
 	 * @param angular_vel	The angular velocity of the body.
 	 * @param linear_vel	The linear velocity of the body.
 	 */
	void setProperties(Body body, Vec2 position, float rotation, float angular_vel, Vec2 linear_vel) {
		
		body.getFixtureList().setFriction(0);
		
		body.setTransform(position, rotation);
		body.setAngularVelocity(angular_vel);
		body.setLinearVelocity(linear_vel);
	}
	
	public boolean isPaused() {
		return isPaused;
	}
	
	public void setPaused( boolean paused ) {
		isPaused = paused;
	}
	
	public void dispose() {
		
	}
	
	/**
	 * This method attempts to find an object in the simulated environment that
	 * is below the stored mouse position - obvious usage is to click and drag objects
	 * around.
	 * 
	 * If an object is found, it is stored in mouse_object.
	 */
	public void findMouseTarget( int x, int y ) {
		// Find the position of the mouse in Box2D world.
		mouse_position = new Vec2( x, y );
		Vec2 mouseInWorld = getScreenProjection().unprojectPosition( new Vector2( mouse_position ) ).getVec2();
		
		// Create a bounding box for the mouse, width & height = 0.01 m.
		AABB mousePlane = new AABB(mouseInWorld.add(new Vec2(-0.01f, -0.01f)), mouseInWorld.add(new Vec2(0.01f, 0.01f)));
		
		// Check in the world if we overlap any shapes
		ClickedObjectCallback callback = new ClickedObjectCallback(mouseInWorld);
		world.queryAABB(callback, mousePlane);
		
		
		
		// If we haven't found any colliding shapes, return (and make the mouse point null)
		if (callback.clicked_fixture == null) {			
			mouse_position = null;
			return;
		} else {
			Body body = callback.clicked_fixture.getBody();
			if( body != ball.getBody() && body != robotY.getBody() && body != robotB.getBody() ) {
				mouse_position = null;
				return;
			}
		}
		
		// The fixture we clicked in the world
		mouse_object = callback.clicked_fixture;
	}
	
	public void dragMouseTarget( int x, int y ) {
		if( mouse_position != null ) {
			mouse_position = new Vec2( x, y );
		}
	}
	
	/**
	 * Nullifies mouse position and mouse object to indicate that no object is currently being
	 * moved around.
	 */
	public void removeMouseTarget() {
		mouse_position = null;
		mouse_object   = null;
	}
	
	/** Initialises the world and populates the world with the desired bodies. 
	 */
	private void createWorld() {
		// Create our world
		world = new World(gravity, true);
		
		ball = new Ball(world, imgBall, ballPos, scaleFactor);
		robotY = new RobotBody(world, imgYellowRobot, ROBOT_SIZE, robotYPos, scaleFactor);
		robotB = new RobotBody(world, imgBlueRobot, ROBOT_SIZE, robotBPos, scaleFactor );
		
		robotY.getBody().setTransform(robotY.getBody().getPosition(), robotYAngle);
		robotB.getBody().setTransform(robotB.getBody().getPosition(), robotBAngle);

		// Create our wall objects
		upperDef			= new BodyDef();
		upperDef.type   	= BodyType.STATIC;
		upperDef.position.set(new Vec2( 0, pitchHeight / 2.0f - vertWallThickness ));
		
		lowerDef			= new BodyDef();
		lowerDef.type 		= BodyType.STATIC;
		lowerDef.position.set(new Vec2( 0, - pitchHeight / 2.0f + vertWallThickness ));
		
		leftDef				= new BodyDef();
		leftDef.type        = BodyType.STATIC;
		leftDef.position.set(new Vec2( - pitchWidth / 2.0f + horizWallThickness, 0 ));
		
		rightDef			= new BodyDef();
		rightDef.type		= BodyType.STATIC;
		rightDef.position.set(new Vec2( pitchWidth / 2.0f - horizWallThickness, 0 ));
		
		// Create our wall bodies
		upperWall = world.createBody(upperDef);
		upperWall.setUserData("Wall: Upper");
		
		lowerWall = world.createBody(lowerDef);
		lowerWall.setUserData("Wall: Lower");
		
		rightWall = world.createBody(rightDef);
		rightWall.setUserData("Wall: Right");
		
		leftWall  = world.createBody(leftDef);
		leftWall.setUserData("Wall: Left");
		
		horizWall = new PolygonShape();
		vertWall  = new PolygonShape();
		
		// Set vertical and horizontal wall sizes
		horizWall.setAsBox(this.horizWallHalfSize * 2.0f, 0.01f);
		vertWall.setAsBox( 0.01f, this.vertWallHalfSize * 2.0f);
		
		upperFixtureDef 			= new FixtureDef();
		upperFixtureDef.density 	= 1;
		upperFixtureDef.friction 	= 0.3f;
		upperFixtureDef.shape 		= horizWall;
		
		lowerFixtureDef 			= new FixtureDef();
		lowerFixtureDef.density 	= 1;
		lowerFixtureDef.friction 	= 0.3f;
		lowerFixtureDef.shape 		= horizWall;
		
		leftFixtureDef 			= new FixtureDef();
		leftFixtureDef.density 	= 1;
		leftFixtureDef.friction 	= 0.3f;
		leftFixtureDef.shape 		= vertWall;
		
		rightFixtureDef 			= new FixtureDef();
		rightFixtureDef.density 	= 1;
		rightFixtureDef.friction	= 0.3f;
		rightFixtureDef.shape		= vertWall;
		
		// Attach fixtures
		upperWall.createFixture(upperFixtureDef);
		lowerWall.createFixture(lowerFixtureDef);
		leftWall.createFixture(leftFixtureDef);
		rightWall.createFixture(rightFixtureDef);
		
		// Create contacts for the wall
		world.setContactListener(this);
		
	}

	public BufferedImage getImage() {
		return image;
	}
	
	public void drawImage() {
		Graphics g = image.getGraphics();
		
		// Draw the pitch
		g.drawImage(imgBackground, 0, 0, panelWidth, panelHeight, null);
		
		// Draw the walls
		g.setColor( Color.red);
					
		drawWall(g, upperWall, false );
		drawWall(g, lowerWall, false );
		drawWall(g, leftWall, true );
		drawWall(g, rightWall, true );
		
		// Draw the robots and the ball
		drawBall( g );
		drawRobot( g, robotY, RobotState.Team.YELLOW );
		drawRobot( g, robotB, RobotState.Team.BLUE );
	}
	
	/**
	 * Convenience method used to draw the given wall body at it's appropriate position.
	 * 
	 * @param g			The graphics to draw the body at.
	 * @param wall		The actual wall to draw.
	 * @param isVert	Is this wall a vertical or horizontal line?
	 */
    public void drawWall( Graphics g, Body wall, boolean isVert) {
    	// Get the wall's centre position.
		Vector2 centre = new Vector2( wall.getPosition() );
	    Vector2 halfSize;
	    if( isVert ) {
	    	// If we have a vertical wall, set the size to a vertical line.
	    	halfSize = new Vector2(0.0f, vertWallHalfSize );
	    } else {
	    	// If we have a vertical wall, set the size to a horizontal line.
	    	halfSize = new Vector2( horizWallHalfSize, 0.0f );
	    }
	    	
    	Vector2 p1 = getScreenProjection().projectPosition( centre.minus( halfSize ) );
    	Vector2 p2 = getScreenProjection().projectPosition( centre.plus( halfSize ) );
    	
    	g.drawLine( p1.getIntX(), p1.getIntY(), p2.getIntX(), p2.getIntY() );
		
	}
    
    public void drawRobot( Graphics g, RobotBody robot, RobotState.Team team ) {
    	// Convert the world position of the robot to our screen coordinates
		Vector2 screenPos   = getScreenProjection().projectPosition( new Vector2( robot.getBody().getPosition() ) );
		double  screenAngle = getScreenProjection().projectAngle( robot.getBody().getAngle() );
		
		BufferedImage image;
		if( team == RobotState.Team.YELLOW )
			image = imgYellowRobot;
		else
			image = imgBlueRobot;
		
		
		AffineTransform transform = new AffineTransform();
		
		// Translate to the upper left of the robot, if we haven't rotated
		transform.translate( screenPos.getX() - image.getWidth()/2, screenPos.getY() - image.getHeight()/2 );
		
		// Rotate the transform (anchor around centre of the image)
		transform.rotate( screenAngle, image.getWidth() / 2, image.getHeight() / 2 );
		
		// Draw our image, using our transform
		((Graphics2D) g).drawImage(image, transform, null);
		g.setColor(Color.red);
		g.drawArc( (int)screenPos.getX() - 10 , (int)screenPos.getY() - 10, 20, 20, 0, 360);
		
		Vector2 p2 = screenPos.plus( Vector2.fromAngle(screenAngle, 70) );
		g.drawLine( screenPos.getIntX(), screenPos.getIntY(), p2.getIntX(), p2.getIntY());
		
		
		g.setColor( Color.red );
		
		Body wheelBody = robot.getLeftWheel().getBody();
		drawVertices( g, wheelBody, ((PolygonShape)wheelBody.getFixtureList().getShape()).getVertices() );
		
		wheelBody = robot.getRightWheel().getBody();
		drawVertices( g, wheelBody, ((PolygonShape)wheelBody.getFixtureList().getShape()).getVertices() );
		
		Body kicker = robot.getKicker();
		drawVertices( g, kicker, (((PolygonShape) kicker.getFixtureList().getShape()).getVertices()));
		
		/*
		Vec2[] kickerVertices = ((PolygonShape) kicker.getFixtureList().getShape()).getVertices();
		g.setColor(Color.blue);
		drawVertices(g,kicker,kickerVertices, screenSize, scale);
		
		Vec2[] mountVertices = ((PolygonShape) mount.getFixtureList().getShape()).getVertices();
		g.setColor(Color.green);
		drawVertices(g,mount,mountVertices, screenSize, scale);
		
		leftWheel.draw(g, screenSize, scale);
		rightWheel.draw(g, screenSize, scale);
		
		
		
		*/
    }
    
    private void drawVertices( Graphics g, Body body, Vec2[] vertices ) {	
		for (int i = 0; i < vertices.length - 1; i++) {
		
			// Retrieve the current and next vertex
			Vec2 v1 = body.getWorldPoint( vertices[i] );
			Vec2 v2 = body.getWorldPoint( vertices[i+1] );
		
			// Convert vertex to screen coordinate space
			Vector2 v1Screen = getScreenProjection().projectPosition( new Vector2( v1 ) );
			Vector2 v2Screen = getScreenProjection().projectPosition( new Vector2( v2 ) );
		
			g.drawLine( v1Screen.getIntX(), v1Screen.getIntY(), v2Screen.getIntX(), v2Screen.getIntY() );
		}
	}
    
    public void drawBall( Graphics g ) {
    	g.setColor( Color.red );

		// Convert the position of the ball to the screen coordinate system.
		Vector2 screenPosition = getScreenProjection().projectPosition( new Vector2( ball.getBody().getPosition() ) );
		
		// Convert the radius to a pixel size.
		int radius = (int)( 0.02f / getScreenProjection().getScale() );
		
		g.drawImage( imgBall, screenPosition.getIntX()-radius, screenPosition.getIntY()-radius, null );
    }

	/** This method loads the images for the simulation
	 * from the files.
	 */
	private void initialiseImages() {
		try {
			imgBackground 	= ImageIO.read(new File(pathBackground));
			imgYellowRobot  = ImageIO.read(new File(pathYellowRobot));
			imgBlueRobot 	= ImageIO.read(new File(pathBlueRobot));
			imgBall			= ImageIO.read(new File(pathBall));
		} catch (IOException ex) {
			
			// Print our error
			ex.printStackTrace();
			return;
		}
	}
	 
	@Override
	public void beginContact(Contact contact) {
		// Do nothing - not interested in begin contact
	}
	
	@Override
	public void endContact(Contact contact) {
		// Do nothing - not interested in begin contact
	}
	
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// Do nothing - not interested in post-solve
	}
	
	/**
	 * This method is a callback called from the world whenever two contacts
	 * collide. This is the preSolve method which is prior to the simulation
	 * correcting the positioning of the bodies in the world.
	 * 
	 * @param contact 		The contact object abstracting the collision details.
	 * @param oldManifold 	The previous manifold containing the normal of collision.
	 */
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Fixture ball_f;
		//Fixture other_f;
		
		// Retrieve and store user data of the bodies connected to fixtures
		Object fixtureAData = contact.getFixtureA().getBody().m_userData;
		Object fixtureBData = contact.getFixtureB().getBody().m_userData;
		
		// If either user data is null - we're not interested
		if (fixtureAData == null || fixtureBData == null) return;
		
		// If this is not a ball collision, ignore
		if (fixtureAData.equals("Ball")) {
			
			// Check the other fixture is a wall
			if (!((String)fixtureBData).contains("Wall"))
				return;
			
			// Store the fixtures to their respective variables
			ball_f = contact.getFixtureA();
			//other_f = contact.getFixtureB();
			
		} else if (fixtureBData.equals("Ball")) {
			
			// Check the other fixture is a wall
			if (!((String)fixtureAData).contains("Wall"))
				return;

			// Store the fixtures to their respective variables
			ball_f = contact.getFixtureB();
			//other_f = contact.getFixtureA();
		} else {
			return;
		}
		
		// Find the normal of the contact
		WorldManifold manifold = new WorldManifold();
		contact.getWorldManifold(manifold);
		
		// Reverse the normal of the ball and apply force to make it 'bounce'
	    if (ball_f == contact.getFixtureA()) {
	    	Vec2 impulse = manifold.normal.mul( Vec2.dot( ball.ball.getLinearVelocity(), manifold.normal ) * ball.ball.getMass() );
	    	impulse = impulse.mul( 2.0f );
	    	ball.ball.applyLinearImpulse( impulse, ball.ball.getPosition());
	    } else {
	    	Vec2 impulse = manifold.normal.mul( Vec2.dot( ball.ball.getLinearVelocity(), manifold.normal ) * ball.ball.getMass() );
	    	impulse = impulse.mul( -2.0f );
	    	ball.ball.applyLinearImpulse( impulse, ball.ball.getPosition());
	    }
	}
	
	/**
	 * The update method is an implementation of the base world's update method which
	 * performs the appropriate movement of objects in the world on each time step.
	 */
	@Override
	public void update() {
		int   nSimStepsPerUpdate = 4;
		float fpsLimit           = 25.0f;
		float simTimestep        = 1f / ( nSimStepsPerUpdate * fpsLimit );
		
		// Our 'hacky' pause function
		if (isPaused) return;
	
		//This locks the current rotation of the object two robots 
		//can be useful when dragging with mouse
		//robotY.getBody().setFixedRotation(panel.mouseFix);
		//robotB.getBody().setFixedRotation(panel.mouseFix);
		
		// Update the mouse joint's position if it exists
		if (mouse_object != null) {
			Body object = mouse_object.getBody();
			Vec2 newPos = getScreenProjection().unprojectPosition( new Vector2( mouse_position ) ).getVec2();
			
			if( object == robotY.getBody() ) {
				robotY.moveTo( newPos );
			} else if( object == robotB.getBody() ) {
				robotB.moveTo( newPos );
			} else {
				object.setTransform( newPos, object.getAngle() );
				object.setLinearVelocity( new Vec2( 0.0f, 0.0f ) );
			}
							
		}
		
		// CRISTI
		Vector2 ws = getRobotInterface( Team.BLUE ).getWheelSpeeds();
		
		float ct = 0.025f;
		
		robotB.update( ( float ) ws.getX() * ct, ( float ) ws.getY() * ct );
		
		ws = getRobotInterface( Team.YELLOW ).getWheelSpeeds();
		robotY.update( ( float ) ws.getX() * ct, ( float ) ws.getY() * ct );
		
		// Assign the yellow robots' position and rotation to the player.
		Vector2 pos = new Vector2( robotY.getBody().getPosition() );
		double  rot = robotY.getBody().getAngle();
		robotStates[ Team.YELLOW.ordinal() ].update( pos, rot, getTimeStep() );
		
		pos = new Vector2( robotB.getBody().getPosition() );
		rot = robotB.getBody().getAngle();
		robotStates[ Team.BLUE.ordinal() ].update( pos, rot, getTimeStep() );
		
		// Assign the ball's position to the ball state
		pos = new Vector2( ball.getBody().getPosition() );
		getBallState().update( pos, getTimeStep() );
		
		resolveKick();
		getRobotInterface(Team.BLUE).update();
		getRobotInterface(Team.YELLOW).update();
		
		for( int i = 0 ; i < nSimStepsPerUpdate ; ++ i )
			world.step( simTimestep, 6, 2 );
			
		// /CRISTI
		
		// Repaint the simulation
		drawImage();
		
		try {
			Thread.sleep( (long)( Math.max( 1.0 / fpsLimit - getTimeStep(), 0.0 ) * 1000.0 ) );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This class provides the ability to check if the fixtures of our world collide
	 * with a given point (the mouse) and store the fixture if it does. Allowing us
	 * to then use this to attach a MouseJoint.
	 * 
	 * @author Ronan
	 */
	public class ClickedObjectCallback implements QueryCallback {
		
		public Vec2 mouse_point;
		
		// The fixture that the click is contained in - will be null
		// if no fixture is found.
		public Fixture clicked_fixture;
		
		public ClickedObjectCallback(Vec2 mouse_point) {
			this.mouse_point = mouse_point;
			clicked_fixture = null;
		}
		
		/**
		 * This method is called for every fixture in the world. It allows us
		 * to test if the given fixture collides with our mouse position and
		 * store it if so.
		 * 
		 * Returning false specifies to the caller that we've found a fixture and
		 * don't need it to continue calling for other fixtures. True continues.
		 */
		@Override
		public boolean reportFixture(Fixture fixture) {
			
			// Test if our mouse point is inside the fixture.
			if (fixture.testPoint(mouse_point)) {
				
				clicked_fixture = fixture;
				return false;
			}
			
			return true;
		}
	}
	
	public void setMode(String mode) {
		if (mode == "friendly"){
			System.out.println("Changed to Friendly mode...");
			ballPos = new Vec2( 0.0f, 0.0f );
			
			robotYPos = new Vec2( 0.2f - pitchWidth / 2.0f, 0.0f );
			robotYAngle = 0;
			
			robotBPos = new Vec2( pitchWidth / 2.0f - 0.2f, 0.0f );
			robotBAngle = (float) Math.PI;
			
		} else if ( mode == "penalty"){
			System.out.println("Changed to Penalty mode...");
			ballPos = new Vec2(0.60f - pitchWidth / 2.0f, 0.0f);
			
			robotYPos = new Vec2( 0.2f - pitchWidth / 2.0f, 0.0f );
			robotYAngle = (float) Math.PI/2;
			
			robotBPos = new Vec2( 0.8f - pitchWidth / 2.0f, 0.0f );
			robotBAngle = (float) Math.PI;
		
		} else {
			System.out.println("there has been an error in choosing mode.");
		}
		
	}
	
	public float getScaleFactor () {
		return this.scaleFactor;
	}
	
	public float getPitchWidth () {
		return this.pitchWidth;
	}
	
	public float getPitchHeight () {
		return this.pitchHeight;
	}
	
	public int getPanelWidth () {
		return this.panelWidth;
	}
	
	public int getPanelHeight () {
		return this.panelHeight;
	}
	
	public RobotBody getRobotFromTeam(Team team){
		if (team.ordinal() == 0) return robotB;
		else return robotY;
		
	}
	
	public void resolveKick(){
		
		if (getRobotInterface(Team.BLUE).shouldKick()){
			robotB.activateKicker();
			countB = 0;
		} else if ( !(getRobotInterface(Team.BLUE).shouldKick()) && countB < 1){
			robotB.reverseKicker();
			countB = countB + 1;
		} else {
			robotB.deactivateKicker();
		}
		
		if (getRobotInterface(Team.YELLOW).shouldKick()) {
			robotY.activateKicker();
			countY = 0;
		} else if ( !(getRobotInterface(Team.YELLOW).shouldKick()) && countY < 1){
			robotY.reverseKicker();
			countY = countY + 1;
		} else {
			robotY.deactivateKicker();
		}
	}
	
	// The images and their respective paths
	private static final String pathBackground 	= "../PC/Data/bg.png";
	private static final String pathYellowRobot = "../PC/Data/yellow_robot.png";
	private static final String pathBlueRobot 	= "../PC/Data/blue_robot.png";
	private static final String pathBall 		= "../PC/Data/ball.png";
	
	private BufferedImage imgBackground;
	private BufferedImage imgYellowRobot;
	private BufferedImage imgBlueRobot;
	private BufferedImage imgBall;
	
	// Panel dimensions - fixed
	private final int panelWidth 	= 800;
	private final int panelHeight 	= 400;
	
	// Our pitch size - fixed dimensions
	private float pitchWidth;
	private float pitchHeight;
	
	// Our scaling factor, metres -> pixels
	private float scaleFactor;

	//The sizes of horizontal and vertical walls
	//private final float horizWallSize = pixels2Metres( panelWidth/2 - 21, scaleFactor);
	//private final float vertWallSize  = pixels2Metres( panelHeight/2 - 16, scaleFactor );
	
	private float horizWallThickness;
	private float vertWallThickness;
	private float horizWallHalfSize;
	private float vertWallHalfSize;
}
