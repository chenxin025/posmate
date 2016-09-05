package com.cynoware.posmate.sdk;

import android.util.Log;


public class GPIO{
	private static final String TAG="GPIO";
    public static final int MODE_INPUT		= 0;
    public static final int MODE_OUTPUT		= 1;
    public static final int MODE_OPENDRAIN	= 2;
    public static final int MODE_QUASI		= 3;

    public static void setMode(Device device, int gpio, int mode, int value){
    	Log.d(TAG, "setMode " + gpio + ":" + mode + ":" + value );    	
    	
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_GPIO;
        buf[2] = cmds.SUBCMD_GPIO_SET_MODE;

        util.int2byte_le(gpio, buf, 4);
        util.int2byte_le(mode, buf, 8);
        util.int2byte_le(value, buf, 12);

        synchronized(device){
            device.writeData(buf, 16);
            device.readData(buf);
        }
    }

    public static int getMode(Device device, int gpio){
    	Log.d(TAG, "getMode " + gpio );
    	
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_GPIO;
        buf[2] = cmds.SUBCMD_GPIO_GET_MODE;
        util.int2byte_le(gpio, buf, 4);

        synchronized(device){
            device.writeData(buf, 8);
            device.readData(buf);
        }

        int mode = util.byte2int_le(buf, 4);
        Log.d(TAG, "getMode " + gpio + "=" + mode);
        return mode;
    }

    public static void output(Device device, int gpio, int value){
    	Log.d(TAG, "output " + gpio + ">" + value );
    	
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_GPIO;
        buf[2] = cmds.SUBCMD_GPIO_OUTPUT;
        util.int2byte_le(gpio, buf, 4);
        util.int2byte_le(value, buf, 8);

        synchronized(device){
            device.writeData(buf, 12);
            device.readData(buf);
        }
    }

    public static int input(Device device, int gpio){
    	byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_GPIO;
        buf[2] = cmds.SUBCMD_GPIO_INPUT;
        util.int2byte_le(gpio, buf, 4);

        synchronized(device){
            device.writeData(buf, 8);
            device.readData(buf);
        }

        int mode = util.byte2int_le(buf, 4); 
        Log.d(TAG, "input " + gpio + "<" + mode );
        return mode;
    }
}

