package simulation.bodies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JPanel;

/*
 * Author: Joe Tam (s0791475)
 */

public class Pitch_old extends JPanel{
	int pitch_width = 732;
	int pitch_height = 366;
	int goal_width = 54;
	int goal_height = 180;
	
	public void Pitch() {
		setSize(pitch_width, pitch_height);
	}
	
    public void paint(Graphics g) {
    	
        super.paint(g);
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(new Color(51,204,0));
        g2d.fillRect(0, 0, pitch_width, pitch_height);
        g2d.setColor(new Color(255,255,255));
        g2d.drawLine(pitch_width / 2, 0, pitch_width / 2, pitch_height);
        g.dispose();
    }
}
