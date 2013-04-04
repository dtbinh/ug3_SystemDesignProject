package simulation.bodies;

import net.phys2d.raw.shapes.Box;

public class Wall extends SimBody{

	
	public Wall(String name, float width, float height) {
		super(name, new Box(width,height),INFINITE_MASS);
	}
}
