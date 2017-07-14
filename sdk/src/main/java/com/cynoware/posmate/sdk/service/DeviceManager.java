package com.cynoware.posmate.sdk.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cynoware.posmate.sdk.SDKInfo;
import com.cynoware.posmate.sdk.bean.LedTextMsg;
import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.configs.PosConstant;
import com.cynoware.posmate.sdk.configs.Settings;
import com.cynoware.posmate.sdk.drawer.CashDrawer;
import com.cynoware.posmate.sdk.drawer.NP10Drawer;
import com.cynoware.posmate.sdk.drawer.P140Drawer;
import com.cynoware.posmate.sdk.io.Beeper;
import com.cynoware.posmate.sdk.io.BleDevice;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.HidDevice;
import com.cynoware.posmate.sdk.led.LED;
import com.cynoware.posmate.sdk.led.NP10LedImpl;
import com.cynoware.posmate.sdk.led.NP11LedImpl;
import com.cynoware.posmate.sdk.led.P140LedImpl;
import com.cynoware.posmate.sdk.listener.ResponseCallBack;
import com.cynoware.posmate.sdk.listener.ResultCallBack;
import com.cynoware.posmate.sdk.printer.Printer;
import com.cynoware.posmate.sdk.qrreader.QrReader;
import com.cynoware.posmate.sdk.util.SharePrefManager;

import java.io.IOException;


/**
 * Created by john on 2017/1/17.
 */

public class DeviceManager {

    private static DeviceManager sDeviceManager = null;
    private Context mContext = null;
    private static LooperThread mLooperThread = null;

    private Printer mPrinter = null;
    private LED mLed = null;

    //Order to show mlitu led in the same time
    private LED mP140COM1 = null;
    private LED mP140COM2 = null;

    private CashDrawer mCahCashDrawer = null;

