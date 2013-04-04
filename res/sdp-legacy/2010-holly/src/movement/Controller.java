package movement;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import org.gnome.gdk.Event;
import org.gnome.gdk.EventButton;
import org.gnome.gdk.Gdk;
import org.gnome.gtk.Button;
import org.gnome.gtk.Frame;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.HBox;
import org.gnome.gtk.HScale;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.Menu;
import org.gnome.gtk.MenuBar;
import org.gnome.gtk.MenuItem;
import org.gnome.gtk.Notebook;
import org.gnome.gtk.ProgressBar;
import org.gnome.gtk.ProgressBarOrientation;
import org.gnome.gtk.Table;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;


public class Controller extends Thread
{
	private Notebook n;
	public ProgressBar progress;
	private ProgressBar batteryLevel;
	private boolean connected;
	public MenuItem reconnect;
	public MenuItem closeConnection;
	private Movement robot;
	private Timer timer;
	private HScale maxPower;
	private HScale ratioScale;
	private HBox h;
	private Frame f;
	
	public static final int DEF_SPEED=700;
	
	public void changeProgress(){
		progress.setFraction(1);
	}
	
	public void createGUI(final Movement robot) throws FileNotFoundException{

		this.robot=robot;
        Window w = new Window();
        timer = new Timer();
        Menu m = new Menu();
        VBox v = new VBox(false, 20);
        Table t = new Table(2,5,false);
        Table informationTable = new Table(2,2, false);
        h = new HBox(false,20);
        progress = new ProgressBar();
        progress.setText("Starting Up");
        progress.setFraction(0.2);
        
        VBox remoteControlBox = new VBox(false,20);
        VBox queueControlBox = new VBox(false,20);
        
        n = new Notebook();
        
        f = new Frame("Robot Information");
        
        batteryLevel = new ProgressBar();
        batteryLevel.setOrientation(ProgressBarOrientation.BOTTOM_TO_TOP);
        batteryLevel.setText("Waiting for Level");
        progress.setFraction(0);
        
        
        
        final Button upButton = new Button("Up");
        final Button downButton = new Button("Down");
        final Button leftButton = new Button("Left");
        final Button rightButton = new Button("Right");
        final Button strafeLeft = new Button("Strafe Left");
        final Button strafeRight = new Button("Strafe Right");
        final Button kickButton = new Button("Kick!");
        
        
        ratioScale = new HScale(0,1000,1);
        maxPower = new HScale(0,900,1);
        
        HBox ratioBox = new HBox(false,20);
        HBox powerBox = new HBox(false,20);
        
        ratioBox.packStart(new Label("Ratio:"), false, false, 0);
        powerBox.packStart(new Label("Max Power:"), false, false, 0);
        
        ratioBox.add(ratioScale);
        powerBox.add(maxPower);
        
        upButton.connect(new Widget.ButtonPressEvent() {
			
			public boolean onButtonPressEvent(Widget arg0, EventButton arg1) {
				System.out.println("Up Button Pressed");
				robot.sendControllerCommand(1,DEF_SPEED,10000);
				return false;
			}
		});
        
        upButton.connect(new Widget.ButtonReleaseEvent() {
			
			public boolean onButtonReleaseEvent(Widget arg0, EventButton arg1) {
				System.out.println("Up Button Release");
				robot.sendControllerCommand(9);
				return false;
			}
		});
        
        downButton.connect(new Widget.ButtonPressEvent() {
			
			public boolean onButtonPressEvent(Widget arg0, EventButton arg1) {
				System.out.println("down Button Pressed");
				robot.sendControllerCommand(2,DEF_SPEED,10000);
				return false;
			}
		});
        
        downButton.connect(new Widget.ButtonReleaseEvent() {
			
			public boolean onButtonReleaseEvent(Widget arg0, EventButton arg1) {
				System.out.println("down Button Release");
				robot.sendControllerCommand(9);
				return false;
			}
		});
        
        leftButton.connect(new Widget.ButtonPressEvent() {
			
			public boolean onButtonPressEvent(Widget arg0, EventButton arg1) {
				System.out.println("left Button Pressed");
				robot.sendControllerCommand(5, DEF_SPEED, -5000);
				return false;
			}
		});
        
        leftButton.connect(new Widget.ButtonReleaseEvent() {
			
			public boolean onButtonReleaseEvent(Widget arg0, EventButton arg1) {
				System.out.println("left Button Release");
				robot.sendControllerCommand(9);
				return false;
			}
		});
        
        rightButton.connect(new Widget.ButtonPressEvent() {
			
			public boolean onButtonPressEvent(Widget arg0, EventButton arg1) {
				System.out.println("right Button Pressed");
				robot.sendControllerCommand(5, DEF_SPEED, 5000);
				return false;
			}
		});
        
        rightButton.connect(new Widget.ButtonReleaseEvent() {
			
			public boolean onButtonReleaseEvent(Widget arg0, EventButton arg1) {
				System.out.println("right Button Release");
				robot.sendControllerCommand(9);
				return false;
			}
		});
        
        strafeLeft.connect(new Widget.ButtonPressEvent() {
			
			public boolean onButtonPressEvent(Widget arg0, EventButton arg1) {
				System.out.println("Strafe Left Button Pressed");
				robot.sendControllerCommand(3,255,300);
				//robot.sendControllerCommand(3);
				return false;
			}
		});
        
        strafeLeft.connect(new Widget.ButtonReleaseEvent() {
			
			public boolean onButtonReleaseEvent(Widget arg0, EventButton arg1) {
				System.out.println("Strafe Left Button Release");
				robot.sendControllerCommand(9);
				
				return false;
			}
		});
        
        strafeRight.connect(new Widget.ButtonPressEvent() {
			
			public boolean onButtonPressEvent(Widget arg0, EventButton arg1) {
				System.out.println("Strafe Right Button Pressed");
				//robot.sendControllerCommand(4);
				robot.sendControllerCommand(4,255,300);
				return false;
			}
		});
        
        strafeRight.connect(new Widget.ButtonReleaseEvent() {
			
			public boolean onButtonReleaseEvent(Widget arg0, EventButton arg1) {
				System.out.println("Strafe Right Button Release");
				
				return false;
			}
		});
        
        kickButton.connect(new Widget.ButtonPressEvent() {
			
			public boolean onButtonPressEvent(Widget arg0, EventButton arg1) {
				System.out.println("Kick Button Pressed");
				
				robot.kick();
				return false;
			}
		});
        
        
        t.attach(strafeLeft,0,1,0,1);
        t.attach(strafeRight,0,1,1,2);
        t.attach(upButton,  2,3,0,1);
        t.attach(downButton,2,3,1,2);
        t.attach(leftButton,1,2,1,2);
        t.attach(rightButton, 3,4,1,2);
        t.attach(kickButton,4,5,0,2);

        
        m.append(new MenuItem("Test"));
        
        Menu aMenu = new Menu();
        
        reconnect = new MenuItem("Reconnect");
        closeConnection = new MenuItem("Close");
        aMenu.add(reconnect);
        aMenu.add(closeConnection);
        MenuItem aMenuItem = new MenuItem("Robot");
        aMenuItem.setSubmenu(aMenu);
        
        
        
        MenuBar menuBar = new MenuBar();
        menuBar.append(aMenuItem);
        menuBar.setSizeRequest(-1, 30);
        
        v.packStart(menuBar, false, false, 0);
        
        
        remoteControlBox.add(t);
        remoteControlBox.add(ratioBox);
        remoteControlBox.add(powerBox);
        
        n.appendPage(remoteControlBox, new Label("Direct Control"));
        n.appendPage(queueControlBox, new Label("Queue Control"));
        
        h.add(n);
        
        informationTable.attach(new Label("Battery Level:"), 0, 1, 0, 1);
        informationTable.attach(batteryLevel, 1, 2, 0, 1);
        
        f.add(informationTable);
        h.add(f);
        
        v.add(h);
        
        v.packEnd(progress, false, false, 0);
  
        w.add(v);
        w.setTitle("Holly Controller");
        w.showAll();
             
        w.connect(new Window.DeleteEvent() {
            public boolean onDeleteEvent(Widget source, Event event) {
                Gtk.mainQuit();
                return false;
            }
        });

		
	}
	
	public void change(){
		progress.setFraction(1);
	}
	
	public Controller(final Movement robot) throws FileNotFoundException {

		Gtk.init(null);
		 try {
				createGUI(robot);
		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
        }
	
	public void run(){
	        Gtk.main();

	}

}
