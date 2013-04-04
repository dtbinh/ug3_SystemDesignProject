package simulation.bodies;

import net.phys2d.raw.Body;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;
import net.phys2d.raw.shapes.DynamicShape;
import net.phys2d.raw.shapes.Line;

public class SimBody extends Body{
	
	public int color;
	
	public SimBody(DynamicShape shape, float mass, int c) {
		super(shape, mass);
		color = c;
	}
	
	public SimBody(String name, DynamicShape shape, float mass, int c) {
		super(name, shape, mass);
		color = c;
	}
	
	
	public SimBody(String name, DynamicShape shape, float mass) {
		super(name, shape, mass);
		
	}
	
	
	//translate the object's coordinate system (0,0 at centre of object) into Graphic's coordinate system (0,0 at top left)
    public int getGraphicsX() {
    	//System.out.println("X");
    	//System.out.println((int)this.getPosition().getX() - getWidth()/2);
    	return (int) (this.getPosition().getX() - this.getWidth()/2);
    	//return (int) (getPosition().getX() - Simulation.PI / 2);
    }
    
    public int getGraphicsY() {
    	//float x =  getPosition().getY() - this.getShape().getBounds().getHeight() / 2;
    	//System.out.println("Y");
		//System.out.println(this.getPosition().getY());
    	return (int) (this.getPosition().getY() - getHeight() / 2);
    	//return (int) (getPosition().getY());
    }
    
    public int getWidth() {
    	return (int) getShape().getBounds().getWidth();
    }
    
    public int getHeight() {
    	return (int) getShape().getBounds().getHeight();
    }
    
    public boolean isCircle() {
    	if (getShape() instanceof Circle) {
    		return true;
    	}
    	else return false;
    }
    
    public boolean isBox() {
    	if (getShape() instanceof Box) {
    		return true;
    	}
    	else return false;
    }
    
    //this method was supposed to stop the object, but anyway it was still 
    //changing its position fraction by fraction
    
    public void clear(){
		if ((getVelocityDelta().getX()<0.002)||(getVelocityDelta().getY()<0.002)){
			//System.out.println("done!!");
			setForce(0,0);}
	}
    
    
    
}
