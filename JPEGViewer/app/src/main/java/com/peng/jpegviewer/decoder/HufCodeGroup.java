package com.peng.jpegviewer.decoder;

import com.peng.jpegviewer.parser.Helper;
import com.peng.jpegviewer.parser.HufTable;
import com.peng.jpegviewer.utils.ALog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richie on 3/25/19
 */
public class HufCodeGroup {
    private final ALog.Category TAG = ALog.Category.Decoder;
    private final HufTable mHufTable;
    private List<HufCode> mCodes = new ArrayList<>();

    HufCodeGroup(HufTable hufTable) {
        mHufTable = hufTable;
        makeCode();
    }

    public HufCode get(int index) {
        return mCodes.get(index);
    }

    private void makeCode() {
        String content = "" + (mHufTable.type == 0 ? "DC" : "AC") + " HUF " + mHufTable.id + " codesize:\n";
        int codeIndex = 0;
        int count = 0;
        for (int i = 0; i < 16; i++) {
            content += mHufTable.codeSize[i] + " ";
            int size = mHufTable.codeSize[i];
            for (int j = 0; j < size; j++) {
                HufCode code = new HufCode(this, codeIndex, mHufTable.data[codeIndex], i + 1);
                mCodes.add(code);
                codeIndex++;
            }
            count += size;
        }
        content += "\n";
        for (HufCode code : mCodes) {
            content += code.key() + "->" + Helper.byte2String(code.value()) + "\n";
        }
        content += "total:" + count + ", data length:" + mHufTable.data.length;
        ALog.v(TAG, content);
    }

}
