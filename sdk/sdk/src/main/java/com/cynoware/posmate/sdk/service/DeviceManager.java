package com.cynoware.posmate.sdk.service;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.cynoware.posmate.sdk.bean.LedTextMsg;
import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.configs.PosConstant;
import com.cynoware.posmate.sdk.configs.Settings;
import com.cynoware.posmate.sdk.drawer.CashDrawer;
import com.cynoware.posmate.sdk.io.Beeper;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.HidDevice;
import com.cynoware.posmate.sdk.led.LED;
import com.cynoware.posmate.sdk.listener.ResponseCallBack;
import com.cynoware.posmate.sdk.listener.ResultCallBack;
import com.cynoware.posmate.sdk.printer.Printer;
import com.cynoware.posmate.sdk.qrreader.QrReader;

/**
 * Created by john on 2017/1/17.
 */

public class DeviceManager {

    private static DeviceManager sDeviceManager = null;
    private Context mContext = null;
    private static LooperThread mLooperThread = null;

    private Printer mPrinter = null;
    private LED mLed = null;
    private CashDrawer mCahCashDrawer = null;

    private HidDevice mDockUSBDevice = null;
    private HidDevice mTrayUSBDevice = null;

    private Beeper mBeeper = null;


    private boolean mIsScanning = true;

    public synchronized  static DeviceManager getInstance(Context context){
        if (sDeviceManager == null){
            sDeviceManager = new DeviceManager(context);
        }
        return sDeviceManager;
    }

    public DeviceManager(Context context){
        mContext = context;
        mDockUSBDevice = new HidDevice(context);
        mTrayUSBDevice = new HidDevice(context);
    }

    public Device getDevice(int channel) {
        switch (channel) {
            case PosConstant.CHANNEL_DOCK_USB:
                return mDockUSBDevice;

//            case PosConstant.CHANNEL_DOCK_BT:
//                return mDockBTDevice;

            case PosConstant.CHANNEL_TRAY_USB:
                return mTrayUSBDevice;
        }

        return null;
    }


    private Printer getPrinter(){
        if (mPrinter == null){
            int channel = Settings.getInstance(mContext).getmPrinterChannel();
            Device device = getDevice(channel);
            mPrinter = new Printer(device, BaseConfig.CONFIG_PRINTER_UART,Printer.TYPE_NP10);

            mBeeper = new Beeper(device,BaseConfig.CONFIG_BEEPER_GPIO);
        }
        return mPrinter;
    }

    private LED getLed(){
        if (mLed == null){
            int channel = Settings.getInstance(mContext).getmLEDChannel();
            Device device = getDevice(channel);
            mLed = new LED(device,BaseConfig.CONFIG_ONBOARD_LED_UART,true);
        }
        return mLed;
    }

    private CashDrawer getCahCashDrawer(){
        if (mCahCashDrawer == null){
            int channel = Settings.getInstance(mContext).getmCashDrawerChannel();
            mCahCashDrawer = new CashDrawer(getDevice(channel),BaseConfig.CONFIG_CASHDRAWER_GPIO);
        }
        return mCahCashDrawer;
    }

    private void initQrReader(){
        int channel = Settings.getInstance(mContext).getmQRScanerChannel();
        int port = BaseConfig.CONFIG_TRAY_QRREADER_UART;
        QrReader.initQrReader(getDevice(channel),port);
    }

    public static void openTask(Context context){
        if (null == context){
            return;
        }
        mLooperThread = new LooperThread(getInstance(context),context);
        mLooperThread.start();
    }




    //=================================================Printer============================================
    public void sendPrintTextMsg(byte[] str, ResponseCallBack callBack, Handler handler){
        if (str == null || str.length == 0 || mLooperThread == null
                || mLooperThread.getmHandler() == null || mLooperThread.isBusy()){
            if (callBack != null){
                callBack.onFailed();
            }
            return;
        }
        mLooperThread.setPrintTextStr(str,callBack,handler);
        mLooperThread.getmHandler().sendEmptyMessage(Types.TYPE_PRINTER_TEXT);
    }

    public void doPrintText(final byte[] text){
        if (mLooperThread != null ){
            Printer printer = getPrinter();
            if (null == printer){
                return;
            }
            printer.doConfig();
            if (printer.checkPaper()){
                printer.printChar(text);
            }else {
                if (null != mBeeper){
                    mBeeper.beep();
                }
            }
        }
    }

    //=========================================Led==========================================
    public void sendLedMsg(int mode, String str, ResponseCallBack callBack, Handler handler){
        if (mLooperThread == null
                || mLooperThread.getmHandler() == null || mLooperThread.isBusy()){
            if (callBack != null){
                callBack.onFailed();
            }
            return;
        }
        mLooperThread.setmLedTextMsg(mode,str,callBack,handler);
        mLooperThread.getmHandler().sendEmptyMessage(Types.TYPE_LED_TEXT);
    }

    public void showLedText(int label, String str){
        LED led = getLed();
        LedTextMsg msg = new LedTextMsg();
        msg.setShowType(label);
        msg.setStrNum(str);
        led.showLedText(msg.getShowType(),msg.getStrNum());
    }

    //=======================================QRReader===================================
    public void sendStartScannerMsg(ResultCallBack resultCallBack,Handler handler){
        if (mLooperThread == null
                || mLooperThread.getmHandler() == null || mLooperThread.isBusy()){
            if (resultCallBack != null){
                resultCallBack.onFailed();
            }
            return;
        }
        mLooperThread.setmResultCallBack(resultCallBack);
        mLooperThread.setmUiHander(handler);
        mLooperThread.getmHandler().sendEmptyMessage(Types.TYPE_QRREADER_START);
    }

    public void doStartScanner(final ResultCallBack callBack,Handler handler){
        int channel = Settings.getInstance(mContext).getmQRScanerChannel();
        int port = BaseConfig.CONFIG_TRAY_QRREADER_UART;
        QrReader.initQrReader(getDevice(channel),port);
        mIsScanning = true;
        try {
            while (mIsScanning) {
                String str = QrReader.startScan(getDevice(channel), port,2000);
                if (null != str && str.trim().length() <= 2) {
                    str = "";
                }
                if (str != null && str.length() > 0) {
                    // TODO 显示结果
                    final String showStr = str;
                    if (null != handler){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callBack != null){
                                    callBack.onStrResult(showStr);
                                }
                            }
                        });
                    }
                    mIsScanning = false;
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
            if (null != handler){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null){
                            callBack.onFailed();
                        }
                    }
                });
            }
        }finally {
            QrReader.stopScan(getDevice(channel), port);
            QrReader.closeQRScanner(getDevice(channel));
            mIsScanning = false;
        }
    }

    public void closeScannerMsg(){
        mIsScanning = false;
    }


    //==============================CachDrawer================================
    public void sendOpenDrawerMsg(ResponseCallBack callBack,Handler hander){
        if (mLooperThread == null
                || mLooperThread.getmHandler() == null || mLooperThread.isBusy()){
            if (callBack != null){
                callBack.onFailed();
            }
            return;
        }

        mLooperThread.setmResponseCallBack(callBack);
        mLooperThread.setmUiHander(hander);
        mLooperThread.getmHandler().sendEmptyMessage(Types.TYPE_OPEN_CACH_DRAWER);
    }

    public void openCachDrawer(){
        CashDrawer cashDrawer = getCahCashDrawer();
        cashDrawer.open();
    }

    public void onDestory(){
        if (mLooperThread == null
                || mLooperThread.getmHandler() == null){
            return;
        }

        mLooperThread.getmHandler().getLooper().quit();

    }


}
