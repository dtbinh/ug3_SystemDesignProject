package PC;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import PC.ControlGUI.ListenCloseWdw;
import javax.swing.BoxLayout;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Milestone2 {

	private JFrame frame;
	
		
	private JButton navigate = new JButton("Navigate");
	private final JButton dribble = new JButton("Dribble");
	private final JButton stop = new JButton("Stop");
	private final JRadioButton yellow = new JRadioButton("Yellow");
	private final JRadioButton blue = new JRadioButton("Blue");
	ButtonGroup group = new ButtonGroup();
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Milestone2 window = new Milestone2();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Milestone2() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 259, 138);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(24)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(dribble)
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(yellow)
								.addComponent(navigate)))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(stop, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(blue, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(64, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(dribble)
						.addComponent(navigate))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(40)
							.addComponent(yellow)
							.addGap(61))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(blue, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
								.addComponent(stop))
							.addContainerGap())))
		);
		dribble.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (yellow.isSelected()){
				Planning.Dribble.main(new String[] {"yellow"});
				} else {
				Planning.Dribble.main(new String[] {"blue"});
				}
			}
		});
		navigate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (yellow.isSelected()){
				Planning.Runner.main(new String[] {"yellow"});
				} else {
				Planning.Runner.main(new String[] {"blue"});
				}
			}
		});
		blue.setSelected(true);
		group.add(yellow);
		group.add(blue);
		
		
		
		frame.getContentPane().setLayout(groupLayout);
				
	}

}
