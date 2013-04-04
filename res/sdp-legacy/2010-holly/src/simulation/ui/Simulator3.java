package simulation.ui;

import java.awt.Color;
import simulation.bodies.Ball;
import simulation.bodies.Robot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MouseInputListener;
import java.awt.Point;

import simulation.Simulation;
import simulation.bodies.SimBody;
import simulation.io.ObjectReader;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

public class Simulator3 extends JFrame implements ActionListener, KeyListener,ChangeListener{
	
	
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
	JPanel second;
	JPanel third;
	JPanel forth;
	JButton runSim;
	JButton restartSim;
	JButton stopSim;
	JButton trial;
	JButton pen;
	JTextArea textArea1;
	JTextArea textArea2;
	JComboBox whatSide;
	JSlider blueSlider;
	JSlider yellowSlider;
	Thread simThread;
	Simulation sim;
	NimRODTheme nt;
	JLabel blueLbl;
	JLabel yellowLbl;
	JLabel ballLbl;
	JLabel blueLbl2;
	JLabel yellowLbl2;
	JLabel ballLbl2;
	ImageIcon yellowPic;
	ImageIcon bluePic;
	ImageIcon ballPic;
	
	//variables for mouse listeners
	//test

	Component[] components ={this,blueLbl,yellowLbl,ballLbl};
	int heldComponent2 = 0;
	int heldComponent =0 ;//0=empty,1=blue,2=yellow,3=ball
	int location=0; //0=blank area, 1 = pitch,3=blue, 4=yellow#
	int onPitch = 0;
	int positionX = 0;
	public Color[] cols = { Color.yellow, Color.blue, Color.red, Color.black, Color.green};
	int positionY = 0;
	
	public Point [] storedPositions = {new Point(100,260),new Point(500,300),new Point(30,30)};
	public int [] storedPositions2 = {500,300, 180, 100,260,180, 30,30};
	String[] sides = {"blue","yellow"};
	
	int PITCH_WIDTH = 624;
	int PITCH_HEIGHT = 346;
	int[] score = {0,0};
	int[] angles = {180,180};
	float[] rotations = {0,0,0};
	Point ghost = new Point(0,0);
	public int color = 0;
	public int side;
	public Graphics2D g2;
	
	
	
