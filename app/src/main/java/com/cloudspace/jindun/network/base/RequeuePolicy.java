/*
* Copyright 2010 - 2014 Monits
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.cloudspace.jindun.network.base;

import com.android.volley.error.VolleyError;


/**
 */
public interface RequeuePolicy {

    /**
     * Define a condition whether this request should retry or not.
     *
     * @param error the given response of the first request.
     * @return true if should retry, false otherwise.
     */
    boolean shouldRequeue(VolleyError error);

    /**
     * In case {@link RequeuePolicy#shouldRequeue(VolleyError)} returns true, this method will be called.
     * Providing listeners which are in charge of requeueing the request in case of success.
     */
    <K> void executeBeforeRequeueing(GsonRequest request /*,ApiCallback<K> callback*/);
}