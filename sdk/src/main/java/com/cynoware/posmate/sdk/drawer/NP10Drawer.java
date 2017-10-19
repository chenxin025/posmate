package com.cynoware.posmate.sdk.drawer;

import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.GPIO;

/**
 * Created by john on 2017/3/27.
 */

public class NP10Drawer implements CashDrawer{

    private Device mDevice;
    private int mGPIO;


    public NP10Drawer(Device device, int gpio){
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
