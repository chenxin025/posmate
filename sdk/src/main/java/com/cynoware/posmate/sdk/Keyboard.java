package com.cynoware.posmate.sdk;


public class Keyboard{
    public static int inputKey(Device device, int[] key, int offset, int count){
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_KEYBOARD;
        buf[2] = cmds.SUBCMD_KEYBOARD_INPUT_KEY;

        if(count == 0) return 0;

        int j = 0;
        int i = 4;
        for(; i < buf.length && j < count;
            i += 2, ++j){
            buf[i + 0] = (byte)(key[j+offset] & 0xFF);
            buf[i + 1] = (byte)((key[j+offset] >> 8) & 0xFF);
        }
        buf[3] = (byte)j;

        synchronized(device) {
            while((device.event & cmds.kKeyboardInputing) != 0){
            	if(device.getCloseFlag() || device.isBroken() )
					break;
            	
                device.deviceWait();
            }
            device.writeData(buf, i);
            device.readData(buf);
        }
        Event.pollByCmd(device);
        return (int)((buf[3] & 0xFF) == 0xFF ? -1 : (buf[3] & 0xFF));
    }
    public static int inputKey(Device device, int[] key) {
        return inputKey(device, key, 0, key.length);
    }

    static int inputStringEx(Device device, byte[] bytes, int offset, int len){
        byte[] buf = new byte[cmds.MAX_CMD_SIZE];
        buf[0] = cmds.CMD_REPORT_ID;
        buf[1] = cmds.CMD_KEYBOARD;
        buf[2] = cmds.SUBCMD_KEYBOARD_INPUT_STRING;

        if(len == 0) return 0;

        if(len > buf.length - 4)
            len = buf.length - 4;
        System.arraycopy(bytes, offset, buf, 4, len);
        buf[3] = (byte)len;

        synchronized(device) {
            while((device.event & cmds.kKeyboardInputing) != 0){
            	if(device.getCloseFlag() || device.isBroken() )
					break;
            	
                device.deviceWait();
            }
            device.writeData(buf, 4 + len);
            device.readData(buf);
        }

        return (int)((buf[3] & 0xFF) == 0xFF ? -1 : (buf[3] & 0xFF));
    }

    public static int inputString(Device device, byte[] bytes, int offset, int len){
        int i;
        for(i = 0; i < len; ){
        	if(device.getCloseFlag() || device.isBroken() )
				break;

            int ret = inputStringEx(device, bytes, offset, len - i);
            if(ret < 0) return ret;
            i += ret;
            offset += ret;
        }
        return i;
    }

    public static int inputString(Device device, byte[] bytes){
        return inputString(device, bytes, 0, bytes.length);
    }

    public static int inputString(Device device, String str){
        return inputString(device, str.getBytes());
    }
}