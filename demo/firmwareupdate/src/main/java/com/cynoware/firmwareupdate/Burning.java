package com.cynoware.firmwareupdate;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 数据准备及烧写过程
 */

public class Burning {

    private static final String TAG = "USB_HOST_IAIOT";
    FileUtils fileUtils = new FileUtils();

    // burning
    int MAX_PACKET = 32;        //64  32
    int writed = 0;
    byte[] WriteReportBuffer = new byte[32];    //256  32
    byte[] readbuf = new byte[256];
    int startCpIndex = 0;
    byte g_packno = 1;
    int ret = -2;
    int readret = -2;

    // 控制传输写数据
    public int WriteData(UsbDeviceConnection conn, byte[] buf) {
        int ret = conn.controlTransfer(32 | 0 | 0x01, 0x09, 0x0302, 0x00, buf, buf.length, 0x2000);
        return ret;
    }

    // 控制传输读数据
    public int ReadData(UsbDeviceConnection conn, byte[] buf) {
        int ret = conn.controlTransfer(32 | 0x80 | 0x01, 0x01, 0x0302, 0x00, buf, buf.length, 0x2000);
        return ret;
    }

    // 批量传输写数据
    public int BulkWriteData(UsbDeviceConnection conn, byte[] buf, UsbEndpoint epOut) {
        int ret = conn.bulkTransfer(epOut, buf, buf.length, 0x2000);
        return ret;
    }

    // 批量传输读数据
    public int BulkReadData(UsbDeviceConnection conn, byte[] buf, UsbEndpoint epIn) {
        int ret = conn.bulkTransfer(epIn, buf, buf.length, 0x2000);
        return ret;
    }

    // 整理待烧写数据
    public void startBurning(UsbDeviceConnection connection, UsbEndpoint epOut, UsbEndpoint epIn, byte[] data, Handler handler) {
        synchronized (connection) {
            Log.d(TAG, "epOut.getType()=" + epOut.getType() + ",epIn.getType()=" + epIn.getType());
            Log.d(TAG, "StartBurning ");
            fileUtils.clearArr(WriteReportBuffer, WriteReportBuffer.length);
            //fileUtils.clearArr(WriteReportBuffer, 64);
            WriteReportBuffer[1] = (byte) 0XA4; //164
            WriteReportBuffer[5] = g_packno;
            WriteReportBuffer[9] = 1;
            ret = WriteData(connection, WriteReportBuffer);
            //ret = BulkWriteData(connection, WriteReportBuffer, epOut);
            Log.d(TAG, "Burning01:" + ret);
//            readret = BulkReadData(connection, readbuf, epIn);
//            int i = 0;
//            for (Byte byte1 : readbuf) {
//                System.err.println("byte1[" + i++ + "]=" + byte1);
//            }
            ret = -2;
            g_packno += 2;

            fileUtils.clearArr(WriteReportBuffer, WriteReportBuffer.length);
            //fileUtils.clearArr(WriteReportBuffer, 64);
            WriteReportBuffer[1] = (byte) 0XA0; //160
            WriteReportBuffer[5] = g_packno;
            WriteReportBuffer[9] = 0;
            WriteReportBuffer[13] = fileUtils.getByteHighLowBit(fileUtils.filelen, 13);
            WriteReportBuffer[14] = fileUtils.getByteHighLowBit(fileUtils.filelen, 14);

            System.arraycopy(data, 0, WriteReportBuffer, 17, 15);     //47   15

            ret = WriteData(connection, WriteReportBuffer);
            //ret = BulkWriteData(connection, WriteReportBuffer, epOut);
            Log.d(TAG, "Burning02:" + ret);
            ret = -2;
            writed = 15;    //47  15
            startCpIndex = 15;      //47  15

            while (writed < fileUtils.filelen) {
                g_packno += 2;
                fileUtils.clearArr(WriteReportBuffer, WriteReportBuffer.length);
                //fileUtils.clearArr(WriteReportBuffer, 64);
                WriteReportBuffer[5] = (byte) g_packno;
                //arraycopy(src, srcPos, dest, destPos, length);
                if (fileUtils.filelen - writed > MAX_PACKET - 9) {
                    System.arraycopy(data, startCpIndex, WriteReportBuffer, 9, MAX_PACKET - 9);
                    writed += (MAX_PACKET - 9);
                    startCpIndex += (MAX_PACKET - 9);
                    burnAndSetProgress(connection, epOut, epIn, writed * 100 / fileUtils.filelen, handler);
                } else {
                    System.arraycopy(data, startCpIndex, WriteReportBuffer, 9, fileUtils.filelen - writed);
                    writed = fileUtils.filelen;
                    startCpIndex = fileUtils.filelen;
                    burnAndSetProgress(connection, epOut, epIn, 100, handler);
                    handler.sendEmptyMessage(5);
                }
            }
        }
    }

    // 烧写并实时更新进度条
    private void burnAndSetProgress(UsbDeviceConnection connection, UsbEndpoint epOut, UsbEndpoint epIn, int nCurProgress, Handler handler) {
        ret = WriteData(connection, WriteReportBuffer);
        //ret = BulkWriteData(connection, WriteReportBuffer, epOut);
        Log.d(TAG, "Burning:" + ret);
        ret = -2;
        Message msg = new Message();
        msg.arg1 = nCurProgress;
        msg.what = 2;
        handler.sendMessage(msg);
    }

}
