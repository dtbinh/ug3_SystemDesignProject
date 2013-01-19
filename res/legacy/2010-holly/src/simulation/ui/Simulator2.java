package simulation.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MouseInputListener;
import java.awt.Point;

import simulation.Simulation;
import simulation.io.ObjectReader;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

public class Simulator2 extends JFrame implements ActionListener, KeyListener{
	
	
	JButton go;
	JComboBox jcmb;
	JMenu jmenu;
	JMenuBar mb;
	JMenuItem quit;
	JMenuItem def;
	JMenuItem open;
	JMenuItem visCoords;
	JPanel visHolder;
	JPanel palette;
	JPanel blueBox;
	JPanel yellowBox;
	JPanel ballBox;
	JPanel borderPane;
	JButton runSim;
	JButton restartSim;
	JButton stopSim;
	JButton trial;
	JButton pen;
	Thread simThread;
	Simulation sim;
	NimRODTheme nt;
	JLabel blueLbl;
	JLabel yellowLbl;
	JLabel ballLbl;
	ImageIcon yellowPic;
	ImageIcon bluePic;
	ImageIcon ballPic;
	
	//variables for mouse listeners

	Component[] components ={this,blueBox,yellowBox,ballBox};
	int heldComponent =0 ;//0=empty,1=blue,2=yellow,3=ball
	int location=0; //0=blank area, 1 = pitch,3=blue, 4=yellow#
	int onPitch = 0;
	int positionX = 0;
	int positionY = 0;
	
