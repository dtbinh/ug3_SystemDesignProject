package movement;

import java.io.FileNotFoundException;

import org.gnome.gdk.Gdk;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.MenuItem;

public class ControllerLogic{

	private static Controller c;
	private static boolean connected;
	private static Movement robot;
	
	public static void main(String [] args) throws FileNotFoundException, InterruptedException{
		robot = new Movement(0, false, false, false);
		//rc = new RobotController();
		c = new Controller(robot);
		connectToGui();
		connectToRobot();
		
	}

	private static void connectToGui() throws FileNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		
		c.start();
        c.reconnect.connect(new MenuItem.Activate() {
			public void onActivate(MenuItem arg0) {
				//connectToRobot();
				robot.connect(false);
			}
			
		});
        
        c.closeConnection.connect(new MenuItem.Activate() {
			public void onActivate(MenuItem arg0) {
				//connectToRobot();
				robot.sendControllerCommand(99);
				
			}
			
		});
		
	}

	private static void connectToRobot() {
		// TODO Auto-generated method stub
		
		c.progress.setText("Attempting to Connect");
		c.progress.setFraction(0.4);
		connected = robot.connect(false);
		if (connected == false){
			c.progress.setFraction(0);
			c.progress.setText("Failed to Connect");
		}
		else {
			c.progress.setText("Successful Connection");
			c.progress.setFraction(1.0);
		}
	}
	
}
