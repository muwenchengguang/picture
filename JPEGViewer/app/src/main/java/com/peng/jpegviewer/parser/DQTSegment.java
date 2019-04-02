package com.peng.jpegviewer.parser;

import com.peng.jpegviewer.utils.ALog;

/**
 * Created by richie on 12/25/18
 */
public class DQTSegment extends Segment {
    public static final int ACCURACY_8BIT = 0;
    public static final int ACCURACY_16BIT = 1;

    private DQTTable[] mDQTTable = new DQTTable[4];
    private int mTables = 0;

    DQTSegment(byte[] data, int pos, int size) {
        super(data, pos, size, Type.DQT);
        parse();
    }

    private void parse() {
        int pos = mDataPos;
        int len;
        byte[] cache;

        while (pos < mPos + mSize) {
            len = 1;
            cache = Helper.readBytes(mData, pos, len);
            pos += len;
            Helper.checkTrue(pos <= mPos + mSize);
            int accurancy = Helper.hi2Int(cache[0]);
            int id = Helper.low2Int(cache[0]);

            len = 64 * (accurancy + 1);
            cache = Helper.readBytes(mData, pos, len);
            pos += len;
            mDQTTable[mTables] = new DQTTable(id, cache);
            mTables++;
            if (mTables > 4) {
                throw new IllegalArgumentException("exceed DQT boundary");
            }
        }

        //String log = "DQT:";
        for (int i = 0; i < mTables; i++) {
            //log += " tb[" + i + "] id:" + mDQTTable[i].id + ", len:" + mDQTTable[i].data.length;
            ALog.v(TAG, mDQTTable[i].toString());
        }

        //ALog.v(TAG, log);

    }
}

class DQTTable {
    public int id;
    public byte[] data;
    DQTTable(int id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public String toString() {
        String content = "DQT:" + id + "\n";
        content += Helper.byte2String(data, 0, data.length);
        return content;
    }
}
