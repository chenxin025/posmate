package com.newland.mesdk.device;

import android.content.Context;

import com.newland.mesdk.moduleinterface.DeviceControllerInterface;

/**
 * Created by YJF on 2015/8/11 0011.
 */
public abstract class AbstractDevice {
	
	protected Context mContext;
	
	protected DeviceControllerInterface mController;
	protected boolean mIsConnected;
	
	public AbstractDevice( Context context ){
		mContext = context;
		mController = null;
		mIsConnected = false;
	}
	
	/**
	 * 初始化控制器
	 */
	public abstract void initController();
	
	
	/**
	 * 关闭控制器
	 */
	public abstract void closeController();

	/**
	 * 连接设备
	 */
	public abstract void connect();
	
	
	/**
	 * 断开连接
	 */
	public abstract void disconnect();

	
	public boolean isControllerAlive() {
		return mController != null;
	}
	
	public boolean isConnected(){
		return mIsConnected;
	}
	
	/*
	public DeviceControllerInterface getController() {
		return mController;
	}*/

}
