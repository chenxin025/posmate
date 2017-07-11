/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.cynoware.posmate.sdk.BleDevice;
import com.cynoware.posmate.sdk.Device;
import com.cynoware.posmate.sdk.HidDevice;
import com.cynoware.posmate.sdk.UsbHandler;
import com.newland.mesdk.device.BlueToothDevice;
import com.newland.mesdk.device.USBDevice;

public class ChannelManager {

	Context mContext;

	public static final int CHANNEL_DOCK_USB = 0;
	public static final int CHANNEL_DOCK_BT = 1;
	public static final int CHANNEL_TRAY_USB = 2;
	public static final int CHANNEL_TRAY_BT = 3;
	public static final int CHANNEL_CARD_USB = 4;
	public static final int CHANNEL_CARD_BT = 5;

	public BleDevice mDockBTDevice = null;
	public HidDevice mDockUSBDevice = null;
	public HidDevice mTrayUSBDevice = null;
	
	public BlueToothDevice mCardBTDevice;
    public USBDevice mCardUSBDevice;    

    private boolean mIsBusy = false;
    
	private Setting mSetting;
	
	private static final String TAG = "ChannelManager";
	
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbHandler.ACTION_USB_PERMISSION)) {
                synchronized (this) {
                	UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                	Log.i(TAG,  "USB Detached - " + device);
                    if(device != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) ){                      	
                       	onUSBDeviceDetected( device );
                    }
                    else {
                    	Toast.makeText( mContext, "USB Channel open failed for no access perssion", Toast.LENGTH_SHORT).show();
                    }
                }
            }else if( action.equals(UsbHandler.ACTION_USB_COMPLETE)){
            	onUSBDeviceDetectComplete();
            }else if(action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
            	UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                	Log.i(TAG,  "USB Detached - " + device);
                	onUSBDeviceAttached(device);                	
                }
            }else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
            	UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                	Log.i(TAG,  "USB Detached - " + device);
                	onUSBDeviceDetached(device);
                }
            }else if(action.equals(UsbHandler.ACTION_USB_SINGLE_PERMISSION) ){
            	UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                	Log.i(TAG,  "USB Detached - " + device);
                	onUSBSinglePermissionGranted(device);
                }
            }
            else if(action.equals(BleDevice.ACTION_BT_CONNECTED)){
            	onDockBTConnected();                
            }
        }
    };
    
    
    private void registerBroadcastReceiver(){
    	IntentFilter filter = new IntentFilter();
        filter.addAction(UsbHandler.ACTION_USB_PERMISSION);
        filter.addAction(UsbHandler.ACTION_USB_COMPLETE);
        filter.addAction(UsbHandler.ACTION_USB_SINGLE_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);        
        filter.addAction(BleDevice.ACTION_BT_SCANNED);
        filter.addAction(BleDevice.ACTION_BT_CONNECTED);
        
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    
	public ChannelManager(Context context) {
		
		mContext = context;
		mSetting = Setting.getInstance(context);

		mDockUSBDevice = new HidDevice(context);
		mTrayUSBDevice = new HidDevice(context);
		mDockBTDevice = new BleDevice((MainActivity) context);
		
		mCardBTDevice = new BlueToothDevice(context);		
        mCardUSBDevice = new USBDevice(context);
        
        registerBroadcastReceiver();

	}

	public Device getDevice(int channel) {
		switch (channel) {
		case CHANNEL_DOCK_USB:
			return mDockUSBDevice;

		case CHANNEL_DOCK_BT:
			return mDockBTDevice;

		case CHANNEL_TRAY_USB:
			return mTrayUSBDevice;
		}

		return null;
	}

	public void open(int channel) {
		switch (channel) {
		case CHANNEL_DOCK_USB:
		case CHANNEL_TRAY_USB:
			UsbHandler.detectUsb(mContext, mDockUSBDevice.mUsbHandler, mTrayUSBDevice.mUsbHandler);			
			break;

		case CHANNEL_DOCK_BT:
			String devName = mSetting.getDockBTName();
			String devAddr = mSetting.getDockBTAddr();
			if (devName.equals("N/A") || devAddr.isEmpty()) {
				Toast.makeText(mContext, "Dock BT channel open failed for no setup", Toast.LENGTH_SHORT).show();
				return;
			}

			// Toast.makeText(mContext, "Opening Dock BT channel", Toast.LENGTH_SHORT).show();
			mDockBTDevice.connect(devAddr);
			break;
			
		/*case CHANNEL_CARD_BT:
			new Thread( new Runnable(){
				@Override
				public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String name = mSetting.getCardBTName();
				    String addr = mSetting.getCardBTAddr();
					mCardBTDevice.init(name, addr);				
				}}).start();
		    
		    break;
		
		case CHANNEL_CARD_USB:
			//mCardUSBDevice.initController();
			new Thread( new Runnable(){
				@Override
				public void run() {
					mCardUSBDevice.connectDevice();					
				}}).start();
			
			break;*/
		}
	}
	
	
	public void onUSBDeviceDetected(UsbDevice device) {
		UsbHandler handler = UsbHandler.open(mContext, device);
		
		if( handler == null )
			return;
		
		int type = handler.getDevType();
		if( type == UsbHandler.DEV_DOCK ){
			mDockUSBDevice.setHandler(handler);			
			com.cynoware.posmate.sdk.Event.pollByCmd(mDockUSBDevice);
			broadcastChannelStatus( CHANNEL_DOCK_USB, 1 );
			UsbHandler.detectUsb(mContext, mDockUSBDevice.mUsbHandler, mTrayUSBDevice.mUsbHandler);
		} else if ( type == UsbHandler.DEV_TRAY ) {
			mTrayUSBDevice.setHandler(handler);
			//mTrayUSBDevice.startEventThread();
			com.cynoware.posmate.sdk.Event.pollByCmd(mTrayUSBDevice);
			broadcastChannelStatus( CHANNEL_TRAY_USB, 1 );
			UsbHandler.detectUsb(mContext, mDockUSBDevice.mUsbHandler, mTrayUSBDevice.mUsbHandler);
		}
	}
	
	public void onUSBSinglePermissionGranted(UsbDevice device) {
		UsbHandler handler = UsbHandler.open(mContext, device);		
		if( handler == null )
			return;
		
		int type = handler.getDevType();
		if( type == UsbHandler.DEV_DOCK ){
			mDockUSBDevice.setHandler(handler);			
			com.cynoware.posmate.sdk.Event.pollByCmd(mDockUSBDevice);
			broadcastChannelStatus( CHANNEL_DOCK_USB, 1 );
		} else if ( type == UsbHandler.DEV_TRAY ) {
			mTrayUSBDevice.setHandler(handler);
			//mTrayUSBDevice.startEventThread();
			com.cynoware.posmate.sdk.Event.pollByCmd(mTrayUSBDevice);
			broadcastChannelStatus( CHANNEL_TRAY_USB, 1 );
		}
	}
	
	
	
	public void onUSBDeviceDetectComplete() {
		mCardUSBDevice.initController();
		new Thread(new Runnable() {
			@Override
			public void run() {
				mCardUSBDevice.connect();
			}
		}).start();
	}
	
	
	private void onUSBDeviceDetached( UsbDevice device ){
		if( mDockUSBDevice != null && mDockUSBDevice.mUsbHandler != null ){
			if( mDockUSBDevice.mUsbHandler.mDevice.equals(device) ){
				mDockUSBDevice.setHandler( null );
				broadcastChannelStatus( ChannelManager.CHANNEL_DOCK_USB, 0);
			}
		}else if( mTrayUSBDevice != null && mTrayUSBDevice.mUsbHandler != null ){
			if( mTrayUSBDevice.mUsbHandler.mDevice.equals(device) ){
				mTrayUSBDevice.setHandler( null );
				broadcastChannelStatus( ChannelManager.CHANNEL_TRAY_USB, 0);
			}
		}
	}
	
	
	private void onUSBDeviceAttached( UsbDevice device ){
		UsbHandler.requestDevicePermission(mContext, device, 0x0416, 0xB000, 0, 0, 0, 2);
	}
	
	
	
	private void broadcastChannelStatus( int channel, int status ){
		Intent intent = new Intent( MainActivity.BROADCASTING_CHANNEL_STATUS); 
		intent.putExtra("channel", channel);
		intent.putExtra("status", status);
		mContext.sendBroadcast(intent);	
	}	

	
	public void onDockBTConnected(){
		
		 new Thread() {
             public void run() {
                 com.cynoware.posmate.sdk.Event.pollByCmd(mDockBTDevice);
             }
         }.start();
         
         broadcastChannelStatus( CHANNEL_DOCK_BT, 1 );         
	}
	
	
	public void close( int channel ) {
		switch (channel) {
		case CHANNEL_DOCK_USB:
			mDockUSBDevice.close();
			broadcastChannelStatus( CHANNEL_DOCK_USB, 0 );
			// Toast.makeText( mContext, "Dock USB Channel closed", Toast.LENGTH_SHORT ).show();
			break;
			
		case CHANNEL_TRAY_USB:
			mTrayUSBDevice.close();
			broadcastChannelStatus( CHANNEL_TRAY_USB, 0 );
			break;

		case CHANNEL_DOCK_BT:
			mDockBTDevice.close();
			broadcastChannelStatus( CHANNEL_DOCK_BT, 0 );
			break;
		}
		
	}

	public void onCreate() {
		if( mSetting.getChannelEnable(CHANNEL_DOCK_USB)){
    		open(ChannelManager.CHANNEL_DOCK_USB);
    	}
    	
		mDockBTDevice.onCreate();
		
    	if( mSetting.getChannelEnable(CHANNEL_DOCK_BT)){
    		open(CHANNEL_DOCK_BT);
    	}
    	
    	//if( mSetting.getChannelEnable(CHANNEL_CARD_BT))
    	//open(CHANNEL_CARD_BT);
    	//open(CHANNEL_CARD_USB);
	}
	

	public void onPause() {
		try {
			if (mDockBTDevice != null)
				mDockBTDevice.onPause();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onResume() {
		try {
			if (mDockBTDevice != null)
				mDockBTDevice.onResume();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onDestroy() {
		try {
			if (mDockBTDevice != null) {
				mDockBTDevice.onDestroy();
				mDockBTDevice.close();
			}

			if (mDockUSBDevice != null)
				mDockUSBDevice.close();

			if (mTrayUSBDevice != null)
				mTrayUSBDevice.close();
			
			if( mCardBTDevice != null )
				mCardBTDevice.disconnect();
			
			if( mCardUSBDevice != null )
				mCardBTDevice.disconnect();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		mContext.unregisterReceiver(mBroadcastReceiver);
	}
	
	
	
	
	public boolean isBusy(){
		return mIsBusy;
	}
	
	
	/**
	 * 可能造成阻塞，不可在UI Thread中调用
	 * TODO 应考虑退出时可能仍在通信
	 */
	public synchronized void setBusy( boolean busy ){
		mIsBusy = busy;
		
		Intent intent = new Intent( MainActivity.BROADCASTING_BUSY_STATUS); //String BROADCASTING_BUSY_STATUS = "busy_status"
		intent.putExtra("busy", busy);
		mContext.sendBroadcast(intent);	
	}
	
}
