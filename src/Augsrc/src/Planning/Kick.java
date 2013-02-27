package Planning;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Kick {


private static Context context;
private static Socket socket;

public static void main(String args[]) throws InterruptedException{
	context = ZMQ.context(1);
	socket = context.socket(ZMQ.REQ);
    socket.connect("ipc:///tmp/nxt_bluetooth_robott");
    	
}

}
