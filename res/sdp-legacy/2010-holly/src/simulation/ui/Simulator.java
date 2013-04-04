package simulation.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.*;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

import simulation.Simulation;
import simulation.io.ObjectReader;

//@author Ben Ledbury
//@author Joe Tam

public class Simulator extends JFrame implements ActionListener, KeyListener{
	
	JButton go;
	JComboBox jcmb;
	JMenu jmenu;
	JMenuBar mb;
	JMenuItem quit;
	JMenuItem def;
	JMenuItem open;
	JMenuItem visCoords;
	JPanel visHolder;
	JPanel borderPane;
	JButton runSim;
	Thread simThread;
	Simulation sim;
	NimRODTheme nt;

	public Simulator(){
		
		setFocusable(true);
		
		//Set appearance
		setUndecorated(true);
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
		
		//Add visualisation holder
		visHolder = new JPanel();
		visHolder.addKeyListener(this);
		visHolder.setBackground(Color.white);
		visHolder.setBounds(15,75,1130,500);//visHolder.setBounds(100,100,732,366);
		
		
		borderPane.add(visHolder, BorderLayout.CENTER);
		
		//Add button pane
		JToolBar buttons = new JToolBar();
		buttons.addKeyListener(this);
		//ImageIcon play = createImageIcon("/afs/inf.ed.ac.uk/user/s07/s0791003/workspace/Robinho/src/simulation/images/play.png", "Play Button");
		runSim = new JButton("Run");
		runSim.addActionListener(this);
		runSim.addKeyListener(this);
		buttons.add(runSim);
		buttons.setBounds(300, 15, 300, 50);//buttons.setBounds(100, 500, 300, 50);
		
		
		borderPane.add(buttons);
		
		this.getContentPane().add(borderPane);
		
		this.addKeyListener(this);
	}

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
	  
	    	//sim = new Simulation(822,394,54,180, null);
	    	sim.setBounds(15,75,1130,590);//sim.setBounds(100,100,732,366);
	    	
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
	    
	}
	
	public static void main(String[] args){
		Simulator sg = new Simulator();
		
		sg.setSize(1150, 630);
		
		sg.setVisible(true);
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
	
	/** Returns an ImageIcon, or null if the path was invalid. */
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


}
