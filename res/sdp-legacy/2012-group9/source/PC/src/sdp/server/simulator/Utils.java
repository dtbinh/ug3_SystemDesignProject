package sdp.server.simulator;
import org.jbox2d.common.Vec2;

import sdp.server.math.Vector2;


public class Utils {

	/**
	 * Converts a metre value to pixels
	 * 
	 * @param metres The metre value to convert
	 * @param scale  The scaling factor to use, X metres * scale = Y pixels
	 * 
	 * @return Returns the appropriate pixel value for the Box2D metres value.
	 */
	public static float metres2Pixels( float metres, float scale ) {
		return metres * scale;
	}
	

	/**
	 * Converts a pixel value to metres
	 * 
	 * @param pixels The pixel value to convert
	 * @param scale	 The scaling factor to use, X pixels / scale = Y pixels
	 * 
	 * @return Returns the appropriate metric value for the screen measurement value.
	 */
	public static float pixels2Metres( float pixels, float scale ) {
		return pixels / scale;
	}
	
	/**
	 * Converts the given screen coordinates to world coordinates
	 * 
	 * @param screen 		The position on the screen
	 * @param scale  		The scaling factor, X pixels / scale = Y metres
	 * @param screenHeight  The screen height to use in calculations - assumes
	 * 					    our origin is set at bottom left of screen.
	 * 
	 * @return Returns the position in Box2D world relative to the screen
	 */
	public static Vec2 screen2World( Vec2 screen, float scale, int screenHeight ) {
		return new Vec2( pixels2Metres(screen.x, scale), 
						 -pixels2Metres(screen.y, scale) + pixels2Metres(screenHeight, scale));
	}
	
	/**
	 * Converts the world coordinates to screen coordinates, with the origin, (0,0) being at the lower
	 * left of the panel.
	 * 
	 * @param world 		The world coordinates.
	 * @param scale			The scaling factor, X metres * scale = Y pixels
	 * @param screenHeight  The height of the screen
	 * 
	 * @return Returns the world coordinates converted to screen position
	 */
	public static Vec2 world2Screen( Vec2 world, float scale, int screenHeight ) {
		return new Vec2( metres2Pixels(world.x, scale), 
						-metres2Pixels(world.y, scale) + screenHeight);
	}
	
	public static Vector2 world2Screen( Vector2 world, float scale, int screenHeight ) {
		return new Vector2( metres2Pixels((float)world.getX(), scale), 
						-metres2Pixels((float)world.getY(), scale) + screenHeight);
	}
	
}
