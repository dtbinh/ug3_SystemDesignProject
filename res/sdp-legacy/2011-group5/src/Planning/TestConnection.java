package Planning;

import java.io.IOException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

import java.io.*;

public class TestConnection {
	public static void main(String[] args) throws NXTCommException, IOException, Exception {
		NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
	    NXTInfo info = new NXTInfo(NXTCommFactory.BLUETOOTH,"GROUP5", "00:16:53:07:76:B0");
	    nxtComm.open(info);
	    DataOutputStream dos = new DataOutputStream(nxtComm.getOutputStream());
    	dos.writeInt(100);
    	dos.flush();
    	System.out.println("done");
	}
}
