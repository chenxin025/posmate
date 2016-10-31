package com.cynoware.firmwareupdate;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.HashMap;

/**
 * USB相关方法类
 */

public class DevUtils {
    private UsbManager mUsbManager;
    Context mContext;
    HashMap<String, UsbDevice> deviceList = null;
    private static final String TAG = "USB_HOST_IAIOT";
    public final int VendorID = 1046;
    public final int LdromProductID = 41750;
    public boolean isConned = false;

    public DevUtils(Context context) {
        this.mContext = context;
    }

    // 判断是否成功切换到更新模式，因为切换需要一段时间，所以加入延时
    public boolean isfindlddev() {
        int i;
        // 设置超时100s，100s还没有找到就返回连接失败
        for (i = 0; i < 100; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            deviceList = mUsbManager.getDeviceList();
            if (!deviceList.isEmpty()) {
                for (UsbDevice usbDevice : deviceList.values()) {
                    if (usbDevice.getVendorId() == VendorID && usbDevice.getProductId() == LdromProductID) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
