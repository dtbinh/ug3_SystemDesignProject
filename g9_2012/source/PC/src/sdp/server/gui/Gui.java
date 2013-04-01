package sdp.server.gui;

public interface Gui {
	public void         initialise();
	public void         update();
	public void         dispose();
	public ControlPanel getStrategyPanel();
	public ImageViewer  getImageViewer();
	
}
