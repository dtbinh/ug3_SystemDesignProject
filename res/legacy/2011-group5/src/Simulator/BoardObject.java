package Simulator;
import java.awt.Color;

import net.phys2d.raw.Body;
import net.phys2d.raw.shapes.DynamicShape;

public class BoardObject {

	private final Color color;
	private int angle;
	protected Body body;

	public BoardObject(float x, float y, String name, DynamicShape shape, float mass, Color color, int angle) {
		this.body = new Body(name, shape, mass);
		setPosition(x, y);
		this.color = color;
		this.angle = angle;
	}

	public void setPosition(float x, float y) {
		body.setPosition(x, y);
	}
	
	public float getX() {
		return body.getPosition().getX();
	}

	public float getY() {
		return body.getPosition().getY();
	}

	public Color getColor() {
		return color;
	}
	
	public double getAngle() {
		return angle;
	}
	
	// keeps the angle between 0 and 359 degrees
	public int convertAngle(double d) {
//		System.out.println("convertAngle received: "+d);
		while (d < 0 || d >= 360) {
			if (d < 0)
				d += 360;
			else if (d >= 360)
				d -= 360;
		}
//		System.out.println("convertAngle returned: "+d);
		return (int) d;
	}
	
	public void setAngle(double d){
		angle = convertAngle(d);
	}
	
	public Body getBody() {
		return body;
	}
	
}