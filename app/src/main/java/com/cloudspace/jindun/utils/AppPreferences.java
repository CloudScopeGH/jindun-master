package com.cloudspace.jindun.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.cloudspace.jindun.module.Module;


/**
 * ckb on 16/2/27.
 */
public class AppPreferences implements Module<Global> {

    private static final String PRE_NAME = "app_pref";

    private static SharedPreferences mPref;

    @Override
    public void initialize(Global box) {
        mPref = box.getContext().getSharedPreferences(PRE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void destroy() {

    }

    public static int getInt(String key, int defValue) {
        return mPref.getInt(key, defValue);
    }

    public static void putInt(String key, int value) {
        mPref.edit().putInt(key, value).commit();
    }

    public static float getFloat(String key, float defValue) {
        return mPref.getFloat(key, defValue);
    }

    public static void putFloat(String key, float value) {
        mPref.edit().putFloat(key, value).commit();
    }


    public static long getLong(String key, long defValue) {
        return mPref.getLong(key, defValue);
    }

    /**
     * 以键值对的形式在sharedpreference中保存信息
     *
     * @param key   键
     * @param value 值
     */
    public static void putString(String key, String value) {
        if (mPref == null || key == null)
            return;
        mPref.edit().putString(key, value).commit();
    }

    public static boolean getBoolean(String key, boolean... defValue) {
        if (key == null || mPref == null)
            return false;
        return defValue.length > 0 ? mPref.getBoolean(key,
                defValue[0]) : mPref.getBoolean(key, false);
    }

    /**
     * 获得配置信息
     *
     * @param key      键
     * @param defValue 找不到键时的默认值
     * @return 若查找到，返回键值；若查找不到但设置了默认值，则返回默认值；其他情况返回空字符串。
     */
    public static String getString(String key, String... defValue) {
        if (key == null || mPref == null)
            return "";
        return defValue.length > 0 ? mPref.getString(key,
                defValue[0]) : mPref.getString(key, "");
    }
}
