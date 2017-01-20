package com.cynoware.posmate.sdk.bean;

import android.os.Handler;

import com.cynoware.posmate.sdk.listener.ResponseCallBack;

/**
 * Created by john on 2017/1/18.
 */

public class LedTextMsg {

    private int showType;
    private String strNum;
    private ResponseCallBack callBack;

    private Handler uiHandler;

    public String getStrNum() {
        return strNum;
    }

    public void setStrNum(String strNum) {
        this.strNum = strNum;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }

    public ResponseCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(ResponseCallBack callBack) {
        this.callBack = callBack;
    }

    public Handler getUiHandler() {
        return uiHandler;
    }

    public void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }
}
