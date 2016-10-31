package com.cynoware.firmwareupdate;

import android.net.Uri;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * 文件操作及文件信息获取等相关的方法
 */

public class FileUtils {

    final long MAX_BIN_FILE_SIZE = 1024 * 512;
    byte[] data = null;
    String md5str = "";
    String bytestr = "";
    static int filelen = 0;
    Uri uri;
    File file = null;


    // 获取文件信息线程
    public void getEditUIData(Handler handler) {
        md5str = getFileMD5(file, filelen, handler);
        bytestr = getFileToByte(uri, filelen);
        while (true) {
            if (!("".equals(bytestr))) {
                handler.sendEmptyMessage(1);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取文件MD5值
    public String getFileMD5(File file, int filelen, Handler handler) {
        if (!file.isFile()) {
            handler.sendEmptyMessage(0);
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[filelen];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    // 文件字节流转换为十六进制
    public static String string2HexString(String strPart) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        return hexString.toString();
    }

    // 文件字节流转换为二进制
    private String strToBinstr(String str) {
        char[] strChar = str.toCharArray();
        String result = "";
        for (int i = 0; i < strChar.length; i++) {
            result += Integer.toBinaryString(strChar[i]) + " ";
        }
        return result;
    }

    // byte数组初始化
    public void clearArr(byte[] arr, int endIndex) {
        for (int i = 0; i < arr.length; i++) {
            if (i <= endIndex) {
                arr[i] = 0;
            } else {
                arr[i] = (byte) 204;
            }
        }
    }

    // 得到文件字节流
    public String getFileToByte(Uri uri, int filelen) {
        FileInputStream is = null;
        data = new byte[filelen];
        try {
            is = new FileInputStream(uri.getPath());
            is.read(data);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Bytes2HexString(data);
    }

    // 得到比较好看的16进制数据用于显示(显示的和windows版本一样了)
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase() + " ";
        }
        return ret;
    }

    // memcpy(pMainDlg->wrbuf + 13, &tranBufSize, 4);
    // 文件大小需要从低位开始给wrbuf赋值，该方法实现
    public static byte getByteHighLowBit(int size, int index) {
        int h1 = size % 256;
        int h2 = size / 256;
        byte buf13 = (byte) h1;
        byte buf14 = (byte) h2;
        if (index == 13)
            return buf13;
        else if (index == 14)
            return buf14;
        else
            return 0;
    }
}