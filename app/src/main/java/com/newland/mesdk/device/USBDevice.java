package com.newland.mesdk.device;

import android.content.Context;
import android.util.Log;

import com.cynoware.posmate.Util;
import com.newland.mtypex.usb.UsbV100ConnParams;
import com.newland.mesdk.moduleinterface.DeviceControllerInterface;
/**
 * Created by YJF USB连接设备
 */
public class USBDevice extends AbstractDevice {
	private static final String ME3X_DRIVER_NAME = "com.newland.me.ME3xDriver";
	///private Context mContext;
	//private DeviceControllerInterface mController = null;
	private String TAG ="CardUSBDevice";
	//private boolean mIsConnected = false;
	private int clicktimes = 0;

	public USBDevice( Context context) {
		super( context );
		//mContext = context;
	}

	@Override
	public void initController() {
		ME3xDeviceDriver me3xDeviceController = new ME3xDeviceDriver(mContext);
		mController = me3xDeviceController.initMe3xDeviceController(ME3X_DRIVER_NAME, new UsbV100ConnParams());
		//baseActivity.btnStateToWaitingConn();
	}
	
	@Override
	public void closeController() {
		if( mController != null ){
			mController.destroy();
			mController = null;
		}	
	}	
	
	/*
	public boolean isConnected(){
		return mIsConnected;
	}*/

	@Override
	public void disconnect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (mController != null) {
						mController.disConnect();
						mController = null;
						Util.showMessage(mContext, "USB Controller disconnect success..." );
						//baseActivity.showMessage("USB Controller disconnect success...", MessageTag.TIP);
						//baseActivity.btnStateToWaitingInit();
					}
				} catch (Exception e) {
					Log.e(TAG, "USb disconnect failed!", e);
					Util.showMessage(mContext, "USBController disconnect Exception：" + e );
					//baseActivity.showMessage("USBController disconnect Exception："+e, MessageTag.ERROR);
				}
			}
		}).start();

	}

	/*@Override
	public boolean isControllerAlive() {
		return mController != null;
	}
	

	@Override
	public DeviceControllerInterface getController() {
		return mController;
	}*/

	@Override
	public void connect() {
		//baseActivity.showMessage("USB Device connection....", MessageTag.TIP);
		try {
			mController.connect();			
			Util.showMessage( mContext,  "Card Reader is connected via USB!" );
			// MyApplication.activity.ISCONNECTED = true;
			mIsConnected = true;
		} catch (Exception ex) {
			//MyApplication.activity.ISCONNECTED = false;
			mIsConnected = false;
			ex.printStackTrace();
			/**
			 * 2016.7.25 revised by puzhimin
			 * 解决bug 050 插入键盘错误提示
			 */
			if(clicktimes < 1){
				clicktimes++;
			}
			else{
				Util.showMessage( mContext, "Card Reader connect failed via USB");
				// baseActivity.showMessage("Exception, USB Device connection failed!", MessageTag.ERROR);
			}

			 //原代码
//			if(clicktimes > 1){
//				Utils.showMessage( mContext, "Card Reader connect failed via USB");
//				// baseActivity.showMessage("Exception, USB Device connection failed!", MessageTag.ERROR);
//			}
//			else{
//				clicktimes++;
//				connect();
//			}

		}
	}


}
