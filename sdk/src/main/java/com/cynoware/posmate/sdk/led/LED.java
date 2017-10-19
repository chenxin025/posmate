package com.cynoware.posmate.sdk.led;

/*
*     通道
*     1 Dock USB
*     2 Dock BLE
*     设备连接方式 1 板上  2 外部
*     外部接口使用
*     1 showLedText(int mode , String str)
 *      mode有五种类型 CMD_INIT_TYPE CMD_PRICE_TYPE  CMD_COLLECT_TYPE  CMD_CHANGE_TYPE  CMD_TOTAL_TYPE
 */



public abstract class LED {

	public static final int CMD_INIT_TYPE = 0;
	public static final int CMD_PRICE_TYPE = 1;
	public static final int CMD_COLLECT_TYPE = 2;
	public static final int CMD_CHANGE_TYPE = 3;
	public static final int CMD_TOTAL_TYPE = 4;

	public  abstract  int showLedText(int mode,String strNum, int port);
	public abstract void closeLed();
	public abstract int[] getSupportComs();
}
