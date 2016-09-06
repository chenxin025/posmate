package com.cynoware.posmate.sdk;


import android.os.SystemClock;

public class Debug{

	public static int write(Device device, byte[] data, int offset, int len){
		byte[] buf = new byte[cmds.MAX_CMD_SIZE];
		int fixLen = (len > cmds.MAX_CMD_SIZE - 4 ? (cmds.MAX_CMD_SIZE - 4) : len);
		buf[0] = cmds.CMD_REPORT_ID;
		buf[1] = cmds.CMD_DEBUG;
		buf[2] = cmds.SUBCMD_DEBUG_WRITE;
		buf[3] = (byte) fixLen;
		System.arraycopy(data, offset, buf, 4, fixLen);

		synchronized(device){
			device.writeData(buf, 4 + fixLen);
			device.readData(buf);
		}
		return (int)(buf[3] & 0xff);
	}

	public static int read(Device device, byte[] data, int offset, int len){
		byte[] buf = new byte[cmds.MAX_CMD_SIZE];
		int fixLen = (len > cmds.MAX_CMD_SIZE - 4 ? (cmds.MAX_CMD_SIZE - 4) : len);
		buf[0] = cmds.CMD_REPORT_ID;
		buf[1] = cmds.CMD_DEBUG;
		buf[2] = cmds.SUBCMD_DEBUG_READ;
		buf[3] = (byte)fixLen;

		synchronized(device){
			device.writeData(buf, 4);
			device.readData(buf);
		}
		if(len > (int)(buf[3] & 0xff))
			len = (int)(buf[3] & 0xff);
		System.arraycopy(buf, 4, data, offset, len);
		return len;
	}


	public static int timedRead(Device device, byte[] data, int offset, int len, long millisecond){
		int ret = 0;
		long start = SystemClock.elapsedRealtime();

		synchronized(device){
			//Try first
			ret = read(device, data, offset, len);
			//The wait
			if (ret <= 0) {
				while (true) {
					if(device.getCloseFlag() || device.isBroken() )
						break;

					if((device.event & cmds.kDebugInfoAvailable) != 0){
						ret = read(device, data, offset, len);
						if(ret == 0){
							//No debug information, synchronize event status
							Event.pollByCmd(device);
							break;
						}
					}

					long elapse = SystemClock.elapsedRealtime() - start;
					long wait = (elapse < millisecond ? millisecond - elapse : 0);
					if (wait == 0) break;
					device.deviceTimedWait(wait);
				}
			}
		}

		return ret;
	}
	public static int timedRead(Device device, byte[] data, long millisecond){
		return timedRead(device, data, 0, data.length, millisecond);
	}

	public static int fixedWrite(Device device, byte[] data, int offset, int len){

		int size = 0;
		while(len > 0){
			if(device.getCloseFlag() || device.isBroken() )
				break;
			
			int ret = write(device, data, offset, len);
			size += ret;
			offset += ret;
			len -= ret;
		}
		return size;
	}
}

