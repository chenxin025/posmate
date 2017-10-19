package com.cynoware.posmate.sdk.io;

/**
 * Created by chenxin on 2017/3/24.
 */

public class P140Gpio {

    private final static String TAG = "P140Gpio";

    private static P140Gpio mP140Gpio = null;

    public static P140Gpio getP140Gpio(){
        if (mP140Gpio == null){
            mP140Gpio = new P140Gpio();
        }
        return mP140Gpio;
    }

    // JNI
    public native void openDrawer();
    public native  void closeDrawer();
    static {
        System.loadLibrary("serial_port");
    }
}
