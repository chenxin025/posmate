package com.cynoware.posmate.sdk.drawer;

import com.cynoware.posmate.sdk.io.P140Gpio;
import com.cynoware.posmate.sdk.io.SerialPort;

/**
 * Created by john on 2017/3/27.
 */

public class P140Drawer implements CashDrawer {

    private P140Gpio mP140Gpio = null;

    public P140Drawer(){
        if (null == mP140Gpio){
            mP140Gpio = new P140Gpio();
        }
    }


    @Override
    public void open() {
        mP140Gpio.openDrawer();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mP140Gpio.closeDrawer();
    }
}
