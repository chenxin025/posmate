package com.cynoware.posmate.sdk.qrreader;

/**
 * Created by john on 2016/9/20.
 */

import com.cynoware.posmate.sdk.io.Device;


/**
 * Created by chx on 2016/9/20.
 */

public interface IScannerDefine {
    public boolean isExistScanner(Device device, int uart);

    public void initScanner(Device device, int uart);

    public String startScan(Device device, int uart, int ms);

    public void closeQRScanner(Device device);

}
