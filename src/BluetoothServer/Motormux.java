package BluetoothServer;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
// import lejos.nxt.SensorPort;

public class Motormux extends I2CSensor{

	public static final byte MOTORMUX_ADD = (byte)0xB4;
	public static final byte MOTORS_DIR[] = {1, 3, 5, 7};
	public static final byte MOTORS_SPEED[] = {2, 4, 6, 8};

	public static final byte FLOAT = (byte)0x00;
	public static final byte FORWARD = (byte)0x01;
	public static final byte REVERSE = (byte)0x02;
	public static final byte BRAKE = (byte)0x03;

	@SuppressWarnings("deprecation")
	public Motormux(I2CPort port) {
		super(port);
		setAddress(MOTORMUX_ADD);
	}


	public void set_speed(int id, int speed) {
		if (speed < 0) {
			sendData(MOTORS_DIR[id], REVERSE);
			speed = -speed;
		} else {
			sendData(MOTORS_DIR[id], FORWARD);
		}

		sendData(MOTORS_SPEED[id], (byte)speed);
	}

	// DO NOT USE
	//public void stop(){
	//}

	public void flt(){
		sendData(MOTORS_DIR[0], FLOAT);
		sendData(MOTORS_DIR[1], FLOAT);
		sendData(MOTORS_DIR[2], FLOAT);
		sendData(MOTORS_DIR[3], FLOAT);
	}
}