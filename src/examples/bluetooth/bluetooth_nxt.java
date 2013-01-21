import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;


public class bluetooth_nxt {

	public static int packet_size = 4;

	public static void main(String[] args) {		
		try {
			LCD.clear();
			LCD.drawString("Waiting for", 0, 0);
			LCD.drawString("Bluetooth...", 0, 1);

			NXTConnection connection = Bluetooth.waitForConnection();
			InputStream dis = connection.openInputStream();
			OutputStream dos = connection.openOutputStream();
			
			Sound.beep();
			LCD.clear();
			LCD.drawString("Connected!", 0, 0);

			byte[] packet = new byte[packet_size];
			while (true) {
				dis.read(packet);
				dos.write(packet);
				dos.flush();
			}

			/*is.close();
			os.close();
			connection.close();
			LCD.drawString("Pong finished.", 0, 2);*/
		} catch (Exception e) {
			LCD.drawString("EXCEPTION!", 0, 3);
			System.err.println(e.getMessage());
		}
	}
}

