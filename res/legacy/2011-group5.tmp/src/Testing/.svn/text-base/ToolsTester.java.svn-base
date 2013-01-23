package Testing;

import java.awt.Point;

import org.junit.Test;
import static org.junit.Assert.*;
import Shared.Line;
import Shared.Rectangle;
import Shared.Tools;


public class ToolsTester {

	Tools tools;
	
	
	@Test
	public void testLineIntersectRect()
	{
		//For most the tests the rectangle wont move or change size
		Point rectTL = new Point(0,10);
		Point rectBR = new Point(20,0);
		
		
		//p2 is always right and below p1
		Point p1;
		Point p2;
		
		//---start off with p1.y above top of rectangle and p2.y below bottom---//
		
			//case 1, both points are to the left of rectangle
			p1 = new Point(-10, 15);
			p2 = new Point(-5,-10);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 2, p1 left of rectangle p2 in middle
			p2 = new Point(8,-10);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 3, p1 left of rectangle, p2 to the right
			p2 = new Point(25,-10);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 4 p1 and p2 in middle
			p1 = new Point(4,15);
			p2 = new Point(16,-10);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 5 p1 in middle, p2 to right
			p2 = new Point(25,-10);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 6 p1 and p2 to right
			p1 = new Point(25, 15);
			p2 = new Point(30,-10);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
		//---now with p1.y and p2 above top of rectangle otherwise same xs---//
			
			//case 1, both points are to the left of rectangle
			p1 = new Point(-10, 15);
			p2 = new Point(-5,12);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 2, p1 left of rectangle p2 in middle
			p2 = new Point(8,12);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 3, p1 left of rectangle, p2 to the right
			p2 = new Point(25,12);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 4 p1 and p2 in middle
			p1 = new Point(4,15);
			p2 = new Point(16,12);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 5 p1 in middle, p2 to right
			p2 = new Point(25,12);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 6 p1 and p2 to right
			p1 = new Point(25, 15);
			p2 = new Point(30,12);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
		//---now with p1.y and p2.y in middle of rectangle---//
			
			//case 1, both points are to the left of rectangle
			p1 = new Point(-10, 8);
			p2 = new Point(-5,-3);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 2, p1 left of rectangle p2 in middle
			p2 = new Point(8,3);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 3, p1 left of rectangle, p2 to the right
			p2 = new Point(25,3);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 4 p1 and p2 in middle
			p1 = new Point(4,8);
			p2 = new Point(16,-10);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 5 p1 in middle, p2 to right
			p2 = new Point(25,3);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case 6 p1 and p2 to right
			p1 = new Point(25, 8);
			p2 = new Point(30,3);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
		
		//---Now some exceptional ones---//
			//horizontal line above
			p1 = new Point(-10,15);
			p2 = new Point(25,15);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//horizontal line matching the Bottom of the rectangle
			p1 = new Point(0,0);
			p2 = new Point(20,0);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//horizontal line in middle
			p1 = new Point(0,5);
			p2 = new Point(20,5);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			
			//Vertical line to left
			p1 = new Point(-10, 15);
			p2 = new Point(-10, -10);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//vertical on Right line of rectangle
			p1 = new Point(20,10);
			p2 = new Point(20,0);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//vertical in middle
			p1 = new Point(5,15);
			p2 = new Point(5,-10);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			
		//cases where end points dont matter
			//case line does not pass through rectangle
			p1 = new Point(15,15);
			p2 = new Point(14,14);
			assertEquals(false, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case line does pass through rectangle
			p1 = new Point(1,2);
			p2 = new Point(0,0);
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			
			//case width of rect is 0, p1 and p2 are on either side of it
			rectTL = new Point(0,10);
			rectBR = new Point(0,0);
			
			p1 = new Point(-1,5);
			p2 = new Point(1, 4);
			
			assertEquals(true, Tools.doesLineIntersectRect(new Line(p1, p2), new Rectangle(rectTL, new Point(rectTL.x,rectBR.y), new Point(rectBR.x, rectTL.y), rectBR),false));
			//there are more cases but I'm happy the class works, feel free to add to these
	}
	
}