	public Point [] storedPositions;// = {new Point(100,100),new Point(200,100),new Point(370,160)};
	public int [] storedPositions2;
	public int color;
	public int side;
	//int PITCH_WIDTH = 624;
	//int PITCH_HEIGHT = 346;
	int[] score = {0,0};	
	
	
	
	
	public Simulator2(int [] pos, int color, int side){
		
		storedPositions2 = pos;
		
		setFocusable(true);
		setLocation(50,50);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("TestFrame");
		//Toolkit toolkit = Toolkit.getDefaultToolkit();
		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		//Set appearance
		setUndecorated(false);
		
		
		
		CustomMouseListener cml = new CustomMouseListener();
		 addMouseListener(cml);
		 addMouseMotionListener(cml);
		//setUndecorated(true);
		try {
			
			nt = new NimRODTheme();
			
			//Highlight Color
			nt.setPrimary2( new Color(34,32,76));
			
			//Main Color
			nt.setSecondary3(new Color(92,84,204));

			NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
			NimRODLookAndFeel.setCurrentTheme(nt);
			
			UIManager.setLookAndFeel(NimRODLF);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		
		borderPane = new JPanel();
		borderPane.addKeyListener(this);
		borderPane.setBorder(BorderFactory.createEtchedBorder(1));
		borderPane.setBackground(new Color(63,58,140));
		borderPane.setLayout(null);
		
		
		//add palette
		 //palette = new JPanel();
		 //palette.setBounds(900,50,100,500);
		 //palette.setBackground(Color.CYAN);
		 //borderPane.add(palette);
		
		
		bluePic = new ImageIcon("simulation/ui/blue.gif", "Blue Robot");
		yellowPic = new ImageIcon("simulation/ui/yellow.gif", "Yellow Robot");
		ballPic = new ImageIcon("simulation/ui/ball.gif", "Ball");
		 
		 //palette contents
		blueBox = new JPanel();
		blueBox.setBounds(870,150,54,60);
		blueBox.setBackground(Color.blue);
		blueBox.addMouseListener(cml);
		blueBox.addMouseMotionListener(cml);
		this.add(blueBox);
		 		 
		yellowBox = new JPanel(); 
		yellowBox.setBounds(870,80,54,60);
		yellowBox.setBackground(Color.yellow);
		yellowBox.addMouseListener(cml);
		yellowBox.addMouseMotionListener(cml);
		this.add(yellowBox);
		
		
		ballBox = new JPanel(); 
		ballBox.setBounds(870,230,40,40);
		ballBox.setBackground(Color.red);
		ballBox.addMouseListener(cml);
		ballBox.addMouseMotionListener(cml);
		this.add(ballBox);
		
		blueLbl = new JLabel("Blue Robot");
		blueLbl.setBounds(900,110,100,50);
		blueLbl.setBackground(new Color(34,32,76));
		//this.add(blueLbl);
		 
		yellowLbl = new JLabel("Yellow Robot");
		yellowLbl.setBounds(900,260,100,50);
		yellowLbl.setBackground(new Color(34,32,76));
		//this.add(yellowLbl);
		 
		ballLbl = new JLabel("Ball",ballPic,JLabel.CENTER);
		ballLbl.setBounds(900,410,100,100);
		ballLbl.setBackground(new Color(34,32,76));
		//this.add(ballLbl);
		
		components[1] = yellowBox;
		components[2] = blueBox;
		components[3] = ballBox;
		
		//Add visualisation holder
		visHolder = new JPanel();
		visHolder.addKeyListener(this);
		visHolder.addMouseListener(cml);
		visHolder.addMouseMotionListener(cml);
		
		
		visHolder.setBackground(Color.white);
		visHolder.setBounds(15,75,850,500);
		
		
				
		borderPane.add(visHolder);
		this.getContentPane().add(borderPane);
		
		//Set up menu
		jmenu = new JMenu("File");
		jmenu.addKeyListener(this);
		
		def = new JMenuItem("Run the default simulation");
		def.addActionListener(this);
		open = new JMenuItem("Open a world from a file");
		open.addActionListener(this);
		visCoords = new JMenuItem("Use the video feed");
		visCoords.addActionListener(this);
		quit = new JMenuItem("Exit");
		quit.addActionListener(this);

		jmenu.add(def);
		jmenu.add(open);
		jmenu.add(visCoords);
		jmenu.add(quit);
		
		mb = new JMenuBar();
		mb.add(jmenu);
		mb.setBorder(BorderFactory.createEtchedBorder(1));
		mb.addKeyListener(this);
		setJMenuBar(mb);
		
		//Add button pane
		JToolBar buttons = new JToolBar();
		buttons.addKeyListener(this);
		//ImageIcon play = createImageIcon("/afs/inf.ed.ac.uk/user/s07/s0791003/workspace/Robinho/src/simulation/images/play.png", "Play Button");
		runSim = new JButton(" | RUN | ");
		runSim.addActionListener(this);
		runSim.addKeyListener(this);
		runSim.setPreferredSize(new Dimension(100, 100));
		buttons.add(runSim);
		restartSim = new JButton("| Reset score | ");
		restartSim.addActionListener(this);
		restartSim.addKeyListener(this);
		restartSim.setPreferredSize(new Dimension(100, 100));
		buttons.add(restartSim);
		stopSim = new JButton(" | stop | ");
		stopSim.addActionListener(this);
		stopSim.addKeyListener(this);
		stopSim.setPreferredSize(new Dimension(100, 100));
		buttons.add(stopSim);
		
		trial = new JButton("| second half | ");
		trial.addActionListener(this);
		trial.addKeyListener(this);
		trial.addActionListener(this);
		trial.addKeyListener(this);
		trial.setPreferredSize(new Dimension(100, 100));
		buttons.add(trial);
		
		pen = new JButton("| penalty kick | ");
		pen.addActionListener(this);
		pen.addKeyListener(this);
		pen.addActionListener(this);
		pen.addKeyListener(this);
		pen.setPreferredSize(new Dimension(100, 100));
		buttons.add(pen);
		buttons.setBounds(250, 600, 400, 50);
		
		
		
		
		borderPane.add(buttons);
		
		
	}
	
	
	protected ImageIcon createImageIcon(String path,
            String description) {
java.net.URL imgURL = getClass().getResource(path);
if (imgURL != null) {
return new ImageIcon(imgURL, description);
} else {
System.err.println("Couldn't find file: " + path);
return null;
}
}	
	@Override
public void actionPerformed(ActionEvent e) {
		
		Object o = e.getSource();
	    //Handle open button action.
	    if (o == quit) {
	    	System.exit(0);
	    }
	    
	    else if (o == open){
	    
	    	JFileChooser fc = new JFileChooser();
		    	
		    int returnVal = fc.showOpenDialog(this);

		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		            
		    ObjectReader or = new ObjectReader(file);
		            
		    }
	    }
	    
	    else if (o==def){
	  	  
	    	sim = new Simulation(storedPositions2, color, side);
	    	sim.setBounds(15,75,1050,590);//sim.setBounds(100,100,732,366);
	    	
	    	borderPane.remove(visHolder);
	    	borderPane.add(sim);
	    	borderPane.validate();
	    	sim.createBufferStrategy(2);
	    	sim.init();
	    	sim.addKeyListener(this);
	    	simThread = new Thread(sim);
	    	
	    }
	    else if (o==visCoords){
	    	
	    }
	    
	    else if (o==runSim){
	    	if(simThread!=null && !simThread.isAlive()){
	    		simThread.start();
	    	}
	    	
	    	
	    }
	    
	    else if (o==stopSim){
	    	if(simThread!=null && simThread.isAlive()){
	    		
	    		
	    		//heldPosition[0][0] = sim.oppRobot.getPosition().getX();
	    		//heldPosition[0][1] = sim.oppRobot.getPosition().getY();
	    		//heldPosition[1][0] = sim.myRobot.getPosition().getX();
	    		//heldPosition[1][1] = sim.myRobot.getPosition().getY();
	    		//heldPosition[2][0] = sim.ball.getPosition().getX();
	    		//heldPosition[2][1] = sim.ball.getPosition().getY();
	    		
	    	}
	    }
	    else if (o==restartSim){
	    	if(simThread!=null && simThread.isAlive()){
	    		//set time and score to zero 
	    		//should not try to directly restart thread but let stop 
	    		//and start do it as they have conditions preventing errors
	    	}
	    }
	    else if (o==trial){
	    	
	    	//
	    	sim.second();
	    	//
	    		
	    }
	    else if (o==pen){
	    	sim.penalty();
	    }
		    
	    }

