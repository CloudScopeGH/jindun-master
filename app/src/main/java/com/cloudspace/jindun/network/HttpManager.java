package com.cloudspace.jindun.network;

import android.os.Looper;

import com.android.volley.Cache;
import com.android.volley.GlobalExecutor;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.cache.DiskBasedCache;
import com.android.volley.misc.Utils;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.http.AbstractHttpStack;
import com.android.volley.toolbox.http.HttpClientStack;
import com.android.volley.toolbox.http.HurlStack;
import com.cloudspace.jindun.BuildConfig;
import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.config.Configs;
import com.cloudspace.jindun.network.url.JindunUrlRewriter;
import com.cloudspace.jindun.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HttpManager {
    /**
     * Default on-disk cache directory for http api request
     */
    private static final String DEFAULT_API_CACHE_DIR = "volley";

    public static RequestQueue sRequestQueue;

    public static synchronized RequestQueue getRequestQueue() {
        if (sRequestQueue == null) {
            sRequestQueue = newHttpRequestQueue();
        }
        return sRequestQueue;
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newHttpRequestQueue() {
        AbstractHttpStack stack = Utils.hasHoneycomb() ? new HurlStack() : new HttpClientStack();

        stack.setUrlRewriter(new JindunUrlRewriter());

        Network network = new BasicNetwork(stack);

        Cache cache = new DiskBasedCache(Utils.getDiskCacheDir(UCAPIApp.getInstance(), DEFAULT_API_CACHE_DIR));

        RequestQueue queue = new RequestQueue(cache, network);
        queue.start();
        return queue;
    }

    public static void addRequest(Request<?> request, Object tag) {
        if (tag == null && BuildConfig.DEBUG) {
            throw new RuntimeException("addRequest -> tag is null");
        }

        request.setTag(tag != null ? tag.toString() : "");

//        setBasicParamHeader(request);

        getRequestQueue().add(request);
    }

    private static void setBasicParamHeader(Request<?> request) {
        long timestamp = System.currentTimeMillis(); //时间戳
        Random random = new Random(); // 获取随机数。
        long nonce = random.nextLong();

        String appKey = Configs.APPKEY; // 开发者平台分配的 App Secret。

        //系统分配的 App Secret、Nonce (随机数)、Timestamp (时间戳)三个字符串按先后顺序拼接成一个字符串并进行 SHA1 哈希计算
        String signature = StringUtil.getSha1(appKey+nonce+timestamp);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("App-Key", appKey);
        headers.put("Nonce", String.valueOf(nonce));
        headers.put("Timestamp", String.valueOf(timestamp));
        headers.put("Signature", signature);

        request.setHeaders(headers);
    }

    public static void cancelAllRequests(final Object tag) {
        if (tag == null) {
            throw new RuntimeException("cancelAllRequests -> tag is null");
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("cancelAllRequest should run in UI Thread.");
        }
        String tempTag = tag.toString();
        getRequestQueue().cancelAll(tempTag);
        GlobalExecutor.cancel(tempTag);
    }

    public static void cancelAllRequests(RequestQueue.RequestFilter filter) {
        getRequestQueue().cancelAll(filter);
    }
}
