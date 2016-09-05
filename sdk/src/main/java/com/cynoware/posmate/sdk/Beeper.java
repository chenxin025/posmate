package com.cynoware.posmate.sdk;

public class Beeper {	
	
	private Device mDevice;
	private int mGPIO;
	
	
	public Beeper( Device device, int gpio ){
		mDevice = device;
		mGPIO = gpio;
	}
	
	public void beep(){
    	try {
    		GPIO.setMode(mDevice, mGPIO, 1, 0);
    		GPIO.output(mDevice, mGPIO, 1);
            Thread.sleep(200);
            GPIO.output(mDevice, mGPIO, 0);
            Thread.sleep(200);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
}
