package com.cynoware.firmwareupdate;

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

import java.util.HashMap;

/**
 * 作用：枚举LDROM(更新模式)下的设备，连接并获取UsbDevice信息
 * <p>
 * 问题：将240(DOCK)引导至LDROM(更新模式)后获取不到设备，123(TRAY)正常
 */

public class OpenLdDev {

    public final int VendorID = 1046;
    public final int LdromProductID = 41750;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    Context mContext;
    private static final String TAG = "USB_HOST_IAIOT";
    private UsbManager mUsbManager;

    DeviceModel mLdDev = new DeviceModel();

    public OpenLdDev(Context context) {
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
        if (!deviceList.isEmpty()) {
            for (UsbDevice usbDevice : deviceList.values()) {
                if (usbDevice.getVendorId() == VendorID && usbDevice.getProductId() == LdromProductID) {
                    Log.d(TAG, "枚举41750设备成功");
                    if (mUsbManager.hasPermission(usbDevice)) {
                        Log.d(TAG, "Found usb device: " + usbDevice);
                        findIntfAndEpt(handler, usbDevice);
                    } else {
                        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "Found usb device: " + usbDevice);
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
        UsbInterface usbInterface = mUsbDevice.getInterface(0);
        UsbEndpoint epIn = usbInterface.getEndpoint(0);
        UsbEndpoint epOut = usbInterface.getEndpoint(1);
        openDevice(usbInterface, epIn, epOut, handler, mUsbDevice);
    }

    // 打开设备
    private void openDevice(UsbInterface mInterface, UsbEndpoint epIn, UsbEndpoint epOut, Handler handler, UsbDevice usbDevice) {
        UsbDeviceConnection connection = null;
        if (mUsbManager.hasPermission(usbDevice)) {
            // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
            connection = mUsbManager.openDevice(usbDevice);
            if (connection == null) {
                return;
            }
            /*UsbRequest request = new UsbRequest();
            request.initialize(connection, epOut);
            //request.setClientData(mContext);
            UsbRequest request2 = new UsbRequest();
            request2.initialize(connection, epIn);
            //request2.setClientData(mContext);*/
            // 声明独占访问Usb接口,这必须在发送或接收属于该接口的任何Usb端点上的数据之前完成。
            if (connection.claimInterface(mInterface, true)) {
                Log.d(TAG, "打开41750设备成功");
                mLdDev.setUsbInterface(mInterface);
                mLdDev.setUsbDevice(usbDevice);
                mLdDev.setUsbEPOut(epOut);
                mLdDev.setUsbEPIn(epIn);
                mLdDev.setUsbDevConn(connection);
            } else {
                Log.d(TAG, "打开41750设备失败");
                connection.close();
            }
        } else {
            Log.d(TAG, "没有权限");
            handler.sendEmptyMessage(4);
        }
    }
}
