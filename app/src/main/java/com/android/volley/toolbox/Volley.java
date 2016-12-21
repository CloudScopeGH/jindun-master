/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.volley.toolbox;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.cache.DiskBasedCache;
import com.android.volley.misc.Utils;
import com.android.volley.toolbox.http.AbstractHttpStack;
import com.android.volley.toolbox.http.HttpClientStack;
import com.android.volley.toolbox.http.HttpStack;
import com.android.volley.toolbox.http.HttpStack.UrlRewriter;
import com.android.volley.toolbox.http.HurlStack;

public class Volley {

    /**
     * Default on-disk cache directory for http api request
     */
    private static final String DEFAULT_API_CACHE_DIR = "volley";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack   An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newHttpRequestQueue(Context context, UrlRewriter urlRewriter) {
        AbstractHttpStack stack = Utils.hasHoneycomb() ?
                new HurlStack() :
                new HttpClientStack();

        stack.setUrlRewriter(urlRewriter);

        Network network = new BasicNetwork(stack);

        Cache cache = new DiskBasedCache(Utils.getDiskCacheDir(context, DEFAULT_API_CACHE_DIR));

        RequestQueue queue = new RequestQueue(cache, network);
        queue.start();

        return queue;
    }


}
