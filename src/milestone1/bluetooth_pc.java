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
	public static final String NXT_MAC_ADDRESS = "00:16:53:07:D5:5F";
	public static final String NXT_NAME = "G5";

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

	private static InputStream in;
	private static OutputStream out;

	public static void main(String args[]) throws IOException, InterruptedException 
	{
		Context context = ZMQ.context(1);

		//  Socket to talk to clients over IPC
		Socket socket = context.socket(ZMQ.REP);
		socket.bind("ipc:///tmp/nxt_bluetooth_robot");


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
				reply_error_code = handle_request(new String(request));


				//  Send status reply back to client
				String replyString = reply_error_code + " 0 0 0 0"; // (no)error code, no sensor (out of 4) is active
				byte[] reply = replyString.getBytes();
				socket.send(reply, 0);

				// Send command to the robot
				//send_command(0xFF);

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
		else if (opcode == OP_ROTATE_RXT_MOTOR)
		{
			// Params: [short short] - rotation in ticks
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