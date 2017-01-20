package com.cynoware.posmate.sdk.drawer;

import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.GPIO;

public class CashDrawer {
	
	private Device mDevice;
	private int mGPIO;
	
	
	public CashDrawer(Device device, int gpio ){
		mDevice = device;
		mGPIO = gpio;
	}
	
	public void open(){
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
