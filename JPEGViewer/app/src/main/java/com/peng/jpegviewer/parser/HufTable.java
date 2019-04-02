package com.peng.jpegviewer.parser;

/**
 * Created by richie on 3/22/19
 */
public class HufTable {
    public final int type;
    public final int id;
    public final byte[] codeSize;
    public final byte[] data;

    public HufTable(int type, int id, byte[] codeSize, byte[] data) {
        this.type = type;
        this.id = id;
        this.codeSize = codeSize;
        this.data = data;
    }

    @Override
    public String toString() {
        String content = "" + (type == 0 ? "DC" : "AC") + " HUF " + id + " codesize:\n";
        int pos = 0;
        String dataItems = "";
        for (int i = 0; i < 16; i++) {
            content += codeSize[i] + " ";
            String item = Helper.byte2String(data, pos, codeSize[i]);
            pos += codeSize[i];
            //dataItems += "\n[" + item + "]";
        }
        return content + dataItems;

    }
}
