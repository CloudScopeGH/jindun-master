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

package com.android.volley.toolbox.http;

import javax.net.ssl.SSLSocketFactory;


/**
 * An HTTP stack abstraction.
 */
public abstract class AbstractHttpStack implements HttpStack {
    protected UrlRewriter mUrlRewriter;
    protected SSLSocketFactory mSslSocketFactory;
    protected String mUserAgent;

    /**
     * @param urlRewriter Rewriter to use for request URLs
     */
    public HttpStack setUrlRewriter(UrlRewriter urlRewriter) {
        this.mUrlRewriter = urlRewriter;
        return this;
    }

    public UrlRewriter getUrlRewriter() {
        return mUrlRewriter;
    }

    /**
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     */
    public HttpStack setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.mSslSocketFactory = sslSocketFactory;
        return this;
    }

    /**
     * @param mUserAgent for custom UserAgent flag
     */
    public HttpStack setUserAgent(String mUserAgent) {
        this.mUserAgent = mUserAgent;
        return this;
    }
}
