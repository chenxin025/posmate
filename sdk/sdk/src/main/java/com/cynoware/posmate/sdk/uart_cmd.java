package com.cynoware.posmate.sdk;

import android.util.Log;

public class uart_cmd {

    public static final int ST_INIT = 0;
    public static final int ST_REPORT_WRITE = 1;
    public static final int ST_SET_FEATURE  = 2;
    public static final int ST_REPORT_EVENT = 3;
    public static final int ST_SET_EVENT    = 4;
    
    private static final String TAG = "uart_cmd";

    private static void onHidResponse(BleDevice bleDevice, byte[] buf, int offset, int size) {
        byte cmd;
        int subcmd;

        Device device = bleDevice;
        synchronized(device) {
            cmd = buf[1 + offset];
            subcmd = buf[2 + offset];

            //Get response of command
            System.arraycopy(buf, offset, bleDevice.hidCmdOut, 0, size);
            bleDevice.readCmd_ = cmd;
            Log.i(TAG, String.format("size = %d, onHidResponse = %02x %02x", size, bleDevice.readCmd_, bleDevice.writeCmd_));

            //Special fix for commands without response
            if(size == 0) bleDevice.readCmd_ = bleDevice.writeCmd_;
            device.notifyAll();
        }
    }

    private static void onUartEvent(BleDevice bleDevice, byte[] buf, int offset, int size) {
        byte[] eventData = new byte[cmds.MAX_EVENT_SIZE + 1];
        int eventMaxSize = eventData.length;
        if (size > cmds.MAX_EVENT_SIZE) size = cmds.MAX_EVENT_SIZE;
        System.arraycopy(buf, offset, eventData, 1, size);

        Log.i(TAG, String.format("onUartEvent : %02x %02x %02x %02x %02x, size = %02x", eventData[0], eventData[1], eventData[2], eventData[3], eventData[4], size));
        Event.onEventPoll(bleDevice, eventData, size + 1);
    }

	private static int CMD_HEAD(BleDevice bleDevice) {
		return bleDevice.uartCmdOut[0];
	}

	private static int CMD_SIZE(BleDevice bleDevice) {
		return bleDevice.uartCmdOut[1];
	}

	private static int CMD_CHECKSUM(BleDevice bleDevice) {
		return util.byte2short_le(bleDevice.uartCmdOut, 2);
	}

    private static int uartCheckSum(byte[] buf, int offset, int size){
        int res = util.cksum(buf, offset, size);
        return (res & 0xffff);
    }

    /* Return true if an event found, otherwise return false */
    public static boolean parseUartData(BleDevice bleDevice, buf rx) {
        while (true) {
            switch (bleDevice.status) {
                case ST_INIT:
                    if (rx.size() < 1)
                        return false;
                    rx.popFront(bleDevice.uartCmdOut, 0, 1);
                    if (CMD_HEAD(bleDevice) == cmds.UART_CMD_WRITE_HEAD)
                        bleDevice.status = ST_REPORT_WRITE;
                    else if (CMD_HEAD(bleDevice) == cmds.UART_CMD_EVENT_HEAD)
                        bleDevice.status = ST_REPORT_EVENT;
                    break;

                case ST_REPORT_WRITE:
                    if (rx.size() < 3)
                        return false;
                    rx.popFront(bleDevice.uartCmdOut, 1, 3);
                    if (CMD_SIZE(bleDevice) > cmds.UART_CMD_MAX_SIZE - 4) {
                        rx.pushFront(bleDevice.uartCmdOut, 1, 3);
                        bleDevice.status = ST_INIT;
                        break;
                    }

                    bleDevice.status = ST_SET_FEATURE;
                    break;

                case ST_REPORT_EVENT:
                    if (rx.size() < 3)
                        return false;
                    rx.popFront(bleDevice.uartCmdOut, 1, 3);
                    if (CMD_SIZE(bleDevice) != 4) {
                        rx.pushFront(bleDevice.uartCmdOut, 1, 3);
                        bleDevice.status = ST_INIT;
                        break;
                    }

                    bleDevice.status = ST_SET_EVENT;
                    break;

                case ST_SET_FEATURE:
                    if (rx.size() < CMD_SIZE(bleDevice))
                        return false;

                    rx.popFront(bleDevice.uartCmdOut, 4, CMD_SIZE(bleDevice));
                    //checksum
                    if (uartCheckSum(bleDevice.uartCmdOut, 4, CMD_SIZE(bleDevice)) != CMD_CHECKSUM(bleDevice)){
                        //checksum failed, cut the header and restore other data to buffer
                        rx.pushFront(bleDevice.uartCmdOut, 1, 3 + CMD_SIZE(bleDevice));
                        bleDevice.status = ST_INIT;
                        break;
                    }

                    //Now, we get command as HID command here
                    onHidResponse(bleDevice, bleDevice.uartCmdOut, 4, CMD_SIZE(bleDevice));
                    bleDevice.status = ST_INIT;
                    break;

                case ST_SET_EVENT:
                    if (rx.size() < CMD_SIZE(bleDevice))
                        return false;

                    rx.popFront(bleDevice.uartCmdOut, 4, CMD_SIZE(bleDevice));
                    //checksum
                    if (uartCheckSum(bleDevice.uartCmdOut, 4, CMD_SIZE(bleDevice)) != CMD_CHECKSUM(bleDevice)){
                        //checksum failed, cut the header and restore other data to buffer
                        rx.pushFront(bleDevice.uartCmdOut, 1, 3 + CMD_SIZE(bleDevice));
                        bleDevice.status = ST_INIT;
                        break;
                    }

                    //Now, we get command as event here
                    onUartEvent(bleDevice, bleDevice.uartCmdOut, 4, CMD_SIZE(bleDevice));
                    bleDevice.status = ST_INIT;
                    return true;
                    //break;
            }
        }

        //return false;
    }

}
