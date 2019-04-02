package com.peng.jpegviewer.utils;

import android.util.Log;

import com.peng.jpegviewer.BuildConfig;


/**
 * Created by richie on 6/12/18.
 */

public class ALog {
    private static boolean Enable = BuildConfig.DEBUG;
    private static final String APP = "DemoPeng";

    public enum Category {
        AppDemo,
        Decoder,

        MessageQueue,
        HttpsClient,
        Utils,
    }

    private static boolean enableCategoryLog(Category tag) {
        return true;
    }

    public static void i(Category tag, String log) {
        if (Enable && enableCategoryLog(tag)) {
            Log.i(APP, "[" + tag + "] " + log);
        }
    }

    public static void w(Category tag, String log) {
        Log.w(APP, "[" + tag + "] " + log);
    }

    public static void d(Category tag, String log) {
        if (Enable && enableCategoryLog(tag)) {
            Log.d(APP, "[" + tag + "] " + log);
        }
    }

    public static void v(Category tag, String log) {
        if (Enable && enableCategoryLog(tag)) {
            Log.v(APP, "[" + tag + "] " + log);
        }
    }

    public static void e(Category tag, String log) {
        Log.e(APP, "[" + tag + "] " + log);
    }

}
