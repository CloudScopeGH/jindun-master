/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.cache;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.StatFs;
import android.support.v4.util.LruCache;

import com.android.ui.drawable.RecyclingBitmapDrawable;
import com.android.volley.VolleyLog;
import com.android.volley.misc.Utils;

import java.io.File;

/**
 * This class holds our bitmap caches (memory and disk).
 */
public class MemLruImageCache implements ImageCache {
    private static final String TAG = "MemLruImageCache";
    // Default memory cache size in kilobytes
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB
    // Default memory cache size as a percent of device memory class
    private static final float DEFAULT_MEM_CACHE_PERCENT = 0.125f;

    private LruCache<String, BitmapDrawable> mMemoryCache;

//    private Set<SoftReference<Bitmap>> mReusableBitmaps;
//	private LruCache<String, SoftReference<BitmapDrawable>> mReusableMemoryCache;

    /**
     * Don't instantiate this class directly, use
     *
     * @param memCacheSize Memory cache size in KB.
     */
    private MemLruImageCache(int memCacheSize) {
        init(memCacheSize);
    }

    public static MemLruImageCache getInstance() {
        int memCacheSize = Math.max(DEFAULT_MEM_CACHE_SIZE, calculateMemCacheSize(DEFAULT_MEM_CACHE_PERCENT));
        return new MemLruImageCache(memCacheSize);
    }

    /**
     * Initialize the cache.
     */
    private void init(int memCacheSize) {
        // Set up memory cache
        VolleyLog.d(TAG, "Memory cache created (size = " + memCacheSize + "KB)");
        // If we're running on Honeycomb or newer, create a set of reusable bitmaps that can be
        // populated into the inBitmap field of BitmapFactory.Options. Note that the set is
        // of SoftReferences which will actually not be very effective due to the garbage
        // collector being aggressive clearing Soft/WeakReferences. A better approach
        // would be to use a strongly references bitmaps, however this would require some
        // balancing of memory usage between this set and the bitmap LruCache. It would also
        // require knowledge of the expected size of the bitmaps. From Honeycomb to JellyBean
        // the size would need to be precise, from KitKat onward the size would just need to
        // be the upper bound (due to changes in how inBitmap can re-use bitmaps).
//        if (Utils.hasHoneycomb()) {
//            mReusableBitmaps =
//                    Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
//        }
        mMemoryCache = new LruCache<String, BitmapDrawable>(memCacheSize) {
            /**
             * Measure item size in kilobytes rather than units which is more practical
             * for a bitmap cache
             */
            @Override
            protected int sizeOf(String key, BitmapDrawable bitmap) {
                final int bitmapSize = getBitmapSize(bitmap) / 1024;
                return bitmapSize == 0 ? 1 : bitmapSize;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);

                VolleyLog.d(TAG, "Memory cache entry removed - " + key);
                if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
                    // The removed entry is a recycling drawable, so notify it
                    // that it has been removed from the memory cache
                    ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
                }/*else if(oldValue != null && mReusableMemoryCache!=null && Utils.hasHoneycomb())//mReusableMemoryCache only hasHoneycomb need
                {
            		mReusableMemoryCache.put(key, new SoftReference<BitmapDrawable>(oldValue));
				}*/
            }
        };

