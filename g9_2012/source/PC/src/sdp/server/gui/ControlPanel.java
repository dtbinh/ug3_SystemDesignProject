package sdp.server.gui;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sdp.server.PropertyProvider;

/** Simple GUI class that creates a 3DS Max style control panel (wishful thinking).
 * For now it only supports sliders and buttons; it does not support scrolling or collapsable categories.
 * 
 * @author Cristian Cobzarenco
 * @author Tymon Zgainski
 *
 */
public class ControlPanel extends JFrame {
	private static final long serialVersionUID = -5666054416824552157L;
	private JPanel panel;
	
	/*
	static {
		try {
		    UIManager.setLookAndFeel(
		        UIManager.getSystemLookAndFeelClassName());
		} catch( Exception ex ) {
		  System.out.println("Unable to load native look and feel");
		}
	}*/
	
	/** Construct a new control panel. The title is initialised to "Control Panel", this can be changed using
	 * JFrame.setTitle(). Also closing any control panel kills the application.
	 */
	public ControlPanel() {
		// FIXME: Add c-tor arguments for the title and exit behaviour.
		super("Control Panel");
		add( panel = new JPanel() );
		panel.setLayout(new GridLayout(0,1) );
		setVisible(true);
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
	}
	
	/** This interface implements callback functionality for sliders. */
	static public interface SliderCallback {
		/** Method that gets called when the value of the slider is changed.
		 * This is called continuously while adjusting the value.
		 * 
		 * @param slider The JSlider whose value was changed.
		 * @param newValue The new value that the slider was changed to.
		 */
		void valueChanged( JSlider slider, int newValue );
	}
	
	/** This interface implements callback functionality for buttons. */
	static public interface ButtonCallback {
		/** Method that gets called when the button gets clicked.
		 * 
		 * @param button The JButton that was clicked (may be used to change its caption, for example).
		 */
		void buttonClicked( JButton button );
	}
	
	/** This interface implements callback functionality for spinners. */
	static public interface SpinnerCallback {
		void valueChanged( SpinnerNumberModel spinner, double newValue );
	}
	
	public void removeButton( String name ) {
		boolean success = false;
		for( Component c : panel.getComponents() ) {
			if( c instanceof JButton ) {
				if( ((JButton)c).getText().equals( name ) ) {
					panel.remove( c );
					panel.revalidate();
					panel.validate();
					success = true;
				}
			}
		}
		
		if( !success ) {
			System.err.println("ControlPanel: removeButton(): Could not remove button '" + name + "'.");
			Thread.dumpStack();
		}
	}
	
	/** Add a new slider to the control panel, with a given name, range and callback.
	 * 
	 * @param name The name of the slider (this is used both for the caption and to retrieve it with getSlider()).
	 * @param init The initial value of the slider.
	 * @param min The minimum value that can be set.
	 * @param max The maximum value that can be set.
	 * @param callback The callback that gets called when the slider's value gets changed.
	 */
	public void addSlider( String name, int init, int min, int max, SliderCallback callback ) {
		JLabel  label  = new JLabel(name, JLabel.CENTER);
		JSlider slider = new JSlider(JSlider.HORIZONTAL,min,max,init);
		slider.addChangeListener( new SliderCallbackAdapter( callback ) );
		slider.setBorder( BorderFactory.createEmptyBorder(0,0,10,0) );
		panel.add( label );
		panel.add( slider );
		pack();
	}
	
	public void addSpinner( String name, double init, double min, double max, double step, SpinnerCallback callback ) {
		JLabel             label = new JLabel( name, JLabel.CENTER );
		SpinnerNumberModel model = new SpinnerNumberModel( init, min, max, step );
		JSpinner           spin  = new JSpinner( model );
		
		spin.getModel().addChangeListener( new SpinnerCallbackAdpater( callback ) );
		spin.setEditor( new JSpinner.NumberEditor( spin, "0.000"));
		//spin.setBorder( BorderFactory.createEmptyBorder(0,0,10,0) );
		
		panel.add( label );
		panel.add( spin );
		pack();
	}
	
	/** Add a new button to the control panel with a given name and callback.
	 * 
	 * @param name The name of the button (also the caption of the button).
	 * @param callback The callback that gets called when the button is clicked.
	 */
	public void addButton( String name, ButtonCallback callback) {
		JButton	button = new JButton(name);
		button.addActionListener( new ButtonCallbackAdapter(callback));
		panel.add(button);
		pack();
	}
	
	/** Get a slider with a given name. This is a linear string search - i.e. not fast.
	 * 
	 * @param name The name of the searched slider.
	 * @return The JSlider with the given name, null if one does not exist.
	 */
	public JSlider getSlider( String name ) {
		int n = panel.getComponentCount() - 1;
		
		for( int i = 0 ; i < n ; ++ i ) {
			// If the current component is a label and the next one is a slider, the label is the slider's
			// caption.
			if( panel.getComponent(i) instanceof JLabel && panel.getComponent(i+1) instanceof JSlider ) {
				JLabel l = (JLabel) panel.getComponent( i );
				if( l.getText().equals(name) ) // Check that the text of the label matched the query.
					return (JSlider) panel.getComponent( i + 1 );
			}
		}
		
		// If we didn't find anything return null.
		return null;
	}
	
	private class PropertySpinner implements SpinnerCallback {
		private PropertyProvider.Property prop;
		
		PropertySpinner( PropertyProvider.Property prop ) {
			System.out.println( prop.getValue() + " " + prop.getMin() + " " + prop.getMax() + " " + prop.getStep() );
			addSpinner( prop.getKey(), prop.getValue(), prop.getMin(), prop.getMax(), prop.getStep(), this );
			this.prop = prop;
		}
		
		@Override
		public void valueChanged( SpinnerNumberModel spinner, double newValue ) {
			prop.setValue( newValue );
		}
	}
	
	public void addPropertyProviderSpinners( PropertyProvider provider ) {
		for( String key : provider.getAllKeys() )
			new PropertySpinner( provider.getProperty( key ) );
	}
	
	/** This class interfaces a ChangeListener with a SliderCallback. */
	static private class SliderCallbackAdapter implements ChangeListener {
		private SliderCallback cb;
		
		public SliderCallbackAdapter( SliderCallback cb ) {
			this.cb = cb;
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider)e.getSource();
			cb.valueChanged( source, (int)source.getValue() );
		}
	}
	
	static private class SpinnerCallbackAdpater implements ChangeListener {
		private SpinnerCallback cb;
		
		public SpinnerCallbackAdpater( SpinnerCallback cb ) {
			this.cb = cb;
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			SpinnerNumberModel source = (SpinnerNumberModel)e.getSource();
			cb.valueChanged( source, source.getNumber().doubleValue() );
		}
	}
	
	/** This class interfaces an ActionListener with a ButtonCallback. */
	static private class ButtonCallbackAdapter implements ActionListener {
		private ButtonCallback callback;
		
		public ButtonCallbackAdapter( ButtonCallback cb ) {
			this.callback = cb;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource();
			callback.buttonClicked(source);
		}
	}
	
	
}
