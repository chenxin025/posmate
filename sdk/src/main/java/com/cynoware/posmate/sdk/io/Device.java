package com.cynoware.posmate.sdk.io;

public abstract class Device {
	/*public static int DETECT_FRAME_USB = 0x00000001;
	public static int DETECT_DOCK_USB = 0x00000002;
	public static int DETECT_DOCK_BT = 0x00000004;
	public static int DETECT_UART = 0x00000008;
	public static int DETECT_ALL = 0xFFFFFFFF;*/

	// 事件状态
	private boolean mCloseFlag = false;
	int event = 0;
	int uartWaiting = 0;

	public abstract boolean isConnected();

	// 基本读写接口, 为兼容USB HID, 最大大小32，data[0]固定为0x01
	public abstract int writeData(byte[] data, int length);	
	public abstract int readData(byte[] data);	
	public abstract int waitEvent(byte[] data);

	// 检查连接状态
	public abstract boolean isBroken();
	
	
	protected void setCloseFlag( boolean value ){
		mCloseFlag = true;
	}

	public boolean getCloseFlag(){
		return mCloseFlag;
	}
	
	public void deviceWait() {
		try {
			this.wait();
		} catch (InterruptedException ex) {
		}
	}

	public int deviceTimedWait(long millisecond) {
		try {
			this.wait(millisecond);
			return 0;
		} catch (InterruptedException ex) {
			return -1;
		}
	}
}