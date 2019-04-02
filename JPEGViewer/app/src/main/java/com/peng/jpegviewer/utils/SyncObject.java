package com.peng.jpegviewer.utils;



/**
 * Created by leo on 17-12-11.
 */

public class SyncObject {
    private static final ALog.Category TAG = ALog.Category.Utils;
    private static int sIndex = 1;
    private final Object mObj = new Object();
    private final int mIndex = sIndex++;
    private final String mRefString;

    public SyncObject(String refString) {
        mRefString = refString;
    }

    @Override
    public String toString() {
        return "SyncObject-" + mIndex + " " + mRefString;
    }

    public int thisWait(long timeMs) {
        long beforeMs = System.currentTimeMillis();
        try {
            synchronized (mObj) {
                ALog.i(TAG, toString() + ":wait");
                if (timeMs <= 0) {
                    mObj.wait();
                } else {
                    mObj.wait(timeMs);
                }
                ALog.i(TAG, toString() + ":wait complete");
            }
        } catch (InterruptedException e) {
        }
        long afterMs = System.currentTimeMillis();
        if (timeMs <= 0) return 0;
        return (afterMs - beforeMs <= timeMs ? 0 : -1);
    }

    public void thisNotify() {
        synchronized (mObj) {
            ALog.i(TAG, toString() + ":notify");
            mObj.notify();
        }
    }
}
