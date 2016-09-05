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
import java.util.HashMap;


public class UsbHandler {
	public static final String ACTION_USB_PERMISSION = "com.cynoware.posmate.USB_PERMISSION";
	public static final String ACTION_USB_SINGLE_PERMISSION = "com.cynoware.posmate.USB_SINGLE_PERMISSION";
	public static final String ACTION_USB_COMPLETE = "com.cynoware.posmate.USB_COMPLETE";
	private static final String TAG = "HidDevice";

	private static final String INTERNAL_NAME_DOCK = "PosMate";
	private static final String INTERNAL_NAME_FRAME = "PMFrame";
	
	public static final int DEV_UNKOWN = -1;
	public static final int DEV_DOCK = 0;
	public static final int DEV_TRAY = 1;
	
	private static final int USB_REQUEST_TYPE_INTERFACE = 0x01;
	private static final int CMD_TIMEOUT_MS = 0x2000;


	private int mDevType;
	public UsbDevice mDevice;
	
	UsbDeviceConnection mDevConn;
	UsbInterface mInterface;
	UsbRequest mRequest;
	
	UsbInterface mInterfaceEvent;	
	UsbRequest mRequestEvent;


	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public UsbHandler(int type, UsbDevice device, UsbDeviceConnection conn,
					  UsbInterface interFace, UsbRequest request, UsbInterface interFaceEvent, UsbRequest requestIn ) {
		mDevice = device;
		mDevType = type;
		mDevConn = conn;
		mInterface = interFace;
		mRequest = request;
		
		mInterfaceEvent = interFace;
		mRequestEvent = requestIn;
	}

	public int getDevType(){
		return mDevType;
	}
	
	/*
	 * Detect and initialize USB connection for PosMate. After the USB connected
	 * was detected, user's application will receive broadcast intent with
	 * action name ACTION_USB_PERMISSION.
	 */
	public static void detectUsb(Context context, UsbHandler handler1, UsbHandler handler2) {
		boolean isfind = findDevice(context, 0x0416, 0xB000, 0, 0, 0, 2, handler1, handler2);
		
		if (!isfind) {
			Log.i(TAG, "Device not found");			
			Toast.makeText(context, "USB Device not found", Toast.LENGTH_SHORT).show();			
		}
	}
	
	
	/**
	 * 
	 * 这里会搜素到所有而不是一个符合描述的设备，并请求权限 原因是USB和TRAY的USB描述是一样的
	 */
	/*static private boolean findDevice(Context context, int vendorId,
			int productId, int deviceClass, int deviceProtocol,
			int deviceSubclass, int interfaceCount) {
		UsbManager usbManager = (UsbManager) context
				.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		int devVendorId = 0, devProductID, devClass, devProtocol, devSubClass, devIFCount;
		boolean found = false;
		for (UsbDevice device : deviceList.values()) {
			if (device.getVendorId() == vendorId)
				devVendorId = device.getVendorId();
			devProductID = device.getProductId();
			devClass = device.getDeviceClass();
			devProtocol = device.getDeviceProtocol();
			devSubClass = device.getDeviceSubclass();
			devIFCount = device.getInterfaceCount();

			Log.i(TAG, String.format("Found USB:%d %d %d %d %d %d",
					devVendorId, devProductID, devClass, devProtocol,
					devSubClass, devIFCount));

			if (devVendorId == vendorId && devProductID == productId
					&& devClass == deviceClass && devSubClass == deviceSubclass
					&& devProtocol == deviceProtocol
					&& devIFCount == interfaceCount) {

				// Request device access
				found = true;
				PendingIntent permissionIntent = PendingIntent.getBroadcast(
						context, 0, new Intent(ACTION_USB_PERMISSION), 0);
				usbManager.requestPermission(device, permissionIntent);
				Log.i(TAG, "RequestPermission");
			}
		}

		return found;
	}*/
	
