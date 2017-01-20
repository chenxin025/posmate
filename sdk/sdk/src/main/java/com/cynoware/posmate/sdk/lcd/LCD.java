package com.cynoware.posmate.sdk.lcd;

/*
*     通道
*     1 Dock USB
*     2 Dock BLE
*     设备连接方式 1 板上  2 外部
*     外部接口使用
*     1 showLedText(int mode , String str)
 *      mode有五种类型 CMD_INIT_TYPE CMD_PRICE_TYPE  CMD_COLLECT_TYPE  CMD_CHANGE_TYPE  CMD_TOTAL_TYPE
 */

import android.content.Context;

import com.cynoware.posmate.sdk.SDKLog;
import com.cynoware.posmate.sdk.io.Device;
import com.cynoware.posmate.sdk.io.UART;
import com.cynoware.posmate.sdk.util.Utils;
import com.cynoware.posmate.sdk.cmd.Cmds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class LCD {

	private static final String TAG = "LCD";
	public static final byte[] CMD_LCD_STR = {0x02,0x0e,0x01};

    public static final byte[] CMD_INIT = {0x1B, 0x40, 0x0C};
    public static final byte[] CMD_PRICE = {0x1B,0x73,0x31};
    public static final byte[] CMD_COLLECT = {0x1B, 0x73, 0x33};
    public static final byte[] CMD_CHANGE = {0x1B, 0x73, 0x34};
    public static final byte[] CMD_TOTAL = {0x1B, 0x73, 0x32};

	public static final int CMD_LCD=0x0E;
	public static final int SUBCMD_LCD_STR_None = 0x00;
	public static final int SUBCMD_LCD_STR_Price = 0x01;
	public static final int SUBCMD_LCD_STR_Total = 0x02;
	public static final int SUBCMD_LCD_STR_Collect = 0x03;
	public static final int SUBCMD_LCD_STR_Change = 0x04;
	public static final int SUBCMD_LCD_STR = 0x05;
	public static final int SUBCMD_LCD_STR_Start =0x06;
	public static final int SUBCMD_LCD_STR_End =0x07;

	public static final int CMD_INIT_TYPE = 0;
	public static final int CMD_PRICE_TYPE = 1;
	public static final int CMD_COLLECT_TYPE = 2;
	public static final int CMD_CHANGE_TYPE = 3;
	public static final int CMD_TOTAL_TYPE = 4;


	private Device mDevice;
	private int mUartPort;
	private boolean mIsOnBoard;

	public LCD(Device device, int uart, boolean isOnBoard ){
		mDevice = device;
		mUartPort = uart;
		mIsOnBoard = isOnBoard;
	}

	public static int fixedWrite(Device device, int uart, byte[] data, int offset, int len){
		int size = 0;
		len=256;
//
//		int h1= len  % 256;
//		int h2 = len / 256;
//		byte buf4 = (byte)h1;
//		byte buf5 = (byte)h2;

		while(len > 0){

			if(device.getCloseFlag() || device.isBroken() )
				break;

			int ret = write(device, uart, data, offset, len);
			//SDKLog.i(TAG,"fixedWrite    START ===ret="+ret);
			size += ret;
			offset += ret;
			len -= ret;
		}
		return size;
	}

	public static  int writeStart(Device device){
		//SDKLog.i( TAG, "Write  len " + len );

		//int fixLen = (len > Cmds.MAX_CMD_SIZE - 6 ? (Cmds.MAX_CMD_SIZE - 6) : len);
		//SDKLog.i( TAG, "Write  fixlen " + fixLen );
		// int sum = Utils.cksum(data, offset, len);

		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR_Start;

		synchronized(device){
			device.writeData(buf,3);
			//device.writeData(Buf, 6 + fixLen);
			device.readData(buf);
		}

		// int sum2 = Utils.byte2int_le(Buf, 4);
		//assert(sum == sum2);

		int writed_bytes_num  = (int)(buf[3] & 0xff);
		SDKLog.i( TAG, "Write writed_bytes_num " + writed_bytes_num );
		return writed_bytes_num;
	}

	/**
	 *added by pu
     */
	public static int writeEnd(Device device){
		//SDKLog.i( TAG, "Write  len " + len );

		//int fixLen = (len > Cmds.MAX_CMD_SIZE - 6 ? (Cmds.MAX_CMD_SIZE - 6) : len);
		//SDKLog.i( TAG, "Write  fixlen " + fixLen );
		// int sum = Utils.cksum(data, offset, len);

		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR_End;

		synchronized(device){
			device.writeData(buf,3);
			//device.writeData(Buf, 6 + fixLen);
			device.readData(buf);
		}

		// int sum2 = Utils.byte2int_le(Buf, 4);
		//assert(sum == sum2);

		int writed_bytes_num  = (int)(buf[3] & 0xff);
		SDKLog.i( TAG, "Write writed_bytes_num " + writed_bytes_num );
		return writed_bytes_num;
	}

	public static  int write(Device device, int uart, byte[] data, int offset, int len){
		SDKLog.i( TAG, "Write  len " + len );

		int fixLen = (len > Cmds.MAX_CMD_SIZE - 4 ? (Cmds.MAX_CMD_SIZE - 4) : len);
//		SDKLog.i( TAG, "Write  fixlen " + fixLen );
		// int sum = Utils.cksum(data, offset, len);

		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR;
		buf[3] = (byte) fixLen;
		//int h1= len  % 256;
		//int h2 = len  / 256;


//		SDKLog.i( TAG, "Write Buf[4] =  " + Buf[4] );
//		SDKLog.i( TAG, "Write Buf[5] = " + Buf[5] );
		System.arraycopy(data, offset, buf, 4, fixLen);

		synchronized(device){
			device.writeData(buf, 4 + fixLen);
			device.readData(buf);
		}
		int writed_bytes_num  = (int)(buf[3] & 0xff);
		SDKLog.i( TAG, "Write writed_bytes_num " + writed_bytes_num );
		return writed_bytes_num;
	}

	public byte[] getBytes(short s, boolean bBigEnding) {
		byte[] buf = new byte[2];
		if (bBigEnding)
			for (int i = buf.length - 1; i >= 0; i--) {
				buf[i] = (byte) (s & 0x00ff);
				s >>= 8;
			}
		else
			for (int i = 0; i < buf.length; i++) {
				buf[i] = (byte) (s & 0x00ff);
				s >>= 8;
			}
		return buf;
	}

	public void showBytesWithLCD(Context context,String str){

//		byte[] pbuf = new byte[16];
//		for (int i = 0; i < 16; i++){
//			pbuf[i] = (byte) 0xff;
//		}
//		write(mDevice,0,pbuf,0,16);
////

		int strLen = str.length();   //strlen = 2
		byte[] allByte = new byte[256];    //allbyte = 36
		for (int x = 0; x < 256; x++){
			allByte[x] = 0;
		}


		byte[] desBytes = str.getBytes();   //desbytes = 3
		SDKLog.i(TAG,"*********************showBytesWithLCD*******"+desBytes);
		if (null == desBytes){
			return;
		}


		writeStart(mDevice);
		int num = desBytes.length;   // num = 3
		int count = 0;
		for (int i = 0; i < num; i++){

			byte[] sendBuf = getAscCode(context,desBytes[i]);

			System.arraycopy(sendBuf,0,allByte,count,8);
			System.arraycopy(sendBuf,8,allByte,count+128,8);
			SDKLog.i(TAG,"*********************showdesBytes*******"+i+" "+sendBuf[i]);
			count += 8;
		}
		/**
		 * revised by pu
		 */
//		for (int i = 0; i < 256 ; i++){
//			SDKLog.i(TAG,"%%%%%%%%%%%%%%%%%%%%%======" + i + "  " +byte2hex(allByte[i]));
//		}

		fixedWrite(mDevice,0,allByte,0,16*strLen);


		writeEnd(mDevice);

	}




	private static String byte2hex(byte buffer){
		String h = "";

		for(int i = 0; i < 1; i++){
			String temp = Integer.toHexString(buffer & 0xFF);
			if(temp.length() == 1){
				temp = "0" + temp;
			}
			h = h + " "+ temp;
		}

		return h;

	}

	private byte[] getAscCode (Context context, byte oneChar)  {
		byte[] desByte = new byte[16];

/**
 * revised by pu
 */
		try {
			InputStream in = context.getAssets().open("ASC16");
			int beginIndex = oneChar*16 + 1;
			in.skip(beginIndex);
			in.read(desByte);
		} catch (IOException e) {
			e.printStackTrace();
		}
/**
 */
//		RandomAccessFile randomFile ;
//		try {
//			randomFile = new RandomAccessFile(path, "r");
//			if (randomFile == null){
//				return null;
//			}
//
//			int beginIndex = oneChar*16 + 1;
//			try {
//				randomFile.seek(beginIndex);
//				randomFile.read(desByte);
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//
//		}

		byte[] buf1 = new byte[16];
		byte[] buf2 = new byte[16];
		for (int i = 0; i < 16 ; i++){
			buf1[i] = 0;
			//SDKLog.i(TAG,"%%%%%%%%%%%%%%%%%%%%%======"+byte2hex(desByte[i]));
		}

		byte mask=0x01;


		for(int j=7;j>=0;j--)
		{
			for(int i=0;i<8;i++)
			{
				int a = desByte[i]&(mask<<j);
				if(a > 0)
					buf1[7-j]|=(mask<<i);
			}
		}

		for(int j=7;j>=0;j--)
		{
			for(int i=8;i<16;i++)
			{
				int b = desByte[i]&(mask<<j);
				if(b > 0)
					buf1[8+7-j]|=(mask<<(i%8));
			}
		}
		return buf1;
		//return desByte;

	}


	public static boolean isExistAscOrHzk(Context context){
		boolean isExitASC16 = false;
		boolean isExitHZK16 = false;
		String path1 = context.getFilesDir().getAbsolutePath()+File.separator+"ASC16";
		String path2 = context.getFilesDir().getAbsolutePath()+ File.separator+"HZK16";
		File file1 = new File(path1);
		File file2 = new File(path2);

		if (file1.exists()){
			isExitASC16 = true;
		}
		if (file2.exists()){
			isExitHZK16 = true;
		}

		if (isExitASC16 && isExitHZK16){
			return true;
		}
		return false;
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

	public int showLedText(int mode,String strNum){
		//do config
		UART.setConfig(mDevice, mUartPort, 2400, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
		UART.fixedWrite(mDevice, mUartPort, new byte[]{0x1B, 0x40});

		byte[] typeCmd = null;
		int result = 0;
		switch (mode) {
			case CMD_INIT_TYPE:
				typeCmd = LCD.CMD_INIT;
				break;

			case CMD_PRICE_TYPE:
				typeCmd = LCD.CMD_PRICE;
				break;

			case CMD_COLLECT_TYPE:
				typeCmd = LCD.CMD_COLLECT;
				break;

			case CMD_CHANGE_TYPE:
				typeCmd = LCD.CMD_CHANGE;
				break;

			case CMD_TOTAL_TYPE:
				typeCmd = LCD.CMD_TOTAL;
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


	/**
	 *added by pu
	 */
	//clean LCD
	public void lcdShowNone(Device device){
		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR_None;

		synchronized(device){
			device.writeData(buf,3);
			//device.writeData(Buf, 6 + fixLen);
			device.readData(buf);
		}
	}
	//show price
	public void  lcdShowPrice(Device device){
		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR_Price;

		synchronized(device){
			device.writeData(buf,3);
			//device.writeData(Buf, 6 + fixLen);
			device.readData(buf);
		}
	}
	public void lcdShowTotal(Device device){
		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR_Total;

		synchronized(device){
			device.writeData(buf,3);
			//device.writeData(Buf, 6 + fixLen);
			device.readData(buf);
		}
	}
	public void lcdShowCollect(Device device){
		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR_Collect;

		synchronized(device){
			device.writeData(buf,3);
			//device.writeData(Buf, 6 + fixLen);
			device.readData(buf);
		}
	}
	public void lcdShowChange(Device device){
		byte[] buf = new byte[Cmds.MAX_CMD_SIZE];
		buf[0] = Cmds.CMD_REPORT_ID;
		buf[1] = CMD_LCD;
		buf[2] = SUBCMD_LCD_STR_Change;

		synchronized(device){
			device.writeData(buf,3);
			//device.writeData(Buf, 6 + fixLen);
			device.readData(buf);
		}
	}
}


