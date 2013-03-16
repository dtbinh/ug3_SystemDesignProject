package Command;

import PitchObject.Position;

public class MoveStraightCommand extends Command {
	private Position movePoint;

	public MoveStraightCommand(Position movePoint){
		this.movePoint = movePoint;
	}
	
	public Position getMovePoint(){
		return this.movePoint;
	}
}
