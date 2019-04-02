package com.peng.jpegviewer.parser;

import com.peng.jpegviewer.utils.ALog;

/**
 * Created by richie on 12/25/18
 */
public class SOSSegment extends Segment {
    public SOSInfo[] mSOSInfo;

    SOSSegment(byte[] data, int pos, int size) {
        super(data, pos, size, Type.SOS);
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

        int count = cache[0];
        mSOSInfo = new SOSInfo[count];
        for (int i = 0; i < count; i++) {
            len = 1;
            cache = Helper.readBytes(mData, pos, len);
            pos += len;
            Helper.checkTrue(pos <= mPos + mSize);

            mSOSInfo[i] = new SOSInfo();
            mSOSInfo[i].colorId = Helper.byte2Int(cache[0]);

            len = 1;
            cache = Helper.readBytes(mData, pos, len);
            pos += len;
            Helper.checkTrue(pos <= mPos + mSize);

            mSOSInfo[i].hDTDCId = Helper.hi2Int(cache[0]);
            mSOSInfo[i].hDTACId = Helper.low2Int(cache[0]);

            ALog.v(TAG, "SOS: color id:" + mSOSInfo[i].colorId + ", dc:" + mSOSInfo[i].hDTDCId + ", ac:" + mSOSInfo[i].hDTACId);
        }

    }
}

class SOSInfo {
    public int colorId;
    public int hDTDCId;
    public int hDTACId;
}
