/**
 * Copyright (C) 2013 The Android Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.volley.toolbox;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.ArrayMap;
import android.widget.ImageView;

import com.android.ui.drawable.RecyclingBitmapDrawable;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.cache.ImageCache;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.Utils;
import com.android.volley.request.ImageRequest;
import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.ext.ninepatch.NinePatchChunk;
import com.cloudspace.jindun.log.APPLog;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper that handles loading and caching images from remote URLs.
 *
 * The simple way to use this class is to call {@link com.android.volley.toolbox.ImageLoader#(String, ImageListener)}
 * and to pass in the default image listener provided by
 * {@link com.android.volley.toolbox.ImageLoader#getImageListener(android.widget.ImageView, int, int)}. Note that all function calls to
 * this class must be made from the main thead, and all responses will be delivered to the main
 * thread as well.
 */
public class ImageLoader {
    /** RequestQueue for dispatching ImageRequests onto. */
    private final RequestQueue mRequestQueue;

    /** Amount of time to wait after first response arrives before delivering all responses. */
    private int mBatchResponseDelayMs = 0;

    /** The cache implementation to be used as an L1 cache before calling into volley. */
    private final ImageCache mCache;

    /**
     * HashMap of Cache keys -> BatchedImageRequest used to track in-flight requests so
     * that we can coalesce multiple requests to the same URL into a single network request.
     */
    private final ConcurrentHashMap<String, BatchedImageRequest> mInFlightRequests =
            new ConcurrentHashMap<String, BatchedImageRequest>();

    /** HashMap of the currently pending responses (waiting to be delivered). */
//    private final ConcurrentHashMap<String, BatchedImageRequest> mBatchedResponses = new ConcurrentHashMap<String, BatchedImageRequest>();

    /** Handler to the main thread. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /** Runnable for in-flight response delivery. */
    private Runnable mRunnable;

    /** {@link android.content.res.Resources} instance for loading resource uris 
     */
    private Resources mResources;
    private ArrayMap<String, String> mHeaders;

    /**
     * Constructs a new ImageLoader with a default LruCache
     * implementation
     * @param queue The RequestQueue to use for making image requests.

    public ImageLoader(RequestQueue queue) {
    this(queue, MemLruImageCache.getInstance());
    } */

    /**
     * Constructs a new ImageLoader.
     * @param queue The RequestQueue to use for making image requests.
     * @param imageCache The cache to use as an L1 cache.
     * @param resources The Resources to use for loading resource uris
     */
    public ImageLoader(RequestQueue queue, ImageCache imageCache, Resources resources) {
        mRequestQueue = queue;
        mCache = imageCache;
        mResources = resources;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    protected ImageCache getImageCache() {
        return mCache;
    }

    protected Cache getCache() {
        return mRequestQueue.getCache();
    }

    /**
     * The default implementation of ImageListener which handles basic functionality
     * of showing a default image until the network response is received, at which point
     * it will switch to either the actual image or the error image.
     * @param view The imageView that the listener is associated with.
     * @param defaultImageResId Default image resource ID to use, or 0 if it doesn't exist.
     * @param errorImageResId Error image resource ID to use, or 0 if it doesn't exist.
     */
    public static ImageListener getImageListener(final ImageView view,
                                                 final int defaultImageResId, final int errorImageResId) {
        return new ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                }
            }

            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                if (response.getBitmapDrawable() != null) {
                    view.setImageDrawable(response.getBitmapDrawable());
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }
        };
    }

    /**
     * Interface for the response handlers on image requests.
     *
     * The call flow is this:
     * 1. Upon being  attached to a request, onResponse(response, true) will
     * be invoked to reflect any cached data that was already available. If the
     * data was available, response.getBitmapDrawable() will be non-null.
     *
     * 2. After a network response returns, only one of the following cases will happen:
     *   - onResponse(response, false) will be called if the image was loaded.
     *   or
     *   - onErrorResponse will be called if there was an error loading the image.
     */
    public interface ImageListener extends ErrorListener {
        /**
         * Listens for non-error changes to the loading of the image request.
         *
         * @param response Holds all information pertaining to the request, as well
         * as the bitmap (if it is loaded).
         * @param isImmediate True if this was called during ImageLoader.get() variants.
         * This can be used to differentiate between a cached image loading and a network
         * image loading in order to, for example, run an animation to fade in network loaded
         * images.
         */
        public void onResponse(ImageContainer response, boolean isImmediate);
    }

    /**
     * Checks if the item is available in the cache.
     * @param requestUrl The url of the remote image
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @return True if the item exists in cache, false otherwise.
     */
    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        throwIfNotOnMainThread();

        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);
        return mCache.getBitmap(cacheKey) != null;
    }

    /**
     * Returns an ImageContainer for the requested URL.
     *
     * The ImageContainer will contain either the specified default bitmap or the loaded bitmap.
     * If the default was returned, the {@link com.android.volley.toolbox.ImageLoader} will be invoked when the
     * request is fulfilled.
     *
     * @param requestUrl The URL of the image to be loaded.
     * param defaultImage Optional default image to return until the actual image is loaded.
     */
