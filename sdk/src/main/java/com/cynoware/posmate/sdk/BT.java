package com.cynoware.posmate.sdk;

public class BT{
    public static void selectAsBtPort(Device device, int uart) {

        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_UART;
        buf[2] = (byte)((cmds.SUBCMD_UART_SET_AS_BT_PORT << 4) | (uart & 0x0F));
        buf[3] = 0;

        synchronized(device){
            device.writeData(buf, 4);
            device.readData(buf);
        }
    }

    public static void setMac(Device device, byte[] mac) {
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_BT;
        buf[2] = cmds.SUBCMD_BT_SET_MAC;
        System.arraycopy(mac, 0, buf, 3, 6);

        synchronized(device) {
            device.writeData(buf, 9);
            device.readData(buf);
        }
    }

    public static void getMac(Device device, byte[] mac) {
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_BT;
        buf[2] = cmds.SUBCMD_BT_GET_MAC;

        synchronized(device) {
            device.writeData(buf, 4);
            device.readData(buf);
        }

        System.arraycopy(buf, 3, mac, 0, 6);
    }

    public static int macToSn(byte[] mac) {
        int i;
        int res = 0;
        for (i = 6; i-- != 0; ) {
            res *= 10;
            res += (mac[i] & 0xFF);
        }
        res %= 1000000;
        return res;
    }
}