	static private boolean findDevice(Context context, int vendorId,
			int productId, int deviceClass, int deviceProtocol,
			int deviceSubclass, int interfaceCount, UsbHandler handler1, UsbHandler handler2) {
		UsbManager usbManager = (UsbManager) context
				.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		int devVendorId = 0, devProductID, devClass, devProtocol, devSubClass, devIFCount;
		boolean found = false;
		for (UsbDevice device : deviceList.values()) {
			if (device.getVendorId() == vendorId)
				devVendorId = device.getVendorId();
			devProductID = device.getProductId();
			devClass = device.getDeviceClass();
			devProtocol = device.getDeviceProtocol();
			devSubClass = device.getDeviceSubclass();
			devIFCount = device.getInterfaceCount();

			/*Log.i(TAG, String.format("Found USB:%d %d %d %d %d %d",
					devVendorId, devProductID, devClass, devProtocol,
					devSubClass, devIFCount));*/

			if (devVendorId == vendorId && devProductID == productId
					&& devClass == deviceClass && devSubClass == deviceSubclass
					&& devProtocol == deviceProtocol
					&& devIFCount == interfaceCount) {

				// Request device access
				found = true;
				
				if( (handler1 != null && device.equals(handler1.mDevice)) ||
					(handler2 != null && device.equals(handler2.mDevice)) )
					continue;
									
				PendingIntent permissionIntent = PendingIntent.getBroadcast(
						context, 0, new Intent(ACTION_USB_PERMISSION), 0);
				usbManager.requestPermission(device, permissionIntent);
				Log.i(TAG, "RequestPermission" + device );
				return found;
			}
		}

		Intent intent = new Intent(ACTION_USB_COMPLETE);
		context.sendBroadcast(intent);
		
		return found;
	}
	
	
	static public boolean requestDevicePermission( Context context, UsbDevice device, int vendorId,
			int productId, int deviceClass, int deviceProtocol,	int deviceSubclass, int interfaceCount ){
		
		UsbManager usbManager = (UsbManager) context
				.getSystemService(Context.USB_SERVICE);
		
		int devVendorId = device.getVendorId();
		int devProductID = device.getProductId();
		int devClass = device.getDeviceClass();
		int devProtocol = device.getDeviceProtocol();
		int devSubClass = device.getDeviceSubclass();
		int devIFCount = device.getInterfaceCount();


		if (devVendorId == vendorId && devProductID == productId
				&& devClass == deviceClass && devSubClass == deviceSubclass
				&& devProtocol == deviceProtocol
				&& devIFCount == interfaceCount) {

			PendingIntent permissionIntent = PendingIntent.getBroadcast(
					context, 0, new Intent(ACTION_USB_SINGLE_PERMISSION), 0);
			usbManager.requestPermission(device, permissionIntent);
			return true;
		}
		
		return false;
	}
	

