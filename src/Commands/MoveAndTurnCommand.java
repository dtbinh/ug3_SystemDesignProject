package Commands;

import JavaVision.Position;

public class MoveAndTurnCommand {
private Position movePoint;
private Position rotatePoint;

public MoveAndTurnCommand(Position movePoint,Position rotatePoint){
	this.movePoint = movePoint;
	this.rotatePoint = rotatePoint;
}

public Position getMovePoint() {
	return movePoint;
}

public Position getRotatePoint() {
	return rotatePoint;
} 

}
