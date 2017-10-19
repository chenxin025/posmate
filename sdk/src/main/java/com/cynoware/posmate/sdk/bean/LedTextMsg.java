package com.cynoware.posmate.sdk.bean;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.cynoware.posmate.sdk.listener.ResponseCallBack;

import java.io.Serializable;

/**
 * Created by john on 2017/1/18.
 */

public class LedTextMsg implements Parcelable {



    private int comId;

    private int showType;

    private String strNum;

    private ResponseCallBack callBack;

    private Handler uiHandler;



    public static final Creator<LedTextMsg> CREATOR = new Creator<LedTextMsg>() {
        @Override
        public LedTextMsg createFromParcel(Parcel in) {
            LedTextMsg msg =  new LedTextMsg();
            msg.setComId(in.readInt());
            msg.setShowType(in.readInt());
            msg.setStrNum(in.readString());
            return msg;
        }

        @Override
        public LedTextMsg[] newArray(int size) {
            return new LedTextMsg[size];
        }
    };

    public int getComId() {
        return comId;
    }

    public void setComId(int comId) {
        this.comId = comId;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(comId);
        dest.writeInt(showType);
        dest.writeString(strNum);
    }
}
