package com.peng.jpegviewer.parser;

import com.peng.jpegviewer.utils.ALog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richie on 12/25/18
 */
public class DHTSegment extends Segment {
    public static int DHT_DC = 0;
    public static int DHT_AC = 1;
    private HufTable[] mHufTable = new HufTable[4];
    private int mTables = 0;

    DHTSegment(byte[] data, int pos, int size) {
        super(data, pos, size, Type.DHT);
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
            int type = Helper.hi2Int(cache[0]);
            int id = Helper.low2Int(cache[0]);

            len = 16;
            cache = Helper.readBytes(mData, pos, len);
            pos += len;

            byte[] codeSize = cache;
            len = 0;
            for (int i = 0; i < codeSize.length; i++) {
                len += Helper.byte2Int(codeSize[i]);
            }
            Helper.checkTrue(len < 256);
            cache = Helper.readBytes(mData, pos, len);
            pos += len;

            mHufTable[mTables] = new HufTable(type, id, codeSize, cache);
            mTables++;
            if (mTables >= 4) {
                break;
            }
        }

        for (int i = 0; i < mTables; i++) {
            ALog.v(TAG, mHufTable[i].toString());
        }

    }

    public List<HufTable> hufmanTables() {
        List<HufTable> tables = new ArrayList<>();
        for (int i = 0; i < mTables; i++) {
            tables.add(mHufTable[i]);
        }
        return tables;
    }
}

