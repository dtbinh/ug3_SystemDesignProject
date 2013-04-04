import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.*;

import lejos.robotics.navigation.DifferentialPilot;

import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class BluetoothNXT {	
	// Error codes
	public static final byte RET_OK = 0;
	public static final byte RET_UNDEFINED_OP = -1;
	public static final byte RET_ERROR_PARSING_OP = -2;
	public static final byte RET_ERROR_PARSING_PARAMS = -3;
	public static final byte RET_NOT_IMPLEMENTED = -4;

	// Opcodes
	// http://lejos.sourceforge.net/nxt/nxj/api/lejos/robotics/navigation/DifferentialPilot.html
	public static final byte OP_SET_MUX_SPEEDS = 1;
	public static final byte OP_TRAVEL = 2;
	public static final byte OP_KICK = 3;
	public static final byte OP_ARC = 4;
	public static final byte OP_ROTATE = 5;
	public static final byte OP_STEER = 6;
	public static final byte OP_TRAVEL_ARC = 7;
	public static final byte OP_STOP = 8; // ?quickStop
	public static final byte OP_NOP = 9;
	
	// Kicker
	public static final int KICK_ANGLE = 130; // in "ticks"
	public static final int KICK_ANGLE_IDLE = -5;
	public static final int KICK_DIRECTION = 1; // change to -1, to change direction

	// Privates
	private static Motormux mux;
	private static DifferentialPilot pilot;
	private static InputStream dis;
	private static OutputStream dos;
	private static TouchSensor sensor1;
	private static TouchSensor sensor2;
	private static TouchSensor sensor3;

	private static NXTRegulatedMotor LEFT_MOTOR = Motor.A;
	private static NXTRegulatedMotor RIGHT_MOTOR = Motor.B;
	private static NXTRegulatedMotor KICKER = Motor.C;

	// Physical robot settings
	private static final boolean INVERSE_WHEELS = false;
	private static final double WHEEL_DIAMETER = 0.0816; // metres
	private static final double TRACK_WIDTH = 0.155; // metres

	private static volatile boolean isKicking = false;


	public static void main(String[] args) {
		mux = new Motormux(SensorPort.S4);

		sensor1 = new TouchSensor(SensorPort.S1);
		sensor2 = new TouchSensor(SensorPort.S2);
		sensor3 = new TouchSensor(SensorPort.S3);

		pilot = new DifferentialPilot(WHEEL_DIAMETER, TRACK_WIDTH, LEFT_MOTOR, RIGHT_MOTOR, INVERSE_WHEELS);
		pilot.reset();		
		pilot.setTravelSpeed(pilot.getMaxTravelSpeed()); // meters per second
		pilot.setRotateSpeed(pilot.getMaxRotateSpeed()); // degrees per second

		Sound.setVolume(100);


		while (true) {
			try {
				LCD.clear();
				LCD.drawString("Waiting for", 0, 0);
				LCD.drawString("Bluetooth...", 0, 1);

				NXTConnection connection = Bluetooth.waitForConnection();
				dis = connection.openInputStream();
				dos = connection.openOutputStream();

				//KICKER.rotate(10, true);
				KICKER.resetTachoCount(); 
				KICKER.stop();

				Sound.beep();
				LCD.clear();
				LCD.drawString("Connected!", 0, 0);
				byte[] opcode = new byte[1];

				while (true) {
					// Read opcode
					dis.read(opcode);

					LCD.drawString("oc: " + opcode[0], 0, 6);

					// End of connection
					if (opcode[0] == 0) {
						Sound.twoBeeps();

						dis.close();
						dos.close();
						break;
					}

					handle_request(opcode[0]);

					// Return status of touch sensors
					opcode[0] = 0;
					if (sensor1.isPressed()) {
						opcode[0] |= (1 << 0);
					}

					if (sensor2.isPressed()) {
						opcode[0] |= (1 << 1);
					}

					if (sensor3.isPressed()) {
						opcode[0] |= (1 << 2);
					}

					// TODO: return isStalled/isMoving/etc

					dos.write(opcode);
					dos.flush();
				}

			} catch (Exception e) {
				// Does not wait for user to notice
				LCD.drawString("EXCEPTION1!", 0, 6);
				e.printStackTrace();
			}
		}
	}

	static void handle_request(byte opcode) {
		try {
			if (opcode == OP_SET_MUX_SPEEDS) {
				byte[] motor_speeds = new byte[4 * 2];
				dis.read(motor_speeds);

				short m1 = bytesToShort(motor_speeds[1], motor_speeds[0]);
				short m2 = bytesToShort(motor_speeds[3], motor_speeds[2]);
				short m3 = bytesToShort(motor_speeds[5], motor_speeds[4]);
				short m4 = bytesToShort(motor_speeds[7], motor_speeds[6]);

				mux.set_speed(0, m1);
				mux.set_speed(1, m2);
				mux.set_speed(2, m3);
				mux.set_speed(3, m4);

				if (m1 == 0 && m2 == 0 && m3 == 0 && m4 == 0) {
					mux.flt();
				}

				//LCD.drawString("M1: " + m1 + "   ", 0, 3);
				//LCD.drawString("M2: " + m2 + "   ", 0, 4);
				//LCD.drawString("M3: " + m3 + "   ", 0, 5);
				//LCD.drawString("M4: " + m4 + "   ", 0, 6);
			}
			else if (opcode == OP_KICK) {
				kick();
			}
			else if (opcode == OP_TRAVEL || opcode == OP_ROTATE) {
				byte[] args = new byte[4 * 1];
				dis.read(args);
				
				float arg1 = bytesToFloat(args[3], args[2], args[1], args[0]);
				
				//System.out.println(arg1);
				
				if (opcode == OP_TRAVEL) {
					pilot.travel(arg1);
				} else {
					pilot.rotate(arg1);
				}
				
			}
			else if (opcode == OP_ARC || opcode == OP_STEER || opcode == OP_TRAVEL_ARC) {
				byte[] args = new byte[4 * 2];
				dis.read(args);
				
				float arg1 = bytesToFloat(args[3], args[2], args[1], args[0]);
				float arg2 = bytesToFloat(args[7], args[6], args[5], args[4]);
				
				if (opcode == OP_ARC) {
					pilot.arc(arg1, arg2);
				} else if (opcode == OP_STEER) {
					pilot.steer(arg1, arg2);
				} else {
					pilot.travelArc(arg1, arg2);
				}
			}
			else {
				LCD.drawString("UNDEF OPCODE: " + opcode, 0, 3);
				dos.flush();
				dis.skip(999);
			}
		}
		catch (Exception e) {
			LCD.drawString("EXCEPTION2!", 0, 3);
			e.printStackTrace();
		}
	}

	static void kick() {

		if (isKicking) {
			return;
		}

		isKicking = true;

		KICKER.setSpeed(KICKER.getMaxSpeed());
		KICKER.resetTachoCount();
		
		if (KICK_DIRECTION > 0) {
			KICKER.forward();
		} else {
			KICKER.backward();
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(80);
				} catch (InterruptedException e) {
				}
				KICKER.rotateTo(0);
				KICKER.stop();
				isKicking = false;
			}
		}).start();

	}
	
	// Why java does not have list splicing like python?
	static short bytesToShort(byte b0, byte b1) {
		return (short) ((short)b0 << 8 | (0xFF & (short)b1));
	}
	
	static float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
		int asInt = (b0 & 0xFF) 
	            | ((b1 & 0xFF) << 8) 
	            | ((b2 & 0xFF) << 16) 
	            | ((b3 & 0xFF) << 24);
		
		return Float.intBitsToFloat(asInt);
	}
}
