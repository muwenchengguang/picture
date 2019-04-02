package com.peng.jpegviewer.utils;

/**
 * Created by richie on 2/28/18.
 */

public class ByteUtils {
    /*数据类型	传输格式	示例
        uint8	大端模式	数值:0x12 —- 传输顺序:0x12
        uint16	大端模式	数值:0x1234 —- 传输顺序:0x12, 0x34
        uint32	大端模式	数值:0x12345678 —- 传输顺序:0x12, 0x34, 0x56, 0x78
        string(字符串)	顺序模式	字符串:“hello” —- 传输顺序:'h', 'e', 'l', 'l', 'o', '\0'
        float	放大1000倍，转换成uint32_t传输	数值: 12.3456，放大1000倍是12345(或12346) —- 传输顺序: 0x00, 0x12, 0x34, 0x56
        */
    public static void shortToByte(short val, byte [] buf, int startPos) {
        int temp = val;
        buf[startPos+0] = (byte) ((val >> 8) & 0xFF);
        buf[startPos+1] = (byte) (val & 0xFF);
    }

    public static short byteToShort(byte [] buf, int startPos) {
        short val = 0;
        val |= ((short)(buf[startPos+0])<<8)&0xFF00;
        val |= (short)(buf[startPos+1])&0x00FF;
        return val;
    }

    public static void intToByte(int val, byte [] buf, int startPos) {
        int temp = val;
        buf[startPos+0] = (byte) ((val >> 24) & 0xFF);
        buf[startPos+1] = (byte) ((val >> 16) & 0xFF);
        buf[startPos+2] = (byte) ((val >> 8) & 0xFF);
        buf[startPos+3] = (byte) (val & 0xFF);
    }

    public static int byteToInt(byte [] buf, int startPos) {
        int val = 0;
        val |= ((int)(buf[startPos+0])<<24)&0xFF000000;
        val |= ((int)(buf[startPos+1])<<16)&0x00FF0000;
        val |= ((int)(buf[startPos+2])<<8)&0x0000FF00;
        val |= (int)(buf[startPos+3])&0x000000FF;
        return val;
    }

    public static void floatToByte(float val, byte [] buf, int startPos) {
        intToByte((int)(val*1000),buf,startPos);
    }

    public static float byteToFloat(byte [] buf, int startPos) {
        return (float)(byteToInt(buf,startPos))/1000;
    }

    public static void stringToByte(String str, byte [] buf, int startPos) {
        System.arraycopy(str.getBytes(), 0, buf, startPos, str.length());
        buf[startPos+str.length()] = 0;
    }
}