    private HidDevice mDockUSBDevice = null;
    private HidDevice mTrayUSBDevice = null;
    private BleDevice mDockBTDevice = null;

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
        mDockBTDevice = new BleDevice(context);
    }

    public void  setmLed(LED led){
        mLed = led;
    }

    public void setmCahCashDrawer( CashDrawer cashDrawer){
        mCahCashDrawer = cashDrawer;
    }

    public void dstroyInstance(){
        if (sDeviceManager != null){
            sDeviceManager = null;
        }
    }

    public Device getDevice(int channel) {
        switch (channel) {
            case PosConstant.CHANNEL_DOCK_USB:
                return mDockUSBDevice;

            case PosConstant.CHANNEL_DOCK_BT:
                return mDockBTDevice;

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

    public int[] getComIds(){
        return getComNums();
    }

    private int[] getComNums(){
        LED led = null;
        int type  = SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SET,2);
        led = getLed(type);
        if (led instanceof P140LedImpl){

        }
        return  led.getSupportComs();
    }



    private LED getLed(int posWhich){
        if (mLed == null && (posWhich == SDKInfo.POS_SET_NP10 ||
                posWhich == SDKInfo.POS_SET_NP11)){

            if (posWhich == SDKInfo.POS_SET_NP10) {
                int channel = Settings.getInstance(mContext).getmLEDChannel();
                Device device = getDevice(channel);
                mLed = new NP10LedImpl(device,BaseConfig.CONFIG_ONBOARD_LED_UART,true);
            }else  if (posWhich == SDKInfo.POS_SET_NP11){
                int channel = Settings.getInstance(mContext).getmLEDChannel();
                Device device = getDevice(channel);
                mLed = new NP11LedImpl(mContext,device,BaseConfig.CONFIG_ONBOARD_LED_UART);

            } else{

//                int id = SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SERIALPORT_ID,SDKInfo.SERIAL_INDEX1);
//                String path = null;
//                if (id == SDKInfo.SERIAL_INDEX1){
//                    path = "/dev/ttymxc1";
//                }else if (id == SDKInfo.SERIAL_INDEX2){
//                path = "/dev/ttymxc2";
//            }
//            try {
//                mLed = new P140LedImpl(path);
//            } catch (IOException e) {
//                    e.printStackTrace();
//
//                }
//
            }

        }else{
            String path = null;
            int id = SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SERIALPORT_ID,SDKInfo.SERIAL_INDEX1);
            Log.i("testg","#################"+id);
            if (id == SDKInfo.SERIAL_INDEX1){
                path = "/dev/ttymxc1";
                if (mP140COM1 == null){
                    try {
                        Log.i("testg","##########222#######"+id);
                        mP140COM1 = new P140LedImpl(path);
                    } catch (IOException e) {
                        Log.i("testg","############33#####");
                        e.printStackTrace();
                    }
                }
                return mP140COM1;


            }else if (id == SDKInfo.SERIAL_INDEX2){
                path = "/dev/ttymxc2";
                if (mP140COM2 == null){
                    try {
                        mP140COM2 = new P140LedImpl(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return mP140COM2;

            }

        }
        return mLed;
    }

    public void setP140ComId(int which){
        SharePrefManager.getInstance().putInt(SDKInfo.PREF_POS_SERIALPORT_ID,which);
    }

    private CashDrawer getCahCashDrawer(int posWhich){
        if (mCahCashDrawer == null){
            if (posWhich == SDKInfo.POS_SET_NP10) {
                int channel = Settings.getInstance(mContext).getmCashDrawerChannel();
                mCahCashDrawer = new NP10Drawer(getDevice(channel), BaseConfig.CONFIG_CASHDRAWER_GPIO);
            }else if (posWhich  == SDKInfo.POS_SET_P140){
                mCahCashDrawer = new P140Drawer();
            }
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
    public void sendLedMsg(int comID,int mode, String str, ResponseCallBack callBack, Handler handler){
        if (mLooperThread == null
                || mLooperThread.getmHandler() == null || mLooperThread.isBusy()){
            if (callBack != null){
                callBack.onFailed();
            }
            return;
        }
        mLooperThread.setmLedTextMsg(comID,mode,str,callBack,handler);
        //mLooperThread.getmHandler().sendEmptyMessage(Types.TYPE_LED_TEXT);
        LedTextMsg msg = new LedTextMsg();
        msg.setComId(comID);
        msg.setShowType(mode);
        msg.setStrNum(str);
        Message message = new Message();
        message.obj = msg;
        message.what = Types.TYPE_LED_TEXT;
        mLooperThread.getmHandler().sendMessageDelayed(message,100);
        //mLooperThread.getmHandler().sendEmptyMessageDelayed(Types.TYPE_LED_TEXT,100);
    }

    public int  showLedText(int comId, int label, String str){

        Log.i("testg","===============comId============"+comId);
        SharePrefManager.getInstance().putInt(SDKInfo.PREF_POS_SERIALPORT_ID,comId);

        int type  = SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SET,2);
        LED led = getLed(type);
        if (led == null){
            return SDKInfo.ERROR_CODE;
        }
        LedTextMsg msg = new LedTextMsg();

        msg.setComId(comId);
        msg.setShowType(label);
        msg.setStrNum(str);
        led.showLedText(msg.getShowType(),msg.getStrNum(),comId);
        return SDKInfo.SUCCESS_CODE;
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


    public void doScann(final ResultCallBack callBack){
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
                    if(callBack != null){
                      callBack.onStrResult(showStr);
                    }

                    mIsScanning = false;
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
            if (callBack != null){
                callBack.onFailed();
            }
        }finally {
            QrReader.stopScan(getDevice(channel), port);
            QrReader.closeQRScanner(getDevice(channel));
            mIsScanning = false;
        }
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
        int type  = SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SET,2);
        CashDrawer cashDrawer = getCahCashDrawer(type);
        if (null != cashDrawer) {
            cashDrawer.open();
        }
    }

    //
    public String getDockBleAddr(Context context){
        return Settings.getInstance(context).getmDockBleAddr();
    }

    public String getDockBleName(Context context){
        return Settings.getInstance(context).getmDockBleName();
    }

    public void onDestory(){
        if (mLooperThread == null
                || mLooperThread.getmHandler() == null){
            return;
        }

        mLooperThread.getmHandler().getLooper().quit();

        int type  = SharePrefManager.getInstance().getInt(SDKInfo.PREF_POS_SET,2);
        if (DeviceManager.getInstance(mContext).getLed(type) != null) {
            DeviceManager.getInstance(mContext).getLed(type).closeLed();
        }

    }


}
