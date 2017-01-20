package com.cynoware.posmate.sdk.configs;


/**
 * Created by john on 2016/9/20.
 */

public abstract class BaseConfig {

    public static final int CONFIG_PRINTER_UART = 5;
    public static final int CONFIG_TRAY_QRREADER_UART = 1;
    public static final int CONFIG_DOCK_QRREADER_UART = 3;
    public static final int CONFIG_ONBOARD_LED_UART = 6;
    public static final int CONFIG_LED_UART = 1;

    public static final int CONFIG_BEEPER_GPIO = 79;

    public static final int CONFIG_FRAME_2D_ENABLE = 39;   //PB7
    public static final int CONFIG_CASHDRAWER_GPIO = 65;

    public static final int SYS_DEV_VERSION_1 = 0;
    public static final int SYS_DEV_VERSION_2 = 1;

    private BaseConfig mAbstractConfig = null;
    protected DeviceConfigInfo mDeviceConfigInfos = null;

    private DeviceConfigInfo mPrinterInfo;
    private DeviceConfigInfo mScannerInfo;

    public static final int CHANNEL_DOCK_USB = 0;
    public static final int CHANNEL_DOCK_BT = 1;
    public static final int CHANNEL_TRAY_USB = 2;
    public static final int CHANNEL_TRAY_BT = 3;
    public static final int CHANNEL_CARD_USB = 4;
    public static final int CHANNEL_CARD_BT = 5;

    protected int mScannerChannel = CHANNEL_TRAY_USB;
    protected int mPrinterChannel = CHANNEL_DOCK_USB;
    protected int mLederChannel = CHANNEL_DOCK_USB;


    public abstract DeviceConfigInfo getPrinterConfigInfo();


    public abstract DeviceConfigInfo getScannerConfigInfo();

    public abstract DeviceConfigInfo getLedConfigInfo();

}
