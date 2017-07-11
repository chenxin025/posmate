package com.cynoware.posmate.sdk.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import com.cynoware.posmate.sdk.SDKInfo;
import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.configs.DeviceConfigInfo;
import com.cynoware.posmate.sdk.configs.PosConstant;
import com.cynoware.posmate.sdk.io.BleDevice;
import com.cynoware.posmate.sdk.io.HidDevice;
import com.cynoware.posmate.sdk.io.UsbHandler;
import com.cynoware.posmate.sdk.listener.ResponseCallBack;
import com.cynoware.posmate.sdk.listener.ResultCallBack;
import com.cynoware.posmate.sdk.util.SharePrefManager;

import static com.cynoware.posmate.sdk.io.UsbHandler.*;

/**
 * Created by john on 2017/1/11.
 */

public class PosService extends Service {


    private static final String TAG = "PosService";

    private OnStatusListener mOnStatusListener = null;

    public BleDevice mDockBTDevice = null;
    public HidDevice mDockUSBDevice = null;
    public HidDevice mTrayUSBDevice = null;


    //UsbManager.ACTION_USB_DEVICE_DETACHED  notify when device first discover usb
    //If app is restarted, is not send nofity with ACTION_USB_DEVICE_DETACHED
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent){
                return;
            }

            String action = intent.getAction();
            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Log.i("sdk","=================BroadcastReceiver:onReceive======="+action);
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){

                if (device != null){
                    onUSBDeviceAttached(device);
                }

            }else if (action.equals(UsbHandler.ACTION_USB_SINGLE_PERMISSION)){

                //User accept permisson with device
                if (device != null){
                    onUSBSinglePermissionGranted(device);
                }
            }else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){

                //Device leave usb
                if(device != null) {
                    onUSBDeviceDetached(device);
                }
            }else  if (action.equals(UsbHandler.ACTION_USB_PERMISSION)){

                synchronized (this) {
                    Log.i(TAG,  "USB Detached - " + device);
                    if(device != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) ){
                        onUSBDeviceDetected( device );
                    }
                    else {
                        //Toast.makeText( this, "USB Channel open failed for no access perssion", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };



    public void setOnStatusListener(OnStatusListener listener){
        this.mOnStatusListener = listener;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        DeviceManager.openTask(this);

        //if (SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SET,2) == SDKInfo.POS_SET_NP10) {
            mDockUSBDevice = (HidDevice) DeviceManager.getInstance(this)
                    .getDevice(PosConstant.CHANNEL_DOCK_USB);
            mTrayUSBDevice = (HidDevice) DeviceManager.getInstance(this)
                    .getDevice(PosConstant.CHANNEL_TRAY_USB);
            mDockBTDevice = (BleDevice) DeviceManager.getInstance(this)
                    .getDevice(PosConstant.CHANNEL_DOCK_BT);

            //regiser broadcat
            registerDeviceReceiver();

            //open dock tray and ble
            openDevice(PosConstant.CHANNEL_DOCK_USB);
            openDevice(PosConstant.CHANNEL_TRAY_USB);
        //}

    }

    public void closeDevice(final int type){
        switch (type){
            case PosConstant.CHANNEL_DOCK_BT:
                if (mDockBTDevice != null){
                    mDockBTDevice.close();
                }
                break;
            case PosConstant.CHANNEL_DOCK_USB:
                if (mDockUSBDevice != null){
                    mDockUSBDevice.close();
                }
                break;

            case PosConstant.CHANNEL_TRAY_USB:
                if (mTrayUSBDevice != null){
                    mTrayUSBDevice.close();
                }
                break;
            default:
                break;
        }
    }

    private void openDevice(final int type){
        switch (type){
            case PosConstant.CHANNEL_DOCK_USB:
            case PosConstant.CHANNEL_TRAY_USB:
                detectUsb(this,mDockUSBDevice.mUsbHandler,mTrayUSBDevice.mUsbHandler);
                break;

            case PosConstant.CHANNEL_DOCK_BT:
                String devName = DeviceManager.getInstance(this).getDockBleName(this);
                String devAddr = DeviceManager.getInstance(this).getDockBleAddr(this);
                if (devName.equals("N/A") || devAddr.isEmpty()) {
                    if (mOnStatusListener != null){
                        mOnStatusListener.onBleOpenFailed();
                    }
                    //Toast.makeText(mContext, "Dock BT channel open failed for no setup", Toast.LENGTH_SHORT).show();
                    return;
                }
                mDockBTDevice.connect(devAddr,mOnStatusListener);
                break;

            default:
                break;
        }
    }


    private void registerDeviceReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_COMPLETE);
        filter.addAction(ACTION_USB_SINGLE_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(com.cynoware.posmate.sdk.io.BleDevice.ACTION_BT_SCANNED);
        filter.addAction(com.cynoware.posmate.sdk.io.BleDevice.ACTION_BT_CONNECTED);

        registerReceiver(mBroadcastReceiver, filter);
    }

    public void onUSBDeviceDetected(UsbDevice device) {
        UsbHandler handler = UsbHandler.open(this, device);

        if( handler == null )
            return;

        int type = handler.getDevType();
        if( type == UsbHandler.DEV_DOCK ){
            SDKLog.i(TAG,"========================>>>>>>>> 3");
            mDockUSBDevice.setHandler(handler);
            com.cynoware.posmate.sdk.io.Event.pollByCmd(mDockUSBDevice);
            SDKLog.i(TAG,"========================>>>>>>>> 4======="+mOnStatusListener);
            Log.i("sdk","========================>>>>>>>> 5");
            if(mOnStatusListener != null){
                mOnStatusListener.onDockUsbAttached();
            }
            UsbHandler.detectUsb(this, mDockUSBDevice.mUsbHandler, mTrayUSBDevice.mUsbHandler);
        } else if ( type == UsbHandler.DEV_TRAY ) {
            mTrayUSBDevice.setHandler(handler);
            com.cynoware.posmate.sdk.io.Event.pollByCmd(mTrayUSBDevice);
            if(mOnStatusListener != null){
                mOnStatusListener.onTrayUsbAttached();
            }
            UsbHandler.detectUsb(this, mDockUSBDevice.mUsbHandler, mTrayUSBDevice.mUsbHandler);
        }
    }

    private void onUSBDeviceAttached(UsbDevice device){
        UsbHandler.requestDevicePermission(this,device, 0x0416, 0xB000, 0, 0, 0, 2);
    }

    private void onUSBDeviceDetached( UsbDevice device ){
        if( mDockUSBDevice != null && mDockUSBDevice.mUsbHandler != null ){
            if( mDockUSBDevice.mUsbHandler.mDevice.equals(device) ){
                SDKLog.i(TAG,"onUSBDeviceDetached         ===============> 1");
                mDockUSBDevice.setHandler( null );

                if (mOnStatusListener != null){
                    mOnStatusListener.onDockUsbDetached();
                }
                //broadcastChannelStatus( ChannelManager.CHANNEL_DOCK_USB, 0);
            }
        }else if( mTrayUSBDevice != null && mTrayUSBDevice.mUsbHandler != null ){
            if( mTrayUSBDevice.mUsbHandler.mDevice.equals(device) ){
                mTrayUSBDevice.setHandler( null );

                if (mOnStatusListener != null){
                    mOnStatusListener.onTrayUsbDetached();
                }
                //broadcastChannelStatus( ChannelManager.CHANNEL_TRAY_USB, 0);
            }
        }
    }

    public void onUSBSinglePermissionGranted(UsbDevice device) {
        UsbHandler handler = UsbHandler.open(this, device);
        if( handler == null )
            return;

        int type = handler.getDevType();
        if( type == com.cynoware.posmate.sdk.io.UsbHandler.DEV_DOCK ){
            SDKLog.i(TAG,"========================>>>>>>>> 2");
            mDockUSBDevice.setHandler(handler);
            com.cynoware.posmate.sdk.io.Event.pollByCmd(mDockUSBDevice);
            //broadcastChannelStatus( CHANNEL_DOCK_USB, 1 );
            if(mOnStatusListener != null){
                mOnStatusListener.onDockUsbAttached();
            }
        } else if ( type == com.cynoware.posmate.sdk.io.UsbHandler.DEV_TRAY ) {
            mTrayUSBDevice.setHandler(handler);
            //mTrayUSBDevice.startEventThread();
            com.cynoware.posmate.sdk.io.Event.pollByCmd(mTrayUSBDevice);
            //broadcastChannelStatus( CHANNEL_TRAY_USB, 1 );
            if(mOnStatusListener != null){
                mOnStatusListener.onTrayUsbAttached();
            }
        }
    }

    public void printText(byte[] str, ResponseCallBack responseCallBack, Handler handler){
        DeviceManager.getInstance(this).sendPrintTextMsg(str,responseCallBack,handler);
    }

    public void showLedText(int comId, int mode, String text, ResponseCallBack responseCallBack, Handler handler){

        DeviceManager.getInstance(this).sendLedMsg(comId,mode,text, responseCallBack, handler);
    }


    public void startScanner(ResultCallBack callBack,Handler handler){
        DeviceManager.getInstance(this).sendStartScannerMsg(callBack,handler);
    }

    public void closeScanner(){
        DeviceManager.getInstance(this).closeScannerMsg();
    }

    public void openCachDrawer(ResponseCallBack responseCallBack, Handler handler){
        DeviceManager.getInstance(this).sendOpenDrawerMsg(responseCallBack,handler);
    }

    public int[] getComsInfo(){
        return DeviceManager.getInstance(this).getComIds();
    }

    public void openBle(OnStatusListener listener){
        mOnStatusListener = listener;
        openDevice(PosConstant.CHANNEL_DOCK_BT);
    }

    public void setPosSet(final int type){
        SharePrefManager manager = SharePrefManager.getInstance();
        manager.putInt(SDKInfo.PREF_POS_SET,type);
        switch (type){
            case SDKInfo.POS_SET_NP10:
                //TODO close P140 devices
                DeviceManager.getInstance(this).setmLed(null);
                DeviceManager.getInstance(this).setmCahCashDrawer(null);
                break;
            case SDKInfo.POS_SET_NP11:
                DeviceManager.getInstance(this).setmLed(null);
                DeviceManager.getInstance(this).setmCahCashDrawer(null);
                break;
            case SDKInfo.POS_SET_P140:
                DeviceManager.getInstance(this).setmLed(null);
                DeviceManager.getInstance(this).setmCahCashDrawer(null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {

        DeviceManager.getInstance(this).onDestory();
        DeviceManager.getInstance(this).dstroyInstance();
        super.onDestroy();
        if (mDockUSBDevice != null){
            mDockUSBDevice.close();
        }

        if (mTrayUSBDevice != null){
            mTrayUSBDevice.close();
        }
        if (null != mBroadcastReceiver) {
            //if (SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SET,2) == SDKInfo.POS_SET_NP10) {
                unregisterReceiver(mBroadcastReceiver);
            //}
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public PosService getService(){
            return PosService.this;
        }
    }
}