	public Simulator3(int color,int side){
		setFocusable(true);
		
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
		
		
		bluePic = createImageIcon("blue.gif", "Blue Robot");
		yellowPic = createImageIcon("yellow.gif", "Yellow Robot");
		ballPic = createImageIcon("ball.gif", "Ball");
		 
		 //palette contents
		blueBox = new JPanel();
		blueBox.setBounds(900,10,100,100);
		blueBox.setBackground(Color.blue);
		blueBox.addMouseListener(cml);
		blueBox.addMouseMotionListener(cml);
		//this.add(blueBox);
		 		 
		yellowBox = new JPanel(); 
		yellowBox.setBounds(900,160,100,50);
		yellowBox.setBackground(Color.yellow);
		yellowBox.addMouseListener(cml);
		yellowBox.addMouseMotionListener(cml);
		//this.add(yellowBox);
		
		
		ballBox = new JPanel(); 
		ballBox.setBounds(900,310,200,50);
		ballBox.setBackground(Color.red);
		ballBox.addMouseListener(cml);
		ballBox.addMouseMotionListener(cml);
		//this.add(ballBox);
		
		blueLbl = new JLabel("Blue Robot",bluePic,JLabel.CENTER);
		blueLbl.setBounds(900,100,200,50);
		blueLbl.setBackground(new Color(34,32,76));
		blueLbl.addMouseListener(cml);
		blueLbl.addMouseMotionListener(cml);
		this.add(blueLbl);
		 
		yellowLbl = new JLabel("Yellow Robot",yellowPic,JLabel.CENTER);
		yellowLbl.setBounds(900,170,200,50);
		yellowLbl.setBackground(new Color(34,32,76));
		yellowLbl.addMouseListener(cml);
		yellowLbl.addMouseMotionListener(cml);
		this.add(yellowLbl);
		
		 
		ballLbl = new JLabel("Ball",ballPic,JLabel.CENTER);
		ballLbl.setBounds(900,240,100,50);
		ballLbl.addMouseListener(cml);
		ballLbl.addMouseMotionListener(cml);
		this.add(ballLbl);
		
		blueLbl2 = new JLabel(bluePic,JLabel.CENTER);
		blueLbl2.addMouseListener(cml);
		blueLbl2.addMouseMotionListener(cml);
				 
		yellowLbl2 = new JLabel(yellowPic,JLabel.CENTER);
		yellowLbl2.addMouseListener(cml);
		yellowLbl2.addMouseMotionListener(cml);
				 
		ballLbl2 = new JLabel(ballPic,JLabel.CENTER);
		ballLbl2.addMouseListener(cml);
		ballLbl2.addMouseMotionListener(cml);
		
		components[1] = yellowLbl;
		components[2] = blueLbl;
		components[3] = ballLbl;
		
		//Add visualisation holder
		visHolder = new Canvas();
		visHolder.addKeyListener(this);
		visHolder.addMouseListener(cml);
		visHolder.addMouseMotionListener(cml);
		visHolder.setBackground(Color.GRAY);
		//visHolder.setBounds(5,0,PITCH_WIDTH,PITCH_HEIGHT);
		
		
		//visHolder.setBackground(Color.white);
		visHolder.setBounds(69,85,PITCH_WIDTH,PITCH_HEIGHT);//visHolder.setBounds(100,100,732,366);
		
				
		//Graphics2D g2 = null;
		
		//g2.setColor(new Color(240,240,240));
		//g2.fillRect(69,85,PITCH_WIDTH,PITCH_HEIGHT);
				
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
		
		textArea1 = new JTextArea(1,10);
		textArea1.setEditable(false);

		JPanel third = new JPanel();
		third.setBounds(900, 300, 175, 75);
		third.add(textArea1);
	    
	    textArea2 = new JTextArea(1, 10);
	    textArea2.setEditable(false);
	    third.add(textArea2);
	     
	    borderPane.add(third);  
		
		JPanel second = new JPanel();
		second.setBounds(900,10,125,40);
		second.setBackground(new Color(34,32,76));
		
		JComboBox whatSides= new JComboBox(sides);
		whatSides.setSelectedIndex(0);
		whatSides.addActionListener(this);
		second.add(whatSides);
		
		blueSlider = new JSlider(JSlider.HORIZONTAL,0,360,0);
		blueSlider.addChangeListener(this);
		
		
		yellowSlider = new JSlider(JSlider.HORIZONTAL,0,360,0);
		yellowSlider.addChangeListener(this);
		
		
		blueSlider.setMajorTickSpacing(10);
		blueSlider.setMinorTickSpacing(1);
		blueSlider.setPaintTicks(true);
		
		yellowSlider.setMajorTickSpacing(10);
		yellowSlider.setMinorTickSpacing(1);
		yellowSlider.setPaintTicks(true);
		
		
		JPanel forth = new JPanel();
		forth.setBounds(900,400,200,75);
		forth.setBackground(new Color(34,32,76));
		
		forth.add(blueSlider);
		forth.add(yellowSlider);
		
		borderPane.add(second);
		borderPane.add(forth);
		
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
	  	  
	    	sim = new Simulation(storedPositions2,color,side);
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
	    	
	    	sim = new Simulation(storedPositions2,color,side);
	    	sim.setBounds(15,75,850,590);//sim.setBounds(100,100,732,366);
	    	borderPane.remove(visHolder);
	    	borderPane.add(sim);
	    	borderPane.validate();
	    	sim.createBufferStrategy(2);
	    	sim.init();
	    	sim.addKeyListener(this);
	    	simThread = new Thread(sim);
	    		
	    }
	    
