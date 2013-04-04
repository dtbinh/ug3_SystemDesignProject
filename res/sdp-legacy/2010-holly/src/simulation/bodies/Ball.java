package simulation.bodies;

import net.phys2d.raw.Body;
import net.phys2d.raw.shapes.Circle;
import net.phys2d.raw.shapes.Shape;

/*
 * Author: Joe Tam (s0791475)
 */

public class Ball extends SimBody{

	public Ball(float radius, float mass, int c) {
		super(new Circle(radius),mass,c);
	}
	
	public Ball(String name, float radius, float mass, int c) {
		super(name, new Circle(radius),mass,c);
	}
	
}
