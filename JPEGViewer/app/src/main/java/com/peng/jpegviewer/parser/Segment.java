package com.peng.jpegviewer.parser;

import com.peng.jpegviewer.utils.ALog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richie on 12/24/18
 */
public class Segment {
    protected final ALog.Category TAG = ALog.Category.AppDemo;
    protected final Type mType;
    protected final int mPos;
    protected final int mDataPos;
    protected int mSize;
    protected final byte[] mData;
    protected List<Segment> mSegments = new ArrayList<>();

    public enum Type {
        SOI, SOF0, SOF2, APP0, APP1, APP2, APP3, APP4, APP5, APP6, APP7, APP8, APP9, APP10,
        APP11, APP12, APP13, APP14, APP15, DQT, DHT, SOS, EOI, DRI, Compress
    }

    int size() {
        return mSize;
    }

    Type type() {
        return mType;
    }

    Segment(byte[] data, int pos, int size, Type type) {
        mData = data;
        mPos = pos;
        mType = type;
        if (type == Type.Compress) {
            mSize = size;
            mDataPos = mPos;
        } else {
            if (mType == Type.SOI) {
                mSize = size;
                mDataPos = mPos + 2;
            } else {
                mSize = Helper.byte2Int(mData, pos + 2, 2);
                mSize += 2;
                mDataPos = mPos + 4;
            }
            String dispStr = mType.name() + "[" + mPos + "-" + (mPos + mSize) + "]";
            ALog.v(TAG, dispStr);
        }

        if (mType == Type.SOI) {
            parseSub();
        }
    }


    private void parseSub() {
        int pos = mDataPos;
        int leftSize = mSize - (mDataPos - mPos);
        boolean hasCompressed = false;
        while (leftSize > 0) {
            //ALog.v(TAG, "new seg at:" + pos + ", left size:" + leftSize);
            Type type = Helper.translateSign(mData, pos);
            Segment segment;
            switch (type) {
                case APP0:
                    segment = new APP0Segment(mData, pos, leftSize);
                    break;
                case DQT:
                    segment = new DQTSegment(mData, pos, leftSize);
                    break;
                case SOF0:
                    segment = new SOF0Segment(mData, pos, leftSize);
                    break;
                case DHT:
                    segment = new DHTSegment(mData, pos, leftSize);
                    break;
                case SOS:
                    segment = new SOSSegment(mData, pos, leftSize);
                    break;
                default:
                    segment = new Segment(mData, pos, leftSize, type);
                    break;
            }
            pos += segment.size();
            leftSize -= segment.size();
            mSegments.add(segment);
            if (segment.type() == Type.SOS) {
                hasCompressed = true;
                break;
            }
        }

        if (hasCompressed) {
            Segment segment = new Segment(mData, pos, leftSize - 2, Type.Compress);
            mSegments.add(segment);
            pos += segment.size();
            Type type = Helper.translateSign(mData, pos);
            if (type != Type.EOI) throw new IllegalArgumentException("not EOI");
        }
    }

    Segment find(Type type) {
        if (mType == type) return this;
        for (Segment segment : mSegments) {
            Segment found = segment.find(type);
            if (found != null) return found;
        }
        return null;
    }

}
