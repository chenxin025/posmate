package com.cynoware.posmate.sdk.io;

import android.os.SystemClock;
import android.util.Log;

import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.util.Utils;
import com.cynoware.posmate.sdk.cmd.Cmds;

public class UART {
	
	private static final String TAG = "UART";
	public static final int DATABIT_5       = 5;
    public static final int DATABIT_6       = 6;
    public static final int DATABIT_7       = 7;
    public static final int DATABIT_8       = 8;
    public static final int PRRITY_NONE     = 0;
    public static final int PRRITY_ODD      = 1;
    public static final int PRRITY_EVEN     = 2;
    public static final int PRRITY_MARK     = 3;
    public static final int PRRITY_SPACE    = 4;
    public static final int STOPBITS_1      = 1;
    public static final int STOPBITS_2      = 2;
    public static final int STOPBITS_1_5    = 3;
    public static final int FLOWCTRL_NONE	= 0;
    public static final int FLOWCTRL_XONOFF = 1;
    public static final int FLOWCTRL_HW		= 2;
    

    public static void setConfig(Device device, int uart, int baudrate, int dataBits, int parity, int stopBits, int flowCtrl){
    	Log.d( TAG, "setConfig " + uart + ":" + baudrate );

        byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
        buf[0] = Cmds.CMD_REPORT_ID;
        buf[1] = Cmds.CMD_UART;
        buf[2] = (byte)((Cmds.SUBCMD_UART_SET_CONFIG << 4) | (uart & 0x0F));
        buf[3] = (byte)flowCtrl;

        Utils.int2byte_le(baudrate, buf, 4);
        Utils.int2byte_le(dataBits, buf, 8);
        Utils.int2byte_le(parity, buf, 12);
        Utils.int2byte_le(stopBits, buf, 16);

        synchronized(device){
            device.writeData(buf, 20);
            device.readData(buf);
        }
    }


    public static  int write(Device device, int uart, byte[] data, int offset, int len){
    	Log.d( TAG, "Write  len " + len );

        int fixLen = (len > Cmds.MAX_CMD_SIZE - 4 ? (Cmds.MAX_CMD_SIZE - 4) : len);
        // int sum = Utils.cksum(data, offset, len);

        byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
        buf[0] = Cmds.CMD_REPORT_ID;
        buf[1] = Cmds.CMD_UART;
        buf[2] = (byte) ((Cmds.SUBCMD_UART_WRITE << 4) | (uart & 0x0F));
        buf[3] = (byte) fixLen;
        System.arraycopy(data, offset, buf, 4, fixLen);

        synchronized(device){
            device.writeData(buf, 4 + fixLen);
            device.readData(buf);
        }

        int sum2 = Utils.byte2int_le(buf, 4);
        //assert(sum == sum2);

        int writed_bytes_num  = (int)(buf[3] & 0xff);
        Log.d( TAG, "Write writed " + writed_bytes_num );
        return writed_bytes_num;
    }


    public static int write(Device device, int uart, byte[] data) {
        return write(device, uart, data, 0, data.length);
    }

    public static int read(Device device, int uart, byte[] data, int offset, int len){
    	Log.d( TAG, "read " + len );

        SDKLog.i(TAG,"read start ============================");
        byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
        int fixLen = (len > Cmds.MAX_CMD_SIZE - 4 ? (Cmds.MAX_CMD_SIZE - 4) : len);
        buf[0] = Cmds.CMD_REPORT_ID;
        buf[1] = Cmds.CMD_UART;
        buf[2] = (byte)((Cmds.SUBCMD_UART_READ << 4) | (uart & 0x0F));
        buf[3] = (byte)fixLen;

        synchronized(device){
            device.writeData(buf, 4);
            device.readData(buf);
        }
        if(len > (int)(buf[3] & 0xff))
            len = (int)(buf[3] & 0xff);
        System.arraycopy(buf, 4, data, offset, len);

        SDKLog.i(TAG,"read end ============================");

        Log.d( TAG, "readed " + len );

        return len;
    }


    public static int read(Device device, int uart, byte[] data){
        return read(device, uart, data, 0, data.length);
    }


