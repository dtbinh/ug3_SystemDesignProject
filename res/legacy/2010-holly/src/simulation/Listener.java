package simulation;

/*
 * Author: Marzena Bihun (s0833648)
 */

import java.awt.Color;
import java.awt.Graphics;

import net.phys2d.raw.CollisionListener;
import net.phys2d.raw.CollisionEvent;
import net.phys2d.raw.Body;

public class Listener implements CollisionListener{
	
	public void collisionOccured(CollisionEvent ce){
		/*
		Body bodya = ce.getBodyA();
		Body bodyb = ce.getBodyB();
		System.out.println("Collision occurred between " + 
			  bodya.toString() + " and " + bodyb.toString());
		if ((bodya.getID()==Simulation.ids[0])&&(bodyb.getID()==Simulation.ids[1])){
			
			//Simulation.goal = true;
			System.out.println("GOAL!!!!!right"); 
			//System.out.println(Simulation.goal); 
			System.out.println(bodya.toString());
		}
		if ((bodya.getID()==Simulation.ids[0])&&(bodyb.getID()==Simulation.ids[2])){
			
			//Simulation.goal = true;
			System.out.println("GOAL!!!!!left"); 
			//System.out.println(Simulation.goal); 
			System.out.println(bodya.toString());
		}
		*/
	}
	
	

}