//    public ImageContainer get(String requestUrl, final ImageListener listener) {
//        return get(requestUrl, listener, 0, 0);
//    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     * @param requestUrl The url of the remote image
     * @param imageListener The listener to call when the remote image is loaded
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @return A container object that contains all of the properties of the request, as well as
     *     the currently available image (default if remote is not loaded).
     */

    public ImageContainer getCache(String requestUrl, ImageListener imageListener,
                                   int maxWidth, int maxHeight, boolean needStates) {
        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        // Try to look up the request in the cache of remote images.
        BitmapDrawable cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true);
            return container;
        }
        ImageContainer container = new ImageContainer(null, requestUrl, cacheKey, imageListener);
        imageListener.onResponse(container, true);
        return container;
    }

    public ImageContainer get(String requestUrl, ImageListener imageListener,
                              int maxWidth, int maxHeight) {
        return get(requestUrl, imageListener, maxWidth, maxHeight, true);
    }

    public ImageContainer get(String requestUrl, final ImageListener imageListener,
                              int maxWidth, int maxHeight, boolean needStates) {
        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();
        if (requestUrl == null) {
            imageListener.onErrorResponse(new VolleyError());
        }
        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        // Try to look up the request in the cache of remote images.
        BitmapDrawable cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null && !cachedBitmap.getBitmap().isRecycled()) {
            // Return the cached bitmap.
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            // .9图片处理
            if (NinePatchChunk.isRawNinePatchBitmap(cachedBitmap.getBitmap())) {
//                new Thread(new HandleNinePatchTask(container,imageListener,cachedBitmap.getBitmap())){}.start();
                container.ninePatchDrawable = NinePatchChunk.create9PatchDrawable(UCAPIApp.getInstance(), cachedBitmap.getBitmap(), null);
            }
//            else {
//                imageListener.onResponse(container, true);
//            }
            imageListener.onResponse(container, true);
            return container;
        }

        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer =
                new ImageContainer(null, requestUrl, cacheKey, imageListener);

        // Update the caller to let them know that they should use the default bitmap.
        imageListener.onResponse(imageContainer, true);

        // Check to see if a request is already in-flight.
        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(imageContainer);
            return imageContainer;
        }

        // The request is not already in flight. Send the new request to the network and
        // track it.
        Request<?> newRequest =
                new ImageRequest(requestUrl, mResources, new Listener<BitmapDrawable>() {
                    @Override
                    public void onResponse(BitmapDrawable response) {
                        onGetImageSuccess(cacheKey, response);
                    }
                }, maxWidth, maxHeight,
                        Config.ARGB_8888, needStates, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onGetImageError(cacheKey, error);
                    }
                });

        newRequest.setHeaders(mHeaders);
        mRequestQueue.add(newRequest);
        mInFlightRequests.put(cacheKey,
                new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }

    private class HandleNinePatchTask implements Runnable {
        private ImageContainer container;
        private ImageListener imageListener;
        private Bitmap bitmap;

        public HandleNinePatchTask(ImageContainer container, ImageListener imageListener, Bitmap bitmap) {
            this.container = container;
            this.imageListener = imageListener;
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            container.ninePatchDrawable = NinePatchChunk.create9PatchDrawable(UCAPIApp.getInstance(), bitmap, null);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    imageListener.onResponse(container, true);
                }
            });
        }
    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     * @param requestUrl The url of the remote image
     * @param imageListener The listener to call when the remote image is loaded
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @return A container object that contains all of the properties of the request, as well as
     *     the currently available image (default if remote is not loaded).
     */
    public ImageContainer set(String requestUrl, ImageListener imageListener,
                              int maxWidth, int maxHeight, Bitmap bitmap) {
        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        BitmapDrawable drawable;
        if (Utils.hasHoneycomb()) {
            // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
            drawable = new BitmapDrawable(mResources, bitmap);
        } else {
            // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
            // which will recycle automagically
            drawable = new RecyclingBitmapDrawable(mResources, bitmap);
        }

        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer =
                new ImageContainer(drawable, requestUrl, cacheKey, imageListener);

        // Update the caller to let them know that they should use the default bitmap.
        imageListener.onResponse(imageContainer, true);
        //setImageSuccess(cacheKey, bitmap);

        // cache the image that was fetched.
        mCache.putBitmap(cacheKey, drawable);

        Response<?> response = Response.success(bitmap, HttpHeaderParser.parseBitmapCacheHeaders(bitmap));
        getCache().put(requestUrl, response.getCacheEntry());

/*        Response<?> response = Response.success(bitmap, HttpHeaderParser.parseBitmapCacheHeaders(bitmap));
        Entry cache = getCache().get(requestUrl);
        cache.data = response.cacheEntry.data;
        getCache().put(requestUrl, cache);*/

        return imageContainer;
    }

    /**
     * Sets the amount of time to wait after the first response arrives before delivering all
     * responses. Batching can be disabled entirely by passing in 0.
     * @param newBatchedResponseDelayMs The time in milliseconds to wait.
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    /**
     * Handler for when an image was successfully loaded.
     * @param cacheKey The cache key that is associated with the image request.
     * @param response The bitmap that was returned from the network.
     */
    private void onGetImageSuccess(String cacheKey, BitmapDrawable response) {
        // cache the image that was fetched.
        mCache.putBitmap(cacheKey, response);

        // remove the request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Update the response bitmap.
            request.mResponseBitmap = response;
            //网络请求图片.9图片处理
            if (NinePatchChunk.isRawNinePatchBitmap(response.getBitmap())) {
                request.ninePatchDrawable = NinePatchChunk.create9PatchDrawable(UCAPIApp.getInstance(), response.getBitmap(), null);
            }

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Handler for when an image was successfully loaded.
     * @param cacheKey The cache key that is associated with the image request.
     * @param response The bitmap that was returned from the network.
     */
    @SuppressWarnings("unused")
    private void setImageSuccess(String cacheKey, BitmapDrawable response) {
        // cache the image that was fetched.
        mCache.putBitmap(cacheKey, response);

        // remove the request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Update the response bitmap.
            request.mResponseBitmap = response;

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Handler for when an image failed to load.
     * @param cacheKey The cache key that is associated with the image request.
     */
    private void onGetImageError(String cacheKey, VolleyError error) {
        // Notify the requesters that something failed via a null result.
        // Remove this request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Set the error for this request
            request.setError(error);

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Container object for all of the data surrounding an image request.
     */
    public class ImageContainer {
        /**
         * The most relevant bitmap for the container. If the image was in cache, the
         * Holder to use for the final bitmap (the one that pairs to the requested URL).
         */
        private BitmapDrawable mBitmap;

        private NinePatchDrawable ninePatchDrawable;

        private final ImageListener mListener;

        /** The cache key that was associated with the request */
        private final String mCacheKey;

        /** The request URL that was specified */
        private final String mRequestUrl;

        /**
         * Constructs a BitmapContainer object.
         * @param bitmap The final bitmap (if it exists).
         * @param requestUrl The requested URL for this container.
         * @param cacheKey The cache key that identifies the requested URL for this container.
         */
        public ImageContainer(BitmapDrawable bitmap, String requestUrl,
                              String cacheKey, ImageListener listener) {
            mBitmap = bitmap;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }

        /**
         * Releases interest in the in-flight request (and cancels it if no one else is listening).
         */
        public void cancelRequest() {
            if (mListener == null) {
                return;
            }

            BatchedImageRequest request = mInFlightRequests.get(mCacheKey);
            if (request != null) {
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if (canceled) {
                    mInFlightRequests.remove(mCacheKey);
                }
            } else {
                // check to see if it is already batched for delivery.
//                request = mBatchedResponses.get(mCacheKey);
//                if (request != null) {
//                    request.removeContainerAndCancelIfNecessary(this);
//                    if (request.mContainers.size() == 0) {
//                        mBatchedResponses.remove(mCacheKey);
//                    }
//                }
            }
        }

        /**
         * Returns the bitmap associated with the request URL if it has been loaded, null otherwise.
         */
        public BitmapDrawable getBitmapDrawable() {
            return mBitmap;
        }

        /**
         * Returns the requested URL for this container.
         */
        public String getRequestUrl() {
            return mRequestUrl;
        }

        public NinePatchDrawable getNinePatchDrawable() {
            return ninePatchDrawable;
        }
    }

    /**
     * Wrapper class used to map a Request to the set of active ImageContainer objects that are
     * interested in its results.
     */
    private class BatchedImageRequest {
        /** The request being tracked */
        private final Request<?> mRequest;

        /** The result of the request being tracked by this item */
        private BitmapDrawable mResponseBitmap;

        private NinePatchDrawable ninePatchDrawable;

        /** Error if one occurred for this response */
        private VolleyError mError;

        /** List of all of the active ImageContainers that are interested in the request */
        private final LinkedList<ImageContainer> mContainers = new LinkedList<ImageContainer>();

        /**
         * Constructs a new BatchedImageRequest object
         * @param request The request being tracked
         * @param container The ImageContainer of the person who initiated the request.
         */
        public BatchedImageRequest(Request<?> request, ImageContainer container) {
            mRequest = request;
            mContainers.add(container);
        }

        /**
         * Set the error for this response
         */
        public void setError(VolleyError error) {
            mError = error;
        }

        /**
         * Get the error for this response
         */
        public VolleyError getError() {
            return mError;
        }

        /**
         * Adds another ImageContainer to the list of those interested in the results of
         * the request.
         */
        public void addContainer(ImageContainer container) {
            mContainers.add(container);
        }

        /**
         * Detatches the bitmap container from the request and cancels the request if no one is
         * left listening.
         * @param container The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mRequest.cancel();
                return true;
            }
            return false;
        }
    }

    private byte[] mBatchedResponsesLock = new byte[0];

    /**
     * Starts the runnable for batched delivery of responses if it is not already started.
     * @param cacheKey The cacheKey of the response being delivered.
     * @param request The BatchedImageRequest to be delivered.
     */
    private void batchResponse(String cacheKey, BatchedImageRequest request) {
//        synchronized (mBatchedResponsesLock) {
//            mBatchedResponses.put(cacheKey, request);
//        }
        // If we don't already have a batch delivery runnable in flight, make a new one.
        // Note that this will be used to deliver responses to all callers in mBatchedResponses.
        mHandler.postDelayed(new BatchedResponsesRunnable(this, request), mBatchResponseDelayMs);
    }

    private static final class BatchedResponsesRunnable implements Runnable {

        private WeakReference<ImageLoader> mContext;
        private BatchedImageRequest request;

        public BatchedResponsesRunnable(ImageLoader loader, BatchedImageRequest request) {
            mContext = new WeakReference<ImageLoader>(loader);
            this.request = request;
        }

        @Override
        public void run() {
//            ImageLoader loader = mContext.get();
//            if (loader == null) {
//                return;
//            }
//            synchronized (loader.mBatchedResponsesLock) {
//                for (BatchedImageRequest bir : loader.mBatchedResponses.values()) {
            for (ImageContainer container : request.mContainers) {
                // If one of the callers in the batched request canceled the request
                // after the response was received but before it was delivered,
                // skip them.
                if (container.mListener == null) {
                    continue;
                }
                if (request.getError() == null) {
                    container.mBitmap = request.mResponseBitmap;
                    container.ninePatchDrawable = request.ninePatchDrawable;
                    container.mListener.onResponse(container, false);
                } else {
                    container.mListener.onErrorResponse(request.getError());
                }
//                    }
//                }
//                loader.mBatchedResponses.clear();
            }
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }

    /**
     * Creates a cache key for use with the L1 cache.
     * @param url The URL of the request.
     * @param maxWidth The max-width of the output.
     * @param maxHeight The max-height of the output.
     */
    public static String getCacheKey(String url, int maxWidth, int maxHeight) {
        int length = 0;
        if (url != null) {
            length = url.length();//防止null异常导致报错 ;
        } else {
            APPLog.v("ImageLoader url null .getCacheKey error");
        }
        return new StringBuilder(length + 12).append("#W").append(maxWidth)
                .append("#H").append(maxHeight).append(url).toString();
    }

    /**
     * Set a {@link android.content.res.Resources} instance if you need to support resource uris for loading images
     * @param  {@link android.content.res.Resources} instance for loading images. Get from {@link android.content.Context#getResources()}

    public void setResources(Resources resources) {
    mResources = resources;
    }

    public Resources getResources() {
    return mResources;
    }
     */
    public void setHeaders(ArrayMap<String, String> headers) {
        mHeaders = headers;
    }
}
