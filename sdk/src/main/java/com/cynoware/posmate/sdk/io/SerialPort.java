package com.cynoware.posmate.sdk.io;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by chenxin on 2017/3/24.
 */

public class SerialPort {

    private static final String TAG = "SerialPort";

    private static SerialPort mSerialPort = null;

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;

    private FileInputStream mFileInputStream;

    private FileOutputStream mFileOutputStream;


    public SerialPort(File device, int baudrate, int flags) throws IOException {
        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public static SerialPort getInstance(File file, final int rate, final int flags)  throws SecurityException, IOException {
        if (mSerialPort == null){
            mSerialPort = new SerialPort(file,rate,flags);
        }

        return mSerialPort;
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }


    //TODO onDestory called
    public void closeAllStreams(){
        try {
            Log.d("SerialPort","closeAllStreams");
            if(mFileInputStream!=null)
                mFileInputStream.close();
            if(mFileOutputStream!=null)
                mFileOutputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.closeAllStreams();
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    // JNI
    public native  FileDescriptor open(String path, int baudrate, int flags);
    public native void close();
    static {
        System.loadLibrary("serial_port");
    }
}
