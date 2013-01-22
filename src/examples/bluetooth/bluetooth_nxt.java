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



public class bluetooth_nxt {

	public static int packet_size = 4;

	public static void main(String[] args) {
		Motormux robot = new Motormux(SensorPort.S4);

		while (true) 
		{
			try 
			{
				LCD.clear();
				LCD.drawString("Waiting for", 0, 0);
				LCD.drawString("Bluetooth...", 0, 1);

				NXTConnection connection = Bluetooth.waitForConnection();
				InputStream dis = connection.openInputStream();
				OutputStream dos = connection.openOutputStream();

				// TODO: volume max
				Sound.beep();
				LCD.clear();
				LCD.drawString("Connected!", 0, 0);

				//byte[] packet = new byte[packet_size];
				while (true) {
					byte[] opcode = new byte[1];

					// Read opcode
					dis.read(opcode);

					if (opcode[0] == (byte)0x01)
					{
						byte[] motor_speeds = new byte[4 * 2];
						dis.read(motor_speeds);

						//LCD.drawString("Yay!", 0, 3);

						short m1 = (short) ((short)motor_speeds[1] << 8 | (255 & (short)motor_speeds[0]));
						short m2 = (short) ((short)motor_speeds[3] << 8 | (255 & (short)motor_speeds[2]));
						short m3 = (short) ((short)motor_speeds[5] << 8 | (255 & (short)motor_speeds[4]));
						short m4 = (short) ((short)motor_speeds[7] << 8 | (255 & (short)motor_speeds[6]));

						robot.set_speed(0, m1);
						robot.set_speed(1, m2);
						robot.set_speed(2, m3);
						robot.set_speed(3, m4);

						if (m1 == 0 && m2 == 0 && m3 == 0 && m4 == 0)
						{
							robot.flt();
						}


						LCD.drawString("M1: " + m1 + "   ", 0, 3);
						LCD.drawString("M2: " + m2 + "   ", 0, 4);
						LCD.drawString("M3: " + m3 + "   ", 0, 5);
						LCD.drawString("M4: " + m4 + "   ", 0, 6);
					}


					// Required to detect disconnect?
					dos.write(opcode);
					dos.flush();
				}

			} catch (Exception e) {
				LCD.drawString("EXCEPTION!", 0, 3);
				System.err.println(e.getMessage());
			}
		}
	}
}

