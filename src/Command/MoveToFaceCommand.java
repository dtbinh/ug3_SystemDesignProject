package Command;

import PitchObject.Position;

public class MoveToFaceCommand extends Command {
private Position movePoint;
private Position rotatePoint; 

public MoveToFaceCommand(Position movePoint, Position rotatePoint){
	this.movePoint = movePoint;
	this.rotatePoint = rotatePoint;
}


public Position getMovePoint(){
	return this.movePoint;
}

public Position getRotatePoint(){
	return this.rotatePoint;
}

}
