package Command;

public class RotateCommand extends Command{
	private double direction;
	
	public RotateCommand(double direction) {
		this.direction = direction;
	}
	
	public double getDirection() {
		return this.direction;
	}
}
