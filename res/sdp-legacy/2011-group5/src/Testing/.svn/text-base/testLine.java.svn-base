package Testing;

import static org.junit.Assert.*;

import java.awt.Point;
import org.junit.Test;

import Shared.Line;

public class testLine {
	Line line;
	@Test
	public void test2Points()
	{
		line = new Line(new Point(0,0), new Point(10,10));
		assertEquals(line.getYfromX(10),10);
		assertEquals(line.getYfromX(-1),-1);
		assertTrue(line.isPointAboveLine(new Point(0,10)));
		assertFalse(line.isPointAboveLine(new Point(0,-10)));
		assertEquals(Line.getGradient(new Point(0,0), new Point(10,10)),1,0);
		assertEquals(Line.getIntercept(new Point(0,0), 1),0,0);
		assertEquals(Line.getIntercept(new Point(8,8), 1),0,0);
		assertTrue(line.isPointOnTheLine(new Point(50,50)));
		assertEquals(line.distanceBetweenPointAndALine(new Point(51,51)),0,1);
		assertEquals(line.distanceBetweenPointAndALine(new Point(0,100)),Math.sqrt(5000),0);
	}
	@Test
	public void test2Points2()
	{
		Point a = new Point(2,8);
		Point b = new Point(3,7);
		Point o = new Point(1,77);
		line = new Line(a, b);
		assertEquals(line.distanceBetweenPointAndALine(o), 48.08326112068523,0);
	}
	@Test
	public void test2Points3()
	{
		Point a = new Point(65,37);
		Point b = new Point(84,96);
		Point o = new Point(18,15);
		line = new Line(a, b);
		assertEquals(line.distanceBetweenPointAndALine(o), 37.99375616529842,0);
	}
	@Test
	public void test2Points4()
	{
		Point a = new Point(24,25);
		Point b = new Point(25,25);
		Point o = new Point(0,0);
		line = new Line(a, b);
		assertEquals(line.distanceBetweenPointAndALine(o), 25,0);
	}
	@Test
	public void test2Points5()
	{
		Point a = new Point(0,8);
		Point b = new Point(100,7);
		Point o = new Point(7,7);
		line = new Line(a, b);
		assertEquals(line.distanceBetweenPointAndALine(o), 0.9299535034872094,0);
	}
	@Test
	public void testPointAngle()
	{
		line = new Line(new Point(0,0), (double)Math.toRadians(45));
		assertEquals(line.getYfromX(10),10);
		assertEquals(line.getYfromX(-1),-1);
		assertTrue(line.isPointAboveLine(new Point(0,10)));
		assertFalse(line.isPointAboveLine(new Point(0,-10)));
		assertEquals(Line.getGradient(new Point(0,0), new Point(10,10)),1,0);
		assertEquals(Line.getIntercept(new Point(0,0), 1),0,0);
		assertEquals(Line.getIntercept(new Point(8,8), 1),0,0);
		assertTrue(line.isPointOnTheLine(new Point(50,50)));
		assertEquals(line.distanceBetweenPointAndALine(new Point(51,51)),0,1);
		assertEquals(Math.sqrt(5000),line.distanceBetweenPointAndALine(new Point(0,100)),0.0000000000001);
	}
	@Test
	public void testPointAngle2()
	{
		Point a = new Point(0,0);
		double angle = Math.atan((double)1/100);
		Point o = new Point(50,1);
		line = new Line(a, angle);
		assertEquals(0.5, line.distanceBetweenPointAndALine(o),0.0001);
	}
}