	    else if(o==whatSide){
	    	color = whatSide.getSelectedIndex();
	    	System.out.println("fire");
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
	
	@Override
	public void stateChanged(ChangeEvent e) {
	    Object source = e.getSource();
	    
	    if(source == yellowSlider)
	    	angles[1] = yellowSlider.getValue();
	    else
	    	angles[0] = blueSlider.getValue();
	    	
	    storedPositions2[2] = angles[0]-90;
	    if(storedPositions2[2]<0)
	    	storedPositions2[2] = 360+storedPositions2[2];
	    	                
	    storedPositions2[5] = angles[1]-90;
	    if(storedPositions2[5]<0)
	    	storedPositions2[5] = storedPositions2[2]+360;
	    
	    System.out.println(angles[0]+" "+angles[1]);
	    visHolder.repaint();
	}
	
	
	class CustomMouseListener extends MouseAdapter{
		
		@Override
		public void mouseClicked(MouseEvent e) {
			System.out.println(e.getComponent());
		
		if(heldComponent == 0){
			Component current = e.getComponent();
			//System.out.println(current);
			
			for(int i=1;i<4;i++){
				if (current == components[i]){
					System.out.println("confirmed "+current+" dupa "+ components[i]);
					heldComponent = i;
					System.out.println(i);
				}
				}
			
			System.out.println("confirmed "+heldComponent+" dupa");
		}
		else{
						
			if(onPitch==1){
				
			 positionX = e.getX();
			 positionY = e.getY();
			
			System.out.println("co-ord "+positionX+" "+positionY);
			
			
				
				switch(heldComponent){
				case 1:storedPositions[0].setLocation(positionX, positionY);
				storedPositions2[3]= positionX;
				storedPositions2[4] = positionY; 
				break;
				case 2:storedPositions[1].setLocation(positionX, positionY);
				storedPositions2[0] = positionX;
				storedPositions2[1] = positionY;
				break;
				case 3:storedPositions[2].setLocation(positionX, positionY);
				storedPositions2[6] = positionX;
				storedPositions2[7] = positionY;
				break;
				
				}
				
				visHolder.repaint();
				
			
			System.out.println("bluebot "+storedPositions2[0]+" "+storedPositions2[1]+" yellowBot "+storedPositions2[2]);
			String l = ("tom");
			textArea1.append(l);
			textArea1.setCaretPosition(textArea1.getDocument().getLength());
			}
			
				heldComponent = 0;
			
			
			
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
			if (onPitch == 1){
			
				heldComponent2 = 0;
			if(e.getX()>=storedPositions[0].x && e.getX()<=(storedPositions[0].x+54) && e.getY()>=storedPositions[0].y && e.getY()<=(storedPositions[0].y+60))	
				heldComponent2 = 1;
			
			if(e.getX()>storedPositions[1].y&&e.getX()<(storedPositions[1].x+54)&&e.getY()>storedPositions[1].y&&e.getY()<(storedPositions[1].y+60))
				heldComponent2 = 2;
			
			if(e.getX()>storedPositions[2].x&&e.getX()<(storedPositions[2].x+20)&&e.getY()>storedPositions[2].y&&e.getY()<(storedPositions[2].y+20))
				heldComponent2 = 3;}
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(heldComponent2 ==1){
				storedPositions[0].x = e.getX();
				storedPositions[0].y = e.getY();
				storedPositions2[3] = e.getX();
				storedPositions2[4]= e.getY();}
			
		if(heldComponent2 ==2){
			storedPositions[1].x = e.getX();
			storedPositions[1].y = e.getY();
			storedPositions2[0] = e.getX();
			storedPositions2[1]= e.getY();}
		
		if(heldComponent2 ==3){
			storedPositions[2].x = e.getX();
			storedPositions[2].y = e.getY();
			storedPositions2[6] = e.getX();
			storedPositions2[7]= e.getY();}
			
			
			visHolder.repaint();
		
			
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			String comp = new String(e.getComponent().toString());
			positionX = e.getX();
			positionY = e.getY();
		//	System.out.println("posistion "+positionX+" "+positionY);
			
		}
	}
	
	
	public class Canvas extends JPanel{

		
		
			
			public Graphics2D g2;
	        @Override
	        public void paint(Graphics g)
	        {
	                super.paintComponent(g);
	                g2 = (Graphics2D) g;
	                
	                for(int i=0;i<2;i++){
	                
	                ((Graphics2D) g2).rotate(Math.toRadians((float)angles[i]), (int)storedPositions[i].x,(int)storedPositions[i].y);

	                
	                g2.setColor(cols[4]);
	                g2.fillRect((int)storedPositions[i].x-27,(int)storedPositions[i].y-30, 54, 60);
	              
	                g2.setColor(cols[3]);
	                ((Graphics2D) g2).fillOval((int)storedPositions[i].x-6,(int)storedPositions[i].y+15, 12, 12);
	                
	                g2.setColor(cols[i]);
	                ((Graphics2D) g2).fillRect((int)storedPositions[i].x-20,(int)storedPositions[i].y, 40, 12);
	                ((Graphics2D) g2).fillRect((int)storedPositions[i].x-6,(int)storedPositions[i].y-27, 12, 30);

	                
	                ((Graphics2D) g2).rotate(Math.toRadians(-(float)angles[i]), (int)storedPositions[i].x,(int)storedPositions[i].y);
	                g2.setColor(Color.red);
					g2.fillOval(storedPositions[2].x, storedPositions[2].y, 20, 20);
	                
	                }
	                
	             }
	}
	
	
	
	
	public static void main(String[] args){
		// ourRobot x,y angle,oppRobot x,y,angle,ball x,y
		//true,20,30,500,300,270
		//int [] coordinates = {500,300, 180, 100,260,180, 30,30};
		//Point[] positions = {new Point(100,100),new Point(200,100),new Point(370,160)};
		
		// positions, color (0-yellow, 1-blue), side (0-left, 1-right) 
		Simulator3 sg = new Simulator3(1,0);
		
		
		sg.setSize(1150, 700);
		sg.setVisible(true);
	}
}
