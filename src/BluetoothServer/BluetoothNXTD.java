package BluetoothServer;

import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;



public class BluetoothNXTD {

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

	public static void main(String[] args) {
		Motormux robot = new Motormux(SensorPort.S4);
		Sound.setVolume(50);

		// Max kicker speed
		Motor.A.setAcceleration(6000);
		Motor.B.setAcceleration(6000);
		while (true) 
		{
			try 
			{
				LCD.clear();
				LCD.drawString("Waiting for", 0, 0);
				LCD.drawString("Bluetooth...", 0, 1);

				NXTConnection connection = Bluetooth.waitForConnection();
				dis = connection.openInputStream();
				dos = connection.openOutputStream();

				Sound.beep();
				LCD.clear();
				LCD.drawString("Connected!", 0, 0);
				byte[] opcode = new byte[1];

				while (true) {
					// Read opcode
					dis.read(opcode);

					LCD.drawString("oc: " + opcode[0], 0, 5);

					// End of connection
					if (opcode[0] == 0)
					{
						Sound.twoBeeps();

						dis.close();
						dos.close();

						break;
					}

					handle_request(opcode[0]);


					//TODO: Dirty workaround for detecting random disconnect
					dos.write(opcode);
					dos.flush();
				}

			} catch (Exception e) {
				// Does not wait for user to notice
				LCD.drawString("EXCEPTION1!", 0, 3);
			}
		}
	}

	static void handle_request(byte opcode) 
	{
		try {
			if (opcode == OP_SET_MOTOR_SPEEDS)
			{
				byte[] motor_speeds = new byte[4 * 2];
				dis.read(motor_speeds);

				short m1 = (short) ((short)motor_speeds[1] << 8 | (255 & (short)motor_speeds[0]));
				short m2 = (short) ((short)motor_speeds[3] << 8 | (255 & (short)motor_speeds[2]));
				short m3 = (short) ((short)motor_speeds[5] << 8 | (255 & (short)motor_speeds[4]));
				short m4 = (short) ((short)motor_speeds[7] << 8 | (255 & (short)motor_speeds[6]));

				if (m1 > 0) 
				{
					Motor.A.setSpeed(m1);
					Motor.A.forward();
				}
				else
				{
					Motor.A.setSpeed(-m1);
					Motor.A.backward();
				}
				
				if (m2 > 0) 
				{
					Motor.B.setSpeed(m2);
					Motor.B.forward();
				}
				else
				{
					Motor.B.setSpeed(-m2);
					Motor.B.backward();
				}
				

				
				LCD.drawString("M1: " + m1 + "   ", 0, 3);
				LCD.drawString("M2: " + m2 + "   ", 0, 4);
				LCD.drawString("M3: " + m3 + "   ", 0, 5);
				LCD.drawString("M4: " + m4 + "   ", 0, 6);
			}
			else if (opcode == OP_KICK)
			{
				// TODO: add to thread
				kick();
			}
			else if (opcode == OP_ROTATE_RXT_MOTOR || opcode == OP_CHANGE_RXT_MOTOR_SPEED || opcode == OP_CHANGE_RXT_MOTOR_ACCELERATION)
			{
				byte[] motor_params = new byte[2 * 2];
				dis.read(motor_params);

				short mA = (short) ((short)motor_params[1] << 8 | (255 & (short)motor_params[0]));
				short mB = (short) ((short)motor_params[3] << 8 | (255 & (short)motor_params[2]));

				LCD.drawString("mA: " + mA + "   ", 0, 3);
				LCD.drawString("mB: " + mB + "   ", 0, 4);

				if (opcode == OP_ROTATE_RXT_MOTOR)
				{
					Motor.A.rotate(mA, true);
					Motor.B.rotate(mB, true);
				}
				else if (opcode == OP_CHANGE_RXT_MOTOR_SPEED)
				{
					Motor.A.setSpeed(mA);
				
					Motor.B.setSpeed(mB);
				
				}
				else if (opcode == OP_CHANGE_RXT_MOTOR_ACCELERATION)
				{
					Motor.A.setAcceleration(mA);
					Motor.B.setAcceleration(mB);
				}

			}
			else
			{
				LCD.drawString("UNDEF OPCODE: " + opcode, 0, 3);
				dos.flush();
				dis.skip(99999);
			}
		}
		catch (Exception e) {
			LCD.drawString("EXCEPTION2!", 0, 3);
			System.err.println(e.getMessage());
		}

	}

	static void kick()
	{
		// Kick
		Motor.C.rotate(KICK_ANGLE * -KICK_DIRECTION, true);

		try{Thread.sleep(600);}catch(Exception e) {}

		// Reset kicker to original position
		Motor.C.rotate(KICK_ANGLE * KICK_DIRECTION, true);
	}



}

