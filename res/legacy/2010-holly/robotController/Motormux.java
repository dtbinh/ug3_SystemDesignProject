import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.nxt.SensorPort;

public class Motormux extends I2CSensor{

	//replace with correct chip address
	public static final byte MOTORMUX_ADD = (byte)0x5a;
	public static final byte MOTOR_1_DIR = 1;
	public static final byte MOTOR_1_SP = 2;
	public static final byte MOTOR_2_DIR = 3;
	public static final byte MOTOR_2_SP = 4;
	public static final byte MOTOR_3_DIR = 5;
	public static final byte MOTOR_3_SP = 6;
	public static final byte MOTOR_4_DIR = 7;
	public static final byte MOTOR_4_SP = 8;
	
	public static final byte flt = (byte) 0x00;
	public static final byte fwd = (byte) 0x01;
	public static final byte reverse = (byte) 0x02;
	public static final byte brake = (byte) 0x03;
	
	private boolean movingLeft;
	private boolean movingRight;

	private int prevSpeedFront, prevSpeedBack;
	
	public Motormux(I2CPort port) {
            super(port);
            setAddress(MOTORMUX_ADD);
	}


    public void rampUp(int speed){
        
        if(speed<0){
            speed=-speed;
            sendData(MOTOR_3_DIR, reverse);
            sendData(MOTOR_4_DIR, reverse);
        }

        else{
            sendData(MOTOR_3_DIR, fwd);
            sendData(MOTOR_4_DIR, fwd);
        }

        sendData(MOTOR_3_SP, (byte) 16);
		sendData(MOTOR_4_SP, (byte) 16);

		for (int i = 16; i < speed; i=i*2){
			sendData(MOTOR_3_SP, (byte)i);
			sendData(MOTOR_4_SP, (byte)i);
			try{
				Thread.sleep(50);	
			} catch (InterruptedException ex){
				
			}
			
		}
		sendData(MOTOR_3_SP, (byte)speed);
		sendData(MOTOR_4_SP, (byte)speed);

    }

    public void setSpeeds(int sFront, int sBack){
        
        if(sFront<0){
            sFront=-sFront;
            sendData(MOTOR_3_DIR, reverse);
        }

        else{
            sendData(MOTOR_3_DIR, fwd);
        }

        if(sBack<0){
            sBack=-sBack;
            sendData(MOTOR_4_DIR, reverse);
        }
        else{
            sendData(MOTOR_4_DIR, fwd);
        }

		if(prevSpeedFront<50 && sFront>50){
			for (int i = 16; i < sFront; i=i*2){
			sendData(MOTOR_3_SP, (byte)i);
			try{
				Thread.sleep(25);
			} catch (InterruptedException ex){

			}

		}

		}

		if(prevSpeedBack<50 && sBack>50){
			for (int i = 16; i < sFront; i=i*2){
			sendData(MOTOR_4_SP, (byte)i);
			try{
				Thread.sleep(25);
			} catch (InterruptedException ex){

			}

		}

		}

		prevSpeedFront = sFront;
		prevSpeedBack = sBack;

        sendData(MOTOR_3_SP, (byte)sFront);
		sendData(MOTOR_4_SP, (byte)sBack);

    }
	
	public void strafeL(byte speed){
		
		movingRight = false; movingLeft=true;
		
		sendData(MOTOR_3_SP, (byte) 16);
		sendData(MOTOR_4_SP, (byte) 16);
		sendData(MOTOR_3_DIR, fwd);
		sendData(MOTOR_4_DIR, fwd);
		
		for (int i = 16; i < speed; i=i*2){
			sendData(MOTOR_3_SP, (byte)i);
			sendData(MOTOR_4_SP, (byte)i);
			try{
				Thread.sleep(50);	
			} catch (InterruptedException ex){
				
			}
			
		}
		sendData(MOTOR_3_SP, (byte)speed);
		sendData(MOTOR_4_SP, (byte)speed);
		
	}

    public void strafeR(byte speed){
    	
    	movingRight = true; movingLeft = false;
    	
    		sendData(MOTOR_3_SP, (byte) 16);
    		sendData(MOTOR_4_SP, (byte) 16);
    		sendData(MOTOR_3_DIR, reverse);
            sendData(MOTOR_4_DIR, reverse);
    		
    		for (int i = 16; i < speed; i=i*2){
    			sendData(MOTOR_3_SP, (byte)i);
    			sendData(MOTOR_4_SP, (byte)i);
    			try{
    				Thread.sleep(50);	
    			} catch (InterruptedException ex){
    				
    			}
    			
    		}
    		sendData(MOTOR_3_SP, (byte)speed);
    		sendData(MOTOR_4_SP, (byte)speed);
        }

        //Possible, but probably won't use
	public void rotateLeft(byte speed){

                sendData(MOTOR_3_SP, speed);
                sendData(MOTOR_3_DIR, fwd);

                sendData(MOTOR_4_SP, speed);
                sendData(MOTOR_4_DIR, reverse);

        }

        //Possible, but probably won't use
	public void rotateRight(byte speed){

            sendData(MOTOR_3_SP, speed);
            sendData(MOTOR_3_DIR, reverse);

            sendData(MOTOR_4_SP, speed);
            sendData(MOTOR_4_DIR, fwd);

        }

        public void stop(){
        	
        	movingLeft=false; movingRight=false;
            sendData(MOTOR_3_DIR, brake);
            sendData(MOTOR_4_DIR, brake);
        }

	public void flt(){
		
		movingLeft=false; movingRight=false;
	    sendData(MOTOR_3_DIR, flt);
        sendData(MOTOR_4_DIR, flt);
	}
	
	public boolean isMovingLeft(){
		return movingLeft;
	}
	
	public boolean isMovingRight(){
		return movingRight;
	}
}
