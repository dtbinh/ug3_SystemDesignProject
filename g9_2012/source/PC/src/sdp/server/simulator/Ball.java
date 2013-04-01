package sdp.server.simulator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/** This class represents a single ball in our Simulated environment. This abstracts details
 * such as the numerous Box2D definitions required to construct the world and the drawing of
 * the ball at it's simulated position.
 */
public class Ball {

	Body ball;

	// Various definitions needed to construct the ball
	BodyDef ballDef;
	CircleShape ballShape;
	FixtureDef ballFix;
	
	BufferedImage image;
	
	float radius = 0.04f;
	
	/**
	 * Constructs a ball, at the given position in the Box2D world provided. The 
	 * image provided is what the ball is drawn as.
	 * 
	 * @param image		The image of the ball.
	 * @param world	   	The Box2D world to construct the ball in.
	 * @param position 	The position to initialise the ball at.
	 * @param scale		The conversion factor between metres and pixels. i.e metres * scale = pixels.
	 */
	public Ball(World world,BufferedImage image , Vec2 position, float scale){
		
		this.image = image;
		
		scaleImage(scale);
		createBall(position, world);
		
		// Set the name of the body to identify in other areas.
		ball.setUserData("Ball");
	}
	
	/** 
	 * @return Returns the ball's body as provided on construction in the world.
	 */
	public Body getBody() {
		return ball;
	}
	
	/**
	 * Performs the necessary initialization of definitions and fixtures needed
	 * to construct the ball in our world.
	 * 
	 * @param position 	The position of the ball.
	 * @param world		The simulated world.
	 */
	public void createBall(Vec2 position, World world){
		
		ballShape = new CircleShape();
		ballShape.m_radius = 0.02f;
		
		ballDef = new BodyDef();
		ballDef.type = BodyType.DYNAMIC;
		ballDef.position = position;
		
		ballFix = new FixtureDef();
		ballFix.density = 1;
		ballFix.friction = 0.3f;
		ballFix.shape = ballShape;
		
		ball = world.createBody(ballDef);
		ball.createFixture(ballFix);
		ball.m_linearDamping = 0.2f;

	}
	
	/**
	 * Convenience method used to draw our ball at it's current location. Performs the necessary
	 * scaling to draw the image at the appropriate radius initialised with.
	 * 
	 * @param g				The graphics to draw to.
	 * @param screenSize	The current size of the panel that we're drawing to.
	 * @param scale			The scale factor - metres * scale = pixels.
	 */
	public void draw(Graphics g, Vec2 screenSize, float scale){
		
		g.setColor( Color.red );

		// Convert the position of the ball to the screen coordinate system.
		Vec2 screenPosition = Utils.world2Screen(ball.getPosition(), scale, (int)screenSize.y);
		
		// Convert the radius to a pixel size.
		int radius = (int)Utils.metres2Pixels(0.02f, scale);
		
		g.drawImage(image, (int)screenPosition.x-radius, (int)screenPosition.y-radius, null);
		
	}
	
	/**
	 * Scales our image to better match the size of the robot and the scale specified
	 * 
	 * @param scale The conversion factor from metres to pixels
	 */	
	private final void scaleImage( float scale ) {
		
		// The pixel size of the robot that we should have
		Vec2 pixelSize = new Vec2(Utils.metres2Pixels( radius, scale ),
								  Utils.metres2Pixels( radius, scale ));
								  
		// Construct a new image the size of the one we wish to have
		BufferedImage new_image = new BufferedImage((int)pixelSize.x, (int)pixelSize.y, image.getType());
		
		// Draw our previous image to this one, scaling it.
		Graphics g = new_image.getGraphics();
		g.drawImage(image, 0, 0, (int)pixelSize.x, (int)pixelSize.y, null);
		g.dispose();
		
		// Assign the new image as our image
		image = new_image;
	}
}

