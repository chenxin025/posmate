/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import android.content.Context;
import android.content.SharedPreferences;

public class Setting {

	public static final int USB = 0;
	public static final int BT = 1;

	public static final String MODEL_POS_MATE = "posmate";
	public static final String MODEL_NP10 = "np10";
	
	private static final String PREF_POS_MODEL = "pos_model";   

	private static final String PREF_CHANNEL_DOCK_USB = "channel_dock_usb";
	private static final String PREF_CHANNEL_DOCK_BT = "channel_dock_bt";
	private static final String PREF_CHANNEL_TRAY_USB = "channel_tray_usb";
	private static final String PREF_CHANNEL_TRAY_BT = "channel_tray_bt";
	private static final String PREF_CHANNEL_CARD_BT = "channel_card_bt";

	private static final String PREF_DOCK_BT_NAME = "dock_bt_name";
	private static final String PREF_DOCK_BT_ADDR = "dock_bt_addr";
	
	private static final String PREF_CARD_BT_NAME = "card_bt_name";
	private static final String PREF_CARD_BT_ADDR = "card_bt_addr";
	
	private static final String PREF_PRINTER_CHANNEL = "printer_channel";
	private static final String PREF_PRINTER_VENDOR = "printer_vendor";
	
	private static final String PREF_LED_CHANNEL = "led_channel";
	private static final String PREF_LED_DEVICE = "led_device";
	
	private static final String PREF_QR_SCANER_CHANNEL = "qrscaner_channel";	
	private static final String PREF_CASH_DRAWER_CHANNEL = "cashdrawer_channel";
	
	private static final String PREF_CARD_READER_CHANNEL = "cardreader_channel";

	private static Setting mInstance = null;
	private static SharedPreferences mPreference = null;

	private String mDockBTName;
	private String mDockBTAddr;
	
	private String mCardBTName;
	private String mCardBTAddr;

	private boolean mChannelDockUSB = true;
	private boolean mChannelDockBT = false;
	private boolean mChannelTrayUSB = true;
	private boolean mChannelTrayBT = false;
	private boolean mChannelCardBT = false;
	
	
	private int mPrinterChannel = 0;
	private int mLEDChannel = 0;
	private int mQRScanerChannel = 0;
	private int mCashDrawerChannel = 0;
	private int mCardReaderChannel = 0;
	
	private int mPrinterVendor = 0;
	private int mLEDDevice = 0;
	private String mPosModel = "np10";

	private Setting(Context context) {
		mPreference = context.getSharedPreferences("setting", 0);
	}

	public static Setting getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Setting(context);
		}

		return mInstance;
	}

	public void loadSetting() {

		mChannelDockUSB = mPreference.getBoolean(PREF_CHANNEL_DOCK_USB, true);
		mChannelDockBT = mPreference.getBoolean(PREF_CHANNEL_DOCK_BT, false);
		mChannelTrayUSB = mPreference.getBoolean(PREF_CHANNEL_TRAY_USB, true);
		mChannelTrayBT = mPreference.getBoolean(PREF_CHANNEL_TRAY_BT, false);
		mChannelCardBT = mPreference.getBoolean(PREF_CHANNEL_CARD_BT, false);

		mDockBTName = mPreference.getString(PREF_DOCK_BT_NAME, "N/A");
		mDockBTAddr = mPreference.getString(PREF_DOCK_BT_ADDR, "");
		
		mCardBTName = mPreference.getString(PREF_CARD_BT_NAME, "N/A");
		mCardBTAddr = mPreference.getString(PREF_CARD_BT_ADDR, "");		
		
//		mPrinterChannel = mPreference.getInt(PREF_PRINTER_CHANNEL, ChannelManager.CHANNEL_DOCK_USB );
//		mLEDChannel = mPreference.getInt(PREF_LED_CHANNEL, ChannelManager.CHANNEL_DOCK_USB );
//		mQRScanerChannel = mPreference.getInt(PREF_QR_SCANER_CHANNEL, ChannelManager.CHANNEL_TRAY_USB );
//		mCashDrawerChannel = mPreference.getInt(PREF_CASH_DRAWER_CHANNEL, ChannelManager.CHANNEL_DOCK_USB );
//		mCardReaderChannel = mPreference.getInt(PREF_CARD_READER_CHANNEL, ChannelManager.CHANNEL_CARD_BT );
		
		mPrinterVendor = mPreference.getInt(PREF_PRINTER_VENDOR, 0 );
		mLEDDevice = mPreference.getInt(PREF_LED_DEVICE, 0 );
		
		mPosModel = mPreference.getString(PREF_POS_MODEL, "np10" );
	}

