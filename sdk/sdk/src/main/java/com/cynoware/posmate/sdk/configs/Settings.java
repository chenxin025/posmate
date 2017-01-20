package com.cynoware.posmate.sdk.configs;

import android.content.Context;

import com.cynoware.posmate.sdk.util.SharePrefManager;

/**
 * Created by john on 2017/1/17.
 */

public class Settings {

    private static Settings sSettings = null;

    private int mPrinterChannel = 0;
    private int mLEDChannel = 0;
    private int mQRScanerChannel = 0;
    private int mCashDrawerChannel = 0;

    public static Settings getInstance(Context context){
        if (sSettings == null){
            sSettings = new Settings();
            sSettings.loadSettings();
        }
        return sSettings;
    }

    public void loadSettings(){
        mPrinterChannel = SharePrefManager.getInstance()
                .getInt(PosConstant.KEY_PRINTER_CHANNEL,PosConstant.CHANNEL_DOCK_USB);
        mLEDChannel = SharePrefManager.getInstance()
                .getInt(PosConstant.KEY_LED_CHANNEL,PosConstant.CHANNEL_DOCK_USB);
        mQRScanerChannel = SharePrefManager.getInstance()
                .getInt(PosConstant.KEY_QR_SCANER_CHANNEL,PosConstant.CHANNEL_TRAY_USB);
        mCashDrawerChannel = SharePrefManager.getInstance()
                .getInt(PosConstant.KEY_CACH_DRAWER_CHANNEL,PosConstant.CHANNEL_DOCK_USB);
    }

    public int getmPrinterChannel(){
        return mPrinterChannel;
    }

    public void setmPrinterChannel(int channel){
        SharePrefManager.getInstance()
                .putInt(PosConstant.KEY_PRINTER_CHANNEL,channel);
    }

    public int getmLEDChannel(){
        return mLEDChannel;
    }

    public void setmLEDChannel(int channel){
        SharePrefManager.getInstance()
                .putInt(PosConstant.KEY_LED_CHANNEL,channel);
    }

    public int getmQRScanerChannel(){
        return mQRScanerChannel;
    }

    public void setmQRScanerChannel(int channel){
        SharePrefManager.getInstance()
                .putInt(PosConstant.KEY_QR_SCANER_CHANNEL,channel);
    }

    public int getmCashDrawerChannel() {
        return mCashDrawerChannel;
    }

    public void setmCashDrawerChannel(int mCashDrawerChannel) {
        this.mCashDrawerChannel = mCashDrawerChannel;
    }

}
