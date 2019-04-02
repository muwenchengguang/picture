package com.peng.jpegviewer.parser;

import android.content.Context;

import com.peng.jpegviewer.utils.ALog;
import com.peng.jpegviewer.utils.FileUtils;

/**
 * Created by richie on 12/24/18
 */
public class JPEGParser {
    private final ALog.Category TAG = ALog.Category.AppDemo;
    private final byte[] mData;
    private final Segment mRoot;



    public JPEGParser(Context context) {
        mData = FileUtils.retrieveAssetFileContent(context, "screen.jpg");
        mRoot = new SOISegment(mData, 0, mData.length);
    }

    public Segment find(Segment.Type type) {
        Segment segment = mRoot.find(type);
        if (segment == null) return null;
        if (segment instanceof DHTSegment) {
            return segment;
        }
        return null;
    }
}
