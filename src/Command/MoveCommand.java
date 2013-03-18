package Command;

import PitchObject.Position;

public class MoveCommand extends Command {
	private Position movePoint;

	public MoveCommand(Position movePoint){
		this.movePoint = movePoint;
	}
	
	public Position getMovePoint(){
		return this.movePoint;
	}
}