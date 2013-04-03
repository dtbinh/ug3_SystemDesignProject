package sdp.server.nxt;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import sdp.server.math.Vector2;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

/** This class wraps the communication with the NXT brick. Messages are sent to the brick using the update()
 * method (which allows the setting of the wheel speeds) and the kick() and robotShutdown() methods.
 * Eventually this will also get the encoder values back from the brick, but this is not implemented yet.
 * 
 * The protocol used for sent messages is this:
 * 		\<OP_CODE\> [PARAMETRS ...]
 * 
 * The number of parameters and their type depends on the OP_CODE which is a single byte. Right now, there
 * is one binary operation (with two floats) and two nullary operations implemented (with no parameters);
 * They are:
 * 		\<OP_SPEEDS:0x0\> \<LEFT_WHEEL_SPEED:float\> \<RIGHT_WHEEL_SPEED:float\>
 * 		\<OP_KICK:0x1\>
 * 		\<OP_SHUTDOWN:0x2\>
 *  
 * @author Tymon Zgainski
 * @author Magda Sternik
 * @author Cristian Cobzarenco
 *
 */
public class Communication {
	private static final byte    OP_SPEEDS    = 1;
	private static final byte    OP_KICK      = 2;
	private static final byte    OP_SHUTDOWN  = 3;
	
	private boolean useUSB = false;
	private boolean connected = false;
	private boolean dummyMode = true;
	private boolean touchLeft;
	private boolean touchRight;
	
	private DataOutputStream os;
	//private InputStream is;
	private NXTComm nxtComm;
	
	/** Checks whether the communication is in Dummy Mode. See setDummyMode for explanation.
	 *  
	 * @return true if it is in dummy mode, false otherwise.
	 */
	public boolean getDummyMode() {
		return dummyMode;
	}
	
	/** Enables or disables Dummy Mode. In dummy mode, no connection to the robot is attempted and all
	 * methods() do nothing quietly. This is useful for testing other components without needing to
	 * connect to the robot.
	 * 
	 * @param newDummyMode true to enable dummy mode, false to disable it.
	 */
	public void setDummyMode( boolean newDummyMode ) {
		dummyMode = newDummyMode;
		if( dummyMode ) {
			if( connected )
				disconnect();
		}
	}
	
	/** Attempts a connection to the robot. This is also automatically done by update() when not connected. Errors are
	 * silently suppressed - probably not a good idea in the long run.
	 */
	public void connect() throws IOException {
		if (this.dummyMode) {
			return;
		}
		
		NXTInfo[] info        = null;
		boolean   deviceFound = false;
		
		try {
			if( this.useUSB ) {
				System.out.println("Communication: Using USB.");
				nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
				info = nxtComm.search(null, NXTCommFactory.USB);
			} else {
				System.out.println("Communication: Using Bluetooth.");
				nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
				info = new NXTInfo [1];
				info[0] = new NXTInfo(NXTCommFactory.BLUETOOTH, "#1", "00:16:53:0B:B5:A3");
			}
		
			if( info.length > 0 ) {
				nxtComm.open( info[0] );
				deviceFound = true;
			}
		} catch (NXTCommException e) {
			throw new IOException( e );
		}
		
		
		if( deviceFound ) {
			os = new DataOutputStream( nxtComm.getOutputStream() );
			//is = nxtComm.getInputStream();
			System.out.println("Communication: Connection established.");
			this.connected = true;
		} else {
			throw new IOException("No device found.");
		}

	}
	
	/** Disconnects from the robot. Does nothing if not connected. */
	public void disconnect() {
		if( this.connected ) {
			try {
				//shutdownRobot();
				nxtComm.close(); nxtComm = null;
				//is.close(); is = null;
				os.close(); os = null;
			} catch (IOException e) {
				
			}
			this.connected = false;
			System.out.println("Communication: Disconnected.");
		}
	}
	
	/** Checks whether the robot is connected.
	 * 
	 * @return True if the robot is connected, false otherwise. Always returns true in Dummy Mode.
	 */
	public boolean isConnected() {
		if (this.dummyMode) {
			return true;
		}
		return this.connected;
	}
	
	/** This method needs to be called every time step, it sends the wheel speeds to the robot and
	 * reads the messages sent by it.
	 * 
	 * This method also automatically connects if disconnected().
	 * 
	 * Also, in Dummy Mode this method (like the rest) does, of course, nothing.
	 * 
	 * @param newWheelSpeeds The desired wheel speeds for the following time step.
	 */
	public void update( Vector2 newWheelSpeeds ) {
		if( dummyMode )
			return;
		
		if( isConnected() ) {
			try {
				os.writeByte( OP_SPEEDS );
				os.writeFloat( (float) newWheelSpeeds.getX() );
				os.writeFloat( (float) newWheelSpeeds.getY() );
				os.flush();
			} catch( IOException e ) {
				System.err.println("Communication: Could not send wheel info.");
				disconnect();
			}
			/*
			try{
				if(is.available()>=0){
					int touchSensors = (int) is.read();

					this.touchRight = false;
					this.touchLeft = false;
				
					if (touchSensors == 1) { this.touchRight = true; }
					if (touchSensors == 2) { this.touchLeft = true; }
					if (touchSensors == 3) { this.touchLeft = true; this.touchRight = true; }
				}
			}
			catch(Exception e){
				System.out.println(e);
			}*/
		} else {
			try {
				connect();
			} catch( IOException e ) {
				System.out.println( "Communication: Cannot connect: " + e.getMessage() );
			}
		}
	}
	
	/** This method sends an OP_KICK message to the robot making it kick at the soonest possible
	 * opportunity.
	 */
	public void kick() {
		if( dummyMode )
			return;
		
		if( isConnected() ) {
			try {
				System.out.println("Communication: Kick!");
				os.writeByte( OP_KICK );
				os.flush();
			} catch( IOException e ) {
				System.err.println("Communication: Could not send kick message.");
				disconnect();
			}
		}
	}
	
	/** This method tells the robot to shutdown, i.e. exit the NXT-side application. */
	public void shutdownRobot() {
		if( dummyMode )
			return;
		
		if( isConnected() ) {
			try {
				os.writeByte( OP_SHUTDOWN );
				os.flush();
			} catch( IOException e ) {
				System.err.println("Communication: Could not send shutdown message.");
				disconnect();
			}
		}
	}
	
	/** This method returns the encoder values (aka. tacho counts) read from the robot last time
	 * update() was called. For now, it's a stub that returns null all the time.
	 * 
	 * @return The values of the encoders, but actually null for now.
	 */
	public double[] getEncoderValues() {
		//TODO: Add encoder I/O.
		return null;
	}

	/** This method checks whether the bumper is pressed. This method is a stub and always returns false.
	 * @return True if the bumper is pressed, false otherwise. 
	 */
	public boolean isLeftBumperPressed() {
		return this.touchLeft;
	}
	
	public boolean isRightPressed(){
		return this.touchRight;
		
	}
}
