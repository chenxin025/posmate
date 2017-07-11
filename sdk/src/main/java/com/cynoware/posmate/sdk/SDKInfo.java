package com.cynoware.posmate.sdk;

/**
 * Created by john on 2017/1/18.
 */

public class SDKInfo {


    public static final int POS_SET_NP10 = 0;

    public static final int POS_SET_NP11 = 1;

    public static final int POS_SET_P140 = 2;

    public static final String PREF_POS_SET = "pos_set";

    public static final int ERROR_CODE = -1;
    public static final int SUCCESS_CODE = 0;


    public static final int SERIAL_INDEX0 = 0;//  /dev//dev/ttymxc1
    public static final int SERIAL_INDEX1 = 1;//  /dev//dev/ttymxc1

    public static final String PREF_POS_SERIALPORT_ID = "pos_led_serial_port";

    public static String getPosSdkVersion(){
        return "2.0.1";
    }


}
