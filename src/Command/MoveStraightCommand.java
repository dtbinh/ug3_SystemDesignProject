package Command;

import JavaVision.Position;

public class MoveStraightCommand {
	private Position movePoint;

	public MoveStraightCommand(Position movePoint){
		this.movePoint = movePoint;
	}
	
	public Position getMovePoint(){
		return this.movePoint;
	}
}
