package Commands;


public class KickCommand extends Command {
	public KickCommand() {}
	
	public boolean isEquals(Object other) {
		return other instanceof KickCommand; 
	}
}