package com.peng.jpegviewer.parser;

import com.peng.jpegviewer.utils.ALog;

/**
 * Created by richie on 12/25/18
 */
public class APP0Segment extends Segment {
    private String mVersion = "";
    private int mDensityUnit;
    private int mDensityX;
    private int mDensityY;

    APP0Segment(byte[] data, int pos, int size) {
        super(data, pos, size, Type.APP0);
        parse();
    }

    private void parse() {
        final byte[] sign = {'J', 'F', 'I', 'F', 0 };
        int pos = mDataPos;
        int len;
        byte[] cache;

        len = 5;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        Helper.checkEqual(cache, sign);

        len = 2;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        mVersion = "" + (Helper.byte2Int(cache[0])) + "." +  (Helper.byte2Int(cache[1]));

        len = 1;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        mDensityUnit = Helper.byte2Int(cache);

        len = 2;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        mDensityX = Helper.byte2Int(cache);

        len = 2;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        mDensityY = Helper.byte2Int(cache);
        ALog.v(TAG, "version:" + mVersion + " densityUnit:" + mDensityUnit + " x:" + mDensityX + ", y:" + mDensityY);

    }


}
