package com.cynoware.posmate.sdk.led;

import android.content.Context;

import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.UART;
import com.cynoware.posmate.sdk.lcd.LCD;

/**
 * Created by chenxin on 2017/4/17.
 * LED  support COM0 and COM1
 */

public class NP11LedImpl extends LED {

    private Device mDevice;
    private int mUartPort;
    public LCD mLcd;
    private Context mContext;


    public NP11LedImpl(Context context, Device device, int uart){
        mContext = context;
        mDevice = device;
        mUartPort = uart;
        mLcd = new LCD(device, uart, true);
    }

    @Override
    public int showLedText(int mode, String strNum,int port) {
        if (port == BaseConfig.COM_PORT_0) {
            mLcd.showBytesWithLCD(mContext, strNum);
            switch (mode) {
                case CMD_CHANGE_TYPE:
                    mLcd.lcdShowChange(mDevice);
                    break;

                case LED.CMD_PRICE_TYPE:
                    mLcd.lcdShowPrice(mDevice);
                    break;

                case LED.CMD_COLLECT_TYPE:
                    mLcd.lcdShowCollect(mDevice);
                    break;

                case LED.CMD_TOTAL_TYPE:
                    mLcd.lcdShowTotal(mDevice);
                    break;

                default:
                    mLcd.lcdShowNone(mDevice);
                    break;
            }
        }else if (port == BaseConfig.COM_PORT_1) {
            showExtLed(mode,strNum,1);
        }

        return 0;
    }

    private void showExtLed(int mode, String strNum, int port){
        mUartPort = port;
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
//        if( !mIsOnBoard )
//            Utils.msleep(100);

        UART.write(mDevice, mUartPort, acttext.getBytes());
        UART.write(mDevice, mUartPort, new byte[]{0x0D});
    }

    @Override
    public void closeLed() {

    }

    @Override
    public int[] getSupportComs() {
        int[] array = {BaseConfig.COM_PORT_0,BaseConfig.COM_PORT_1};
        return array;
    }
}
