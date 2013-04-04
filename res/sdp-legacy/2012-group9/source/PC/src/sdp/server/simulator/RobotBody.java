package sdp.server.simulator;


import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.PrismaticJoint;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;


/**
 * The RobotBody class provides a means for encapsulating the various
 * Bodies, Fixtures and Shapes that a robot owns when simulating it
 * in the Box2D physics world.
 * 
 * @author Ronan, Kieran
 *
 */
public class RobotBody {

	// The dimensions of the robot, in metres.
	private Vec2 size;
	
	// An image representing this robot
	private BufferedImage image;
	
	// The actual body created by the world for this robot
	private Body robot;
	
	private Body kicker;
	private Body mount;
	
	// The bodies for our wheels
	private RobotWheel leftWheel;
	private RobotWheel rightWheel;
	
	//private Joint leftJoint;
	//private Joint rightJoint;
	
	private PrismaticJoint kickerJoint;
	//private WeldJoint mountJoint;
	
	//private int controlState = 0;
	
	/**
	 * Basic constructor, assigns parameters and creates the
	 * appropriate constructs for the Box2D world.
	 * 
	 * @param world 		The Box2D world - used to create our body
	 * @param _image 		The image for this robot
	 * @param _size 		The size of this robot, in metres
	 * @param initial_pos	The initial position to centre the robot at
	 * @param scale			The scaling factor from metres to pixels
	 */
	public RobotBody( World world, BufferedImage _image, Vec2 _size, Vec2 initial_pos, float scale ) {
		
		image = _image;
		size  = _size;
		
		// Scale our image if it doesn't represent the size of the robot
		scaleImage( scale );
		
		// Create our robot definitions
		constructRobot( world, initial_pos );
		constructMount( world );
		constructKicker( world );
		constructWheels( world );
	}
	
	public RobotWheel getLeftWheel() {
		return leftWheel;
	}
	public RobotWheel getRightWheel() {
		return rightWheel;
	}
	public Body getKicker(){
		return kicker;
	}
	public Body getBody(){
		return robot;
	}
	
	public void moveTo( Vec2 newPos ) {
		Vec2 oldPos       = robot.getPosition();
		Vec2 displacement = newPos.sub( oldPos );
		
		leftWheel.getBody().setTransform( leftWheel.getBody().getPosition().add( displacement ), leftWheel.getBody().getAngle() );
		rightWheel.getBody().setTransform( rightWheel.getBody().getPosition().add( displacement ), rightWheel.getBody().getAngle() );
		kicker.setTransform( kicker.getPosition().add( displacement ), kicker.getAngle() );
		robot.setTransform(newPos, robot.getAngle());
	}
	
	
	/**
	
	 * Scales our image to better match the size of the robot and the scale specified
	 * 
	 * @param scale The conversion factor from metres to pixels
	 */
	private final void scaleImage( float scale ) {
		
		// The pixel size of the robot that we should have
		Vec2 pixelSize = new Vec2(Utils.metres2Pixels( size.x, scale ),
								  Utils.metres2Pixels( size.y, scale ));
								  
		// Construct a new image the size of the one we wish to have
		BufferedImage new_image = new BufferedImage((int)pixelSize.x, (int)pixelSize.y, image.getType());
		
		// Draw our previous image to this one, scaling it.
		Graphics g = new_image.getGraphics();
		g.drawImage(image, 0, 0, (int)pixelSize.x, (int)pixelSize.y, null);
		g.dispose();
		
		// Assign the new image as our image
		image = new_image;
	}
	
