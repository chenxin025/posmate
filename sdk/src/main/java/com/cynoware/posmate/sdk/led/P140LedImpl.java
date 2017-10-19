package com.cynoware.posmate.sdk.led;

import android.util.Log;

import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.io.SerialPort;
import com.cynoware.posmate.sdk.io.UART;
import com.cynoware.posmate.sdk.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by john on 2017/3/28.
 */

public class P140LedImpl extends LED {



    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream = null;

    public P140LedImpl(String path) throws IOException {

        //mSerialPort = SerialPort.getInstance(new File(path),2400,0);
        mSerialPort = new SerialPort(new File(path),2400,0);
        mOutputStream = mSerialPort.getOutputStream();

    }


    private void wrtieBuffer(String text){
        if( text == null || text.isEmpty() )
            return;

        String acttext = text;

        if(acttext.length() < 8){
            int charsize = 8-text.length();
            for(int i=0;i<charsize;i++){
                acttext = " " + acttext;
            }
        }

        try {
            mOutputStream.write(new byte[]{0x1B, 0x51, 0x41});
            mOutputStream.write(acttext.getBytes());
            mOutputStream.write( new byte[]{0x0D});
        } catch (IOException e) {
            Log.i("testt","******************************111");
            e.printStackTrace();
        }
    }

    @Override
    public int showLedText(int mode, String strNum,int port) {
        OutputStream outputStream = mSerialPort.getOutputStream();

        int result = 0;
        byte[] typeCmd = null;
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
            try {
                outputStream.write(typeCmd);
            } catch (IOException e) {
                Log.i("testt","******************************222");
                e.printStackTrace();
            }
            wrtieBuffer(strNum);
        }

        return 0;
    }

    @Override
    public void closeLed() {
        if (null != mSerialPort){
            mSerialPort.closeSerialPort();
        }
    }

    @Override
    public int[] getSupportComs() {
        int[] array = {BaseConfig.COM_PORT_1,BaseConfig.COM_PORT_2};
        return array;
    }


}