//        mReusableMemoryCache = new LruCache<String, SoftReference<BitmapDrawable>>(memCacheSize) {
//            /**
//             * Measure item size in kilobytes rather than units which is more practical
//             * for a bitmap cache
//             */
//            @Override
//            protected int sizeOf(String key, SoftReference<BitmapDrawable> bitmapRef) {
//                final int bitmapSize = getBitmapSize(bitmapRef.get()) / 1024;
//                return bitmapSize == 0 ? 1 : bitmapSize;
//            }
//
//            @Override
//            protected void entryRemoved(boolean evicted, String key, SoftReference<BitmapDrawable> oldValue, SoftReference<BitmapDrawable> newValue) {
//            	super.entryRemoved(evicted, key, oldValue, newValue);
//
//            	VolleyLog.d(TAG, "Memory cache entry removed - " + key);
//              // The removed entry is a standard BitmapDrawable
//
//             /* if (Utils.hasHoneycomb()) {
//                  // We're running on Honeycomb or later, so add the bitmap
//                  // to a SoftReference set for possible use with inBitmap later
//                  mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.get().getBitmap()));
//              }*/
//            }
//        };
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param data   Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, BitmapDrawable bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        synchronized (mMemoryCache) {
            // Add to memory cache
            //if (mMemoryCache.get(data) == null) {
            VolleyLog.d(TAG, "Memory cache put - " + data);
            if (RecyclingBitmapDrawable.class.isInstance(bitmap)) {
                // The removed entry is a recycling drawable, so notify it
                // that it has been removed from the memory cache
                ((RecyclingBitmapDrawable) bitmap).setIsCached(true);
            }
            mMemoryCache.put(data, bitmap);
            //}
        }
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public BitmapDrawable getBitmapFromMemCache(String data) {
        if (data != null) {
            synchronized (mMemoryCache) {
                final BitmapDrawable memBitmap = mMemoryCache.get(data);
                if (memBitmap != null) {
                    VolleyLog.d(TAG, "Memory cache hit - " + data);
                    return memBitmap;
                }/*else{//mMemoryCache 中没找到
                	final SoftReference<BitmapDrawable> bitmapRef = mReusableMemoryCache.get(data);
                	if(bitmapRef!=null){
                		BitmapDrawable bitmap = bitmapRef.get();
                		if(bitmap!=null){
                			mMemoryCache.put(data, bitmap);
                			mReusableMemoryCache.remove(data);
                		}
                		return bitmap;
                	}
                }*/
            }
            VolleyLog.d(TAG, "Memory cache miss - " + data);
        }
        return null;
    }

    /**
     * @param options - BitmapFactory.Options with out* options populated
     * @return Bitmap that case be used for inBitmap
     */
    public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        //BEGIN_INCLUDE(get_bitmap_from_reusable_set)
        Bitmap bitmap = null;

        /*if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }*/
        return bitmap;
        //END_INCLUDE(get_bitmap_from_reusable_set)
    }

    /**
     * Clears the memory cache.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            VolleyLog.d(TAG, "Memory cache cleared");
        }
    }

    /**
     * Sets the memory cache size based on a percentage of the max available VM memory.
     * Eg. setting percent to 0.2 would set the memory cache to one fifth of the available
     * memory. Throws {@link IllegalArgumentException} if percent is < 0.05 or > .8.
     * memCacheSize is stored in kilobytes instead of bytes as this will eventually be passed
     * to construct a LruCache which takes an int in its constructor.
     * <p/>
     * This value should be chosen carefully based on a number of factors
     * Refer to the corresponding Android Training class for more discussion:
     * http://developer.android.com/training/displaying-bitmaps/
     *
     * @param percent Percent of memory class to use to size memory cache
     * @return Memory cache size in KB
     */
    public static int calculateMemCacheSize(float percent) {
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                    + "between 0.05 and 0.8 (inclusive)");
        }
        return Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
    }

    /**
     * Get the size in bytes of a bitmap.
     */
    @TargetApi(19)
    public static int getBitmapSize(Bitmap bitmap) {
        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
        // larger than bitmap byte count.
  /*      if (Utils.hasKitKat()) {
            return bitmap.getAllocationByteCount();
        }*/

        if (Utils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }

        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
     * @param value
     * @return size in bytes
     */
    public static int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();
        return getBitmapSize(bitmap);
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressWarnings("deprecation")
    @TargetApi(9)
    public static long getUsableSpace(File path) {
        if (Utils.hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     *
     * @param fm          The FragmentManager manager to use.
     * @param fragmentTag The tag of the retained fragment (should be unique for each memory
     *                    cache that needs to be retained).
     * @return The existing instance of the Fragment or the new instance if just
     * created.
     */
 /*   private static RetainFragment getRetainFragment(FragmentManager fm, String fragmentTag) {
        // Check to see if we have retained the worker fragment.
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(fragmentTag);

        // If not retained (or first time running), we need to create and add it.
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, fragmentTag).commitAllowingStateLoss();
        }

        return mRetainFragment;
    }*/
    @Override
    public BitmapDrawable getBitmap(String key) {
        return getBitmapFromMemCache(key);
    }

    @Override
    public void putBitmap(String key, BitmapDrawable bitmap) {
        addBitmapToCache(key, bitmap);
    }

    @Override
    public void invalidateBitmap(String url) {
        if (url == null) {
            return;
        }

        synchronized (mMemoryCache) {
            // Add to memory cache
            //if (mMemoryCache.get(data) == null) {
            VolleyLog.d(TAG, "Memory cache remove - " + url);
            mMemoryCache.remove(url);
            //}
        }
    }

    @Override
    public void clear() {
        clearCache();
    }

    /**
     * @param candidate - Bitmap to check
     * @param targetOptions - Options that have the out* value populated
     * @return true if <code>candidate</code> can be used for inBitmap re-use with
     *      <code>targetOptions</code>
     */
   /* @TargetApi(19)
    private static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {
        //BEGIN_INCLUDE(can_use_for_inbitmap)
        if (!Utils.hasKitKat()) {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth
                    && candidate.getHeight() == targetOptions.outHeight
                    && targetOptions.inSampleSize == 1;
        }

        // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
        // is smaller than the reusable bitmap candidate allocation byte count.
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
        //END_INCLUDE(can_use_for_inbitmap)
    }*/


    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     *
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    private static int getBytesPerPixel(Config config) {
        if (config == Config.ARGB_8888) {
            return 4;
        } else if (config == Config.RGB_565) {
            return 2;
        } else if (config == Config.ARGB_4444) {
            return 2;
        } else if (config == Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

   /* *//**
     * A simple non-UI Fragment that stores a single Object and is retained over configuration
     * changes. It will be used to retain the BitmapCache object.
     *//*
    public static class RetainFragment extends Fragment {
        private Object mObject;

        *//**
     * Empty constructor as per the Fragment documentation
     *//*
        public RetainFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure this Fragment is retained over a configuration change
            setRetainInstance(true);
        }

        *//**
     * Store a single object in this Fragment.
     *
     * @param object The object to store
     *//*
        public void setObject(Object object) {
            mObject = object;
        }

        *//**
     * Get the stored object.
     *
     * @return The stored object
     *//*
        public Object getObject() {
            return mObject;
        }
    }*/
}