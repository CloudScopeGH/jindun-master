/*
 * Copyright 2011 - AndroidQuery.com (tinyeeliu@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cloudspace.jindun.ext.aq;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;


import com.cloudspace.jindun.log.APPLog;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utility methods. Warning: Methods might chankeyboardReceiverged in future versions.
 */

public class AQUtility {

    private static boolean debug = false;
    private static Object wait;

    public static void setDebug(boolean debug) {
        AQUtility.debug = debug;
    }

    public static void debugWait(long time) {

        if (!debug) return;

        if (wait == null) wait = new Object();

        synchronized (wait) {

            try {
                wait.wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public static void debugNotify() {

        if (!debug || wait == null) return;

        synchronized (wait) {
            wait.notifyAll();
        }

    }


    public static void debug(Object msg) {
        if (debug) {
            APPLog.w("AQuery", msg + "");
        }
    }

    public static void warn(Object msg, Object msg2) {
        APPLog.w("AQuery", msg + ":" + msg2);
    }

    public static void debug(Object msg, Object msg2) {
        if (debug) {
            APPLog.w("AQuery", msg + ":\n" + msg2);
        }
    }

    public static void debug(Throwable e) {
        if (debug) {
            String trace = Log.getStackTraceString(e);
//			Log.w("AQuery", trace);
            APPLog.w("AQuery", trace);
        }
    }

    public static void report(Throwable e) {

        if (e == null) return;

        try {

            //debug(e);
            warn("reporting", Log.getStackTraceString(e));

            if (eh != null) {
                eh.uncaughtException(Thread.currentThread(), e);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static UncaughtExceptionHandler eh;

    public static void setExceptionHandler(UncaughtExceptionHandler handler) {
        eh = handler;
    }

    private static Map<String, Long> times = new HashMap<String, Long>();

    public static void time(String tag) {

        times.put(tag, System.currentTimeMillis());

    }

    public static long timeEnd(String tag, long threshold) {


        Long old = times.get(tag);
        if (old == null) return 0;

        long now = System.currentTimeMillis();

        long diff = now - old;

        if (threshold == 0 || diff > threshold) {
            debug(tag, diff);
        }

        return diff;


    }

    public static Object invokeHandler(Object handler, String callback, boolean fallback, boolean report, Class<?>[] cls, Object... params) {

        return invokeHandler(handler, callback, fallback, report, cls, null, params);

    }

    public static Object invokeHandler(Object handler, String callback, boolean fallback, boolean report, Class<?>[] cls, Class<?>[] cls2, Object... params) {
        try {
            return invokeMethod(handler, callback, fallback, cls, cls2, params);
        } catch (Exception e) {
            if (report) {
                AQUtility.report(e);
            } else {
                AQUtility.debug(e);
            }
            return null;
        }
    }


    private static Object invokeMethod(Object handler, String callback, boolean fallback, Class<?>[] cls, Class<?>[] cls2, Object... params) throws Exception {

        if (handler == null || callback == null) return null;

        Method method = null;

        try {
            if (cls == null) cls = new Class[0];
            method = handler.getClass().getMethod(callback, cls);
            return method.invoke(handler, params);
        } catch (NoSuchMethodException e) {
            //AQUtility.debug(e.getMessage());
        }


        try {
            if (fallback) {

                if (cls2 == null) {
                    method = handler.getClass().getMethod(callback);
                    return method.invoke(handler);
                } else {
                    method = handler.getClass().getMethod(callback, cls2);
                    return method.invoke(handler, params);
                }

            }
        } catch (NoSuchMethodException e) {
        }

        return null;

    }

    public static void transparent(View view, boolean transparent) {

        float alpha = 1;
        if (transparent) alpha = 0.5f;

        setAlpha(view, alpha);

    }


    private static void setAlpha(View view, float alphaValue) {

        if (alphaValue == 1) {
            view.clearAnimation();
        } else {
            AlphaAnimation alpha = new AlphaAnimation(alphaValue, alphaValue);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the animation ends
            view.startAnimation(alpha);
        }

    }

    public static void ensureUIThread() {

        if (!isUIThread()) {
            AQUtility.report(new IllegalStateException("Not UI Thread"));
        }

    }

    public static boolean isUIThread() {

        long uiId = Looper.getMainLooper().getThread().getId();
        long cId = Thread.currentThread().getId();

        return uiId == cId;


    }


    private static Handler handler;

    public static Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public static void post(Runnable run) {
        getHandler().post(run);
    }

    public static void post(Object handler, String method) {
        post(handler, method, new Class[0]);
    }


    public static void post(final Object handler, final String method, final Class<?>[] sig, final Object... params) {
        post(new Runnable() {

            @Override
            public void run() {

                AQUtility.invokeHandler(handler, method, false, true, sig, params);

            }
        });
    }

    public static void postAsync(Object handler, String method) {
        postAsync(handler, method, new Class[0]);
    }

    public static void postAsync(final Object handler, final String method, final Class<?>[] sig, final Object... params) {

        ExecutorService exe = getFileStoreExecutor();

        exe.execute(new Runnable() {

            @Override
            public void run() {

                AQUtility.invokeHandler(handler, method, false, true, sig, params);

            }
        });
    }

    public static void removePost(Runnable run) {
        getHandler().removeCallbacks(run);
    }

    public static void postDelayed(Runnable run, long delay) {
        getHandler().postDelayed(run, delay);
    }

    public static void removeCallbacks(Runnable runnable) {
        getHandler().removeCallbacks(runnable);
    }

    public static void postAtFrontOfQueue(Runnable run) {
        getHandler().postAtFrontOfQueue(run);
    }

    private static String getMD5Hex(String str) {
        byte[] data = getMD5(str.getBytes());

        BigInteger bi = new BigInteger(data).abs();

        String result = bi.toString(36);
        return result;
    }


    private static byte[] getMD5(byte[] data) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(data);
            byte[] hash = digest.digest();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            AQUtility.report(e);
        }

        return null;

    }

    private static final int IO_BUFFER_SIZE = 1024 * 4;

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    public static byte[] toBytes(InputStream is) {

        byte[] result = null;

        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            copy(is, baos);
            result = baos.toByteArray();
        } catch (IOException e) {
            AQUtility.report(e);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            close(is);
            close(baos);
        }

        return result;

    }

    public static byte[] toBytes(File file) {
        byte[] result = null;
        if (file != null && file.exists()) {
            try {
                result = AQUtility.toBytes(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static ScheduledExecutorService storeExe;

    private static ScheduledExecutorService getFileStoreExecutor() {

        if (storeExe == null) {
            storeExe = Executors.newSingleThreadScheduledExecutor();
        }

        return storeExe;
    }

    public static void store(File file, byte[] data, Object object) {
        try {
            if (object instanceof String) {//过滤不正确的json结果
                boolean isok = ((String) object).contains("\"errorcode\":\"ok\"");
                if (!isok) {
                    return;
                }
            }
            if (file != null && data != null && data.length > 0) {
                AQUtility.write(file, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将序列化对象直接写入文件
     *
     * @param obj  实现Serializable接口的对象
     * @param file
     * @throws IOException
     */
    public static void storeObject(Object obj, File file) {
        if (file != null && obj != null) {
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            try {
                fos = new FileOutputStream(file);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(obj);
            } catch (Exception e) {
                e.printStackTrace();
                file.delete();
            } finally {
                close(oos);
                close(fos);
            }
        }
    }

    private static File cacheDir;
    //	private static File pcacheDir;
    private static Map<String, File> subFileDirs = new HashMap<String, File>();

    public static File getCacheDir(Context context, int policy) {
        return getCacheDir(context);
    }

    public static File getCacheDir(Context context, String subDir) {

        if (null != subDir && subDir.length() > 0) {
            String dirPath = getCacheDir(context) + File.separator + subDir;
            File file = null;
            file = subFileDirs.get(dirPath);
            if (null == file) {
                file = new File(dirPath);
            }
            if (!file.exists()) {
                file.mkdirs();
            }
            if (null == subFileDirs.get(dirPath)) {
                subFileDirs.put(dirPath, file);
            }
            return file;

        } else {
            return getCacheDir(context);
        }

    }

    public static File getCacheDir(Context context) {
        if (cacheDir == null && context != null) {
            cacheDir = new File(context.getCacheDir(), "aquery");
            cacheDir.mkdirs();
        }
        return cacheDir;

    }

    public static void setCacheDir(File dir) {
        cacheDir = dir;
        if (cacheDir != null) {
            cacheDir.mkdirs();
        }
    }


    private static File makeCacheFile(File dir, String name) {
        return new File(dir, name);
    }

    private static String getCacheFileName(String url) {

        String hash = getMD5Hex(url);
        return hash;
    }

    public static File getCacheFile(File dir, String url) {
        if (url == null) return null;
        if (url.startsWith(File.separator)) {
            return new File(url);
        }

        String name = getCacheFileName(url);
        return makeCacheFile(dir, name);
    }

    public static File getExistedCacheByUrl(File dir, String url) {

        File file = getCacheFile(dir, url);
        if (file == null || !file.exists()) {
            return null;
        }
        return file;
    }

    public static File getExistedCacheByUrlSetAccess(File dir, String url) {
        File file = getExistedCacheByUrl(dir, url);
        if (file != null) {
            lastAccess(file);
        }
        return file;
    }

    private static void lastAccess(File file) {
        long now = System.currentTimeMillis();
        file.setLastModified(now);
    }

    public static void delete(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            AQUtility.report(e);
        }
    }

    //根据文件个数来删除
    public static void cleanCacheCount(File[] files, int maxSize) {
        int deletes = 0;
        int size = files.length;
        if (size > maxSize) {
            for (int i = maxSize; i < size; i++) {
                File f = files[i];
                if (f.isFile()) {
                    f.delete();
                    deletes++;
                }
            }
        }
        AQUtility.debug("deleted-count", "total " + size + "delete " + deletes);
    }

    public static void clearCachedFiles(File dir, int maxSize) {
        if (dir != null && dir.isDirectory()) {
            String[] fileNames = dir.list();
            if (null == fileNames || fileNames.length < maxSize) {
                return;
            }
            File[] files = dir.listFiles();
            try {
                System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        return (int) (rhs.lastModified() - lhs.lastModified());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            AQUtility.cleanCacheCount(files, maxSize);
        }
    }

    public static int dip2pixel(Context context, float n) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
    }

    /**
     * @param file
     */
    public static Object file2Object(File file) {
        Object obj = null;
        if (file != null && file.exists()) {
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                obj = ois.readObject();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(fis);
                close(ois);
            }
        }
        return obj;
    }


    public static void write(File file, byte[] data) {

        try {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    AQUtility.debug("file create fail", file);
                    AQUtility.report(e);
                }
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            AQUtility.report(e);
        }

    }

    public static void close(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
        }
    }
}