	/**
	 * Method used to create the robot's shape, fixture and body for
	 * the simulated world.
	 * 
	 * @param world 		The world to create the body in.
	 * @param initial_pos	The initial position to centre the robot at
	 */
	private final void constructRobot( World world, Vec2 initial_pos ) {
		
		// Define the barebones of the robot
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set( initial_pos );
	//	bodyDef.angle = initial_angle;
				
		// Define the shape of our robot - uses 1/2 dimensions
		PolygonShape bodyShape = new PolygonShape();
		bodyShape.setAsBox( size.x / 2, size.y / 2 );

		// Create a fixture for the shape
		FixtureDef bodyFixture = new FixtureDef();
		bodyFixture.density    = ROBOT_DENSITY;
		bodyFixture.friction   = ROBOT_FRICTION;
		bodyFixture.shape      = bodyShape;
		
		// Create our actual body
		robot = world.createBody(bodyDef);
		
		robot.m_linearDamping = 10;
		robot.m_angularDamping = 10;
		
		// Attach our fixture to it
		robot.createFixture(bodyFixture);
	}
	
	
	private final void constructMount( World world){
		BodyDef mountDef = new BodyDef();
		mountDef.type = BodyType.DYNAMIC;
		mountDef.position.set( robot.getPosition().clone().add(new Vec2(0.01f, 0)));
		mountDef.angle = (float) (Math.PI/2);
		
		PolygonShape mountShape = new PolygonShape();
		mountShape.setAsBox((size.x/4), 0.02f );
		
		// Create a fixture for the shape
		FixtureDef mountFixture = new FixtureDef();
		mountFixture.density    = ROBOT_DENSITY;
		mountFixture.friction   = ROBOT_FRICTION;
		mountFixture.shape      = mountShape;
		
		//add mount to world and attach fixture
		mount = world.createBody(mountDef);
		mount.createFixture(mountFixture);
			
		//attach the mount to the robot
		WeldJointDef _mountDef = new WeldJointDef();
		_mountDef.initialize(robot, mount, mount.getPosition());
		_mountDef.collideConnected = false;
		
		/*mountJoint = (WeldJoint)*/ world.createJoint(_mountDef);

	}
	
	/**
	 * Method used to create the kicker's shape, fixture and body for
	 * the simulated world.
	 * 
	 * @param world 		The world to create the body in.
	 */
	private final void constructKicker(World world){
		
		BodyDef kickerDef = new BodyDef();
		kickerDef.type = BodyType.DYNAMIC;
		kickerDef.position.set(robot.getPosition().clone().add(new Vec2((float)Math.cos(robot.getAngle())*0.05f, (float)Math.sin(robot.getAngle())*0.05f)));
		kickerDef.angle = (float) (Math.PI/2);
		
		//creates kicker with a width equal to that of the robot
		PolygonShape kickerShape = new PolygonShape();
		kickerShape.setAsBox((size.x/6), 0.002f );
		
		// Create a fixture for the shape
		FixtureDef kickerFixture = new FixtureDef();
		kickerFixture.density    = ROBOT_DENSITY;
		kickerFixture.friction   = ROBOT_FRICTION;
		kickerFixture.shape      = kickerShape;
	
		//add kicker to word and attach fixture
		kicker = world.createBody(kickerDef);
		kicker.createFixture(kickerFixture);
		
		//attach the kicker to the mount
		PrismaticJointDef _kickerDef = new PrismaticJointDef();
		_kickerDef.initialize(mount, kicker, mount.getPosition(), new Vec2(1,0));
		_kickerDef.lowerTranslation = 0.0f;// -0.01f;
		_kickerDef.upperTranslation = 0.1f; //0.03f;
		_kickerDef.enableLimit 		= true;
		_kickerDef.maxMotorForce 	= 10;
		_kickerDef.motorSpeed		= 0;
		_kickerDef.enableMotor		= true;
		_kickerDef.collideConnected = false;
		
		kickerJoint = (PrismaticJoint) world.createJoint(_kickerDef);
		
		kicker.setBullet( true );
		reverseKicker();
	}
	
	/**
	 * Method used to create the robot's wheels and attach to the robot we have
	 * *already* created. (Therefore has to be called after constructRobot!)
	 * 
	 * @param world		The world to create the wheels in
	 */
	private final void constructWheels( World world ) {
		
		// Spit out an error if we have called in the wrong order
		if (robot == null) {
			System.err.println("Warning: called constructWheels before constructRobot!");
			return;
		}
		
		leftWheel = new RobotWheel(world, robot.getPosition().clone().add( leftWheelRelativePosition));
		rightWheel = new RobotWheel(world, robot.getPosition().clone().add( rightWheelRelativePosition));
		
		// Construct our joints for the wheels
		PrismaticJointDef _leftJoint = new PrismaticJointDef();
		_leftJoint.initialize(robot, leftWheel.getBody(), leftWheel.getWorldCenter(), new Vec2(1,0));
		_leftJoint.collideConnected = false;
		_leftJoint.enableLimit = true;
		_leftJoint.upperTranslation = _leftJoint.lowerTranslation = 0;
		
		PrismaticJointDef _rightJoint = new PrismaticJointDef();
		_rightJoint.initialize(robot, rightWheel.getBody(), rightWheel.getWorldCenter(), new Vec2(1,0));
		_rightJoint.collideConnected = false;
		_rightJoint.enableLimit = true;
		_rightJoint.upperTranslation = _rightJoint.lowerTranslation = 0;
		
		// Create our joints in the world
		/* leftJoint  = */ world.createJoint(_leftJoint);
		/* rightJoint = */ world.createJoint(_rightJoint);
	}
	