	public void keyPressed(KeyEvent e) {
		System.out.println("Key Pressed");
		if(sim!=null){
			sim.keyPressed(e);
		}
		
	}

	public void keyReleased(KeyEvent e) {
		if(sim!=null){
			sim.keyReleased(e);
		}
	}

	public void keyTyped(KeyEvent e) {
		if(sim!=null){
			sim.keyTyped(e);
		}
		
	}
	


	
	
	class CustomMouseListener extends MouseAdapter{
		
		@Override
		public void mouseClicked(MouseEvent e) {
			//System.out.println(e.getComponent());
		
		if(heldComponent == 0){
			Component current = e.getComponent();
			//System.out.println(current);
			
			for(int i=1;i<4;i++){
				if (current == components[i]){
					//System.out.println("confirmed "+current+" dupa "+ components[i]);
					heldComponent = i;
					//System.out.println(i);
				}
				}
			
			//System.out.println("confirmed "+heldComponent+" dupa");
		}
		else{
						
			if(onPitch==1){
				
			 positionX = e.getX();
			 positionY = e.getY();
			
			//System.out.println("co-ord "+positionX+" "+positionY);
			
			
				
				switch(heldComponent){
				case 1:storedPositions[0].setLocation(positionX, positionY);
				break;
				case 2:storedPositions[1].setLocation(positionX, positionY);
				break;
				case 3:storedPositions[2].setLocation(positionX, positionY);
				break;
				
				}
				
			
			System.out.println("bluebot "+storedPositions[0]+" "+storedPositions[1]+" yellowBot "+storedPositions[2]);

			}
			else{
				heldComponent = 0;
			}
			
			
		}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			Component current = (e.getComponent());
			if (current==visHolder){
				onPitch=1;
			}
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			Component current = (e.getComponent());
			if (current==visHolder){
				onPitch=0;
			}
			
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
			
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		
			
			
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			String comp = new String(e.getComponent().toString());
			positionX = e.getX();
			positionY = e.getY();
		//	System.out.println("posistion "+positionX+" "+positionY);
			
		}
	}
	
	
	public static void main(String[] args){
		
		// ourRobot x,y angle,oppRobot x,y,angle,ball x,y
		//true,20,30,500,300,270
		int [] coordinates = {500,300, 180, 100,260,180, 30,30};
		//Point[] positions = {new Point(100,100),new Point(200,100),new Point(370,160)};
		
		// positions, color (0-yellow, 1-blue), side (0-left, 1-right) 
		Simulator2 sg = new Simulator2(coordinates, 1, 0);
		
		
		sg.setSize(1150, 700);
		sg.setVisible(true);
	}
}
