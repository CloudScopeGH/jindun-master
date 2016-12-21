package com.cloudspace.jindun.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cloudspace.jindun.crash.DVMCrashHandler;
import com.cloudspace.jindun.crash.ErrorReoprter;
import com.cloudspace.jindun.module.ModuleBox;

import java.util.Date;

/**
 * App的设备相关基础模块构建
 * <p/>
 * ckb on 15/11/26.
 */
public final class Global extends ModuleBox {

    private static Context appContext = null;

    private static Global instance = null;
    private Date startDate;

    public static Context getContext() {
        if (appContext == null) {
            throw new RuntimeException("app is not extend base appliction in androidmanifest.xml");
        }
        return appContext;
    }

    private final static void setContext(Context appContext) {
        Global.appContext = appContext;
    }

    public static void init(Context appContext) {
        setContext(appContext);
        if (instance != null) {
            throw new RuntimeException("a Global already exists");
        }
        instance = new Global();
    }

    public Global() {
        super(getContext().getClassLoader());
        initialize(this);
    }

    public void initialize(ModuleBox box) {
        setStartDate(new Date());
        loadModules();
        initModules();
    }

    public static Object loadClass(ClassLoader loader, String clazzFullName) {
        try {
            Class<?> modClass = loader.loadClass(clazzFullName);
            Object mod = modClass.newInstance();
            return mod;
        } catch (Exception e) {
            Log.e(" class load error [" + clazzFullName + "]", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Date getStartDate() {
        return startDate;
    }

    private void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * 注册所有模块
     */
    private void loadModules() {
        loadModule(DVMCrashHandler.class.getName()); //crash 要第一个被注册
        loadModule(Device.class.getName());
        loadModule(AppPreferences.class.getName());
        loadModule(ErrorReoprter.class.getName());
    }

    public AppPreferences getAppPreferences() {
        return (AppPreferences) getModuleByName(AppPreferences.class.getSimpleName());
    }

    public final static Object getSystemService(@NonNull String name) {
        return getContext().getSystemService(name);
    }

    public final Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return getContext().registerReceiver(receiver, filter);
    }

    public final void unregisterReceiver(BroadcastReceiver receiver) {
        getContext().unregisterReceiver(receiver);
    }

}
