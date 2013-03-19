package BluetoothServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;

import java.util.*;

public class BluetoothPC extends Thread {
	public static String NXT_MAC_ADDRESS;
	public static String NXT_NAME;

	// Error codes
	public static final int RET_OK = 0;
	public static final int RET_UNDEFINED_OP = -1;
	public static final int RET_ERROR_PARSING_OP = -2;
	public static final int RET_ERROR_PARSING_PARAMS = -3;
	public static final int RET_NOT_IMPLEMENTED = -4;

	// Opcodes
	public static final int OP_NOP = 7; // No operation, just update touch sensors
	public static final int OP_SET_MOTOR_SPEEDS = 1; // [short short short short]
	public static final int OP_CHANGE_ROBOT_DIRECTION = 2;
	public static final int OP_KICK = 3;
	public static final int OP_ROTATE_RXT_MOTOR = 4;
	public static final int OP_CHANGE_RXT_MOTOR_SPEED = 5;
	public static final int OP_CHANGE_RXT_MOTOR_ACCELERATION = 6;

	// Privates
	private static InputStream in;
	private static OutputStream out;

	private static volatile LinkedList <String>req_queue = new LinkedList<String>(); // Thread safe
	private static volatile int robot_connection_status = 0; // 0 - unconnected, 1 - connected
	private static volatile int[] touch_sensors = {0, 0, 0, 0};

	public synchronized void run() {
		//  Socket to talk to clients over IPC
		Context context = ZMQ.context(1);
		Socket socket = context.socket(ZMQ.REP);

		// Bind
		System.out.println("Binding to port 5555...");
		socket.bind("tcp://127.0.0.1:5555");

		String last_req = null;
		String new_req = null;
		String reply_string = null;


		while (true) {
			last_req = null;
			new_req = null;

			// Wait for request
			byte[] request = socket.recv(0);
			new_req = new String(request);
			System.out.println("IPC: Request: [" + new_req + "]");

			// TODO: trim string?
			// TODO: check for syntax?

			synchronized(req_queue) {

				if (req_queue.size() <= 5) {
					// Get last element of the queue
					if (!req_queue.isEmpty())
						last_req = req_queue.getLast();

					// Check if queue is empty
					if (last_req != null) {
						// Compare and remove if opcode matches
						if (split(new_req, " ")[0].equals(split(last_req, " ")[0])) {
							System.out.println("\tUpdate!");

							// The last pushed op is the same as new one, only update parameters
							// Remove last element from the queue
							req_queue.removeLast();
						}
					}

					// Push (or "update") with new request
					req_queue.push(new_req);

				} else { // Drop, unlees exception
					// Never drop: "1 0 0 0 0" or "3" if preceding command is "1 0 0 0 0"
					if (req_queue.size() <= 8 && (new_req.equals("1 0 0 0 0") ||
							(new_req.equals("3") && req_queue.getLast().equals("1 0 0 0 0")) )) {

						System.out.println("\tAdded due to exception");
						req_queue.push(new_req);

					} else {
						System.out.println("\tDrop!");
					}
				}
			}

			// Reply with status and touch sensor data
			reply_string = robot_connection_status + " " + touch_sensors[0] + " " + touch_sensors[1] + " " + touch_sensors[2] + " " + touch_sensors[3];
			System.out.println("IPC: Reply  : [" + reply_string + "]\n");

			// Send reply
			socket.send(reply_string.getBytes(), 0);
		}

	}

