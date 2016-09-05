package com.cynoware.posmate.sdk;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.BitSet;
import com.cynoware.posmate.sdk.EscCommand.WrongParaException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.cynoware.posmate.sdk.UART;
import android.graphics.Bitmap;
import android.graphics.Color;


public class Printer {

	private static final String BLANK_ROWS = "\n";

	public static final int TYPE_PRINTF_BITMAP_0 = 0;
	public static final int TYPE_PRINTF_BITMAP_1 = 1;

	public static final int TYPE_YANKE = 0;
	public static final int TYPE_JINGXIN_1 = 1;
	public static final int TYPE_JINGXIN_2 = 2;
	public static final int TYPE_JINGXIN_3 = 3;
	public static final int TYPE_JINGXIN_4 = 4;

	public static int CHARSET_TYPE;
	public static final int CHARSET_ENGLISH = 0;
	public static final int CHARSET_KOREAN = 1;
	public static final int CHARSET_JAPANESE = 2;
	public static final int CHARSET_CHINESE = 3;
	 
    private static final byte[] CMD_1C_21_00 = { 0x1C, 0x21, 0x00};
    private static final byte[] CMD_1C_21_01 = { 0x1C, 0x21, 0x01};

	private Device mDevice;
	private int mUartPort;
	private int mType;
	private int mBaudrate;

