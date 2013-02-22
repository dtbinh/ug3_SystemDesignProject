package Planning;

import JavaVision.Position;
import java.util.LinkedList;

public class CommandStack {
	private LinkedList<Command> stack;
	
	public CommandStack() {
		stack = new LinkedList<Command>();
	}
	
	public Command getFirst() {
		return stack.getFirst();
	}
	
	public Command pop() {
		return stack.pop();
	}
	
	public void clear() {
		stack.clear();
	}
	
	public void push(Command command) {
		stack.push(command);
	}
	
	public void pushKickCommand(Position kickPoint, Position ballPoint) {
		stack.push(new KickCommand());
		stack.push(new MoveCommand(kickPoint, ballPoint, true));
	}
	
	public void pushMoveCommand(Position movePoint, Position rotatePoint, 
								boolean hardRotate) {
		// TODO: object avoidance
		stack.push(new MoveCommand(movePoint, rotatePoint, hardRotate));
	}
}