	public static void main(String args[]) throws IOException, InterruptedException {
		// Start enother thread for IPC commns
		BluetoothPC obj = new BluetoothPC();
		Thread t1 = new Thread(obj);
		t1.start();

		// Parse command line argument
		if (args.length > 0 && args[0].equals("dummy")) {
			System.out.println("Dummy bot server");
			NXT_MAC_ADDRESS = "00:16:53:0B:B5:A3";
			NXT_NAME = "G5Dummy";	
		} else {
			NXT_MAC_ADDRESS = "00:16:53:07:D5:5F";
			NXT_NAME = "G5";
		}

		while (true) {
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

			// Clear queue
			req_queue.clear();

			//in.close();
			//out.close();
			int ack = 0;
			String request = null;

			while (true) {
				// Parse request from request queue
				//System.out.println(req_queue.isEmpty());

				if (!req_queue.isEmpty()) {
					synchronized(req_queue) {
						// Send request to robot
						request = req_queue.pop();
					}

					handle_request(request);

					// Read ACK (status of touch sensors)
					ack = in.read();

					// Parse ACK response
					boolean s1 = (ack & (1 << 0)) != 0;
					boolean s2 = (ack & (1 << 1)) != 0;
					boolean s3 = (ack & (1 << 2)) != 0;
					boolean s4 = (ack & (1 << 3)) != 0;

					// Typecast to Int (JAVA-style!)
					touch_sensors[0] = s1 ? 1 : 0;
					touch_sensors[1] = s2 ? 1 : 0;
					touch_sensors[2] = s3 ? 1 : 0;
					touch_sensors[3] = s4 ? 1 : 0;

					/*try {
						Thread.sleep(50);
					} catch (Exception e2) {
						e2.printStackTrace();
					}*/
				} else {
					try {
						Thread.sleep(1);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}
	}

	static int handle_request(String request) {
		System.out.println("BT:  Request: [" + request + "]");
		String[] request_tokens = split(request, " ");

		int opcode;
		try {
			opcode = Integer.parseInt(request_tokens[0]);
		} catch (NumberFormatException e) {
			System.out.println("\tFailed to parse opcode: " + e.toString());
			return RET_ERROR_PARSING_OP; // Error parsing opcode
		}
		System.out.println("\topcode: " + opcode);

		short[] request_codes = new short[request_tokens.length];
		for (int i = 1; i < request_tokens.length; i++) {
			try {
				request_codes[i] = (short)Integer.parseInt(request_tokens[i]);
			} catch (NumberFormatException e) {
				System.out.println("\tFailed to parse params: " + e.toString());
				return RET_ERROR_PARSING_PARAMS; // Error parsing params
			}
		}

		byte[] command;

		if (opcode == OP_SET_MOTOR_SPEEDS) {
			// Params: [short short short short] - speeds of motors
			short m1 = request_codes[1];
			short m2 = request_codes[2];
			short m3 = request_codes[3];
			short m4 = request_codes[4];
			System.out.println("\tParams: m1: " + m1 + ", m2: " + m2 + ", m3: " + m3 + ", m4: " + m4);

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

		} else if (opcode == OP_KICK ||
				opcode == OP_NOP) {
			command = new byte[1];
			command[0] = (byte)opcode;

		} else if (opcode == OP_ROTATE_RXT_MOTOR ||
				opcode == OP_CHANGE_RXT_MOTOR_SPEED ||
				opcode == OP_CHANGE_RXT_MOTOR_ACCELERATION) {
			// Params: [short short]
			short mA = request_codes[1];
			short mB = request_codes[2];
			System.out.println("\tParams: mA: " + mA + ", mB: " + mB);

			command  = new byte[1 + 2 * 2];

			command[0] = (byte)opcode;

			// A
			command[1] = (byte)(mA & 0xff);
			command[2] = (byte)((mA >> 8) & 0xff);

			// B
			command[3] = (byte)(mB & 0xff);
			command[4] = (byte)((mB >> 8) & 0xff);

		} else {
			System.out.println("\tUndefined opcode: " + opcode); 
			return RET_UNDEFINED_OP; // Undefined opcode
		}

		// Send to command to robot
		send_command(command);
		//nxtComm.send(command, 9, true);

		return RET_OK; // No error parsing request
	}

	static void send_command(byte[] command) {
		try  {   
			out.write(command);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static String[] split(String str, String delim) {
		StringTokenizer tokenizer = new StringTokenizer(str, delim);
		ArrayList<String> tokens = new ArrayList<String>(str.length());
		while (tokenizer.hasMoreTokens()) {
			tokens.add(tokenizer.nextToken());
		}
		return tokens.toArray(new String[0]);
	}
}
