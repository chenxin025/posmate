package com.cynoware.posmate.sdk;

import android.util.Log;

public class Event{
    private static final String TAG = "Event";

    
    // 目前仅Hid调用
    public static void eventPoll(Device device) {
        byte[] buf = new byte[cmds.MAX_EVENT_SIZE + 1];

        int size = device.waitEvent(buf);
        onEventPoll(device, buf, size);
    }

    
    public static void onEventPoll(Device device, byte[] buf, int size){
        if(size == buf.length){
            int event = util.byte2int_le(buf, 1);

            Log.i(TAG, String.format("event %x", event));
            synchronized(device) {
                if (event != device.event) {
                    device.event = event;
                    device.notifyAll();
                }
            }
        }
        else if(device.isBroken()) {
            synchronized (device) {
                device.notifyAll();
            }
        }
    }

    public static void setEvent(Device device, int event, int param){
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_EVENT;
        buf[2] = cmds.SUBCMD_EVENT_SET;
        util.int2byte_le(event, buf, 4);
        util.int2byte_le(param, buf, 8);

        synchronized(device){
            device.writeData(buf, 12);
            device.readData(buf);
        }
    }

    public static void pollByCmd(Device device){
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_EVENT;
        buf[2] = cmds.SUBCMD_EVENT_GET;         //SUBCMD_EVENT_GET = 0x00

        synchronized(device){
            device.writeData(buf, 4);
            device.readData(buf);

            int event = util.byte2int_le(buf, 4);
            if (event != device.event) {
                device.event = event;
                device.notifyAll();
            }
        }
    }
}
