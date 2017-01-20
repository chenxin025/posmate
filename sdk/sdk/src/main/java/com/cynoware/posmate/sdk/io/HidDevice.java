package com.cynoware.posmate.sdk.io;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.cmd.Cmds;

import java.nio.ByteBuffer;

public class HidDevice extends Device {

	private static final int HID_EVENT_SIZE = 9;

	private static final String TAG = "HidDevice";
	private Thread mEventThread;
	private boolean mIsBroken = false;
	public UsbHandler mUsbHandler;
	private Context mContext;
	

	public HidDevice(Context context) {
		super();
		mEventThread = null;
		mContext = context;
		mIsBroken = false;
	}
	
	
	public int writeData(byte[] data, int length) {

		UsbManager usbManager = (UsbManager)mContext
				.getSystemService(Context.USB_SERVICE);

		///UsbDeviceConnection conn = usbManager.openDevice(mUsbHandler.mDevice);
		//boolean flag = usbManager.hasPermission(mUsbHandler.mDevice);
		//Log.i("cf","broken ######################"+flag+ "    conn="+conn);
		if ( !isConnected() )
			return -1;


		int res = mUsbHandler.setFeature(data);
		
		if (res < 0) {
			Log.i("cf", "===broken in writeData");
			mIsBroken = true;
		}
		
		return res;
	}

	
	public int readData(byte[] data) {
		if ( !isConnected() )
			return -1;
		
		int res = mUsbHandler.getFeature(Cmds.CMD_REPORT_ID, data);
		
		if (res < 0) {
			Log.i(TAG, "===broken in readData");
			mIsBroken = true;
		}
		
		return res;
	}

	
	/* 等待设备事件，返回事件buffer长度 */
	public int waitEvent(byte[] data) {
		if ( !isConnected() )
			return -1;
		
		ByteBuffer buffer = ByteBuffer.allocate(HID_EVENT_SIZE);
		
		if ( mUsbHandler.mRequestEvent.queue(buffer, buffer.capacity())) {
			UsbRequest res = mUsbHandler.mDevConn.requestWait();
			if (res == null)
				mIsBroken = true;
			
			else if (res == mUsbHandler.mRequestEvent) {
				if (buffer.position() != 0)
					buffer.flip();
				if (buffer.limit() >= data.length) {
					buffer.get(data);
					return data.length;
				} else
					mIsBroken = true;
			}
		} else
			mIsBroken = true;

		return -1;
	}


	/* 检查连接状态 */
	public boolean isBroken() {
		return mIsBroken;
	}

	

	
	/*void eventThread() {
		while (!getCloseFlag() ) {
			Event.eventPoll(this);
		}
	}*/

	
	/**
	 * 创建Event Thread
	 */
	public void startEventThread() {
		Thread eventThread = null;
		SDKLog.i(TAG,"$$$$$$$$$$$$$$$$$$$$$$$$"+mEventThread);
		Thread.dumpStack();
		synchronized (this) {
			if (mEventThread == null) {
				
				mEventThread = new Thread() {
					public void run() {

						//SDKLog.i(TAG,"$$$$$$$$$$$$$$$$$$$$$$$$"+Thread.dumpStack());
						SDKLog.i(TAG, "====================> getCloseFlag()"+getCloseFlag()+ "     isConnected()= "+isConnected() );
						while (!getCloseFlag() && isConnected() ) {
							//Event.eventPoll(HidDevice.this);
							
							ByteBuffer buffer = ByteBuffer.allocate(HID_EVENT_SIZE);
							
							byte[] data = new byte[Cmds.MAX_EVENT_SIZE + 1];
							int size = -1;
							SDKLog.i(TAG,"**************usb event 11****************");
							if ( mUsbHandler.mRequestEvent.queue(buffer, buffer.capacity())) {
								SDKLog.i(TAG,"**************usb event 22****************");
								UsbRequest res = mUsbHandler.mDevConn.requestWait();
								SDKLog.i(TAG,"**************usb event 33****************");
								if (res == null || mUsbHandler == null )
									mIsBroken = true;
								
								else if (res == mUsbHandler.mRequestEvent) {
									if (buffer.position() != 0)
										buffer.flip();
									if (buffer.limit() >= data.length) {
										buffer.get(data);
										size = data.length;
									} else
										mIsBroken = true;
								}
							} else
								mIsBroken = true;

							if( size != -1 )
								Event.onEventPoll(HidDevice.this, data, size);
						}
					}
				};
				
				eventThread = mEventThread;
			}
		}

		if (eventThread != null)
			eventThread.start();
	}
	
	
	
	private void stopEventThread() {
		Thread eventThread;
		synchronized (this) {
			eventThread = mEventThread;
			mEventThread = null;
		}

		if (eventThread != null) {
			setCloseFlag(true);
			
			Event.setEvent(this, Cmds.kConnectionClosed, 0);
			
			synchronized (this) {
				notifyAll();
			}
			
			//closeHandler();
			/*try {
				eventThread.join();
			} catch (InterruptedException ex) {
			}*/
		}
	}
	
	/*
	 * Set the accepted handler for further USB transfer.
	 */
	public void setHandler( UsbHandler handler) {
		mUsbHandler = handler;
		mIsBroken = false;
		startEventThread();
	}

	
	public void closeHandler(){
		stopEventThread();
		
		if( mUsbHandler != null ){
			mUsbHandler.close();
			mUsbHandler = null;
		}
	}
	
	public void close() {			
		closeHandler();
	}
	
	
	/*
	 * Check if the USB device is connected or not.
	 */
	public boolean isConnected() {
		SDKLog.i("abc","mUsbHandler="+mUsbHandler + "   mIsBroken="+mIsBroken);
		return mUsbHandler != null && mIsBroken == false;
	}
};
