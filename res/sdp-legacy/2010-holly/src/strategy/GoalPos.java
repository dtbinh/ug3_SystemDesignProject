/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package strategy;

/**
 *
 * @author Lau
 */

public class GoalPos {
	private int ourSide;
	private int oppSide;
	public final static int BLUE = 0;
	public final static int YELLOW = 1;
	public final static int LEFT = 0;
	public final static int RIGHT = 1;
	// LEFT SIDE : (40,220) up to (40,360)
	// RIGHT SIDE : (730,220) up to (730,360)
	public final static int lside_begin_x = 40;
	public final static int lside_begin_y = 220;
	public final static int lside_end_x = 40;
	public final static int lside_end_y = 360;
	public final static int rside_begin_x = 730;
	public final static int rside_begin_y = 220;
	public final static int rside_end_x = 730;
	public final static int rside_end_y = 360;

	public int[] whereToShoot(int side, int us_x, int us_y, int opp_x, int opp_y){
		int[] w = new int[2];
		if (side == LEFT)
		{
			if(lside_begin_y - opp_y > 0)
			{

			}
		}
		else
		{

		}
		return w;
	}

}
