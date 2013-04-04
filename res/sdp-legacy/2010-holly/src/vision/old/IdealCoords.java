/**
 * 
 */
package vision.old;

import java.awt.image.Raster;
import java.util.ArrayList;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import baseSystem.Singleton;



/**
 * @author Lau
 *  List of available commands
 *	Type 1: forward
 *	Type 2: backward
 *	Type 3: strafe left
 *	Type 4: strafe right
 *	Type 5: rotate
 *	Type 6: adjust forward/backward
 *	Type 7: adjust strafing
 *	Type 8: kick
 *	Type 9: stop
 */
public class IdealCoords {
	
	public final static int BLUE = 0;
	public final static int YELLOW = 1;
	
	private int[] coords;
	ArrayList<int[]>  commands;
	Singleton s;
	int angle, real_angle ; 
	
	public IdealCoords()
	{
		s.getSingleton();
		this.coords = s.getCoordinates();
		if(s.ourColor == BLUE)
			{
			angle = coords[5];
			}
		else
		{
			angle = coords[8];
		}
		this.commands = s.getCommands();
	}


	public int idealComp(){
		for (int i=0 ;i<commands.size();i++)
			if(commands.get(i)[0] == 5)
				{
				angle = angle + commands.get(i)[2]; 
				}
		
		this.coords = s.getCoordinates();
		if(s.ourColor == BLUE)
		{
		real_angle = coords[5];
		}
		else
		{
		real_angle = coords[8];
		}
		
		System.out.println("We thought we have an angle of " + angle + " degrees, but we actually have an angle of " + real_angle + "degrees" );
		
		return angle;
	}
	
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
