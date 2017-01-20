package com.cynoware.posmate.sdk.bean;

import android.os.Handler;

import com.cynoware.posmate.sdk.listener.ResponseCallBack;

import java.io.Serializable;

/**
 * Created by john on 2017/1/17.
 */

public class PrintTextMsg {



    private byte[] text;

    private int charType;

    private ResponseCallBack callBack;

    private Handler uiHander;

    public byte[] getText() {
        return text;
    }

    public void setText(byte[] text) {
        this.text = text;
    }

    public int getCharType() {
        return charType;
    }

    public void setCharType(int charType) {
        this.charType = charType;
    }



    public ResponseCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(ResponseCallBack callBack) {
        this.callBack = callBack;
    }

    public Handler getUiHander() {
        return uiHander;
    }

    public void setUiHander(Handler uiHander) {
        this.uiHander = uiHander;
    }
}
