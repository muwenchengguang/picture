/**
 * 
 * @author PengPeng
 * @date 2016-06-13
 * 
 */

package com.peng.jpegviewer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PrefManager {
    private Context mContext = null;
    private SharedPreferences mPrefs = null;
    private static final String PREF_TAG = "XKit";
    private static PrefManager sPrefManager = null;

    private PrefManager(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
    }

    public static void create(Context app) {
        if (sPrefManager == null) {
            sPrefManager = new PrefManager(app);
        }
    }

    public static PrefManager instance() {
        if (sPrefManager == null) throw new NullPointerException("PrefManager is null");
        return sPrefManager;
    }

    public String getString(String key, String def) {
        return mPrefs.getString(key, def);
    }

    public int getInt(String key, int def) {
        return mPrefs.getInt(key, def);
    }

    public void setString(String key, String value) {
        mPrefs.edit().putString(key, value).commit();
    }

    public void setInt(String key, int value) {
        mPrefs.edit().putInt(key, value).commit();
    }

    public void addStringSetItem(String key, String itemValue) {
        Set<String> stringSet = mPrefs.getStringSet(key, new HashSet<String>());
        if (!stringSet.contains(itemValue)) {
            stringSet.add(itemValue);
            mPrefs.edit().putStringSet(key, stringSet).commit();
        }
    }

    public Set<String> getStringSet(String key) {
        return mPrefs.getStringSet(key, new HashSet<String>());
    }

}
