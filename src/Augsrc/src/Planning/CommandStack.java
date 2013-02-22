package Planning;

import JavaVision.Position;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class CommandStack {
	private LinkedList<Command> stack;
	
	public CommandStack() {
		stack = new LinkedList<Command>();
	}
	
	public Command getFirst() {
		Command returncommand;
		try {
		returncommand =  stack.getFirst();
		} catch (NoSuchElementException e){
		returncommand = null;
		}
		return returncommand;
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
	
	public boolean isEmpty(){
		return stack.isEmpty();
	}
	
}