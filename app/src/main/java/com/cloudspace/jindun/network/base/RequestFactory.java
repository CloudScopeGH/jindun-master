package com.cloudspace.jindun.network.base;

import com.android.volley.Request.Method;

import java.lang.reflect.Type;


/**
 * Volley adapter for JSON requests that will be parsed into Java objects by Gson.
 */
public class RequestFactory {

    private static RequestFactory factory;

    public RequestFactory() {

    }

    public static RequestFactory newInstance() {
        if (factory == null) {
            factory = new RequestFactory();
        }
        return factory;
    }

    public <K> GsonRequest create(String url) {//删除操作不在意结果的
        return new GsonRequest(url, null, null);
    }

    public <K> GsonRequest create(String url, ApiCallback<K> callback) {
        return new GsonRequest(url, null, callback);
    }

    public <K> GsonRequest create(String url, Type type, ApiCallback<K> callback) {
        return new GsonRequest(url, type, callback);
    }

    public <K> GsonRequest createPost(String url, ApiCallback<K> callback) {
        return new GsonRequest(Method.POST, url, null, callback);
    }

    public <K> GsonRequest createPost(String url, Type type, ApiCallback<K> callback) {
        return new GsonRequest(Method.POST, url, type, callback);
    }

    public <K> GsonRequest createPost(String url, byte[] body, Type type, ApiCallback<K> callback) {
        return new GsonRequest(Method.POST, url, body, type, callback);
    }


    /**
     * 支持删除操作
     *
     * @param method   DELETE_LOCAL_CACHE 删除本地操作,
     *                 GET		 						 get方法
     *                 POST 							 post方法
     * @param url
     * @param type
     * @param callback
     * @return
     */
    public <K> GsonRequest create(int method, String url, Type type, ApiCallback<K> callback) {
        return new GsonRequest(method, url, type, callback);
    }

}