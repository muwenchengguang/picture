package com.peng.jpegviewer.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by richie on 5/8/18.
 */

public class Helper {

    public static boolean isNewVersion(Context context, String version) {
        return strcmp(version, getVersion(context)) > 0;
    }

    public static String getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return pi.versionName;
    }

    public static int strcmp(String left, String right) {
        if (left == null || right == null) throw new NullPointerException("string is null");
        int lLen = left.length();
        int rLen = right.length();
        int count = lLen < rLen ? lLen : rLen;
        for (int i = 0; i < count; i++) {
            char lc =left.charAt(i);
            char rc = right.charAt(i);
            int diff = lc - rc;
            if (diff > 0) return 1; // left bigger
            if (diff < 0) return -1; // right bigger
        }
        if (lLen == rLen) return 0;
        return (lLen > rLen ? 1 : -1);
    }

    public static boolean isNumber(String content) {
        if (content == null || content.length() <= 0) return false;
        content = content.trim();
        if (content.length() <= 0) return false;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch < '0' || ch > '9') return false;
        }
        return true;
    }

    public static int parseNumber(String content) {
        if (content == null || content.length() <= 0) return -1;
        content = content.trim();
        if (content.length() <= 0) return -1;
        int number = 0;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch < '0' || ch > '9') return -1;
            number = number*10;
            number += ch - '0';
        }
        return number;
    }

    public static int parseNumberWithNoCheck(String content) {
        if (content == null || content.length() <= 0) return -1;
        content = content.trim();
        if (content.length() <= 0) return -1;
        int number = 0;
        boolean found = false;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch < '0' || ch > '9') {
                if (!found) continue;
                break;
            }
            if (!found) found = true;
            number = number*10;
            number += ch - '0';
        }
        return found ? number : -1;
    }

    public static String number2String(byte[] data, int offset, int count) {
        if (offset < 0 || count <= 0 || (count + offset > data.length)) {
            return "";
        }
        String result = "";
        for (int i = offset; i < count + offset; i++) {
            if (data[i] >= 0 && data[i] <= 15) {
                result += chs[data[i]];
            }
        }
        return result;
    }

    private static final char[] chs = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String hex2String(byte[] data) {
        String result = "";
        for (byte item : data) {
            result += chs[(item>>4)&0x0f];
            result += chs[item&0x0f];
        }
        return result;
    }

    public static String hex2String(byte[] data, int offset, int count) {
        if (offset < 0 || count <= 0 || (count + offset > data.length)) {
            return "";
        }

        String result = "";
        for (int i = offset; i < count + offset; i++) {
            byte item = data[i];
            result += chs[(item>>4)&0x0f];
            result += chs[item&0x0f];
        }
        return result;
    }

    public static String hex2String(byte hex) {
        String result = "";
        result += chs[(hex>>4)&0x0f];
        result += chs[hex&0x0f];
        return result;
    }
}
