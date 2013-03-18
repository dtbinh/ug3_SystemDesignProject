package Command;

import PitchObject.Position;

public class MoveAndTurnCommand extends MoveCommand{
	private double direction;
	
	public MoveAndTurnCommand(Position movePoint, double direction){
		super(movePoint);
		this.direction = direction;
	}

	public double getDirection() {
		return this.direction;
	} 
}
