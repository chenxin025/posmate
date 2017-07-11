package com.cynoware.posmate.sdk.qrreader;

import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.GPIO;
import com.cynoware.posmate.sdk.qrreader.QrReader;
import com.cynoware.posmate.sdk.io.UART;
import com.cynoware.posmate.sdk.util.Utils;

import static com.cynoware.posmate.sdk.qrreader.QrReader.CMD_SCAN;


/**
 * Created by john on 2016/9/20.
 */

public class NP10ScannerImpl implements IScannerDefine {

    public static final int TriggerMode_PressKey    = 0x00;
    public static final int TriggerMode_Continuous  = 0x01;
    public static final int TriggerMode_Auto        = 0x02;
    public static final int TriggerMode_Respond     = 0x03;
    public static final int TriggerMode_Wave        = 0x04;

    public static final int QR_UART_TIMEOUT_MS = 1000;

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

    @Override
    public void closeQRScanner(Device device) {
        GPIO.output(device, BaseConfig.CONFIG_FRAME_2D_ENABLE, 0);
    }

    @Override
    public boolean isExistScanner(Device device, int uart) {
        return false;
    }

    @Override
    public void initScanner(Device device, int uart) {
        GPIO.output(device, BaseConfig.CONFIG_FRAME_2D_ENABLE, 1);
        UART.setConfig(device, uart,115200, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_NONE);
        QrReader.setScanTriggerMode(device, uart, TriggerMode_PressKey);
    }

    @Override
    public String startScan(Device device, int uart, int ms) {
        byte[] buf = new byte[1024];
        int len = doScan(device, uart, buf, 0, buf.length, ms);
        if (len >= 0)

            return new String(buf, 0, len);
        else
            return null;
    }

    public static int doScan(Device device, int uart, byte[] buf, int offset, int size,int outtime_ms) {

        UART.clearBuffer(device, uart);
        NP10ScannerImpl.ControlHeader controlHeader = new NP10ScannerImpl.ControlHeader();
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

    public static int doControl(Device device, int uart, byte[] sendBuf, byte[] recvBuf, int maxRecvSize,
                         int timeout_ms,
                         NP10ScannerImpl.ControlHeader controlHeader) {
        //Log.d(TAG, "doControl start");
        int ret = UART.fixedWrite(device, uart, sendBuf);
        if (ret < 0)
            return ret;
        //Log.d(TAG, "doControl read start");

        byte[] header = new byte[3];
        ret = UART.fixedRead(device, uart, header, 0, 1, timeout_ms);

        if (ret != 1)
            return ret;
        //Log.d(TAG, "header compare");

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
}
