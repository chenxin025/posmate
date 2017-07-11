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

import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.UART;
import com.cynoware.posmate.sdk.util.Utils;

public class NP10LedImpl extends LED{

    public static final byte[] CMD_INIT = {0x1B, 0x40, 0x0C};
    public static final byte[] CMD_PRICE = {0x1B,0x73,0x31};
    public static final byte[] CMD_COLLECT = {0x1B, 0x73, 0x33};
    public static final byte[] CMD_CHANGE = {0x1B, 0x73, 0x34};
    public static final byte[] CMD_TOTAL = {0x1B, 0x73, 0x32};




	private Device mDevice;
	private int mUartPort;
	private boolean mIsOnBoard;

	public NP10LedImpl(Device device, int uart, boolean isOnBoard ){
		mDevice = device;
		mUartPort = uart;
		mIsOnBoard = isOnBoard;
	}

	private void config( ){
		UART.setConfig(mDevice, mUartPort, 2400, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
		UART.fixedWrite(mDevice, mUartPort, new byte[]{0x1B, 0x40});
    	if(!mIsOnBoard){
    	//    Utils.msleep(500);
    	}
	}

	
	private void showSpecial( byte[] special  ){
		
		if( special == null )
			return;		
		
   	 	UART.fixedWrite(mDevice, mUartPort, special);
        if(!mIsOnBoard){
        	//Utils.msleep(500);
        }		
	}
	
	
	private void showText( String text ){
		
   	 	if( text == null || text.isEmpty() )
   	 		return;
   	 	
   	 	String acttext = text;
   	 		
	   	if(acttext.length() < 8){
	   		int charsize = 8-text.length();
	   		for(int i=0;i<charsize;i++){
	   			acttext = " " + acttext;
	   		}
	   	}
	   	 	
	   	UART.fixedWrite(mDevice, mUartPort, new byte[]{0x1B, 0x51, 0x41});
	    if( !mIsOnBoard )
        	Utils.msleep(100);
	        
	   	UART.write(mDevice, mUartPort, acttext.getBytes());
	   	UART.write(mDevice, mUartPort, new byte[]{0x0D});
	}

	public int showLedText(int mode,String strNum, int port){
		//do config
		switch (port){
			case BaseConfig.COM_PORT_0:
				mUartPort = BaseConfig.NP10_OR_NP11_LED_PRORT_INNER;
				break;

			case BaseConfig.COM_PORT_1:
				mUartPort = BaseConfig.NP10_OR_NP11_PRORT_COM1;
				break;

			case BaseConfig.COM_PORT_2:
				mUartPort = BaseConfig.NP10_OR_NP11_PRORT_COM2;
				break;

			default:
				mUartPort = -1;
				break;

		}
		if (mUartPort == -1){
			return -1;
		}

		UART.setConfig(mDevice, mUartPort, 2400, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
		UART.fixedWrite(mDevice, mUartPort, new byte[]{0x1B, 0x40});

		byte[] typeCmd = null;
		int result = 0;
		switch (mode) {
			case CMD_INIT_TYPE:
				typeCmd = NP10LedImpl.CMD_INIT;
				break;

			case CMD_PRICE_TYPE:
				typeCmd = NP10LedImpl.CMD_PRICE;
				break;

			case CMD_COLLECT_TYPE:
				typeCmd = NP10LedImpl.CMD_COLLECT;
				break;

			case CMD_CHANGE_TYPE:
				typeCmd = NP10LedImpl.CMD_CHANGE;
				break;

			case CMD_TOTAL_TYPE:
				typeCmd = NP10LedImpl.CMD_TOTAL;
				break;

			default:
				result = -1;
				break;
		}

		if (result == 0 ){
			UART.fixedWrite(mDevice, mUartPort, typeCmd);
			showText(strNum);
		}

		return result;
	}

	@Override
	public void closeLed() {

	}

	@Override
	public int[] getSupportComs() {
		int[] array = {BaseConfig.COM_PORT_0,BaseConfig.COM_PORT_1,BaseConfig.COM_PORT_2};
		return array;
	}
}
