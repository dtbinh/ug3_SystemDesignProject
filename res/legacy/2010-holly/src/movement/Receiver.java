package movement;

import java.io.IOException;
import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class Receiver{
	
	public static boolean start()
	{
		boolean open=false;
		NXTCommand nxtCommand = new NXTCommand();
		NXTComm nxtComm = null;
		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			NXTInfo nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH,"Holly", "00:16:53:09:92:F5");
			open = nxtComm.open(nxtInfo,NXTComm.LCP);
			nxtCommand.setNXTComm(nxtComm);
		} catch (NXTCommException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(open){
			try {
				nxtCommand.startProgram("BrickController.nxj");
				nxtCommand.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nxtComm.getInputStream();
		
		return open;
	}
}
