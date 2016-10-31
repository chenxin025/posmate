package com.cynoware.firmwareupdate;

/**
 * 作用：枚举APROM(正常模式)下的设备，连接并获取UsbDevice信息
 * <p>
 * 240:VID=1046,PID=45056
 * 123:VID=1046,PID=45056
 * <p>
 * 过程：枚举设备->找到设备的接口->分配相应的端点->连接设备->在IN端点进行读操作，在OUT端点进行写操作
 * IN 输入 0 读，OUT 输出 1 写
 * <p>
 * 经测试知Aprom(正常模式)下，240(Dock)和123(Tray)板子均有两个接口，并且每个接口只有一个端点
 * Ldrom(更新模式)下，240(Dock)板子找不到设备，123(Tray)板子只有一个接口，这个接口有两个端点
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class OpenApDev {

    // 连接USB
    public final int VendorID = 1046;
    public final int ApromProductID = 45056;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    Context mContext;
    private static final String TAG = "USB_HOST_IAIOT";
    private UsbManager mUsbManager;
    DeviceModel mApDockDev = new DeviceModel();
    DeviceModel mApTrayDev = new DeviceModel();

    public OpenApDev(Context context) {
        this.mContext = context;
    }


    public void tryGetUsbPermission(Handler handler) {
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (mUsbManager == null) {
            return;
        }
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(TAG, "deviceList.size():" + deviceList.size());
        if (!deviceList.isEmpty()) {
            for (UsbDevice usbDevice : deviceList.values()) {
                if (usbDevice.getVendorId() == VendorID && usbDevice.getProductId() == ApromProductID) {
                    Log.d(TAG, "枚举设备成功");
                    if (mUsbManager.hasPermission(usbDevice)) {
                        Log.d(TAG, "Got permission for usb device: " + usbDevice);
                        findIntfAndEpt(handler, usbDevice);
                    } else {
                        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "Got permission for usb device: " + usbDevice);
                        findIntfAndEpt(handler, usbDevice);
                    }
                } else {
                    Log.d(TAG, "不合适的：VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId());
                }
            }
        } else {
            Log.d(TAG, "未枚举到设备");
        }
    }

    // 寻找设备的接口和通信端点
    public void findIntfAndEpt(Handler handler, UsbDevice mUsbDevice) {
        if (mUsbDevice == null) {
            Log.d(TAG, "UsbDevice is null");
            return;
        }
        UsbInterface usbInInterface = mUsbDevice.getInterface(0);
        UsbInterface usbOutInterface = mUsbDevice.getInterface(1);
        UsbEndpoint epIn = usbInInterface.getEndpoint(0);
        UsbEndpoint epOut = usbOutInterface.getEndpoint(0);
        openDevice(usbInInterface, usbOutInterface, epIn, epOut, handler, mUsbDevice);
    }

    // 打开设备
    private void openDevice(UsbInterface mInInterface, UsbInterface mOutInterface, UsbEndpoint epIn, UsbEndpoint epOut, Handler handler, UsbDevice usbDevice) {
        UsbDeviceConnection connection = null;
        if (mUsbManager.hasPermission(usbDevice)) {
            // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
            connection = mUsbManager.openDevice(usbDevice);
            if (connection == null) {
                return;
            }
            // 声明独占访问Usb接口,这必须在发送或接收属于该接口的任何Usb端点上的数据之前完成。
            if (connection.claimInterface(mInInterface, true)) {
                Log.d(TAG, "打开设备成功");
                if ("PosMate".equals(getDeviceName(connection))) {
                    mApDockDev.setUsbDevice(usbDevice);
                    mApDockDev.setUsbDevConn(connection);
                    Log.d(TAG, "PosMate(Dock) is OK");
                } else if ("PMFrame".equals(getDeviceName(connection))) {
                    mApTrayDev.setUsbDevice(usbDevice);
                    mApTrayDev.setUsbDevConn(connection);
                    Log.d(TAG, "PMFrame(Tray) is OK");
                } else {
                    Log.d(TAG, "DeviceName is other：" + getDeviceName(connection));
                }
            } else {
                Log.d(TAG, "打开设备失败");
                connection.close();
            }
        } else {
            Log.d(TAG, "没有权限");
            handler.sendEmptyMessage(4);
        }
    }


    // 根据DeviceName判断是Dock还是Tray
    public static String getDeviceName(UsbDeviceConnection conn) {
        String str = "";
        byte[] buf = new byte[32];
        buf[0] = 2;
        buf[1] = 9;
        int res = setFeature(conn, buf);
        Log.d(TAG, "============" + res);
        if (res < 0) {
            return str;
        } else {
            res = getFeature(conn, (byte) 2, buf);
            if (res < 0) {
                return str;
            } else {
                try {
                    int ex;
                    for (ex = 8; ex < buf.length && buf[ex] != 0; ++ex) {
                        ;
                    }

                    str = new String(buf, 8, ex - 8, "UTF-8");
                } catch (UnsupportedEncodingException var5) {
                    ;
                }

                return str;
            }
        }
    }

    static int getFeature(UsbDeviceConnection conn, byte reportID, byte[] buf) {
        buf[0] = reportID;
        int res = conn.controlTransfer(161, 1, 770, 0, buf, buf.length, 8192);
        return res;
    }

    private static int setFeature(UsbDeviceConnection connection, byte[] buf) {
        int res = connection.controlTransfer(33, 9, 770, 0, buf, buf.length, 8192);
        return res;
    }
}
