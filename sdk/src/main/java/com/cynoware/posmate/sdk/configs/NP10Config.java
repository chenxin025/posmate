package com.cynoware.posmate.sdk.configs;


/**
 * Created by john on 2016/9/20.
 */

public class NP10Config extends BaseConfig {

    private DeviceConfigInfo mPrinterInfo = null;
    private DeviceConfigInfo mScannerInfo = null;
    private DeviceConfigInfo mLedInfo = null;


    public NP10Config(){
        if (null == mPrinterInfo){
            mPrinterInfo = new DeviceConfigInfo();
            mPrinterInfo.mDevVersion = 0;
        }

        if (null == mScannerInfo){
            mScannerInfo = new DeviceConfigInfo();
            mScannerInfo.mDevVersion = 0;
        }

        if (null == mLedInfo){
            mLedInfo = new DeviceConfigInfo();
            mLedInfo.mDevVersion = 0;
        }
    }

    @Override
    public DeviceConfigInfo getLedConfigInfo() {
        if (null == mLedInfo){
            mLedInfo = new DeviceConfigInfo();
            mLedInfo.mDevVersion = 0;
        }
        return mLedInfo;
    }

    @Override
    public DeviceConfigInfo getPrinterConfigInfo() {
        if (null == mPrinterInfo){
            mPrinterInfo = new DeviceConfigInfo();
            mPrinterInfo.mDevVersion = 0;
        }
        return mPrinterInfo;
    }

    @Override
    public DeviceConfigInfo getScannerConfigInfo() {
        if (null == mScannerInfo){
            mScannerInfo = new DeviceConfigInfo();
            mScannerInfo.mDevVersion = 0;
        }
        return mScannerInfo;
    }


}
