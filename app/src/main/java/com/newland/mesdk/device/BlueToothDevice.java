package com.newland.mesdk.device;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.cynoware.posmate.Util;
import com.newland.mtype.DeviceMenuEvent;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtypex.bluetooth.BlueToothV100ConnParams;
import com.newland.mesdk.moduleinterface.DeviceControllerInterface;

/**
 * Created by YJF on 2015/8/14 0014. 蓝牙设备
 */
public class BlueToothDevice extends AbstractDevice {
	private static final String ME3X_DRIVER_NAME = "com.newland.me.ME3xDriver";
	private String mBTAddr;
	// private String mBTName;
	//private Context mContext;
	
	//private DeviceControllerInterface mController = null;
	
	//private boolean mIsConnected;
	
	public BlueToothDevice( Context context) {
		super(context);
	}
		
	
	// Add by Jie Zhuang
	/*public void init( String name, String addr ){
		mBTAddr = addr;
		// mBTName = name;
		new Thread(new Runnable() {
			@Override
			public void run() {
				initController();
			}
		}).start();
	}*/
	
	
	/**
	 * 
	 * @param addr
	 */
	public void setBTAddress( String addr ){
		mBTAddr = addr;
	}
	
	
	// 初始化蓝牙设备的控制器
	@Override
	public void initController() {
		ME3xDeviceDriver me3xDeviceController = new ME3xDeviceDriver(mContext);
		mController = me3xDeviceController.initMe3xDeviceController(ME3X_DRIVER_NAME, new BlueToothV100ConnParams(mBTAddr,
				new DeviceEventListener<DeviceMenuEvent>() {
					@Override
					public void onEvent(DeviceMenuEvent event, Handler arg1) {
						try {
							String eCode = event.getEcode();
							Util.showMessage( mContext, "Me3xDevice code:" + eCode );
						} catch (Exception ex) {
							Util.showMessage( mContext, "Me3xDevice error");
						}
					}

					@Override
					public Handler getUIHandler() {
						return null;
					}
				}));
	}
	
	public void closeController(){
		mController.destroy();
	}

		
	
	
	

	public void connect() {
		try {
			mController.connect();
			mIsConnected = true;
		} catch (Exception e1) {
			mIsConnected = false;
			e1.printStackTrace();
		}

	}

	//@Override
	/*public void disconnect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (mController != null) {
						mController.disConnect();
						mController = null;
					}
				} catch (Exception e) {
					Log.e("CardBTDevice", "deleteCSwiper failed!", e);
				} 
			}
		}).start();
	}*/

	public void disconnect(){
		if( mController != null ){
			mController.disConnect();
			mIsConnected = false;
		}
	}
	
	
	public void close(){
		if( mController != null ){
			mController.disConnect();
			mController.destroy();
			mController = null;
		}
	}

}
