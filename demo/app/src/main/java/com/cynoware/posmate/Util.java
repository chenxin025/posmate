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
import android.widget.Toast;

import com.cynoware.posmate.sdk.Device;

public class Util {

	public static boolean msleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	
	public static boolean checkDeviceAvailable( Device device, Activity activity  ){
		if(device == null){			
			if( activity != null ){
				Toast.makeText( activity, "Device not found!",
                    Toast.LENGTH_SHORT).show();
			}
			
            return false;
        }
        
    	if(!device.isConnected()){
    		
    		if( activity != null ){
    			Toast.makeText( activity, "Device not connected",
                    Toast.LENGTH_SHORT).show();
    		}

    		return false;
    	}
    	
    	return true;
	}
	
	
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
	
	

}
