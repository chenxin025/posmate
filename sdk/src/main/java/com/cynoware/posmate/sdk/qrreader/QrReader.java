package com.cynoware.posmate.sdk.qrreader;

import android.util.Log;

import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.GPIO;
import com.cynoware.posmate.sdk.io.UART;
import com.cynoware.posmate.sdk.util.Utils;
import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.configs.ConfigFactory;

/*
*   二维扫描模块说明
*   1 只支持按键模式
*   2 只支持Tray USB 通道
*
*   提供的对外接口
*   1 初始化二维扫描模块 initQrReader
*   2 开始扫描           startScan
*   3 停止扫描           stopScan
*   4 关闭扫描模块       closeQRScanner
*
*/

public class QrReader {

	private static final String TAG = "QRReadr";
	public static final int QR_UART_TIMEOUT_MS = 1000;
    public static final int QR_UART_LONG_TIMEOUT_MS = 5000;
    public static final int QR_UART_BAUDRATE = 115200;

    private static IScannerDefine mInstance = null;

    public static final byte[] CMD_GET_VERSION = {0x43, 0x02, (byte)0xC2};
    public static final byte[] CMD_SET_DATA = {0x21, 0x51, 0x43, 0x03};
    public static final byte[] CMD_SCAN = {0x32, 0x75, 0x01};
    public static final byte[] CMD_STOP = {0x32, 0x75, 0x02};
    public static final byte[] CMD_TRIGGER_MODE = { 0x21, 0x61, 0x41, 0x00 };
    public static final byte[] CMD_BEEP = { 0x21, 0x63, 0x42, 0x00};
    

    public static final int TriggerMode_PressKey    = 0x00;
    public static final int TriggerMode_Continuous  = 0x01;
    public static final int TriggerMode_Auto        = 0x02;
    public static final int TriggerMode_Respond     = 0x03;
    public static final int TriggerMode_Wave        = 0x04;
    
    //public static int uart;
    static class ConfigHeader {
        byte type_;
        byte pid_;
        byte fid_;
    }

    static class ControlHeader {
        byte type_;
        byte size1_;
        byte size0_;
    }

    private synchronized  static IScannerDefine getInstance(){
        int type = ConfigFactory.getConfigType().getScannerConfigInfo().mDevVersion;
        SDKLog.i(TAG,"IScannerDefine getInstance "+type);

        switch (type){
            case BaseConfig.SYS_DEV_VERSION_1:
                if (null == mInstance){
                    mInstance = new NP10ScannerImpl();
                }
                break;

            case BaseConfig.SYS_DEV_VERSION_2:
                if (null == mInstance){
                    mInstance = new NP11ScannerImpl();
                }
                break;

            default:
                break;
        }
        return mInstance;
    }

    public  static void initQrReader(Device device, int uart){
        IScannerDefine baseApi = getInstance();
        if (null == baseApi){
            SDKLog.i(TAG,"initQrReader=============== mDefine=null");
            return;
        }
        baseApi.initScanner(device,uart);
    }

    static int getConfigParamSize(ConfigHeader header) {
        switch (header.type_) {
            case (byte)0x22:
                return 2;
            case (byte)0x33:
                return 2;
            case (byte)0x24:
            case (byte)0x44: {
                int bit7 = (header.fid_ & 0x80);
                int bit6 = (header.fid_ & 0x40);
                if (bit7 != 0 && bit6 != 0) return -1;    //From later param, may > 2
                if (bit7 == 0 && bit6 != 0) return 1;
                if (bit7 != 0 && bit6 == 0) return 2;
                return 0;
            }
        }
        return 0;
    }

    static int doConfig(Device device, int uart, byte[] sendBuf, byte[] recvBuf, int maxRecvSize, long timeout_ms) {
    	// UART.setConfig(device, uart, QR_UART_BAUDRATE, 8, UART.PRRITY_NONE, UART.STOPBITS_1);
        Log.d(TAG, "doConfig start");
        Log.i(TAG,"fixedWrite ==========================ret===============");
        int ret = UART.fixedWrite(device, uart, sendBuf);
        Log.i(TAG,"fixedWrite ==========================ret:"+ret);
        if (ret < 0) return ret;
        Log.d(TAG, "begin to fixread");
        byte[] temp = new byte[3];
        ret = UART.fixedRead(device, uart, temp, 0, 3, timeout_ms);
        if (ret != 3) return ret;
        ConfigHeader header = new ConfigHeader();
        header.type_ = temp[0];
        header.pid_ = temp[1];
        header.fid_ = temp[2];
        int paramSize = getConfigParamSize(header);

        if (paramSize == -1) {
            ret = UART.fixedRead(device, uart, temp, 0, 2, QR_UART_TIMEOUT_MS);
            if (ret != 2) return ret;
            paramSize = Utils.byte2short_be(temp, 0);
        }

        int recvSize = (paramSize < maxRecvSize ? paramSize : maxRecvSize);
        ret = UART.fixedRead(device, uart, recvBuf, 0, recvSize, QR_UART_TIMEOUT_MS);
        if (ret != recvSize) return ret;

        //Read and drop remaining data
        while (recvSize < paramSize) {
            byte[] buf = new byte[64];
            int size = (paramSize - recvSize);
            if (size > buf.length) size = buf.length;
            int ret1 = UART.timedRead(device, uart, buf, 0, size, QR_UART_TIMEOUT_MS);
            if (ret1 != recvSize) break;
        }
        Log.d(TAG, "doConfig end");
        return ret;
    }

