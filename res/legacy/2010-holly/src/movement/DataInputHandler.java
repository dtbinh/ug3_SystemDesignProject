package movement;
import baseSystem.Singleton;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataInputHandler extends Thread {

	private DataInputStream input;
	private Singleton s;
	private int inputCode;
	private boolean[] sensorReadings;

	public void run(){
		
		sensorReadings = new boolean[4];
		
		while(true){
			try {
			switch(input.readInt()){
			
			//Sensor readings
			case(2):
						sensorReadings[0] = input.readBoolean();
						sensorReadings[1] = input.readBoolean();
						sensorReadings[2] = input.readBoolean();
						sensorReadings[3] = input.readBoolean();

						for(int i=0; i<3; i++){
							if(sensorReadings[i]){
								//System.out.println("Sensor " + i + "collided");
							}
						}

						break;
						
			case -5:
				System.out.println("Waiting");
				s.setWaiting();
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			s.setCollided(sensorReadings);
		}
	}

	public DataInputHandler(DataInputStream dis, Singleton s){
		input = dis;
		this.s=s;
	}

}
