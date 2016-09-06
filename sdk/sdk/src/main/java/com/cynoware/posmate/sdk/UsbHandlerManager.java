package com.cynoware.posmate.sdk;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import android.widget.Toast;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbConstants;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 
 * 程序刚开始运行时，检查系统中已连接的全部USB设备，逐一开始请求授权
 * 收到授权消息时，应记录用户授权的状态
 * 
 *
 */

public class UsbHandlerManager {

	class DeviceItem{
		UsbDevice mDevice;
		boolean mIsGranted;
		boolean mIsOpen;
		
	};
	
	UsbManager mUsbManager;
	Context mContext;
	List<DeviceItem> mList;
	
	
	private DeviceItem findDevice( UsbDevice device ){
		if( mList == null )
			return null;
		
		for( int i=0; i<mList.size(); i++ ){
			DeviceItem item = mList.get(i);
			if( item.mDevice.equals(device) ){
				return item;
			}			
		}
		
		return null;
	}
	
	
	private DeviceItem findUngrantedDevice(){
		if( mList == null )
			return null;
		
		for( int i=0; i<mList.size(); i++ ){
			DeviceItem item = mList.get(i);
			if( !item.mIsGranted  ){
				return item;
			}			
		}
		
		return null;
	}
		
	private DeviceItem addDevice( UsbDevice device, boolean isGranted, boolean isOpen ){
		DeviceItem item = findDevice(device);
		if( item != null )
			return item;
		
		item = new DeviceItem();
		item.mDevice = device;
		item.mIsGranted = isGranted;
		item.mIsOpen = isOpen;
		
		mList.add( item );
		return item;
	}
	
	
	/**
	 * 列举所有的设备，并将我们需要的保存在一个结构里
	 */
	public void init( Context context ){
		mUsbManager = (UsbManager)context
				.getSystemService(Context.USB_SERVICE);
		mContext = context;
		mList = new ArrayList<DeviceItem>();
	}
	
	
	private void enumerateDevices( Context context, int vendorId,
			int productId, int deviceClass, int deviceProtocol,
			int deviceSubclass, int interfaceCount){
		
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		int devVendorId = 0, devProductID, devClass, devProtocol, devSubClass, devIFCount;
		for (UsbDevice device : deviceList.values()) {
			if (device.getVendorId() == vendorId)
				devVendorId = device.getVendorId();
			devProductID = device.getProductId();
			devClass = device.getDeviceClass();
			devProtocol = device.getDeviceProtocol();
			devSubClass = device.getDeviceSubclass();
			devIFCount = device.getInterfaceCount();

			if (devVendorId == vendorId && devProductID == productId
					&& devClass == deviceClass && devSubClass == deviceSubclass
					&& devProtocol == deviceProtocol
					&& devIFCount == interfaceCount) {
				
				addDevice( device, true, false );
			}
		}
	}
	
		
	public void getHandler( ){ 
		
	}
	
	
	/**
	 * 开始获取授权
	 */
	private void requestPerssion(){
		DeviceItem item = findUngrantedDevice();
		if( item != null ){
			PendingIntent permissionIntent = PendingIntent.getBroadcast(
				mContext, 0, new Intent(UsbHandler.ACTION_USB_PERMISSION), 0);
			mUsbManager.requestPermission(item.mDevice, permissionIntent);
		}
	}
	
	/**
	 * 获取一个设备
	 */
	public void onPermissionCallback(){
		
	}
	
	
	public void onAttachCallback(){
		
	}
	
	public void onDetachCallback(){
		
	}	
};
