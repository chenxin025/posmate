package com.cynoware.posmate.sdk;


public class util {
    static void int2byte_le(int from, byte[] to, int offset){
        to[offset + 3] = (byte)((from >> 24) & 0xFF);
        to[offset + 2] = (byte)((from >> 16) & 0xFF);
        to[offset + 1] = (byte)((from >> 8) & 0xFF);
        to[offset + 0] = (byte)(from & 0xFF);
    }

    static int byte2int_le(byte[] from, int offset){
        int v = (int)(from[offset + 3] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 2] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 1] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 0] & 0xFF);
        return v;
    }

    static void int2byte_be(int from, byte[] to, int offset){
        to[offset + 0] = (byte)((from >> 24) & 0xFF);
        to[offset + 1] = (byte)((from >> 16) & 0xFF);
        to[offset + 2] = (byte)((from >> 8) & 0xFF);
        to[offset + 3] = (byte)(from & 0xFF);
    }

    static int byte2int_be(byte[] from, int offset){
        int v = (int)(from[offset + 0] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 1] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 2] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 3] & 0xFF);
        return v;
    }

    static void short2byte_le(int from, byte[] to, int offset){
        to[offset + 1] = (byte)((from >> 8) & 0xFF);
        to[offset + 0] = (byte)(from & 0xFF);
    }

    static int byte2short_le(byte[] from, int offset){
        int v = (int)(from[offset + 1] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 0] & 0xFF);
        return v;
    }


    static void short2byte_be(int from, byte[] to, int offset){
        to[offset + 0] = (byte)((from >> 8) & 0xFF);
        to[offset + 1] = (byte)(from & 0xFF);
    }
    static int byte2short_be(byte[] from, int offset){
        int v = (int)(from[offset + 0] & 0xFF);
        v <<= 8;
        v |= (int)(from[offset + 1] & 0xFF);
        return v;
    }

    static int cksum(byte[] buf, int offset, int size){
        int ret = 0;
        while(size-- != 0)
            ret += (int)(buf[offset + size] & 0xFF);
        return ret;
    }

    public static boolean msleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }
}
