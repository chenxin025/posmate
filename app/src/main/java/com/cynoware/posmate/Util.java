/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;


public class Util {

	public static boolean msleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	
//	public static boolean checkDeviceAvailable( Device device, Activity activity  ){
//		if(device == null){
//			if( activity != null ){
//				Toast.makeText( activity, "Device not found!",
//                    Toast.LENGTH_SHORT).show();
//			}
//
//            return false;
//        }
//
//    	if(!device.isConnected()){
//
//    		if( activity != null ){
//    			Toast.makeText( activity, "Device not connected",
//                    Toast.LENGTH_SHORT).show();
//    		}
//
//    		return false;
//    	}
//
//    	return true;
//	}
	
	
	public static void showMessage( final Context context, final String message) {

		if (null == context){
			return;
		}
		Activity activity = (Activity)context;
		activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText( context, message, Toast.LENGTH_SHORT).show();;
 			}
 		});
 	}

//	public static  DeviceInfo getQRScannerInfo(Context context, ChannelManager manager){
//		DeviceInfo info = new DeviceInfo();
//
//		int channel = Setting.getInstance(context).getQRScanerChannel();
//		info.device = manager.getDevice(channel);
//
//		if( channel == ChannelManager.CHANNEL_TRAY_USB  )
//			info.port = config.CONFIG_TRAY_QRREADER_UART;
//		else
//			info.port = config.CONFIG_DOCK_QRREADER_UART;
//		return info;
//	}

//	public static DeviceInfo getLedInfo(Context context,ChannelManager manager,boolean isBoard){
//		DeviceInfo info = new DeviceInfo();
//
//		int channel = Setting.getInstance(context).getLEDChannel();
//		info.device = manager.getDevice(channel);
//
//		if (isBoard){
//			info.port = config.CONFIG_ONBOARD_LED_UART;
//		}else{
//			info.port = config.CONFIG_LED_UART;
//		}
//		return  info;
//	}

//	public static DeviceInfo getPrinterInfo(Context context, ChannelManager manager){
//		DeviceInfo info = new DeviceInfo();
//		int channel = Setting.getInstance(context).getPrinterChannel();
//		info.device = manager.getDevice(channel);
//		info.port = config.CONFIG_PRINTER_UART;
//		return  info;
//	}

	public static long getCalledTime(long start) {
		return (SystemClock.currentThreadTimeMillis() - start);
	}
}
