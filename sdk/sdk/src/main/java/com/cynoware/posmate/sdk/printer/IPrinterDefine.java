package com.cynoware.posmate.sdk.printer;

import android.graphics.Bitmap;

import com.cynoware.posmate.sdk.io.Device;

/**
 * Created by john on 2016/10/10.
 */

public interface IPrinterDefine {

    public void doConfig(Device device, int uart, int baudrate, int dataBits, int parity, int stopBits, int flowCtrl);

    public boolean checkPaper(Device device, int uart);

    public void printBarcode(Device device, int uart, String str );

    public void printChar(Device device, int uart, byte[] text);

    public void printBitmap(Device device, int uart, final int whichType, final Bitmap bmp);

    public void downloadNVBitmap(Device device, int uart, Bitmap bmp);
}
