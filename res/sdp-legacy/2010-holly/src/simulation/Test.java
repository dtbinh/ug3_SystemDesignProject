package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.JFrame;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;

public class Test extends JFrame{
	
	World world = new World(new Vector2f(0,0), 100);
	Body body1 = new Body(new Box(200, 200), 5);
	Body ball = new Body(new Circle(100),5);

	public Test() {
		setLayout(null);
		setBackground(Color.black);
		setSize(500,500);
		setVisible(true);

		world.add(ball);
		ball.addForce(new Vector2f(100000,100000));
		
		//for (int i = 0; i < 10; i++) {
		while (true) {
			world.step();
			System.out.println(ball.getPosition().getX());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			repaint();
		//}
		}
	}
	
	public static void main(String args[]) {
		new Test();   
	}

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.red);
        g2d.fillOval((int)ball.getPosition().getX(), (int)ball.getPosition().getY(), (int)ball.getShape().getBounds().getWidth(), (int)ball.getShape().getBounds().getHeight());
        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }
}