	public void update( float leftSpeed, float rightSpeed ) {
		
		leftWheel.updateFriction();
		leftWheel.updateDrive( leftSpeed );
		
		rightWheel.updateFriction();
		rightWheel.updateDrive( rightSpeed );
	}

	public void activateKicker(){
		kickerJoint.setMotorSpeed(1);
	}
	public void reverseKicker(){
		kickerJoint.setMotorSpeed(-1);

	}
	public void deactivateKicker(){
		
		kickerJoint.setMotorSpeed(0);
	}
	
	


	/**
	 * Custom drawing code for a RobotBody - draws the robot's image
	 * Centered on it's world position at it's appropriate angle.
	 * 
	 * If debug mode is set, will also draw the vertices of the robot.
	 * 
	 * @param g 	Graphics to draw to
	 * @param debug Draws the vertices of the robot
	 */
/*		public final void drawRobot( Graphics g, Vec2 screenSize, float scale, boolean debug ) {
		
		// Convert the world position of the robot to our screen coordinates
		Vec2 screenPos = Utils.world2Screen( robot.getPosition(), scale, (int)screenSize.y );
		
		AffineTransform transform = new AffineTransform();
		
		// Translate to the upper left of the robot, if we haven't rotated
		transform.translate( screenPos.x - image.getWidth()/2, screenPos.y - image.getHeight()/2 );
		
		// Rotate the transform (anchor around centre of the image)
		transform.rotate( -robot.getAngle(), image.getWidth()/2, image.getHeight()/2 );
		
		// Draw our image, using our transform
		((Graphics2D) g).drawImage(image, transform, null);
		
		Vec2[] kickerVertices = ((PolygonShape) kicker.getFixtureList().getShape()).getVertices();
		g.setColor(Color.blue);
		drawVertices(g,kicker,kickerVertices, screenSize, scale);
		
		Vec2[] mountVertices = ((PolygonShape) mount.getFixtureList().getShape()).getVertices();
		g.setColor(Color.green);
		drawVertices(g,mount,mountVertices, screenSize, scale);
		
		leftWheel.draw(g, screenSize, scale);
		rightWheel.draw(g, screenSize, scale);
		
		g.setColor(Color.red);
		g.drawArc( (int)screenPos.x, (int)screenPos.y, 20, 20, 0, 360);
	}
	
	/**
	 * Method used to draw a connection line between all vertices for a given Body.
	 * 
	 * @param g 		 The graphics to draw to.
	 * @param body		 The body the vertices belong to	
	 * @param vertices	 The vertex data, relative to the body's shape
	 * @param screenSize The size of the screen drawn to
	 * @param scale		 The scaling factor, metres -> pixels
	 */
	/*private static final void drawVertices( Graphics g, Body body, Vec2[] vertices, Vec2 screenSize, float scale ) {
		
		for (int i = 0; i < vertices.length - 1; i++) {
			
			// Retrieve the current and next vertex
			Vec2 v1 = body.getWorldPoint( vertices[i] );
			Vec2 v2 = body.getWorldPoint( vertices[i+1] );
			
			// Convert vertex to screen coordinate space
			Vec2 v1Screen = Utils.world2Screen( v1, scale, (int)screenSize.y);
			Vec2 v2Screen = Utils.world2Screen( v2, scale, (int)screenSize.y);
			
			g.drawLine( (int)v1Screen.x, (int)v1Screen.y, (int)v2Screen.x, (int)v2Screen.y);
		}
	}
	*/
	
	// Basic constants for the robot's simulation
	private final static float ROBOT_DENSITY  = 1f;
	private final static float ROBOT_FRICTION = 0.3f;
	
	private final static Vec2 leftWheelRelativePosition  = new Vec2(  0.08f,  0.065f );
	private final static Vec2 rightWheelRelativePosition = new Vec2(  0.08f, -0.065f );
}
