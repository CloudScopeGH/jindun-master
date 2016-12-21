/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.volley;

import com.android.volley.error.VolleyError;

/**
 * Encapsulates a parsed response for delivery.
 *
 * @param <T> Parsed type of this response
 */
public class Response<T> {
    public final static int NO_DELIVERED = 0;
    public final static int CACHE = 1;
    public final static int NETWORK = 2;

    /**
     * Callback interface for delivering parsed responses.
     */
    public interface Listener<T> {
        /**
         * Called when a response is received.
         */
        public void onResponse(T response);
    }

    /**
     * Callback interface for delivering error responses.
     */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        public void onErrorResponse(VolleyError error);
    }

    /**
     * Returns a successful response containing the parsed result.
     */
    public static <T> Response<T> success(T result, Cache.Entry cacheEntry) {
        return new Response<T>(result, cacheEntry);
    }

    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    public static <T> Response<T> error(VolleyError error) {
        return new Response<T>(error);
    }

    /**
     * Parsed response, or null in the case of error.
     */
    private T result;

    /**
     * Cache metadata for this response, or null in the case of error.
     */
    private Cache.Entry cacheEntry;

    /**
     * Detailed error information if <code>errorCode != OK</code>.
     */
    private VolleyError error;

    private int source;

    /**
     * True if this response was a soft-expired one and a second one MAY be coming.
     */
    public boolean intermediate = false;

    private Response() {
        source = NO_DELIVERED;
    }

    private Response(T result, Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
    }

    private Response(VolleyError error) {
        this.result = null;
        this.cacheEntry = null;
        this.error = error;
    }

    public Response<T> result(T result) {
        this.result = result;
        return this;
    }

    public Response<T> cacheEntry(Cache.Entry cacheEntry) {
        this.cacheEntry = cacheEntry;
        return this;
    }

    public Response<T> setError(VolleyError error) {
        this.error = error;
        return this;
    }

    public Response<T> source(int source) {
        this.source = source;
        return this;
    }

    public T getResult() {
        return result;
    }

    public Cache.Entry getCacheEntry() {
        return cacheEntry;
    }

    public VolleyError getError() {
        return error;
    }

    public int getSource() {
        return source;
    }

    /**
     * Returns whether this response is considered successful.
     */
    public boolean isSuccess() {
        return error == null;
    }

    public static Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    public static Response.ErrorListener createToastErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                //TODO show tips
//            	ToastMaker.showToastShort(VolleyErrorHelper.getErrorActionMessage(error));
            }
        };
    }
}
