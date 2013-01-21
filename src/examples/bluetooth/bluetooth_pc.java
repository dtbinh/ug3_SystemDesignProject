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

public class bluetooth_pc
{
	public static final String NXT_MAC_ADDRESS = "00:16:53:07:D5:5F";
	public static final String NXT_NAME = "G5";

	private static InputStream in;
	private static OutputStream out;

	public static void main(String args[]) throws IOException, InterruptedException 
	{

		System.out.println("Initialising Bluetooth connection...");
		NXTInfo nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, NXT_NAME, NXT_MAC_ADDRESS);

		try {
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			nxtComm.open(nxtInfo);

			in = nxtComm.getInputStream();
			out = nxtComm.getOutputStream();

			System.out.println("Connection established!");

		} catch (NXTCommException e) {
			throw new IOException("Failed to connect: " + e.toString());
		}

		//in.close();
		//out.close();

		while (true) {
			sendCommand(0xFF);
			int b = in.read();

			System.out.println(b);
		}
	}

	static void sendCommand(int opcode){
		try {   
			out.write(opcode);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			out.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}               

	}

}