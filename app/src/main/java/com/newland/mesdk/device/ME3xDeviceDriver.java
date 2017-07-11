package com.newland.mesdk.device;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.cynoware.posmate.Util;
import com.newland.mesdk.interfaceImpl.DeviceControllerInterfaceImpl;
import com.newland.mesdk.moduleinterface.DeviceControllerInterface;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;

/**
 * Created by YJF 
 * 初始化设备控制器
 */
public class ME3xDeviceDriver {
	private Context mContext;
	private DeviceControllerInterface controller;

	public ME3xDeviceDriver( Context context) {
		mContext = context;
	}

	public DeviceControllerInterface initMe3xDeviceController(String driverPath, DeviceConnParams params) {
		controller = DeviceControllerInterfaceImpl.getInstance(driverPath);
		controller.init(mContext, driverPath, params, new DeviceEventListener<ConnectionCloseEvent>() {
			@Override
			public void onEvent(ConnectionCloseEvent event, Handler handler) {
				if (event.isSuccess()) {
					Util.showMessage( (Activity)mContext, "Equipment was active off customers!");
					//baseActivity.showMessage(, MessageTag.NORMAL);
				}
				if (event.isFailed()) {
					Util.showMessage( (Activity)mContext, "Device link exception disconnect!");
					// baseActivity.showMessage("Device link exception disconnect！", MessageTag.ERROR);
				}
			}

			@Override
			public Handler getUIHandler() {
				return null;
			}
		});
		return controller;
	}

}
