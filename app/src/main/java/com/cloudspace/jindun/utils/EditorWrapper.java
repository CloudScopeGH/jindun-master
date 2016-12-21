package com.cloudspace.jindun.utils;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;

import com.cloudspace.jindun.UCAPIApp;

import java.util.Set;


public class EditorWrapper implements SharedPreferences.Editor {

    private SharedPreferences.Editor impl;
    private Object lock;

    public EditorWrapper(SharedPreferences.Editor editor) {
        this.impl = editor;
        this.lock = UCAPIApp.getInstance().getSharedPreferenceLock();
    }

    @Override
    public SharedPreferences.Editor putString(String key, String value) {
        //android.util.Log.d("speditwrapper", "putString().....");
        synchronized (lock) {
            return impl.putString(key, value);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
        //android.util.Log.d("speditwrapper", "putStringSet().....");
        synchronized (lock) {
            return impl.putStringSet(key, values);
        }
    }

    @Override
    public SharedPreferences.Editor putInt(String key, int value) {
        //android.util.Log.d("speditwrapper", "putInt().....");
        synchronized (lock) {
            return impl.putInt(key, value);
        }
    }

    @Override
    public SharedPreferences.Editor putLong(String key, long value) {
        //android.util.Log.d("speditwrapper", "putLong().....");
        synchronized (lock) {
            return impl.putLong(key, value);
        }
    }

    @Override
    public SharedPreferences.Editor putFloat(String key, float value) {
        //android.util.Log.d("speditwrapper", "putFloat().....");
        synchronized (lock) {
            return impl.putFloat(key, value);
        }
    }

    @Override
    public SharedPreferences.Editor putBoolean(String key, boolean value) {
        //android.util.Log.d("speditwrapper", "putBoolean().....");
        synchronized (lock) {
            return impl.putBoolean(key, value);
        }
    }

    @Override
    public SharedPreferences.Editor remove(String key) {
        //android.util.Log.d("speditwrapper", "remove().....");
        synchronized (lock) {
            return impl.remove(key);
        }
    }

    @Override
    public SharedPreferences.Editor clear() {
        //android.util.Log.d("speditwrapper", "clear().....");
        synchronized (lock) {
            return impl.clear();
        }
    }

    @Override
    public boolean commit() {
        //android.util.Log.d("speditwrapper", "commit().....");
        synchronized (lock) {
            return impl.commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void apply() {
        //android.util.Log.d("speditwrapper", "apply().....");
        synchronized (lock) {
            impl.apply();
        }
    }
}
