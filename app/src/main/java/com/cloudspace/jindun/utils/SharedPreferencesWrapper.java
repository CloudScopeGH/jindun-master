package com.cloudspace.jindun.utils;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Map;
import java.util.Set;

public class SharedPreferencesWrapper implements SharedPreferences {

    private SharedPreferences impl;

    public SharedPreferencesWrapper(SharedPreferences sp) {
        this.impl = sp;
    }

    @Override
    public boolean contains(String key) {
        //android.util.Log.d("spwrapper", "contains().....");
        return impl.contains(key);
    }

    @Override
    public Map<String, ?> getAll() {
        //android.util.Log.d("spwrapper", "getAll().....");
        return impl.getAll();
    }

    @Override
    public String getString(String key, String defValue) {
        //android.util.Log.d("spwrapper", "getString().....");
        return impl.getString(key, defValue);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        //android.util.Log.d("spwrapper", "getStringSet().....");
        return impl.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        //android.util.Log.d("spwrapper", "getInt().....");
        return impl.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        //android.util.Log.d("spwrapper", "getLong().....");
        return impl.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        //android.util.Log.d("spwrapper", "getFloat().....");
        return impl.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        //android.util.Log.d("spwrapper", "getBoolean().....");
        return impl.getBoolean(key, defValue);
    }

    @Override
    public Editor edit() {
        //android.util.Log.d("spwrapper", "edit().....");
        return new EditorWrapper(impl.edit());
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        impl.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        impl.unregisterOnSharedPreferenceChangeListener(listener);
    }

}