    private static Object[] timedRead_(Device device, int uart, byte[] data, int offset, int len, long millisecond){
        Integer ret = 0;
        int uart_mask = (int)(Cmds.kUartRx0Aailable << uart);
        long start = SystemClock.elapsedRealtime();

        Boolean cancelled = false;

        synchronized(device){
            //Try first
            ret = read(device, uart, data, offset, len);
            SDKLog.i(TAG,"============111111==========="+ret);
            //The wait
            if (ret <= 0) {
                while (true) {
                    if (device.getCloseFlag() || device.isBroken() )
                    	break;



                    SDKLog.i(TAG,"============222222222222111111111==========="+ret);
                   if ((device.event & uart_mask) != 0) {
                        ret = read(device, uart, data, offset, len);
                        SDKLog.i(TAG,"============3333333333==========="+ret);
                        if (ret == 0) {
                            device.uartWaiting |= uart_mask;
                            Event.pollByCmd(device);
                            cancelled = ((device.uartWaiting & uart_mask) == 0);
                            device.uartWaiting &= ~uart_mask;
                        }
                        break;
                    }

                    long elapse = SystemClock.elapsedRealtime() - start;

                    long wait = (elapse < millisecond ? millisecond - elapse : 0);
                    if (wait == 0)
                    	break;


                    device.deviceTimedWait(wait);
                }
            }
        }

        Object[] retObj = new Object[2];
        retObj[0] = ret;
        retObj[1] = cancelled;
        return retObj;
    }

    public static int timedRead(Device device, int uart, byte[] data, int offset, int len, long millisecond) {
        Object[] retObj = timedRead_(device, uart, data, offset, len, millisecond);
        return ((Integer)retObj[0]).intValue();
    }

    public static int timedRead(Device device, int uart, byte[] data, long millisecond){
        return timedRead(device, uart, data, 0, data.length, millisecond);
    }

    public static int fixedRead(Device device, int uart, byte[] data, int offset, int len, long millisecond){
        int size = 0;
        long start = SystemClock.elapsedRealtime();

        while(len > 0){
            if(device.getCloseFlag() || device.isBroken())          //getCloseFlag() >> return mCloseFlag
            	break;

            long elapse = SystemClock.elapsedRealtime() - start;
            long wait = (elapse < millisecond ? millisecond - elapse : 0);

            Object[] retObj = timedRead_(device, uart, data, offset, len, wait);

            int ret = ((Integer)retObj[0]).intValue();
            boolean cancelled = ((Boolean)retObj[1]).booleanValue();

            size += ret;
            offset += ret;
            len -= ret;

            if (0 == len){
                SDKLog.i(TAG,"len=====================0");
                break;
            }
            if(wait == 0 || cancelled )
            	break;
        }
        return size;
    }
    public static int fixedRead(Device device, int uart, byte[] data, long millisecond){
        return fixedRead(device, uart, data, 0, data.length, millisecond);
    }

    public static void cancelIo(Device device, int uart){
        int uart_mask = (Cmds.kUartRx0Aailable << uart);
        synchronized (device) {

            if ((device.uartWaiting & uart_mask) != 0) {
                device.uartWaiting &= ~uart_mask;
                device.notifyAll();
            }
        }
    }

    public static int fixedWrite(Device device, int uart, byte[] data, int offset, int len){
        int size = 0;
        SDKLog.i(TAG,"fixedWrite    START ===LEN="+len);
        while(len > 0){

            if(device.getCloseFlag() || device.isBroken() )
            	break;

            int ret = write(device, uart, data, offset, len);
            //SDKLog.i(TAG,"fixedWrite    START ===ret="+ret);
            size += ret;
            offset += ret;
            len -= ret;
        }
        return size;
    }

    public static int fixedWrite(Device device, int uart, byte[] data) {
        return fixedWrite(device, uart, data, 0, data.length);
    }

    public static void clearBuffer(Device device, int uart, boolean clearTx, boolean clearRx){
        byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
        buf[0] = Cmds.CMD_REPORT_ID;
        buf[1] = Cmds.CMD_UART;
        buf[2] = (byte)((Cmds.SUBCMD_UART_CLEAR_BUF << 4) | (uart & 0x0F));
        buf[3] = (byte)((clearTx ? 0x01 : 0x00) | (clearRx ? 0x02 : 0x00));

        synchronized(device){
            device.writeData(buf, 4);
            device.readData(buf);
        }
    }

    public static void clearBuffer(Device device, int uart){
        clearBuffer(device, uart, false, true);
    }

}

