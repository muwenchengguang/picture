package com.peng.jpegviewer.parser;

/**
 * Created by richie on 12/25/18
 */
public class SOISegment extends Segment {
    SOISegment(byte[] data, int pos, int size) {
        super(data, pos, size, Type.SOI);
        parse();
    }

    private void parse() {

    }
}
