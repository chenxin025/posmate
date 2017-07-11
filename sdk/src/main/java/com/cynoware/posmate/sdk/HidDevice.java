package com.cynoware.posmate.sdk;

import android.content.Context;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import java.nio.ByteBuffer;

public class HidDevice extends Device {	
	
	private static final int HID_EVENT_SIZE = 9;
	
	private static final String TAG = "HidDevice";
	private Thread mEventThread;		
	private boolean mIsBroken = false;
	public UsbHandler mUsbHandler;
	

	public HidDevice(Context context) {
		super();
		mEventThread = null;
		//mContext = context;
		mIsBroken = false;
	}
	
	
	public int writeData(byte[] data, int length) {
		if ( !isConnected() )
			return -1;
		
		int res = mUsbHandler.setFeature(data);
		
		if (res < 0) {
			Log.i(TAG, "broken in writeData");
			mIsBroken = true;
		}
		
		return res;
	}

	
	public int readData(byte[] data) {
		if ( !isConnected() )
			return -1;
		
		int res = mUsbHandler.getFeature(cmds.CMD_REPORT_ID, data);
		
		if (res < 0) {
			Log.i(TAG, "broken in readData");
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
		
		synchronized (this) {
			if (mEventThread == null) {
				
				mEventThread = new Thread() {
					public void run() {
						
						while (!getCloseFlag() && isConnected() ) {
							//Event.eventPoll(HidDevice.this);
							
							ByteBuffer buffer = ByteBuffer.allocate(HID_EVENT_SIZE);
							
							byte[] data = new byte[cmds.MAX_EVENT_SIZE + 1];
							int size = -1;
							
							if ( mUsbHandler.mRequestEvent.queue(buffer, buffer.capacity())) {
								UsbRequest res = mUsbHandler.mDevConn.requestWait();
								
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
			
			Event.setEvent(this, cmds.kConnectionClosed, 0);
			
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
		return mUsbHandler != null && mIsBroken == false;
	}
};
