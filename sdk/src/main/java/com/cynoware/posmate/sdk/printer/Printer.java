package com.cynoware.posmate.sdk.printer;

import android.graphics.Bitmap;

import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.cmd.EscCommand;
import com.cynoware.posmate.sdk.io.UART;
import com.cynoware.posmate.sdk.configs.BaseConfig;
import com.cynoware.posmate.sdk.configs.ConfigFactory;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;


/**
 *    通信通道
 *    1 Dock USB
 *    2 Dock BLE
 *    打印机类型 TYPE_YANKE TYPE_JINGXIN_1 ...
 *    支持的图片打印方式
 *    1 NV打印
 *    2 光栅打印
 *
 *    提供外部调用的接口
 *    1 doConfig 配置波特率
 *    2 checkPaper 检查是否有纸
 *    3 downloadNVBitmap NV方式 download
 *      该方式对图片要求有限制不超过8k，可通过 getBitmapSizeWithKB 获取大小
 *    4.1 printChar    字符打印 支持中英日韩繁体中文
 *    4.2 printBitmap 图片打印
 *    4.3 printBarcode 条形码打印
 *
 *
 */

public class Printer {

	private static final String TAG = "Printer";

	private static IPrinterDefine mPrinterInstance = null;
	private static final String BLANK_ROWS = "\n";

	public static final int TYPE_PRINTF_BITMAP_0 = 0;
	public static final int TYPE_PRINTF_BITMAP_1 = 1;

	public static final int TYPE_YANKE = 0;
	public static final int TYPE_JINGXIN_1 = 1;
	public static final int TYPE_JINGXIN_2 = 2;
	public static final int TYPE_JINGXIN_3 = 3;
	public static final int TYPE_JINGXIN_4 = 4;
	public static final int TYPE_NP10 = 5;

	public static int CHARSET_TYPE;
	public static final int CHARSET_ENGLISH = 0;
	public static final int CHARSET_KOREAN = 1;
	public static final int CHARSET_JAPANESE = 2;
	public static final int CHARSET_CHINESE = 3;
	 
    private static final byte[] CMD_1C_21_00 = { 0x1C, 0x21, 0x00};
    private static final byte[] CMD_1C_21_01 = { 0x1C, 0x21, 0x01};

	public static final int SELECT_LEFT_MODE = 0;
	public static final int SELECT_CENTER_MODE = 1;
	public static final int SELECT_RIGHT_MODE = 2;

	private Device mDevice;
	private int mUartPort;
	private int mType;
	private int mBaudrate;

	private synchronized  static IPrinterDefine getInstance(){
		int type = ConfigFactory.getConfigType().getPrinterConfigInfo().mDevVersion;
		SDKLog.i(TAG,"IScannerDefine getInstance "+type);

		switch (type){
			case BaseConfig.SYS_DEV_VERSION_1:
				if (null == mPrinterInstance){
					mPrinterInstance = new NP10PrinterImpl();
				}
				break;

			case BaseConfig.SYS_DEV_VERSION_2:
				if (null == mPrinterInstance){
					mPrinterInstance = new NP10PrinterImpl();
				}
				break;

			default:
				break;
		}
		return mPrinterInstance;
	}

	public Printer(Device device, int uart, int type){
		mDevice = device;
		mUartPort = uart;
		mType = type;
		mBaudrate = getBaudrate( type );
	}


	
	private int getBaudrate( int type ){
		switch (type) {
		case TYPE_YANKE:
			return 9600;

		case TYPE_JINGXIN_1:
			return 115200;

		case TYPE_JINGXIN_2:
			return 9600;

		case TYPE_JINGXIN_3:
			return 115200;

		case TYPE_JINGXIN_4:
			return 115200;

		case TYPE_NP10:
			return 230400;
			
		default:
			return 115200;
		}
	}

