package com.peng.jpegviewer;

import android.app.Activity;
import android.os.Bundle;

import com.peng.jpegviewer.decoder.JPEGDecoder;
import com.peng.jpegviewer.parser.JPEGParser;
import com.peng.jpegviewer.utils.ALog;



public class MainActivity extends Activity {
    private final ALog.Category TAG = ALog.Category.AppDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JPEGParser parser = new JPEGParser(this);
        JPEGDecoder decoder = new JPEGDecoder(parser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
