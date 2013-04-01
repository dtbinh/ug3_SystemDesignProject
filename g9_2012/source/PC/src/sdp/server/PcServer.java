package sdp.server;

import sdp.server.RobotState.Team;
import sdp.server.World.FailedInitException;
import sdp.server.gui.Gui;
import sdp.server.gui.NxtGui;
import sdp.server.gui.SimGui;
import sdp.server.math.Vector2;
import sdp.server.nxt.NxtWorld;
import sdp.server.simulator.SimWorld;
import sdp.server.strategy.StrategyInterface;

/** Entry point into the application.
 * 
 * @author Cristian Cobzarenco
 *
 */
public class PcServer {
	private World                world;
	private Control              ctrl;
	private Gui                  gui;
	private StrategyInterface    strategy;
	private final World.Settings defaultSettings = new World.Settings(
			new BallState( 0.02 ),
			new RobotState[] {
				new RobotState( RobotState.Team.YELLOW, new Vector2( 0.256, 0.154 ) ),
			    new RobotState( RobotState.Team.BLUE,   new Vector2( 0.256, 0.154 ) )
			},
			//new Vector2( 1.14, 0.63 ) // PITCH 1
			new Vector2( 1.216, 0.74 ) // PITCH1 
	);
	
	private boolean running;
	
	public PcServer() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public Control getControl() {
		return ctrl;
	}
	
	public Gui getGui() {
		return gui;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void setStrategy( StrategyInterface newStrategy ) {
		if( strategy != null )
			strategy.disable();
		
		strategy = newStrategy;
		if( strategy != null )
			strategy.enable();
	}
	
	public StrategyInterface getStrategy() {
		return strategy;
	}
	
	public void setupNxtWorld() {
		world = new NxtWorld( defaultSettings );
		ctrl  = new Control( world, Team.YELLOW );
		gui   = new NxtGui( this, (NxtWorld)world );
	}
	
	public void setupSimWorld() {
		world = new SimWorld( defaultSettings );
		ctrl  = new Control( world, Team.YELLOW );
		gui   = new SimGui( this, (SimWorld)world );
	}
	
	public void run() {
		running = true;
		try {
			world.initialise();
			gui.initialise();
		} catch( FailedInitException e ) {
			running = false;
			System.err.println( "PcServer: World initialisation failed: " + e.getMessage() );
		}
		
		while( running ) {
			world.step();
			
			if( gui != null )
				gui.update();
			
			ctrl.update( world.getTimeStep() );
			
			if( strategy != null )
				strategy.update();
		}
		
		if( strategy != null )
			strategy.disable();
		
		gui.dispose();
		ctrl.dispose();
		world.dispose();
	}
	
	public void shutdown() {
		running = false;
	}
	
	public static void main( String args[] ) {
		PcServer server = new PcServer();
		if( (args.length == 0 || args[ 0 ].equals( "nxt" )) ) {
			System.out.println("PcServer: Running NXT...");
			server.setupNxtWorld();
		} else {
			System.out.println("PcServer: Running simulator...");
			server.setupSimWorld();
		}
		
		server.run();
	}
}
