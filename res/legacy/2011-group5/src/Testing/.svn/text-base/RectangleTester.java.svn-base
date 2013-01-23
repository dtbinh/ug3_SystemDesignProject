package Testing;

import java.awt.Point;

import org.junit.Test;

import Shared.Line;
import Shared.Rectangle;
import Shared.RobotDetails_Editable;
import Shared.Tools;


public class RectangleTester {

	
	@Test
	public void testOrdering()
	{
		
		Point us = new Point(263,176);
		Point ball = new Point(176,254);
		
		Line line = new Line(us, ball);
		
		RobotDetails_Editable them = new RobotDetails_Editable(new Point(221,209), 10);
		
		Rectangle a = them.getRect();
		Tools.printCoors("TL ", a.cTL);
		Tools.printCoors("TR ", a.cTR);
		Tools.printCoors("BL ", a.cBL);
		Tools.printCoors("BR ", a.cBR);
		
		
		Tools.doesLineIntersectRect(line, them.getRect(), false);
		
		System.out.println(Tools.doesLineIntersectRect(line, them.getRect(), false));
		
	}
	
}
