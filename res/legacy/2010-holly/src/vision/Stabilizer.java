/**
 * 
 */
package vision;

/**
 * @author lau
 *
 */
public class Stabilizer {
	public int ang_count = 0, ang_sum = 0, old_ang = -1, stabilized_angle = -1;
	private int MAX_COUNT = 10;
	
	public void stab_ang(int angle)
	{
		if(old_ang == -1)
		{
			old_ang = angle;
			
		}
		
		if((angle-(15*angle/100)) <= old_ang || (angle+(15*angle/100)) <= old_ang)
		{
			old_ang = angle;
			ang_count ++;
			ang_sum += angle;
			//System.out.println("ang count" + ang_count );
			if(ang_count == MAX_COUNT)
			{
				stabilized_angle = (ang_sum/ang_count);
				//System.out.println("the stabilized angle is" + stabilized_angle);
				ang_count = 0;
				ang_sum = 0;
			}
		}
		

	}
	
	public int getStabilizedAngle(){
		return stabilized_angle;
	}

}
