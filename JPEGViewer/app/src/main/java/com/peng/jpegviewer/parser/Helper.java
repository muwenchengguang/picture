package com.peng.jpegviewer.parser;

import com.peng.jpegviewer.utils.ALog;

/**
 * Created by richie on 12/24/18
 */
public class Helper {

    public static String byte2String(byte data) {
        byte high = (byte) (data >> 4 & 0x0f);
        byte low = (byte) (data &0x0f);
        String h = Integer.toHexString(high);
        String l = Integer.toHexString(low);
        String var = "0x" + h + l;
        return var;
    }

    public static String byte2String(byte[] data, int pos, int size) {
        String var = "";
        int count = 0;
        for (int i = pos; i < pos + size; i++) {
            byte item = data[i];
            byte high = (byte) (item >> 4 & 0x0f);
            byte low = (byte) (item & 0x0f);
            String h = Integer.toHexString(high);
            String l = Integer.toHexString(low);
            var += "0x" + h + l + ' ';
            count++;
            if (count%8 == 0) {
                var += '\n';
            }
        }
        return var;
    }

    public static byte[] sign2Byte(Segment.Type type) {
        byte[][] signArr = {
                {(byte) 0xff, (byte) 0xd8},
                {(byte) 0xff, (byte) 0xc0},
                {(byte) 0xff, (byte) 0xc2},
                {(byte) 0xff, (byte) 0xe0},
                {(byte) 0xff, (byte) 0xe1},
                {(byte) 0xff, (byte) 0xe2},
                {(byte) 0xff, (byte) 0xe3},
                {(byte) 0xff, (byte) 0xe4},
                {(byte) 0xff, (byte) 0xe5},
                {(byte) 0xff, (byte) 0xe6},
                {(byte) 0xff, (byte) 0xe7},
                {(byte) 0xff, (byte) 0xe8},
                {(byte) 0xff, (byte) 0xe9},
                {(byte) 0xff, (byte) 0xea},
                {(byte) 0xff, (byte) 0xeb},
                {(byte) 0xff, (byte) 0xec},
                {(byte) 0xff, (byte) 0xed},
                {(byte) 0xff, (byte) 0xee},
                {(byte) 0xff, (byte) 0xef},
                {(byte) 0xff, (byte) 0xdb},
                {(byte) 0xff, (byte) 0xc4},
                {(byte) 0xff, (byte) 0xda},
                {(byte) 0xff, (byte) 0xd9},
                {(byte) 0xff, (byte) 0xdd}
        };
        int index = type.ordinal();
        if (index >= signArr.length) throw new IllegalArgumentException("segement type:" + type  + " not allowed");
        return signArr[index];
    }

    public static Segment.Type translateSign(byte[] data, int pos) {
        for (Segment.Type item : Segment.Type.values()) {
            if (item == Segment.Type.Compress) continue;
            byte[] iData = sign2Byte(item);
            if (iData[0] == data[pos] && iData[1] == data[pos + 1]) return item;
        }
        String sign = byte2String(data, pos, 2);
        throw new IllegalArgumentException("unexpected sign:" + sign);
    }

    public static byte[] readBytes(byte[] data, int pos, int count) {
        byte[] copy = new byte[count];
        System.arraycopy(data, pos, copy, 0, count);
        return copy;
    }

    public static void checkEqual(byte[] d1, byte[] d2) {
        boolean hasThrow = false;
        if (d1.length != d2.length) hasThrow = true;
        if (!hasThrow) {
            for (int i = 0; i < d1.length; i++) {
                if (d1[i] != d2[i]) {
                    hasThrow = true;
                    break;
                }
            }
        }
        if (hasThrow) {
            throw new IllegalArgumentException("check eq failed");
        }
    }

    public static void checkTrue(boolean isTrue) {
        if (!isTrue) {
            throw new IllegalArgumentException("check failed");
        }
    }

    public static int byte2Int(byte[] data) {
        int result = 0;
        for (byte item : data) {
            result = result<<8;
            result += item&0xff;
        }
        return result;
    }

    public static int byte2Int(byte data) {
        return data&0xff;
    }

    public static int byte2Int(byte[] data, int from, int count) {
        int result = 0;
        for (int i = from; i < from + count; i++) {
            result = result<<8;
            result += data[i]&0xff;
        }
        return result;
    }

    public static int hi2Int(byte data) {
        return data>>4&0x0f;
    }

    public static int low2Int(byte data) {
        return data&0x0f;
    }
}
