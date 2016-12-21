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

import android.os.Handler;
import android.os.HandlerThread;

import com.android.volley.error.VolleyError;

import java.util.concurrent.Executor;

/**
 * Delivers responses and errors.
 */
public class ExecutorDelivery implements ResponseDelivery {
    /**
     * Used for posting responses, typically to the main thread.
     * 在Request中提供一个选项 uiResponse，让调用的API自行决定，回调的结果是在主线程还是子线程中执行
     */
    private final Executor mResponsePoster;

    public ExecutorDelivery() {
        final HandlerThread handlerThread = new HandlerThread("Delivery");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }
    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on

    public ExecutorDelivery(final Handler handler) {
    // Make an Executor that just wraps the handler.
    mResponsePoster = new Executor() {
    @Override public void execute(Runnable command) {
    mUiHandler.post(command);
    }
    };
    } */

    /**
     * Creates a new response delivery interface, mockable version
     * for testing.
     * <p/>
     * public ExecutorDelivery(Executor executor) {
     * mResponsePoster = executor;
     * }
     */

    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        postResponse(request, response, null);
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {
//        request.markDelivered();
//        request.addMarker("post-response");// modify by luo 挪置response callback之后
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, runnable));
    }

    @Override
    public void postError(Request<?> request, VolleyError error) {
//        request.addMarker("post-error");
        Response<?> response = Response.error(error);
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, null));
    }

    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;
        private final Runnable mRunnable;

        public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable) {
            mRequest = request;
            mResponse = response;
            mRunnable = runnable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (mRequest.isCanceled()) {
                mRequest.finish("canceled-at-delivery");
                return;
            }

            if (mRequest.mResponseTwice || (!mRequest.mResponseTwice && !mRequest.hasHadResponseDelivered())) {//response两次，或者如果一次的话，看是否已经返回一次了
                // Deliver a normal response or error, depending.
                if (mResponse.getSource() == 1 && mResponse.getCacheEntry() != null) {
//            		mRequest.addMarker("response－getCacheEntry softttl "+ mResponse.getCacheEntry().softTtl + " ttl " + mResponse.getCacheEntry().ttl  + "  Expired " + mResponse.getCacheEntry().isExpired() + "  refresh " + mResponse.getCacheEntry().refreshNeeded());
                }
                mRequest.addMarker("real-content-post-response" + mResponse.getSource());
                if (mResponse.isSuccess()) {
                    mRequest.deliverResponse(mResponse);
                } else {
                    mRequest.deliverError(mResponse.getError());
                }
            }

            mRequest.markDelivered();
            if (mResponse.isSuccess()) {
                mRequest.addMarker("post-response");
            } else {
                mRequest.addMarker("post-error");
            }

            // If this is an intermediate response, add a marker, otherwise we're done
            // and the request can be finished.
            if (mResponse.intermediate) {
                mRequest.addMarker("intermediate-response");
            } else {
                mRequest.finish("done");
            }

            // If we have been provided a post-delivery runnable, run it.
            if (mRunnable != null) {
                mRunnable.run();
            }
        }
    }
}