	public void doConfig(){
		getInstance().doConfig(mDevice, mUartPort, mBaudrate, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
	}
	
	public void printChar( byte[] text){
		getInstance().printChar(mDevice,mUartPort,text);
	}

	//printBarcode
	public void printBarcode( String str ){
		getInstance().printBarcode(mDevice,mUartPort,str);
	}

	//Print QRcode
	public void printQRcode( String str ){

//		String text = str.toUpperCase();
//		//text = text.replace( "/[^-0-9A-Z. $/+%]/", "");
//		if( !text.equals(str) ){
//			return;
//		}

		if( str.isEmpty() )
			return;

		EscCommand escCmd = new EscCommand();


		escCmd.setQRSize((byte) 0x06);
		escCmd.addQRCodePrint(str.getBytes());
		escCmd.QRPrint();

		byte[] buf = escCmd.createCommandBuffer();

		UART.fixedWrite( mDevice, mUartPort, buf );
	}

	public void downloadNVBitmap(Bitmap bmp){
		getInstance().downloadNVBitmap(mDevice,mUartPort,bmp);
	}

	public void printNVBitmap(){
		UART.clearBuffer(mDevice, mUartPort);

		EscCommand escCmd = new EscCommand();
		escCmd.addPrintNvBitmap((byte) 0x01, (byte) 0x00);
		byte[] buf = escCmd.createCommandBuffer();
        UART.fixedWrite( mDevice, mUartPort, buf );     
	}

	public void flush(){
		UART.clearBuffer(mDevice, mUartPort);
	}
	
	public boolean checkPaper(){
		boolean flag = getInstance().checkPaper(mDevice,mUartPort);
		return flag;
	}
	
	
	/*private final byte[] INITIALIZE_PRINTER = new byte[]{0x1B,0x40};

    private final byte[] PRINT_AND_FEED_PAPER = new byte[]{0x0A};

    private final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte)0x1B, (byte)0x2A};
    private final byte[] SET_LINE_SPACING = new byte[]{0x1B, 0x33};*/

    
    private byte[] buildPOSCommand(byte[] command, byte... args) {
        byte[] posCommand = new byte[command.length + args.length];

        System.arraycopy(command, 0, posCommand, 0, command.length);
        System.arraycopy(args, 0, posCommand, command.length, args.length);

        return posCommand;
    }

	public void downloadNVBitmap1(Bitmap bmp){

		if( bmp == null )
			return;

		int width = bmp.getWidth();
		int height = bmp.getHeight();

		byte xL = (byte)(width % 256);
		byte xH = (byte)(width / 256);
		byte yL = (byte)(height % 256);
		byte yH = (byte)(height / 256);

		UART.clearBuffer(mDevice, mUartPort);

		byte[] escFSQ = new byte[] { 0x1C, 0x71, 0x01, xL, xH, yL, yH };

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteBmp = stream.toByteArray();

		byte[] final_buf = new byte[escFSQ.length + byteBmp.length];
		int count = 0;

		System.arraycopy(escFSQ, 0, final_buf, 0,  escFSQ.length );
		count += escFSQ.length;

		System.arraycopy(byteBmp, 0, final_buf, count, byteBmp.length );
		count += byteBmp.length;

		UART.fixedWrite( mDevice, mUartPort, final_buf );
	}

	public void printFastBitmap(Bitmap bitmap){
		UART.clearBuffer(mDevice, mUartPort);

		EscCommand escCmd = new EscCommand();
		escCmd.addRastBitImageParams(bitmap);

		byte[] buf = escCmd.createCommandBuffer();

		UART.fixedWrite( mDevice, mUartPort, buf );
	}

	public void printBitmap(final int whichType, final Bitmap bmp){
		getInstance().printBitmap(mDevice,mUartPort,whichType,bmp);
	}


	public static double getBitmapSizeWithKB(Bitmap btp){
		if (null == btp){
			return 0;
		}
		long size = btp.getByteCount()/8;
		DecimalFormat df = new DecimalFormat("#.00");
		double sizeLong = Double.valueOf(df.format((double) size / 1024));
		return sizeLong;
	}
}
