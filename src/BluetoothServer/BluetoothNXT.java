package BluetoothServer;

import java.io.InputStream;
import java.io.OutputStream;

//import java.io.DataInputStream;
//import java.io.DataOutputStream;
// import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
// import lejos.nxt.Button;
import lejos.nxt.Motor;
// import lejos.nxt.Motor.*;
import lejos.nxt.SensorPort;

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
	public static final byte OP_SET_MOTOR_SPEEDS = 1;
	public static final byte OP_CHANGE_ROBOT_DIRECTION = 2;
	public static final byte OP_KICK = 3;
	public static final byte OP_ROTATE_RXT_MOTOR = 4;
	public static final byte OP_CHANGE_RXT_MOTOR_SPEED = 5;
	public static final byte OP_CHANGE_RXT_MOTOR_ACCELERATION = 6;
	// M1 - kick
	public static final int KICK_ANGLE = 130; // in "ticks"
	public static final int KICK_ANGLE_IDLE = -5;
	public static final int KICK_DIRECTION = 1; // change to -1, to change direction
	// M1 - drive
	public static final int DRIVE_DISTANCE = 2500; // in "ticks"
	public static final int LEFT_DIRECTION = 1; // change to -1, to change direction
	public static final int RIGHT_DIRECTION = -1; // change to 1, to change direction
	public static final int ACCELERATION = 2500; // Default 6000
	// Privates
	private static Motormux robot;
	private static InputStream dis;
	private static OutputStream dos;
	private static TouchSensor sensor1;
	private static TouchSensor sensor2;
	private static TouchSensor sensor3;
	
    private static volatile boolean isKicking = false;
    
    


	public static void main(String[] args) {
		robot = new Motormux(SensorPort.S4);

		sensor1 = new TouchSensor(SensorPort.S1);
		sensor2 = new TouchSensor(SensorPort.S2);
		sensor3 = new TouchSensor(SensorPort.S3);

		// Sound.setVolume(50);

		// Max kicker speed
		//Motor.C.setSpeed((int) Motor.C.getMaxSpeed());
		//Motor.C.setAcceleration(10000);
	
		//Motor.A.setSpeed(6000);
		//Motor.B.setSpeed(6000);
		//Motor.A.setAcceleration(6000);
		//Motor.B.setAcceleration(6000);

		while (true) {
			try {
				LCD.clear();
				LCD.drawString("Waiting for", 0, 0);
				LCD.drawString("Bluetooth...", 0, 1);

				NXTConnection connection = Bluetooth.waitForConnection();
				dis = connection.openInputStream();
				dos = connection.openOutputStream();

				//Motor.C.rotate(10, true);
				//Motor.C.resetTachoCount(); 
				//Motor.C.stop();

				Sound.beep();
				LCD.clear();
				LCD.drawString("Connected!", 0, 0);
				byte[] opcode = new byte[1];

				while (true) {
					// Read opcode
					dis.read(opcode);

					LCD.drawString("oc: " + opcode[0], 0, 5);

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

					//TODO: Dirty workaround for detecting random disconnect
					dos.write(opcode);
					dos.flush();
				}

			} catch (Exception e) {
				// Does not wait for user to notice
				LCD.drawString("EXCEPTION1!", 0, 3);
				e.printStackTrace();
			}
		}
	}

	static void handle_request(byte opcode) {
		try {
			if (opcode == OP_SET_MOTOR_SPEEDS) {
				byte[] motor_speeds = new byte[4 * 2];
				dis.read(motor_speeds);

				short m1 = (short) ((short)motor_speeds[1] << 8 | (255 & (short)motor_speeds[0]));
				short m2 = (short) ((short)motor_speeds[3] << 8 | (255 & (short)motor_speeds[2]));
				short m3 = (short) ((short)motor_speeds[5] << 8 | (255 & (short)motor_speeds[4]));
				short m4 = (short) ((short)motor_speeds[7] << 8 | (255 & (short)motor_speeds[6]));

				robot.set_speed(0, m1);
				robot.set_speed(1, m2);
				robot.set_speed(2, m3);
				robot.set_speed(3, m4);

				if (m1 == 0 && m2 == 0 && m3 == 0 && m4 == 0) {
					robot.flt();
				}

				LCD.drawString("M1: " + m1 + "   ", 0, 3);
				LCD.drawString("M2: " + m2 + "   ", 0, 4);
				LCD.drawString("M3: " + m3 + "   ", 0, 5);
				LCD.drawString("M4: " + m4 + "   ", 0, 6);
			}
			else if (opcode == OP_KICK) {
				// TODO: add to thread
				kick();
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
		/*
        Motor.A.setSpeed(900);
        Motor.A.rotate(-40);
        Motor.A.rotate(+40);
        Motor.A.stop();
        isKicking = false;
		*/
		Motor.A.setSpeed((int) Motor.A.getMaxSpeed());
		Motor.A.resetTachoCount();
		Motor.A.backward();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(80);
				} catch (InterruptedException e) {
					//
				}
				Motor.A.rotateTo(0);
				Motor.A.stop();
				isKicking = false;
			}
		}).start();
	
	}
}
