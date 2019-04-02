package com.peng.jpegviewer.decoder;

import android.util.Log;

import com.peng.jpegviewer.parser.DHTSegment;
import com.peng.jpegviewer.parser.HufTable;
import com.peng.jpegviewer.parser.JPEGParser;
import com.peng.jpegviewer.parser.SOSSegment;
import com.peng.jpegviewer.parser.Segment;
import com.peng.jpegviewer.utils.ALog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richie on 3/22/19
 */
public class JPEGDecoder {
    private final ALog.Category TAG = ALog.Category.Decoder;
    private final JPEGParser mJPEGParser;
    private final DHTSegment mDHTSegment;
    private final SOSSegment mSOSSegment;
    private final List<HufTable> mHufTables;
    private final List<HufCodeGroup> mHufCodes = new ArrayList<>();

    public JPEGDecoder(JPEGParser parser) {
        mJPEGParser = parser;
        mDHTSegment = (DHTSegment) mJPEGParser.find(Segment.Type.DHT);
        mSOSSegment = (SOSSegment) mJPEGParser.find(Segment.Type.SOS);
        mHufTables = mDHTSegment.hufmanTables();
        makeHufTable();
        buildCodeData();
    }

    private void makeHufTable() {
        ALog.v(TAG, "available huffman tables");
        mHufCodes.clear();
        for (HufTable table : mHufTables) {
            ALog.v(TAG, table.toString());
            mHufCodes.add(new HufCodeGroup(table));
            break;
        }

    }

    private void buildCodeData() {
        ALog.v(TAG, "buildCodeData");

    }
}
