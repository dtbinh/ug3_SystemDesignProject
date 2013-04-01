package sdp.server.simulator;

import java.awt.Color;
import java.awt.Graphics;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * This class abstracts the necessary procedures for simulating a wheel attached
 * to our Robot. There are a few 'hacks' that are required to simulate top-down
 * wheels, of which this class provides the details. 
 */
public class RobotWheel {

	// Our Box2D objects.
	private Body wheel;
	private BodyDef wheelDef;
	private PolygonShape wheelShape;
	private FixtureDef wheelFix;
	
	/**
	 * Constructs a wheel at the given position in the world.
	 * 
	 * @param world			The Box2D world to create the wheel in.
	 * @param initial_pos	The position to center the wheel at.
	 */
	public RobotWheel( World world, Vec2 initial_pos) {
		createWheel(initial_pos, world);
	}
	
	/**
	 * Method used to create the shape, fixture and body for
	 * a robot's wheel in the simulated world
	 * 
	 * @param world 		The world to create the wheel body in.
	 * @param initial_pos	The wheel's initial position
	 */
	private void createWheel( Vec2 initial_pos, World world){
		
		// Construct our wheel shape
		wheelShape = new PolygonShape();
		wheelShape.setAsBox( wheelSize.x / 2, wheelSize.y / 2 );
		
		// Construct our wheel body definitions
		wheelDef = new BodyDef();
		wheelDef.type = BodyType.DYNAMIC;
		wheelDef.position.set( initial_pos );
		
		// Create a fixture for the wheels
		wheelFix = new FixtureDef();
		wheelFix.density = 0.8f / (wheelSize.x*wheelSize.y);;
		wheelFix.friction = 0.3f;
		wheelFix.shape = wheelShape;
		
		wheel = world.createBody( wheelDef );
		wheel.createFixture( wheelFix );
	}
	
	/**
	 * This method removes lateral velocity from wheels - this restricts the wheels to 
	 * only have velocity in the direction that they are facing. This is a necessary hack
	 * for Box2D as we're simulating a top-down view, which is unusual.
	 */
	public void updateFriction(){
		Vec2 impulse = getLateralVelocity().mul((-1) * wheel.getMass());
		
		wheel.applyLinearImpulse(impulse, wheel.getWorldCenter());
		
		wheel.applyAngularImpulse(0.1f * wheel.getInertia() * -wheel.getAngularVelocity() );
		
	}
	
	/**
	 * This method updates the wheel's speeds, up to some maximum desired-speed.
	 * The method provides an implementation which accelerates / decelerates
	 * to the desired speed.
	 * 
	 * @param desiredSpeed	The speed we want the wheels to drive at.
	 */
	public void updateDrive( float desiredSpeed ){
		
		Vec2 currentForwardNormal = wheel.getWorldVector(new Vec2(1,0));
		float currentSpeed = Vec2.dot(currentForwardNormal, getForwardVelocity() );
		float force = 0;
		
		if( Math.abs( desiredSpeed - currentSpeed ) > 0.01 ) {
			if( desiredSpeed > currentSpeed )
				force = 8.0f;
			else
				force = - 8.0f;
			
			wheel.applyForce(currentForwardNormal.mul(force), wheel.getWorldCenter());
		} else
			wheel.setLinearVelocity( currentForwardNormal.mul(desiredSpeed) );
		
		
	}
	
	/**
	 * Draws this robot wheel's vertices onto the screen.
	 * 
	 * @param g 			The graphics to draw to.
	 * @param screenSize	The dimensions of the panel we're drawing to.
	 * @param scale			The metres -> pixels scale factor.
	 */
	public void draw(Graphics g, Vec2 screenSize, float scale){
		// Draw robot vertices
		g.setColor( Color.red );
		
		PolygonShape shape = (PolygonShape) wheel.getFixtureList().getShape();
		Vec2[] vertices = shape.m_vertices;
		
		drawVertices( g, wheel, vertices, screenSize, scale );
	}
	
	private static final void drawVertices( Graphics g, Body body, Vec2[] vertices, Vec2 screenSize, float scale ) {
	
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
	
	public Body getBody(){
		return wheel;
	}
	
	public Vec2 getWorldCenter(){
		return wheel.getWorldCenter();	
	}
	
	/**
	 * @return Returns the lateral velocity of the wheel - this is removed from the wheel's
	 * 		   velocities on each time-step.
	 */
	public Vec2 getLateralVelocity(){
		
		Vec2 currentNormal = wheel.getWorldVector(new Vec2(0,-1));
		return currentNormal.mul(Vec2.dot(currentNormal, wheel.getLinearVelocity()));
	}
	
	public Vec2 getForwardVelocity(){
		
		Vec2 currentNormal = wheel.getWorldVector(new Vec2(1,0));
		return currentNormal.mul(Vec2.dot(currentNormal, wheel.getLinearVelocity()));
	}
	
	Vec2 wheelSize = new Vec2( 0.05f, 0.02f ); 
	
}
