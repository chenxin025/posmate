package com.cynoware.posmate.sdk.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cynoware.posmate.sdk.bean.LedTextMsg;
import com.cynoware.posmate.sdk.bean.PrintTextMsg;
import com.cynoware.posmate.sdk.listener.ResponseCallBack;
import com.cynoware.posmate.sdk.listener.ResultCallBack;

/**
 * Created by john on 2017/1/17.
 */

public class LooperThread extends Thread {

    private Context mContext = null;
    private Handler mHandler = null;
    private boolean mIsBusy = false;
    private DeviceManager mDeviceManager = null;

    private PrintTextMsg mPrintTextStr = null;
    private ResponseCallBack mCallBack = null;

    private ResultCallBack mResultCallBack = null;

    private LedTextMsg mLedTextMsg = null;
    private Handler mUiHander = null;

    public LooperThread(DeviceManager manager, Context context){
        mDeviceManager = manager;
        mContext = context;
    }

    public Handler getmHandler(){
        return mHandler;
    }

    @Override
    public void run() {
        Looper.prepare();

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                processMsg(msg);
            }
        };

        Looper.loop();
    }

    private void processMsg(Message msg){
        setBusy(true);
        switch (msg.what){
            case Types.TYPE_PRINTER_TEXT:
                mPrintTextStr = getmPrintTextStr();
                mDeviceManager.doPrintText(mPrintTextStr.getText());
                mCallBack = mPrintTextStr.getCallBack();
                mUiHander = mPrintTextStr.getUiHander();
                onSucceCalled(mUiHander,mCallBack);
                //setPrintTextStr(null,0,null);

                break;

            case Types.TYPE_LED_TEXT:
                mLedTextMsg = getmLedTextMsg();
                int label = mLedTextMsg.getShowType();
                String text = mLedTextMsg.getStrNum();
                mCallBack = mLedTextMsg.getCallBack();
                mUiHander = mLedTextMsg.getUiHandler();
                mDeviceManager.showLedText(label,text);
                onSucceCalled(mUiHander,mCallBack);
                break;

            case Types.TYPE_QRREADER_START:
                mResultCallBack = getmResultCallBack();
                mUiHander = getmUiHander();
                mDeviceManager.doStartScanner(mResultCallBack,mUiHander);
                break;

            case Types.TYPE_OPEN_CACH_DRAWER:
                mCallBack = getmResponseCallBack();
                mDeviceManager.openCachDrawer();
                if (null != mCallBack){
                    mCallBack.onSuccess();
                }
                break;
        }

        setBusy(false);
    }

    private void onSucceCalled(Handler handler,final ResponseCallBack callBack){
        if (handler == null || callBack == null){
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != callBack){
                    callBack.onSuccess();
                }
            }
        });
    }


    public void setBusy(boolean isBusy){
        mIsBusy = isBusy;
    }

    public boolean isBusy(){
        return mIsBusy;
    }

    public void setPrintTextStr(byte[] text, ResponseCallBack callBack, Handler handler){
        mPrintTextStr = new PrintTextMsg();
        mPrintTextStr.setText(text);
        mPrintTextStr.setCallBack(callBack);
        mPrintTextStr.setUiHander(handler);
    }

    public PrintTextMsg getmPrintTextStr(){
        return mPrintTextStr;
    }

    public LedTextMsg getmLedTextMsg() {
        return mLedTextMsg;
    }

    public void setmLedTextMsg(int mode, String text, ResponseCallBack callBack, Handler handler) {
        mLedTextMsg = new LedTextMsg();
        mLedTextMsg.setShowType(mode);
        mLedTextMsg.setStrNum(text);
        mLedTextMsg.setCallBack(callBack);
        mLedTextMsg.setUiHandler(handler);
    }

    public void setmResultCallBack(ResultCallBack callBack){
        mResultCallBack = callBack;
    }

    public ResultCallBack getmResultCallBack(){
        return mResultCallBack;
    }

    public void setmUiHander(Handler hander){
        mUiHander = hander;
    }

    public Handler getmUiHander(){
        return mUiHander;
    }

    public void setmResponseCallBack(ResponseCallBack responseCallBack){
        mCallBack = responseCallBack;
    }

    public ResponseCallBack getmResponseCallBack(){
        return mCallBack;
    }


}
