package PC;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import Planning.Robot;
import Planning.Dribble;


public class ControlGUI extends JFrame{
	private JFrame frame =new JFrame("Control Panel");
	
	private JPanel dribblePnl= new JPanel(); 
	private JPanel navigatePnl= new  JPanel();
	private JPanel stopPnl = new JPanel();
	
	private JButton dribble = new JButton("Dribble");
	private JButton navigate = new JButton("navigate");
	private JButton stop = new JButton("Stop");
	
	static Robot r = new Robot(); 
	
	
	public static void main(String[] args){
		
		ControlGUI gui = new ControlGUI();
		gui.Launch();
		gui.action();
		
	}
	
	public ControlGUI(){
		dribblePnl.add(dribble);
		navigatePnl.add(navigate);
		stopPnl.add(stop);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(dribblePnl,BorderLayout.WEST);
		frame.getContentPane().add(navigatePnl,BorderLayout.CENTER);
		frame.getContentPane().add(stop,BorderLayout.EAST);
		frame.addWindowListener(new ListenCloseWdw());
		
	}
	
	

	public void action(){		
		dribble.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("dribble...");
				Planning.Dribble.main(new String[] {"blue"});
			}
			
		});
		
		navigate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("navigate...");			
						
			}
		});
		
		stop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("Stop...");
			}
		});
		
	}
	
	public class ListenCloseWdw extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			System.exit(0);
		}
	}
	
	public void Launch(){
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	
	
	
}

