package com.android.volley;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public class GlobalExecutor {
    private static Handler sMainLooperHandler;

    private static Handler getUiHandler() {
        if (sMainLooperHandler == null) {
            sMainLooperHandler = new Handler(Looper.getMainLooper());
        }
        return sMainLooperHandler;
    }

    public static void cancel(Object tag) {
        getUiHandler().removeCallbacksAndMessages(tag);
    }

    public static void execute(Runnable runnable) {
        execute(runnable, null, false);
    }

    public static void postUI(Runnable runnable) {
        execute(runnable, null, true);
    }

    public static void execute(Runnable runnable, Object tag, boolean isMainThread) {
        if (runnable == null) {
            return;
        }
        if (isMainThread && !isUiThread()) {
            if (tag == null) {
                getUiHandler().post(runnable);
            } else {
                getUiHandler().postAtTime(runnable, tag, SystemClock.uptimeMillis());
            }
        } else {
            runnable.run();
        }
    }


    public static void postDelayed(Runnable r, long delayed) {
        getUiHandler().postDelayed(r, delayed);
    }


    public static void postUINow(Runnable runnable) {
        if (isUiThread()) {
            runnable.run();
            return;
        }
        getUiHandler().postAtFrontOfQueue(runnable);
    }


    private static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}