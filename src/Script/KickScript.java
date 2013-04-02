package Script;

public class KickScript extends AbstractBaseScript {
	public static void main(String[] args) {
		KickScript ks = new KickScript(args);
		ks.run();
	}
	
	public KickScript(String[] args) {
		super(args);

	}
	
	public void run() {
		started = true;
		sendZeros();
		planKick();
		playExecute();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		planKick();
		playExecute();
	}
}
