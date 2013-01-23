package Planning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class SimulatorCommunication implements CommunicationInterface{
	
	OutputStream os;
    InputStream is;
    
    PrintWriter writer;
    InputStreamReader isReader;
    BufferedReader reader;
    ServerSocket serverSocket;
    
    public SimulatorCommunication (){
    }

	public void sendToRobot(int command) {
	    try {
	    	writer.println(command);
	    	writer.flush();
	    } catch(Exception e) {
	        System.out.println("Error sending command to the robot");
	        System.out.println(e.toString());
	    }
	}
	// Not sure how I can supply all the needed information by returning an int
	// My idea was to send a String that gets parsed and all needed info extracted
	public int recieveFromRobot() {
		String message;
		String[] splitMessage = new String[5];
		return 0;
	}

	public void openConnection() throws IOException{
		
		serverSocket = new ServerSocket(5346);
		
		Socket simSocket = serverSocket.accept();
		
		is = simSocket.getInputStream();
		os = simSocket.getOutputStream();
		
		writer = new PrintWriter(os);
		isReader = new InputStreamReader(is);
		reader = new BufferedReader(isReader);
		
		System.out.println("Connection succesfull.");
	}

	public void closeConnection() {
		try {
			is.close();
			os.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