	private static String getDeviceName(UsbDeviceConnection conn) {
		String str = "";
		byte[] buf = new byte[cmds.MAX_CMD_SIZE];
		buf[0] = cmds.CMD_REPORT_ID;
		buf[1] = cmds.CMD_GET_INTERFACE_NUMBER;

		int res = setFeature(conn, buf);
		if (res < 0)
			return str;

		res = getFeature(conn, cmds.CMD_REPORT_ID, buf);
		if (res < 0)
			return str;

		// String str = String.copyValueOf(buf, 8, 8);
		try {
			int i;
			for (i = 8; i < buf.length && buf[i] != (byte) 0; ++i)
				;
			str = new String(buf, 8, (i - 8), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
		}
		return str;
	}
	
	
	/*
	 * Open an USB device. This function should be called after user
	 * applications receives broadcast intent with action name
	 * ACTION_USB_PERMISSION.
	 */
	@SuppressLint("NewApi") static public UsbHandler open(Context context, UsbDevice usbDevice) {
		try{
			
			UsbManager usbManager = (UsbManager) context
					.getSystemService(Context.USB_SERVICE);
			
			UsbDeviceConnection conn = usbManager.openDevice(usbDevice);
			if( conn == null )
				return null;
			
			UsbInterface usbInterface = usbDevice.getInterface(0);
			LogUtil.i(TAG,"cccccccccccccccccc11"+usbInterface.toString());
			
			if( usbInterface == null )
				return null;
			
			if( !conn.claimInterface(usbInterface, true) ){
				conn.close();
				return null;
			}
			
			
			UsbInterface usbInterfaceEvent = usbDevice.getInterface(1);
			LogUtil.i(TAG,"cccccccccccccccccc22"+usbInterfaceEvent.toString());
			if( usbInterfaceEvent == null )
				return null;
			
			if( !conn.claimInterface(usbInterfaceEvent, true) ){
				conn.close();
				return null;
			}
			
			UsbRequest request = new UsbRequest();			
			UsbEndpoint endPoint = usbInterface.getEndpoint(0);
			request.initialize( conn, endPoint);
			
			UsbRequest requestEvent = new UsbRequest();
			UsbEndpoint endPointEvent = usbInterface.getEndpoint(0);// usbInterfaceEvent.getEndpoint(0);
			requestEvent.initialize( conn, endPointEvent);
			
			String name = getDeviceName(conn);
			int type = UsbHandler.DEV_UNKOWN;
			
			if ( name.equals(INTERNAL_NAME_DOCK) )
				type = UsbHandler.DEV_DOCK;
			else if (name.equals(INTERNAL_NAME_FRAME) )
				type = UsbHandler.DEV_TRAY;
			else{
				conn.close();
				return null;
			}
			
			return new UsbHandler( type, usbDevice, conn, usbInterface, request, usbInterfaceEvent, requestEvent );		
		}
		catch( Exception e ){
			e.printStackTrace();
			return null;
		}		
	}
	
	

	static int getFeature(UsbDeviceConnection conn, byte reportID,
			byte[] buf) {
		buf[0] = reportID; /* Report Number */
		// buf.length must equat to MAX_CMD_SIZE
		int res = conn.controlTransfer(UsbConstants.USB_TYPE_CLASS
				| UsbConstants.USB_DIR_IN | USB_REQUEST_TYPE_INTERFACE, 0x01, /*
																			 * Get
																			 * report
																			 */
				0x0302, /* Request type: feature */
				0x00, /* Interface number */
				buf, buf.length, CMD_TIMEOUT_MS);
		return res;
	}

	private static int setFeature(UsbDeviceConnection connection, /*
																 * unsigned char
																 * reportID,
																 */byte[] buf) {
		// must buf[0] == reportID; /* Report Number */
		// buf.length must equat to MAX_CMD_SIZE
		int res = connection.controlTransfer(UsbConstants.USB_TYPE_CLASS
				| UsbConstants.USB_DIR_OUT | USB_REQUEST_TYPE_INTERFACE, 0x09, /*
																				 * Set
																				 * report
																				 */
				0x0302, /* Request type: feature */
				0x00, /* Interface number */
				buf, buf.length, CMD_TIMEOUT_MS);
		return res;
	}

	public int setFeature( byte[] buf) {
		return setFeature( this.mDevConn, buf );	
	}
	
	public int getFeature( byte reportID, byte[] buf) {
		return getFeature( this.mDevConn, reportID, buf );
	}
	
	
	
	public void close(){

		if (mDevConn != null){
			
			if (mRequest != null) {
				mRequest.cancel();
				// mRequest.close();
			}
			
			if( mRequestEvent != null ){
				mRequestEvent.cancel();
			}
			
			if( mInterface != null )
				mDevConn.releaseInterface(mInterface);
			
			if( mInterfaceEvent != null )
				mDevConn.releaseInterface(mInterfaceEvent);
			
			mDevConn.close();
			
			mDevConn = null;
		}
	}
};
