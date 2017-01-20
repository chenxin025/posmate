package com.cynoware.posmate.sdk.service;

/**
 * Created by john on 2017/1/11.
 */

public interface OnStatusListener {

    void onTrayUsbAttached();

    void onTrayUsbDetached();


    void onDockUsbAttached();

    void onDockUsbDetached();



    void onBleAttached();
}
