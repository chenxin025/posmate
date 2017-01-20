package com.cynoware.posmate.sdk.io;

import android.util.Log;

import com.cynoware.posmate.sdk.util.Utils;
import com.cynoware.posmate.sdk.cmd.Cmds;

public class Event {
    private static final String TAG = "Event";

    
    // 目前仅Hid调用
    public static void eventPoll(com.cynoware.posmate.sdk.io.Device device) {
        byte[] buf = new byte[Cmds.MAX_EVENT_SIZE + 1];

        int size = device.waitEvent(buf);
        onEventPoll(device, buf, size);
    }


    public static void onEventPoll(com.cynoware.posmate.sdk.io.Device device, byte[] buf, int size){
        if(size == buf.length){
            int event = Utils.byte2int_le(buf, 1);

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

    public static void setEvent(com.cynoware.posmate.sdk.io.Device device, int event, int param){
        byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
        buf[0] = Cmds.CMD_REPORT_ID;
        buf[1] = Cmds.CMD_EVENT;
        buf[2] = Cmds.SUBCMD_EVENT_SET;
        Utils.int2byte_le(event, buf, 4);
        Utils.int2byte_le(param, buf, 8);

        synchronized(device){
            device.writeData(buf, 12);
            device.readData(buf);
        }
    }

    public static void pollByCmd(Device device){
        byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
        buf[0] = Cmds.CMD_REPORT_ID;
        buf[1] = Cmds.CMD_EVENT;
        buf[2] = Cmds.SUBCMD_EVENT_GET;         //SUBCMD_EVENT_GET = 0x00

        synchronized(device){
            device.writeData(buf, 4);
            device.readData(buf);

            int event = Utils.byte2int_le(buf, 4);
            if (event != device.event) {
                device.event = event;
                device.notifyAll();
            }
        }
    }
}
