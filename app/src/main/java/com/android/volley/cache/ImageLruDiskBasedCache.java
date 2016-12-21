package com.android.volley.cache;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.VolleyLog;
import com.android.volley.cache.DiskBasedCache.CacheHeader;
import com.android.volley.misc.IOUtils;
import com.android.volley.misc.IOUtils.CountingInputStream;
import com.android.volley.misc.ImageUtils;
import com.android.volley.misc.Utils;
import com.cloudspace.jindun.BuildConfig;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cache implementation that caches files directly onto the hard disk in the specified directory
 * using DiskLruCache
 * 参照 bitmapfun.util.imageCache
 * https://github.com/chentaov5/BitmapFun/blob/master/src/com/example/android/bitmapfun/util/ImageCache.java
 */
public class ImageLruDiskBasedCache implements Cache {

    private static final String TAG = "ImageLruDiskBasedCache";

    // Default disk cache size in bytes
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 20; // 10MB

    // Constants to easily toggle various caches

//    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
//    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
//    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final int DISK_CACHE_INDEX = 0;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    public int mDiskMaxCacheSize = DEFAULT_DISK_CACHE_SIZE;
    public File mRootDirectory;

    public ImageLruDiskBasedCache(File rootDirectory) {
        mRootDirectory = rootDirectory;
    }

    public ImageLruDiskBasedCache(File rootDirectory, int maxDiskCacheSizeInBytes) {
        mRootDirectory = rootDirectory;
        mDiskMaxCacheSize = maxDiskCacheSizeInBytes;
    }

    public void putBitmap(String data, Bitmap value) {
        if (data == null || value == null) {
            return;
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            value.compress(
                                    DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public Bitmap getBitmap(String data) {

        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = ImageUtils.decodeSampledBitmapFromDescriptor(fd, Integer.MAX_VALUE, Integer.MAX_VALUE);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
            return bitmap;
        }
    }

    public boolean containsKey(String key) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskLruCache.get(key);
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return contained;
    }

    public void clearCache() {
        if (BuildConfig.DEBUG) {
            Log.d("cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskLruCache.getDirectory();
    }

    /**
     * Initializes the disk cache.  Note that this includes disk access so this should not be
     * executed on the main/UI thread. By default an ImageCache does not initialize the disk
     * cache when it is created, instead you should call initDiskCache() to initialize it on a
     * background thread.
     */
    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                if (/*mCacheParams.diskCacheEnabled && */mRootDirectory != null) {
                    if (!mRootDirectory.exists()) {
                        mRootDirectory.mkdirs();
                    }
                    if (Utils.getUsableSpace(mRootDirectory) > mDiskMaxCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(mRootDirectory, APP_VERSION, VALUE_COUNT, mDiskMaxCacheSize);
                            if (BuildConfig.DEBUG) {
                                VolleyLog.d("Disk cache initialized");
                            }
                        } catch (final IOException e) {
                            mRootDirectory = null;
                            VolleyLog.e("initDiskCache - " + e);
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    /**
     * Creates a pseudo-unique filename for the specified cache key.
     *
     * @param key The key to generate a file name for.
     * @return A pseudo-unique filename.
     */
    @SuppressWarnings("unused")
    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

    /**
     * Returns a file object for the given cache key.
     */
    public File getFileForKey(String key) {
        return new File(mRootDirectory, key + ".0");
    }

    @Override
    public Entry get(String data) {
        if (data == null) {
            return null;
        }
        final String key = hashKeyForDisk(data);
        // if the entry does not exist, return.

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                File file = getFileForKey(key);
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            CountingInputStream cis = new CountingInputStream(inputStream);
                            CacheHeader entry = CacheHeader.readHeader(cis); // eat header
                            if (file.length() - cis.getBytesRead() <= 0)
                                return null;
                            byte[] dataBytes = IOUtils.streamToBytes(cis, (int) (file.length() - cis.getBytesRead()));
                            return entry.toCacheEntry(dataBytes);
                        }
                    }
                } catch (final Exception e) {
                    remove(key);
                    Log.e(TAG, "getDiskLruBasedCache - " + e);
                    return null;
                } catch (OutOfMemoryError e) {
                    VolleyLog.e("Caught OOM for %d byte image, path=%s: %s", file.length(), file.getAbsolutePath(), e.toString());
                    return null;
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void put(String data, Entry value) {
        if (data == null || value == null) {
            return;
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    //DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    //if (snapshot == null) {
                    final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX);
                        CacheHeader e = new CacheHeader(key, value);
                        e.writeHeader(out);
                        out.write(value.data);
                        editor.commit();
                        out.close();
                    }
/*                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }*/
                } catch (final IOException e) {
                    Log.e(TAG, "putDiskLruBasedCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "putDiskLruBasedCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        initDiskCache();
    }

    @Override
    public void invalidate(String key, boolean fullExpire) {
        Entry entry = get(key);
        if (entry != null) {
            entry.softTtl = -1;
            if (fullExpire) {
                entry.ttl = -1;
            }
            put(key, entry);
        }
    }

    @Override
    public void remove(String data) {
        if (data == null) {
            return;
        }

        synchronized (mDiskCacheLock) {
            // remove to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                try {
                    mDiskLruCache.remove(key);
                } catch (final IOException e) {
                    Log.e(TAG, "removeDiskLruBasedCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "removeDiskLruBasedCache - " + e);
                }
            }
        }
    }

    /**
     * clear the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    @Override
    public void clear() {
        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Disk cache cleared");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "clearCache - " + e);
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Disk cache flushed");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache closed");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }
}