//	public boolean getChannelEnable(int channel) {
//
//		switch (channel) {
//		case ChannelManager.CHANNEL_DOCK_USB:
//			return mChannelDockUSB;
//
//		case ChannelManager.CHANNEL_DOCK_BT:
//			return mChannelDockBT;
//
//		case ChannelManager.CHANNEL_TRAY_USB:
//			return mChannelTrayUSB;
//
//		case ChannelManager.CHANNEL_TRAY_BT:
//			return mChannelTrayBT;
//
//		case ChannelManager.CHANNEL_CARD_BT:
//			return mChannelCardBT;
//
//		default:
//			return false;
//		}
//	}

//	public void setChannelEnable(int channel, boolean enable) {
//
//		String pref;
//
//		switch (channel) {
//		case ChannelManager.CHANNEL_DOCK_USB:
//			mChannelDockUSB = enable;
//			pref = PREF_CHANNEL_DOCK_USB;
//			break;
//
//		case ChannelManager.CHANNEL_DOCK_BT:
//			mChannelDockBT = enable;
//			pref = PREF_CHANNEL_DOCK_BT;
//			break;
//
//		case ChannelManager.CHANNEL_TRAY_USB:
//			mChannelTrayUSB = enable;
//			pref = PREF_CHANNEL_TRAY_USB;
//			break;
//
//		case ChannelManager.CHANNEL_TRAY_BT:
//			mChannelTrayBT = enable;
//			pref = PREF_CHANNEL_TRAY_BT;
//			break;
//
//		case ChannelManager.CHANNEL_CARD_BT:
//			mChannelCardBT = enable;
//			pref = PREF_CHANNEL_CARD_BT;
//			break;
//
//		default:
//			return;
//		}
//
//		SharedPreferences.Editor editor = mPreference.edit();
//		editor.putBoolean(pref, enable);
//		editor.commit();
//	}

	public String getDockBTName() {
		return mDockBTName;
	}

	public void setDockBTName(String name) {
		mDockBTName = name;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putString(PREF_DOCK_BT_NAME, mDockBTName);
		editor.commit();
	}

	public String getDockBTAddr() {
		return mDockBTAddr;
	}

	public void setDockBTAddr(String addr) {
		mDockBTAddr = addr;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putString(PREF_DOCK_BT_ADDR, mDockBTAddr);
		editor.commit();
	}
	
	
	public String getCardBTName() {
		return mCardBTName;
	}

	public void setCardBTName(String name) {
		mCardBTName = name;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putString(PREF_CARD_BT_NAME, mCardBTName);
		editor.commit();
	}	
	
	public String getCardBTAddr() {
		return mCardBTAddr;
	}

	public void setCardBTAddr(String addr) {
		mCardBTAddr = addr;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putString(PREF_CARD_BT_ADDR, mCardBTAddr);
		editor.commit();
	}

	
	public int getPrinterChannel() {
		return mPrinterChannel;
	}

	public void setPrinterChannel(int channel) {
		mPrinterChannel = channel;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putInt(PREF_PRINTER_CHANNEL, mPrinterChannel);
		editor.commit();
	}
	
	
	public int getLEDChannel() {
		return mLEDChannel;
	}

	public void setLEDChannel(int channel) {
		mLEDChannel = channel;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putInt(PREF_LED_CHANNEL, mLEDChannel);
		editor.commit();
	}
	
	public int getQRScanerChannel() {
		return mQRScanerChannel;
	}

	public void setQRScanerChannel(int channel) {
		mQRScanerChannel = channel;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putInt(PREF_QR_SCANER_CHANNEL, mQRScanerChannel);
		editor.commit();
	}

	
	public int getCashDrawerChannel() {
		return mCashDrawerChannel;
	}

	public void setCashDrawerChannel(int channel) {
		mCashDrawerChannel = channel;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putInt(PREF_CASH_DRAWER_CHANNEL, mCashDrawerChannel);
		editor.commit();
	}
	
	public int getCardReaderChannel() {
		return mCardReaderChannel;
	}

	public void setCardReaderChannel(int channel) {
		mCardReaderChannel = channel;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putInt(PREF_CARD_READER_CHANNEL, mCardReaderChannel);
		editor.commit();
	}
	
	public int getPrinterVendor() {
		return mPrinterVendor;
	}

	public void setPrinterVendor(int vendor) {
		mPrinterVendor = vendor;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putInt(PREF_PRINTER_VENDOR, mPrinterVendor);
		editor.commit();
	}
	
	public int getLEDDevice() {
		return mLEDDevice;
	}

	public void setLEDDevice(int device) {
		mLEDDevice = device;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putInt(PREF_LED_DEVICE, mLEDDevice);
		editor.commit();
	}

	public String getPosModel() {
		return mPosModel;
	}

	public void setPosModel(String model) {
		mPosModel = model;

		SharedPreferences.Editor editor = mPreference.edit();
		editor.putString(PREF_POS_MODEL, mPosModel);
		editor.commit();
	}

	
	
	
}
