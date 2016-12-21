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

package com.android.volley.error;

import com.android.volley.NetworkResponse;

/**
 * Indicates that the server's response could not be parsed.
 */
@SuppressWarnings("serial")
public class ActionError extends VolleyError {
    public String url = null;

    public ActionError() {
    }

    public ActionError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public ActionError(String message, String sourceUrl) {
        super(message);
        this.url = sourceUrl;
        System.err.println(url);
    }

    @Override
    public void init() {
        errorCode = ErrorCode.ACTION_ERROR;
    }
}
