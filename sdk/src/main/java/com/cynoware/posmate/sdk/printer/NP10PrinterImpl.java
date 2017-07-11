package com.cynoware.posmate.sdk.printer;

import android.graphics.Bitmap;

import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.cmd.EscCommand;
import com.cynoware.posmate.sdk.io.UART;

import java.nio.charset.Charset;

/**
 * Created by john on 2016/10/10.
 */

public class NP10PrinterImpl implements IPrinterDefine {

    public static final int CHARSET_ENGLISH = 0;
    public static final int CHARSET_KOREAN = 1;
    public static final int CHARSET_JAPANESE = 2;
    public static final int CHARSET_CHINESE = 3;

    public static final int TYPE_PRINTF_BITMAP_0 = 0;
    public static final int TYPE_PRINTF_BITMAP_1 = 1;

    @Override
    public void doConfig(Device device, int uart, int baudrate, int dataBits, int parity, int stopBits, int flowCtrl) {
        UART.setConfig(device, uart, baudrate, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
    }


    @Override
    public boolean checkPaper(Device device, int uart) {
        UART.clearBuffer(device, uart);
        EscCommand escCommand = new EscCommand();

        escCommand.addCheckPaper();
        byte[] buf = escCommand.createCommandBuffer();
        UART.fixedWrite(device, uart, buf);

        byte[] read = new byte[1];
        int res = UART.fixedRead(device, uart, read, 0, 1, 10000);
        return res != 0 && read[0] == 1;
    }

    @Override
    public void printBarcode(Device device, int uart, String str) {
        String text = str.toUpperCase();
        //text = text.replace( "/[^-0-9A-Z. $/+%]/", "");
        if( !text.equals(str) ){
            return;
        }
        if( text.isEmpty() )
            return;

        EscCommand escCmd = new EscCommand();
        escCmd.addBarCodeWidth( (byte)0x02 );
        escCmd.addBarCodeHeight((byte) 0x50);
        escCmd.addBarCodeLayout(EscCommand.BARCODE_TEXT_BELOW);
        try {
            escCmd.addBarCodePrint( EscCommand.BARCODE_CODE_39, (byte)0x00, text.getBytes() );
        } catch (EscCommand.WrongParaException e) {
            e.printStackTrace();
        }
        byte[] buf = escCmd.createCommandBuffer();
        UART.fixedWrite(device, uart, buf );
    }


    @Override
    public void printChar(Device device, int uart,byte[] buf) {
        String charsetName = null;                                            //编码方式
        UART.clearBuffer(device, uart);
//        EscCommand escCmd = new EscCommand();
//        escCmd.addInitializePrinter();
//
//        if (charset == CHARSET_KOREAN || charset == CHARSET_CHINESE) {
//            charsetName = "KS_C_5601-1987";
//            escCmd.addSetCharset(EscCommand.CHARSET_KOREAN);        //设置打印字符
//        } else if (charset == CHARSET_JAPANESE) {
//            charsetName = "Shift_JIS";
//            escCmd.addSetCharset(EscCommand.CHARSET_JAPANESE);            //设置打印字符
//        }
//
//        // Process encoding.
//        byte[] text_bytes;
//        if( charsetName != null ){
//            Charset cs = Charset.forName(charsetName);    	//返回一个指定字符格式的CharSet
//            text_bytes = text.getBytes(cs);					//编码字符串转换成一个使用给定字符集的字节序列，并将结果存储到一个新的字节数组
//        }else{												//byte[] getBytes(Charset charset)
//            text_bytes = text.getBytes();
//        }
//        escCmd.add(text_bytes);
//        byte[] buf = escCmd.createCommandBuffer();
        UART.fixedWrite( device, uart, buf );
    }

    @Override
    public void printBitmap(Device device, int uart, int whichType, Bitmap bmp) {
        switch (whichType){
            case TYPE_PRINTF_BITMAP_0:
                printNVBitmap(device,uart);
                break;
            case TYPE_PRINTF_BITMAP_1:
                if (null != bmp){
                    printFastBitmap(device,uart,bmp);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void downloadNVBitmap(Device device, int uart, Bitmap bmp) {

        if( bmp == null )
            return;
//        int width = bmp.getWidth();
//        int height = bmp.getHeight();
//        byte xL = (byte)(width % 256);
//        byte xH = (byte)(width / 256);
//        byte yL = (byte)(height % 256);
//        byte yH = (byte)(height / 256);

        Bitmap[] bmps = { bmp };
        EscCommand escCmd = new EscCommand();
        escCmd.addDownloadNvBitImage(bmps);
        UART.clearBuffer(device, uart);
        byte[] buf = escCmd.createCommandBuffer();
        UART.fixedWrite( device, uart, buf );
    }

    private void printNVBitmap(Device dev, int port){
        UART.clearBuffer(dev, port);
        EscCommand escCmd = new EscCommand();
        escCmd.addPrintNvBitmap((byte) 0x01, (byte) 0x00);
        byte[] buf = escCmd.createCommandBuffer();
        UART.fixedWrite( dev, port, buf );
    }

    private void printFastBitmap(Device dev, int port,Bitmap bitmap){
        UART.clearBuffer(dev, port);
        EscCommand escCmd = new EscCommand();
        escCmd.addRastBitImageParams(bitmap);
        byte[] buf = escCmd.createCommandBuffer();
        UART.fixedWrite( dev, port, buf );
    }

}
