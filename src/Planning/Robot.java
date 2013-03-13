package Planning;

import JavaVision.Position;

public class Robot extends ObjectDetails {
	
	public Robot() {
		
	}
	
	public Robot (Position coors, float angle) {
		this.setCoors(coors);
		this.setAngle(angle);
	}
}