package com.peng.jpegviewer.decoder;

import com.peng.jpegviewer.utils.ALog;

/**
 * Created by richie on 3/25/19
 */
public class HufCode {
    private final ALog.Category TAG = ALog.Category.Decoder;
    private final HufCodeGroup mHufCodeGroup;
    private final int mIndex;
    private final int mLength;
    private int mKey;
    private final byte mValue;

    HufCode(HufCodeGroup group, int index, byte value, int length) {
        mHufCodeGroup = group;
        mValue = value;
        mLength = length;
        mIndex = index;

        if (mIndex <= 0) {
            mKey = 0;
        } else {
            int lastIndex = mIndex - 1;
            HufCode code = mHufCodeGroup.get(lastIndex);
            int lastKey = code.key();
            int lastKeyLength = code.keyLength();
            mKey = lastKey + 1;
            int deltaLength = mLength - lastKeyLength;
            while (deltaLength > 0) {
                mKey = mKey<<1;
                deltaLength--;
            }
        }
    }

    public int key() {
        return mKey;
    }

    public int keyLength() {
        return mLength;
    }

    public byte value() {
        return mValue;
    }

    public int index() {
        return mIndex;
    }

}
