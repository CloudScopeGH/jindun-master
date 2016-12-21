package com.cloudspace.jindun.log;

import android.util.Log;

/**
 * ckb on 16/2/26.
 */
public class APPLog {

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;
    public static final int SHOW_STATS = 8;
    private static int current_level = 1;
    private static final String JINDUN_DEAULT = "jindun_log";
    private final static String TAG_MEMORY = "memory";

    public interface TAG {

        public static final String TRIDIMENSION = "TRIDIMENSION";
        public static final String PUSH = "PUSH";
        public static final String LIVEROOM = "LIVEROOM";


    }

    public static int getCurrentLevel() {
        return current_level;
    }

    public static void v(String tag, String msg) {
        if (current_level < VERBOSE)
            Log.v(tag, msg + "");
    }

    public static void d(String tag, String msg) {
        if (current_level < DEBUG)
            Log.d(tag, msg + "");
    }

    public static void i(String tag, String msg) {
        if (current_level < INFO)
            Log.i(tag, msg + "");
    }

    public static void w(String tag, String msg) {
        if (current_level < WARN)
            Log.w(tag, msg + "");
    }

    public static void e(String tag, String msg) {
        if (current_level < ERROR)
            Log.e(tag, msg + "");
    }

    public static void v(String msg) {
        if (current_level < VERBOSE)
            Log.v(JINDUN_DEAULT, msg + "");
    }

    public static void d(String msg) {
        if (current_level < DEBUG)
            Log.d(JINDUN_DEAULT, msg + "");
    }

    public static void i(String msg) {
        if (current_level < INFO)
            Log.i(JINDUN_DEAULT, msg + "");
    }

    public static void w(String msg) {
        if (current_level < WARN)
            Log.w(JINDUN_DEAULT, msg + "");
    }

    public static void e(String msg) {
        if (current_level < ERROR)
            Log.e(JINDUN_DEAULT, msg + "");
    }

    public static void println(int priority, String tag, String msg) {
        System.err.println(tag + ": " + msg + "\n");
    }

    public static void setCurrentDebugLevel(int level) {
        current_level = level;
    }

    public static void mem(String msg) {
        if (current_level < DEBUG)
            Log.d(TAG_MEMORY, msg + "");
    }

    public static void mem_i(String msg) {
        if (current_level < DEBUG)
            Log.i(TAG_MEMORY, msg + "");
    }


    public static void debugVerboseLog(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void debugLog(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void debugInfoLog(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void debugErrorLog(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void debugErrorUploadLog(String tag, String msg) {
        debugErrorLog(tag, msg);
//		if (PlayerConstants.MEDIAPLAYER_LOG_TAG.equals(tag)
//				|| "StreamProxy".equals(tag) || "HTTPFetcher".equals(tag)
//				|| "DownloadManager".equals(tag) || "DownloadTask".equals(tag)) {
//			KTVUIUtility.captureAndUploadErrorLog("E " + tag + " " + msg);
//		}
    }

    public static void debugFatalLog(String tag, Throwable ex) {
//		if (PlayerConstants.MEDIAPLAYER_LOG_TAG.equals(tag)
//				|| "StreamProxy".equals(tag) || "HTTPFetcher".equals(tag)
//				|| "DownloadManager".equals(tag) || "DownloadTask".equals(tag)) {
//			KTVUIUtility.handleMediaPlayerException(ex);
//		}
    }

    public static void debugFatalLog(String tag, String msg, Throwable ex) {
        debugErrorLog(tag, msg);
        debugFatalLog(tag, ex);
    }

}
