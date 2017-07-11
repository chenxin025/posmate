package com.cynoware.posmate.sdk;


public class LED {	
	
    public static final byte[] CMD_INIT = {0x1B, 0x40, 0x0C};
    public static final byte[] CMD_PRICE = {0x1B,0x73,0x31};
    public static final byte[] CMD_COLLECT = {0x1B, 0x73, 0x33};
    public static final byte[] CMD_CHANGE = {0x1B, 0x73, 0x34};
    public static final byte[] CMD_TOTAL = {0x1B, 0x73, 0x32};
    
   
	private Device mDevice;
	private int mUartPort;
	private boolean mIsOnBoard;
	
	public LED( Device device, int uart, boolean isOnBoard ){
		mDevice = device;
		mUartPort = uart;
		mIsOnBoard = isOnBoard;
	}
	
	public void config( ){
		UART.setConfig(mDevice, mUartPort, 2400, 8, UART.PRRITY_NONE, UART.STOPBITS_1, UART.FLOWCTRL_XONOFF);
		UART.fixedWrite(mDevice, mUartPort, new byte[]{0x1B, 0x40});
    	if(!mIsOnBoard){
    	//    Util.msleep(500);
    	}
	}

	
	public void showSpecial( byte[] special  ){
		
		if( special == null )
			return;		
		
   	 	UART.fixedWrite(mDevice, mUartPort, special);
        if(!mIsOnBoard){
        	//Util.msleep(500);
        }		
	}
	
	
	public void showText( String text ){
		
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
        	util.msleep(100);
	        
	   	UART.write(mDevice, mUartPort, acttext.getBytes());
	   	UART.write(mDevice, mUartPort, new byte[]{0x0D});
	}	
}
