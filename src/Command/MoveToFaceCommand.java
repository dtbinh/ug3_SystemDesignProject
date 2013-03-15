package Command;

import JavaVision.Position;

public class MoveToFaceCommand {
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
