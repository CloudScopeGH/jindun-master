package com.cloudspace.jindun.crash;

import android.util.Log;

import com.cloudspace.jindun.module.Module;
import com.cloudspace.jindun.utils.Global;


public class DVMCrashHandler implements Module<Global>, Thread.UncaughtExceptionHandler, Runnable {

    private Thread.UncaughtExceptionHandler otherCrashHandler;

    public Global global;

    private Thread shutdownThread;

    @Override
    public void initialize(Global box) {
        global = box;
        otherCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void destroy() {
        //这个模块的周期希望比App还长
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            if (!handleException(ex) && otherCrashHandler != null) {
                otherCrashHandler.uncaughtException(thread, ex);
                Log.e("DVMCrashHandler", ex.getMessage(), ex);
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private boolean handleException(Throwable ex) {
        shutdownThread = new Thread(this, "Shutdown Thread");
        shutdownThread.setDaemon(false);
        shutdownThread.start();
        return false;
    }

    @Override
    public void run() {
        Log.i("DVMCrashHandler", "uncatched exception happened");
        global.destroyModules();
    }
}
