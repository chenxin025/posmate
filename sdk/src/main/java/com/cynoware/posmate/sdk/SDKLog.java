package com.cynoware.posmate.sdk;

import android.util.Log;

public class SDKLog {

	private static final boolean IS_OPEN_LOG = true;
	
	private static final String BASE_TAG = "PosSdk::";
	
	private static final String APP_CODE_VERSION = "20160825-";
	
	private static String getCallerInfo() {
        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        StackTraceElement ele = null;
        String className = "";
        String methodName = "";
        int lineNO = 0;
        if (stack.length > 2) {
            ele = stack[2];
            try {
                className = Class.forName(ele.getClassName()).getSimpleName();
                methodName = ele.getMethodName();
                lineNO = ele.getLineNumber();
            } catch (ClassNotFoundException e) {
            }
        }

        String callerInfo = className + ":" + methodName + ":" + APP_CODE_VERSION + lineNO + "=>";
        return callerInfo;
    }
	
	public static void i(String tag, String msg){
		if (IS_OPEN_LOG){
			Log.i(BASE_TAG + tag, getCallerInfo() + msg);
		}
	}
	
	public static void d(String tag, String msg){
		if (IS_OPEN_LOG){
			Log.d(BASE_TAG + tag, getCallerInfo() + msg);
		}
	}
	
	public static void w(String tag, String msg){
		if (IS_OPEN_LOG){
			Log.w(BASE_TAG + tag, getCallerInfo() + msg);
		}
	}
	
	public static void e(String tag, String msg){
		if (IS_OPEN_LOG){
			Log.e(BASE_TAG + tag, getCallerInfo() + msg);
		}
	}


}
