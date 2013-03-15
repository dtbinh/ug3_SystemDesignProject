package Command;

import PitchObject.Position;

public class RotateCommand {
private Position rotatePosition;

public RotateCommand(Position rotatePosition) {
	this.rotatePosition = rotatePosition;
}

public Position getRotatePosition() {
	return rotatePosition;
}
}