	public Printer( Device device, int uart, int type){
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
			return 9600;
			
		default:
			return 115200;
		}
	}

	public void doConfig(){
		UART.setConfig(mDevice, mUartPort, mBaudrate, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
	}
	
	
	
	public void print( String text, int charset ){
		String charsetName = null;											//编码方式
		byte[] cmdSetCharset = null;
		
		UART.clearBuffer(mDevice, mUartPort);
		
		EscCommand escCmd = new EscCommand();
		
		
    	escCmd.addInitializePrinter();
		
		switch(mType){
	    case TYPE_YANKE:
	    	if( charset == CHARSET_KOREAN ){
	    		charsetName = "KS_C_5601-1987";
	    		escCmd.add(CMD_1C_21_00);
	    	}
	    	else if( charset == CHARSET_JAPANESE ){
	    		charsetName = "Shift_JIS";
	    		escCmd.add(CMD_1C_21_01);
	    	}
	    	else if( charset == CHARSET_CHINESE ){
	    		charsetName = "EUC-KR";
	    		escCmd.add(CMD_1C_21_00);
	    	}
		break;
		
		case TYPE_JINGXIN_1:
			charsetName = "GBK";
			break;
			
		case TYPE_JINGXIN_2:
		case TYPE_JINGXIN_3:
		case TYPE_JINGXIN_4:
			if( charset == CHARSET_KOREAN || charset == CHARSET_CHINESE ){
				charsetName = "KS_C_5601-1987";
				escCmd.addSetCharset(EscCommand.CHARSET_KOREAN);		//设置打印字符
			}
		    else if( charset == CHARSET_JAPANESE ){
		    	charsetName = "Shift_JIS";
		    	escCmd.addSetCharset(EscCommand.CHARSET_JAPANESE);			//设置打印字符
		    }
			break;	
		}
		
		// Process encoding.
		byte[] text_bytes;
		
		if( charsetName != null ){
			Charset cs = Charset.forName(charsetName);    	//返回一个指定字符格式的CharSet
			text_bytes = text.getBytes(cs);					//编码字符串转换成一个使用给定字符集的字节序列，并将结果存储到一个新的字节数组
		}else{												//byte[] getBytes(Charset charset)
			text_bytes = text.getBytes();
		}	
    	
		escCmd.add(text_bytes);
    	
		byte[] buf = escCmd.createCommandBuffer();
		
		UART.fixedWrite( mDevice, mUartPort, buf );
	}

	//printBarcode
	public void printBarcode( String str ){

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
		} catch (WrongParaException e) {
			e.printStackTrace();
		}

		byte[] buf = escCmd.createCommandBuffer();

		UART.fixedWrite( mDevice, mUartPort, buf );
	}

	public void downloadNVBitmap(Bitmap bmp){
		
		if( bmp == null )
			return;
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		
		byte xL = (byte)(width % 256);
		byte xH = (byte)(width / 256);
		byte yL = (byte)(height % 256);
		byte yH = (byte)(height / 256);
		
		Bitmap[] bmps = { bmp };
		
		EscCommand escCmd = new EscCommand();
		escCmd.addDownloadNvBitImage(bmps);
		/**
		 * revised by puzhimin 2016.7.25
		 * 解决bug 048  按download键会打印图片
		 */
		UART.clearBuffer(mDevice, mUartPort);

		byte[] buf = escCmd.createCommandBuffer();
		LogUtil.i("printer","##########################print bitmap########################");

        UART.fixedWrite( mDevice, mUartPort, buf );
	}

	public void printNVBitmap(){
		UART.clearBuffer(mDevice, mUartPort);

		EscCommand escCmd = new EscCommand();
		escCmd.addPrintNvBitmap((byte) 0x01, (byte) 0x00);
		byte[] buf = escCmd.createCommandBuffer();
        UART.fixedWrite( mDevice, mUartPort, buf );     
	}
	
	
	
	/*public boolean printBitmap(Bitmap bmp){
		EscCommand escCmd = new EscCommand();
		
		try {
			escCmd.addBmpPrint(bmp);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
		
		byte[] buf = escCmd.createCommandBuffer();
		
		UART.fixedWrite( mDevice, mUartPort, buf );	
		
		return true;
	}
	
	
	public boolean printBitmap2(Bitmap bmp){
		
		if( bmp == null )
			return false;
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		
		UART.clearBuffer(mDevice, mUartPort);

        //设置字符行间距为n点行  
        //byte[] data = new byte[] { 0x1B, 0x33, 0x00 };  
		byte[] data1 = new byte[] { 0x1B, 0x33, 0x00};
        //String send = "" + (char)(27) + (char)(51) + (char)(0);  
        //byte[] data = new byte[send.length()];  
        //for (int i = 0; i < send.length(); i++)  
        //{  
        //    data[i] = (byte)send[i];  
        //}  
       UART.fixedWrite( mDevice, mUartPort, data1 );

        byte[] data = new byte[] { 0x00, 0x00, 0x00};

        int pixelColor;  


        //ESC * m nL nH d1…dk   选择位图模式  
        // ESC * m nL nH  
        byte[] escBmp = new byte[] { 0x1B, 0x2A, 0x21, 0x00, 0x00 };  

        //nL, nH  
        escBmp[3] = (byte)(width % 256);  
        escBmp[4] = (byte)(width / 256);
        
        //设置模式为位图模式  
        int step_height = 24;
        int buf_len = step_height / 8;

        //循环图片像素打印图片  
        //循环高  
        for (int i = 0; i < (height / step_height + 1); i++)  
        {  
        	UART.fixedWrite( mDevice, mUartPort, escBmp );
            
            //循环宽  
            for (int px = 0; px < width; px++)  
            {  
            	int y_start = i * step_height;
            	int y_end = y_start + step_height;
            	
            	data[0] = 0x00;  
                data[1] = 0x00;  
                data[2] = 0x00;// Clear to Zero.
            	
                for ( int py = y_start, k=0; py < y_end; py++, k++)  
                {  
                	
                    if (py < height)  // if within the BMP size  
                    {  
                        pixelColor = bmp.getPixel(px, py);  
                        if ( Color.red(pixelColor)== 0)  
                        {  
                            data[k / 8] += (byte)(0x80 >> (k % 8));                        }  
                    }  
                }  
                //一次写入一个data，24个像素  
                
                UART.fixedWrite( mDevice, mUartPort, data, 0, buf_len );
            }  
            
           

            //换行，打印第二行  
            //byte[] data2 = { 0xA };  
            //UART.fixedWrite( mDevice, mUartPort, data2 );
        } // data  
        
        String strNewLine = "\n\n";
        byte[] bufNewLine =  strNewLine.getBytes();
        UART.fixedWrite( mDevice, mUartPort, bufNewLine );
        return true;
	}
	
	
public boolean printBitmapTemp(Bitmap bmp){
		
		if( bmp == null )
			return false;
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		
		UART.clearBuffer(mDevice, mUartPort);

        //设置字符行间距为n点行  
        //byte[] data = new byte[] { 0x1B, 0x33, 0x00 };  
		byte[] data1 = new byte[] { 0x1B, 0x33, 0x00};
        //String send = "" + (char)(27) + (char)(51) + (char)(0);  
        //byte[] data = new byte[send.length()];  
        //for (int i = 0; i < send.length(); i++)  
        //{  
        //    data[i] = (byte)send[i];  
        //} 
       UART.fixedWrite( mDevice, mUartPort, data1 );

        byte[] data = new byte[] { 0x00, 0x00, 0x00};

        int pixelColor;  


        //ESC * m nL nH d1…dk   选择位图模式  
        // ESC * m nL nH  
        byte[] escBmp = new byte[] { 0x1B, 0x2A, 0x21, 0x00, 0x00 };  

        //nL, nH  
        escBmp[3] = (byte)(width % 256);  
        escBmp[4] = (byte)(width / 256);
        
        //设置模式为位图模式  
        int step_height = 24;
        int buf_len = step_height / 8;

        //循环图片像素打印图片  
        //循环高  
        for (int i = 0; i < (height / step_height + 1); i++)  
        {  
        	UART.fixedWrite( mDevice, mUartPort, escBmp );
            
            //循环宽  
            for (int px = 0; px < width; px++)  
            {  
            	int y_start = i * step_height;
            	int y_end = y_start + step_height;
            	
            	data[0] = 0x00;  
                data[1] = 0x00;  
                data[2] = 0x00;// Clear to Zero.
            	
                for ( int py = y_start, k=0; py < y_end; py++, k++)  
                {  
                	
                    if (py < height)  // if within the BMP size  
                    {  
                        pixelColor = bmp.getPixel(px, py);  
                        if ( Color.red(pixelColor)== 0)  
                        {  
                            data[k / 8] += (byte)(0x80 >> (k % 8));                        }  
                    }  
                }  
                //一次写入一个data，24个像素  
                
                UART.fixedWrite( mDevice, mUartPort, data, 0, buf_len );
            }  
            
           

            //换行，打印第二行  
            //byte[] data2 = { 0xA };  
            //UART.fixedWrite( mDevice, mUartPort, data2 );
        } // data  
        
        String strNewLine = "\n\n";
        byte[] bufNewLine =  strNewLine.getBytes();
        UART.fixedWrite( mDevice, mUartPort, bufNewLine );
        return true;
	}
	
	
	
	
	public void downloadNVBitmap(Bitmap bmp){
		
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

	
	public void printNVBitmap(){
		
		UART.clearBuffer(mDevice, mUartPort);

        byte[] escFSP = new byte[] { 0x1C, 0x70, 0x01, 0x08 };  
        UART.fixedWrite( mDevice, mUartPort, escFSP );
     
	}*/


	public void flush(){
		UART.clearBuffer(mDevice, mUartPort);
	}
	
	
	
	public boolean checkPaper(){
		/**
		 * 2016.8.19 by pu
		 * clearBuffer
		 */
		UART.clearBuffer(mDevice, mUartPort);

		if( mType != TYPE_JINGXIN_4 )
			return true;
		
		EscCommand escCommand = new EscCommand();
		
		escCommand.addCheckPaper();
		byte[] buf = escCommand.createCommandBuffer();
		UART.fixedWrite(mDevice, mUartPort, buf);
		 
		byte[] read = new byte[1];
		int res = UART.fixedRead(mDevice, mUartPort, read, 0, 1, 10000);				//构造函数中mUartPort= uart;
		return res != 0 && read[0] == 1;
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
    
    
    /*private BitSet getBitsImageData(Bitmap image) {
        int threshold = 127;
        int index = 0;
        int dimenssions = image.getWidth() * image.getHeight();
        BitSet imageBitsData = new BitSet(dimenssions);

        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int color = image.getPixel(x, y);
                int  red = (color & 0x00ff0000) >> 16;
                int  green = (color & 0x0000ff00) >> 8;
                int  blue = color & 0x000000ff;
                int luminance = (int)(red * 0.3 + green * 0.59 + blue * 0.11);
                //dots[index] = (luminance < threshold);
                imageBitsData.set(index, (luminance < threshold));
                index++;
            }
        }

        return imageBitsData;
    }

    
    public void printBitmap3( Bitmap image) {

            BitSet imageBits = getBitsImageData(image);

            byte widthLSB = (byte)(image.getWidth() & 0xFF);
            byte widthMSB = (byte)((image.getWidth() >> 8) & 0xFF);

            // COMMANDS
            byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);
            byte[] setLineSpacing24Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 24);
            byte[] setLineSpacing30Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 30);

            UART.fixedWrite( mDevice, mUartPort, INITIALIZE_PRINTER );      
            UART.fixedWrite( mDevice, mUartPort, setLineSpacing24Dots );


            int offset = 0;
            while (offset < image.getHeight()) {
            	
            	UART.fixedWrite( mDevice, mUartPort, selectBitImageModeCommand );

                int imageDataLineIndex = 0;
                byte[] imageDataLine = new byte[3 * image.getWidth()];

                for (int x = 0; x < image.getWidth(); ++x) {

                    // Remember, 24 dots = 24 bits = 3 bytes.
                    // The 'k' variable keeps track of which of those
                    // three bytes that we're currently scribbling into.
                    for (int k = 0; k < 3; ++k) {
                        byte slice = 0;

                        // A byte is 8 bits. The 'b' variable keeps track
                        // of which bit in the byte we're recording.
                        for (int b = 0; b < 8; ++b) {
                            // Calculate the y position that we're currently
                            // trying to draw. We take our offset, divide it
                            // by 8 so we're talking about the y offset in
                            // terms of bytes, add our current 'k' byte
                            // offset to that, multiple by 8 to get it in terms
                            // of bits again, and add our bit offset to it.
                            int y = (((offset / 8) + k) * 8) + b;

                            // Calculate the location of the pixel we want in the bit array.
                            // It'll be at (y * width) + x.
                            int i = (y * image.getWidth()) + x;

                            // If the image (or this stripe of the image)
                            // is shorter than 24 dots, pad with zero.
                            boolean v = false;
                            if (i < imageBits.length()) {
                                v = imageBits.get(i);
                            }
                            // Finally, store our bit in the byte that we're currently
                            // scribbling to. Our current 'b' is actually the exact
                            // opposite of where we want it to be in the byte, so
                            // subtract it from 7, shift our bit into place in a temp
                            // byte, and OR it with the target byte to get it into there.
                            slice |= (byte) ((v ? 1 : 0) << (7 - b));
                        }

                        imageDataLine[imageDataLineIndex + k] = slice;

                        // Phew! Write the damn byte to the buffer
                        //printOutput.write(slice);
                    }

                    imageDataLineIndex += 3;
                }

                UART.fixedWrite( mDevice, mUartPort, imageDataLine );
                offset += 24;
                UART.fixedWrite( mDevice, mUartPort, PRINT_AND_FEED_PAPER );
            }

            UART.fixedWrite( mDevice, mUartPort, setLineSpacing30Dots );
        
    }*/

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
		switch (whichType){
			case TYPE_PRINTF_BITMAP_0:
				printNVBitmap();
				break;
			case TYPE_PRINTF_BITMAP_1:
				if (null != bmp){
					printFastBitmap(bmp);
				}
				break;
			default:
				break;
		}
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
