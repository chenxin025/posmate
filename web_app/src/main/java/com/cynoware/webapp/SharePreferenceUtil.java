package com.cynoware.webapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePreferenceUtil {

	private static final String TAG = "SharePreferenceUtil";
	
	private static final String SHAREPRE_FILE_NAME = "com.hss.drfish_preferences";
	
	private SharedPreferences mSharePreferences;
	
	private static SharePreferenceUtil mSharePre;
	
	public static SharePreferenceUtil getInstance() {
		
		if (mSharePre == null) {
			mSharePre = new SharePreferenceUtil();
		}
		
		return mSharePre;
	}
	
	public void init(Context context) {

		
		mSharePreferences = context.getSharedPreferences(SHAREPRE_FILE_NAME, Context.MODE_PRIVATE);
	}
	
	public void putString(String key, String value) {

		Editor editor = mSharePreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void putInt(String key, int value) {

		Editor editor = mSharePreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public void putLong(String key, long value) {
		
		Editor editor = mSharePreferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public void putBoolean(String key, boolean value) {

		Editor editor = mSharePreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public String getString(String key, String defValue) {
		if (mSharePreferences == null) {
			throw new RuntimeException("SharedPreferences is not init", new Throwable());
		}
		return mSharePreferences.getString(key, defValue);
	}
	
	public boolean getBoolean(String key, boolean defValue) {
		if (mSharePreferences == null) {
			throw new RuntimeException("SharedPreferences is not init", new Throwable());
		}
		return mSharePreferences.getBoolean(key, defValue);
	}
	
	public int getInt(String key, int defValue) {
		if (mSharePreferences == null) {
			throw new RuntimeException("SharedPreferences is not init", new Throwable());
		}
		return mSharePreferences.getInt(key, defValue);
	}
	
	public long getLong(String key, long defValue) {
		if (mSharePreferences == null) {
			throw new RuntimeException("SharedPreferences is not init", new Throwable());
		}
		return mSharePreferences.getLong(key, defValue);
	}
	
	public void clear() {
		
		mSharePreferences = null;
		mSharePre = null;
	}
}
