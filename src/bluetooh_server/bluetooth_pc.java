import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;
//import org.zeromq.ZMQ.Context;
//import org.zeromq.ZMQ.Socket;

public class bluetooth_pc
{
	public static String NXT_MAC_ADDRESS;// = "00:16:53:07:D5:5F";
	public static String NXT_NAME;// = "G5";
	
	// Error codes
	public static final int RET_OK = 0;
	public static final int RET_UNDEFINED_OP = -1;
	public static final int RET_ERROR_PARSING_OP = -2;
	public static final int RET_ERROR_PARSING_PARAMS = -3;
	public static final int RET_NOT_IMPLEMENTED = -4;

	// Opcodes
	public static final int OP_SET_MOTOR_SPEEDS = 1;
	public static final int OP_CHANGE_ROBOT_DIRECTION = 2;
	public static final int THIS_IS_SPARTAAA = 3; // kick
	public static final int OP_ROTATE_RXT_MOTOR = 4;
	public static final int OP_CHANGE_RXT_MOTOR_SPEED = 5;
	public static final int OP_CHANGE_RXT_MOTOR_ACCELERATION = 6;

	private static InputStream in;
	private static OutputStream out;

	public static void main(String args[]) throws IOException, InterruptedException 
	{
		// Parse command line argument
		if (args.length > 0 && args[0].equals("dummy"))
		{
			System.out.println("Dummy bot server");
			NXT_MAC_ADDRESS = "00:16:53:0B:B5:A3";
			NXT_NAME = "G5Dummy";	
		} 
		else
		{
			NXT_MAC_ADDRESS = "00:16:53:07:D5:5F";
			NXT_NAME = "G5";
		}

		Context context = ZMQ.context(1);

		//  Socket to talk to clients over IPC
		Socket socket = context.socket(ZMQ.REP);
		socket.bind("tcp://127.0.0.1:5555");


		int reply_error_code;

		while (true) 
		{
			// TODO: add timeout
			System.out.println("Initialising Bluetooth connection...");
			NXTInfo nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, NXT_NAME, NXT_MAC_ADDRESS);


			try {
				NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
				nxtComm.open(nxtInfo);

				in = nxtComm.getInputStream();
				out = nxtComm.getOutputStream();

				System.out.println("Connection established!");

			} catch (NXTCommException e) {
				System.out.println("Failed to connect: " + e.toString());
				Thread.sleep(250);
				continue;
			}

			//in.close();
			//out.close();

			while (true) 
			{
				// TODO: detect disconnect

				// Wait for request
				byte[] request = socket.recv(0);
				
				// Parse request and send it to the robot
				reply_error_code = handle_request(new String(request));

				// Read status of touch sensors
				int touch_sensors = in.read();

				// Parse response
				boolean s1 = (touch_sensors & (1 << 0)) != 0;
				boolean s2 = (touch_sensors & (1 << 1)) != 0;
				boolean s3 = (touch_sensors & (1 << 2)) != 0;
				boolean s4 = (touch_sensors & (1 << 3)) != 0;
				
				// Typecast to Int (JAVA-style!)
				int s1_int = s1 ? 1 : 0;
				int s2_int = s2 ? 1 : 0;
				int s3_int = s3 ? 1 : 0;
				int s4_int = s4 ? 1 : 0;
				
				//  Send status reply back to client
				String replyString = reply_error_code + " " + s1_int + " " + s2_int + " " + s3_int + " " + s4_int;
				System.out.println("Reply: [" + replyString + "]\n");
				
				byte[] reply = replyString.getBytes();
				socket.send(reply, 0);


				// Send reply to the IPC
				//int b = in.read();

				//System.out.println(b);
			}
		}
	}

	static int handle_request(String request) 
	{
		System.out.println("Request: [" + request + "]");

		int opcode;
		byte[] command;

		// Get opcode
		try 
		{
			opcode = Integer.parseInt(request.split(" ")[0]);
			System.out.println("\topcode: " + opcode);
		} 
		catch (NumberFormatException e) 
		{
			System.out.println("\tFailed to parse opcode: " + e.toString());
			return RET_ERROR_PARSING_OP; // Error parsing opcode
		}  

		if (opcode == OP_SET_MOTOR_SPEEDS) // Set motor speeds
		{
			// Params: [short short short short] - speeds of motors
			short m1, m2, m3, m4;
			try
			{
				m1 = (short)Integer.parseInt(request.split(" ")[1]);
				m2 = (short)Integer.parseInt(request.split(" ")[2]);
				m3 = (short)Integer.parseInt(request.split(" ")[3]);
				m4 = (short)Integer.parseInt(request.split(" ")[4]);
				System.out.println("\tParams: m1: " + m1 + ", m2: " + m2 + ", m3: " + m3 + ", m4: " + m4);

			}
			catch (NumberFormatException e) 
			{
				System.out.println("\tFailed to parse params: " + e.toString());
				return RET_ERROR_PARSING_PARAMS; // Error parsing params
			}

			// Parameters parsed, construct command to robot
			command = new byte[1 + 2 * 4]; // 1 for opcode, 2 * 4 for motor speeds
			
			// opcode
			command[0] = (byte)opcode;
			
			// m1
			command[1] = (byte)(m1 & 0xff);
			command[2] = (byte)((m1 >> 8) & 0xff);
			
			// m2
			command[3] = (byte)(m2 & 0xff);
			command[4] = (byte)((m2 >> 8) & 0xff);
			
			// m3
			command[5] = (byte)(m3 & 0xff);
			command[6] = (byte)((m3 >> 8) & 0xff);

			// m4
			command[7] = (byte)(m4 & 0xff);
			command[8] = (byte)((m4 >> 8) & 0xff);
		}
		else if (opcode == OP_CHANGE_ROBOT_DIRECTION) // Change robot direction, calculate motor speeds 
		{
			// Params: [short short short] = [speed movement_angle rotation_angle] 
			System.out.println("\tNot implemented opcode: " + opcode); 
			return RET_NOT_IMPLEMENTED; // Not implemented opcode			
		}
		else if (opcode == THIS_IS_SPARTAAA)
		{
			command = new byte[1];
			command[0] = (byte)opcode;
		}
		else if (opcode == OP_ROTATE_RXT_MOTOR || opcode == OP_CHANGE_RXT_MOTOR_SPEED || opcode == OP_CHANGE_RXT_MOTOR_ACCELERATION)
		{
			// Params: [short short]
			short mA, mB;
			try
			{
				mA = (short)Integer.parseInt(request.split(" ")[1]);
				mB = (short)Integer.parseInt(request.split(" ")[2]);
				System.out.println("\tParams: mA: " + mA + ", mB: " + mB);

			}
			catch (NumberFormatException e) 
			{
				System.out.println("\tFailed to parse params: " + e.toString());
				return RET_ERROR_PARSING_PARAMS; // Error parsing params
			}
			
			command  = new byte[1 + 2 * 2];
			
			command[0] = (byte)opcode;
			
			// A
			command[1] = (byte)(mA & 0xff);
			command[2] = (byte)((mA >> 8) & 0xff);
			
			// B
			command[3] = (byte)(mB & 0xff);
			command[4] = (byte)((mB >> 8) & 0xff);
		}
		else
		{
			System.out.println("\tUndefined opcode: " + opcode); 
			return RET_UNDEFINED_OP; // Undefined opcode
		}

		// Send to command to robot
		send_command(command);
		//nxtComm.send(command, 9, true);

		return RET_OK; // No error parsing request
	}

	static void send_command(byte[] command)
	{
		try 
		{   
			out.write(command);
		}  
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		try 
		{
			out.flush();
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}               

	}

}