    static int doControl(Device device, int uart, byte[] sendBuf, byte[] recvBuf, int maxRecvSize,
                         int timeout_ms,
                         ControlHeader controlHeader) {
        Log.d(TAG, "doControl start");
        int ret = UART.fixedWrite(device, uart, sendBuf);
        if (ret < 0) 
        	return ret;
        Log.d(TAG, "doControl read start");
        
        byte[] header = new byte[3];
        ret = UART.fixedRead(device, uart, header, 0, 1, timeout_ms);
        Log.d(TAG, "doControl read start   ret="+ret);
        if (ret != 1)
        	return ret;
        Log.d(TAG, "header compare");
        
        //if(type != NULL) *type = header[0];
        controlHeader.type_ = header[0];
        if(header[0] != 0x03){
            //Fallback to raw data
            if (maxRecvSize <= 0) return 0;
            recvBuf[0] = header[0];
            for (ret = 1; ret < maxRecvSize; ) {
                int len = maxRecvSize - ret;
                len = UART.timedRead(device, uart, recvBuf, ret, len, QR_UART_TIMEOUT_MS);
                if (len == 0) break;
                ret += len;
            }
            return ret;
        }
        else {
            ret = UART.fixedRead(device, uart, header, 1, 2, QR_UART_TIMEOUT_MS);
            if (ret != 2) return ret;

            controlHeader.size0_ = header[1];
            controlHeader.size1_ = header[2];
            int paramSize = paramSize = Utils.byte2short_be(header, 1);

            int recvSize = (paramSize < maxRecvSize ? paramSize : maxRecvSize);
            ret = UART.fixedRead(device, uart, recvBuf, 0, recvSize, QR_UART_TIMEOUT_MS);
            if (ret != recvSize) return ret;

            //Read and drop remaining data
            while (recvSize < paramSize) {
                byte[] buf = new byte[64];
                int size = (paramSize - recvSize);
                if (size > buf.length) size = buf.length;
                int ret1 = UART.timedRead(device, uart, buf, 0, size, QR_UART_TIMEOUT_MS);
                if (ret1 != recvSize) break;
            }
        }
        return ret;
    }

    public static String getVersion(Device device, int uart) {
        byte[] buf = new byte[256];
        int ret = doConfig(device, uart, CMD_GET_VERSION, buf, buf.length, QR_UART_TIMEOUT_MS);
        if (ret >0 )
        	return new String(buf, 0, ret);
        else
        	return null;
    }
    
    public static int setScanTriggerMode(Device device, int uart, int mode) {
    	byte[] buf = new byte[64];
        return doConfig(device, uart, CMD_TRIGGER_MODE, buf, buf.length, QR_UART_TIMEOUT_MS);
    }
    
    
    public static int setRawData(Device device, int uart) {
        Log.d(TAG, "setRawData start");
        byte[] buf = new byte[256];
        return doConfig(device, uart, CMD_SET_DATA, buf, buf.length, QR_UART_TIMEOUT_MS);
    }

    public static int stopScan(Device device, int uart) {
    	Log.i( TAG, "stopScan");
    	
        byte[] buf = new byte[64];
        //UART.cancelIo(device, uart);
        return doConfig(device, uart, CMD_STOP, buf, buf.length, QR_UART_TIMEOUT_MS);
    }

    public static int scan(Device device, int uart, byte[] buf, int offset, int size,int outtime_ms) {
    	
    	UART.clearBuffer(device, uart);
        ControlHeader controlHeader = new ControlHeader();
        byte[] raw = new byte[1024];
        int ret = doControl(device, uart, CMD_SCAN, raw, raw.length, outtime_ms, controlHeader);
        if(controlHeader.type_ == 0x03) {
            if (ret < 5) {
                if (!device.isBroken())
                    return 0;    //Return 0 is just timeout, not error
                else
                    return -1;
            }

            int len = Utils.byte2short_be(raw, 3);
            if (5 + len >= ret) len = ret - 5;
            if (len > size) len = size;
            System.arraycopy(raw, 5, buf, offset, len);
            return len;
        }
        else{
            if (ret <= 0) {
                if (!device.isBroken())
                    return 0;    //Return 0 is just timeout, not error
                else
                    return -1;
            }

            int len = ret;
            if (len + 1 > size) len = size - 1;
            System.arraycopy(raw, 0, buf, offset, len);
            return len;
        }
    }

    public static String startScan(Device device, int uart, int ms) {
        byte[] buf = new byte[1024];
        int len = scan(device, uart, buf, 0, buf.length, ms);
        if (len >= 0)

            return new String(buf, 0, len);
        else
            return null;
    }

    public static boolean scannerExists(Device device, int uart) {
        if(device == null)
            return false;
        Log.d(TAG, "scannerExists start");
        UART.setConfig(device, uart, QR_UART_BAUDRATE, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
        UART.clearBuffer(device, uart);

        String version = getVersion(device, uart);
        if (version == null)
            return false;
        Log.d(TAG, "scannerExists end");
        return true;
    }

    public static void closeQRScanner(Device device){
        GPIO.output(device, BaseConfig.CONFIG_FRAME_2D_ENABLE, 0);
    }
}

