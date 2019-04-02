package com.peng.jpegviewer.parser;

import com.peng.jpegviewer.utils.ALog;

/**
 * Created by richie on 12/25/18
 */
public class SOF0Segment extends Segment {
    public int mDensity;
    public int mColorSpaceCount;
    public int mWidth;
    public int mHeight;
    public ColorInfo[] mColorInfo;

    SOF0Segment(byte[] data, int pos, int size) {
        super(data, pos, size, Type.SOF0);
        parse();
    }

    private void parse() {
        int pos = mDataPos;
        int len;
        byte[] cache;

        len = 1;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        Helper.checkTrue(pos <= mPos + mSize);
        mDensity = Helper.byte2Int(cache[0]);


        len = 2;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        Helper.checkTrue(pos <= mPos + mSize);
        mHeight = Helper.byte2Int(cache);

        len = 2;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        Helper.checkTrue(pos <= mPos + mSize);
        mWidth = Helper.byte2Int(cache);

        len = 1;
        cache = Helper.readBytes(mData, pos, len);
        pos += len;
        mColorSpaceCount = Helper.byte2Int(cache);

        mColorInfo = new ColorInfo[mColorSpaceCount];
        for (int i = 0; i < mColorSpaceCount; i++) {
            len = 1;
            cache = Helper.readBytes(mData, pos, len);
            pos += len;
            mColorInfo[i] = new ColorInfo();
            mColorInfo[i].colorId = Helper.byte2Int(cache);

            len = 1;
            cache = Helper.readBytes(mData, pos, len);
            pos += len;
            mColorInfo[i].hfactor = Helper.hi2Int(cache[0]);
            mColorInfo[i].vfactor = Helper.low2Int(cache[0]);

            len = 1;
            cache = Helper.readBytes(mData, pos, len);
            pos += len;
            mColorInfo[i].dqt = Helper.byte2Int(cache);
        }

        String log = "SOF0:";
        log += " density:" + mDensity + " w:" + mWidth + " h:" + mHeight + ", colors:" + mColorSpaceCount;

        ALog.v(TAG, log);
    }
}

class ColorInfo {
    int colorId;
    int hfactor;
    int vfactor;
    int dqt;
}
