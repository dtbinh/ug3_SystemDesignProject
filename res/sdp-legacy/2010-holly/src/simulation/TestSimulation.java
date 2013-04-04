package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;

/*
 * Author: Joe Tam (s0791475)
 */

public class TestSimulation {
	
	JFrame frame = new JFrame();
	World world = new World(new Vector2f(0,10),20);
	Body ball = new Body(new Circle(50), 50);
	StaticBody ground = new StaticBody(new Box(400, 20));
	
	public static void main(String args[]) {
		new TestSimulation();
	}
	
	public TestSimulation() {
		initGUI();
		
		World world = new World(new Vector2f(10,10),20);
		world.setGravity(0, 1000);
		
		
		ball.setPosition(50, 30);
		ground.setPosition(0, 100);
		ball.setForce(0, -100000000);
		
		
		world.add(ground);
		world.add(ball);
		
		cycle();
	}
	
	public void cycle() {
		while(true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			world.step();
			System.out.println(ball.getPosition().getY());
		}
	}
	
	public void initGUI() {
		frame.setSize(300,300);
		frame.setBackground(Color.black);
		frame.setVisible(true);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.red);
		g2d.drawRect((int)ground.getPosition().getX(), (int)ground.getPosition().getY(), (int)ground.getShape().getBounds().getWidth()	, (int)ground.getShape().getBounds().getHeight());
	}
	
}
