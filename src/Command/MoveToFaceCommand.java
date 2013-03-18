package Command;

import PitchObject.Position;

public class MoveToFaceCommand extends MoveCommand {
	private double direction; 
	
	public MoveToFaceCommand(Position movePoint, double direction){
		super(movePoint);
		this.direction = direction;
	}
	
	public double getDirection(){
		return this.direction;
	}
}
