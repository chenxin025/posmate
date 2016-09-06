package com.cynoware.posmate.sdk;

import android.util.Log;

public class LoopbackTest {
    private static final String TAG = "LoopbackTest";

    public static boolean test(Device device) {
        boolean ret;

        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_LOOPBACK_TEST;

        for (int i = 2; i < buf.length; ++i)
            buf[i] = (byte) (Math.random() * 256);

        byte[] res = new byte[cmds.MAX_CMD_SIZE];
        for (int i = 0; i < res.length; ++i)
            res[i] = 0;
        synchronized (device) {
            device.writeData(buf, buf.length);
            device.readData(res);
        }

        ret = true;
        for (int i = 2; i < buf.length; ++i) {
            if ((byte) (buf[i] + 1) != res[i]) {
                ret = false;
                break;
            }
        }

        if (!ret) {
            for (int i = 0; i < buf.length; ++i) {
                Log.i(TAG, String.format("%02x %02x", (int) (buf[i] & 0xFF), (int) (res[i] & 0xFF)));
            }
        }

        if(ret)
            Log.i(TAG, "Test OK");
        else
            Log.i(TAG, "Test Fail");
        return ret;
    }
}
