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
import com.cloudspace.jindun.utils.ToastMaker;

/**
 * Exception style class encapsulating Volley errors
 */
@SuppressWarnings("serial")
public class VolleyError extends Exception {
    public interface ErrorCode {
        public static final int GENERIC_ERROR = 0;
        public static final int NETWORK_ERROR = -1;
        public static final int SERVER_ERROR = -2;
        public static final int TIMEOUT_ERROR = -3;
        public static final int NO_CONNECTION_ERROR = -4;
        public static final int PARSE_ERROR = -5;
        public static final int BAD_REQUEST_ERROR = -6;
        public static final int AUTH_FAILURE_ERROR = -7;
        public static final int ACTION_ERROR = -8;

    }

    public final NetworkResponse networkResponse;
    /**
     * Error code to identify the error type
     */
    public int errorCode;

    /**
     * 出错时获取到的服务器返回的字符串
     */
    public String responseString;


    public VolleyError() {
        init();
        networkResponse = null;
    }

    public VolleyError(NetworkResponse response) {
        init();
        networkResponse = response;
    }

    public VolleyError(String exceptionMessage) {
        super(exceptionMessage);
        init();
        networkResponse = null;
    }

    public VolleyError(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        init();
        networkResponse = null;
    }

    public VolleyError(Throwable cause) {
        super(cause);
        init();
        networkResponse = null;
    }

    public void init() {
        errorCode = ErrorCode.GENERIC_ERROR;
    }

    /**
     * 可以直接调用的显示错误信息的代码
     * 同
     */
    public void toastError() {
        ToastMaker.showToastShort(VolleyErrorHelper.getErrorActionMessage(this));
    }
}
