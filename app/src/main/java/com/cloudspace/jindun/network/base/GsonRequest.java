package com.cloudspace.jindun.network.base;

import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.GlobalExecutor;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.ActionError;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.ParseError;
import com.android.volley.error.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.network.NetworkMonitor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Volley adapter for JSON requests that will be parsed into Java objects by Gson.
 */
public class GsonRequest extends Request<String> {
    public static final String OK_MSG = "ok";

    private final Type type;
    private ApiCallback listener;
    private final byte[] mRequestBody;

    private RequeuePolicy requeuePolicy;
    private long mTTL;

    private long mSoftTTL;

    public GsonRequest(int method, String url, byte[] requestBody, Type type, final ApiCallback callback) {
        super(method, url, callback);
        this.type = type;
        this.listener = callback;
        this.mRequestBody = requestBody;
    }

    /**
     * Make a request and return a parsed object from JSON.
     *
     * @param url    URL of the request to make
     * @param method Relevant class object, for Gson's reflection
     * @param type   Map of request headers
     */
    public GsonRequest(int method, String url, Type type, final ApiCallback callback) {
        super(method, url, callback);
        this.type = type;
        this.listener = callback;
        this.mRequestBody = null;
    }

    public GsonRequest(String url, Type type, final ApiCallback callback) {
        this(Method.GET, url, type, callback);
    }

    public Type getType() {
        return type;
    }

    @Override
    public Request<?> setTag(Object tag) {
        if (listener != null) {
            listener.setTag(tag);
        }
        return super.setTag(tag);
    }

    public GsonRequest setRequeuePolicy(RequeuePolicy requeuePolicy) {
        this.requeuePolicy = requeuePolicy;
        return this;
    }

    public GsonRequest setParams(Map<String, String> params) {
        super.setParams(params);
        return this;
    }

    public GsonRequest setParams(String key, String value) {
        super.setParams(key, value);
        return this;
    }


    public GsonRequest setParams(String key, Object value) {
        super.setParams(key, String.valueOf(value));
        return this;
    }


    public GsonRequest setParamsIgnoreNone(String key, String value) {
        if (value != null) {
            super.setParams(key, value);
        }
        return this;
    }


    public GsonRequest addFile(String name, File file) {
        super.addFile(name, file);
        return this;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mRequestBody == null ? super.getBody() : mRequestBody;
    }

    /**
     * 将用来解析api返回的数据结果成需要的Model
     * <p/>
     * 将运行在ExecutorDelivery中,表示请求回来了正确的结果
     * 该过程根据request请求时设置的uiResponse 参数决定是否在UI线程中执行。
     * 和网络请求和缓存查询不是在同一个线程
     * <p/>
     * 其中包括错误状态。result将为null,error将会有数据
     */
    @Override
    protected void deliverResponse(Response<String> response) {
        try {
            JsonObject json = getObject(response.getResult());
            checkErrorCode(json);
            Object result = handleResult(json, type);
            // 聊天气泡返回数据兼容
            if (result == null) {
                result = UCAPIApp.getGson().fromJson(json.getAsJsonObject(), type);
            }
            if (null != listener) {
                listener.setRequestState(isCanceled());
                boolean isUiResponse = listener.isUiResponse();
                GlobalExecutor.execute(new SuccessRunnable(listener, this, result, getParams()), getTag(), isUiResponse);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            if (e instanceof ActionError) {
                ((VolleyError) e).responseString = response.getResult();
                deliverError((ActionError) e);
            } else {
                if (e instanceof JsonSyntaxException || e instanceof NullPointerException) {
                }
                ParseError pError = new ParseError(e);
                pError.responseString = response.getResult();
                deliverError(pError);
            }
//			System.err.println("error " + new JindunUrlRewriter().rewriteUrl(getUrl()));
            //数据解析有问题，服务器给的数据有误，缓存失效
            getRequestQueue().getCache().remove(getCacheKey());
            Log.e("deliverResponse  error", response == null ? "null----" : response.getResult() + "---url:" + getUrl());
        } finally {
            response.result(null);
            response.cacheEntry(null);
            response = null;
        }
    }

    class SuccessRunnable implements Runnable {
        final ApiCallback listener;
        final Request request;
        final Object result;
        final Map<String, String> maps;

        public SuccessRunnable(ApiCallback listener, Request request, Object result, Map<String, String> maps) {
            this.listener = listener;
            this.request = request;
            this.result = result;
            this.maps = maps;
        }

        @Override
        public void run() {
            if (null != listener && !request.isCanceled()) {
                listener.onSuccess(result, getParams());
            }
        }
    }

    class ErrorRunnable implements Runnable {
        final ApiCallback listener;
        final VolleyError error;
        final Request request;

        public ErrorRunnable(ApiCallback listener, Request request, VolleyError error) {
            this.listener = listener;
            this.error = error;
            this.request = request;
        }

        @Override
        public void run() {
            if (null != listener && !request.isCanceled()) {
                listener.onErrorResponse(error);
            }
        }
    }

    @Override
    public void deliverError(final VolleyError error) {
        if (error != null && requeuePolicy != null && requeuePolicy.shouldRequeue(error)) {
            requeuePolicy.executeBeforeRequeueing(this);
            return;
        }
        if (listener != null) {
            listener.setRequestState(isCanceled());
            boolean isUiResponse = listener.isUiResponse();
            GlobalExecutor.execute(new ErrorRunnable(listener, this, error), getTag(), isUiResponse);
        }
        //持久化纪录日志，方便分析原因
//        if(error!=null) {
//            KTVUIUtility.appendDebugLog(error.getMessage() + "");
//        }
    }


    public JsonObject getObject(String content) throws JsonSyntaxException {
        return new JsonParser().parse(content).getAsJsonObject();
    }

    /**
     * 处理错误信息
     * return errorcode
     **/
    public String checkErrorCode(JsonObject json) throws JsonSyntaxException, ActionError {
        JsonElement errorJson = json.get("errorcode");
        // 聊天气泡返回数据处理
        if (errorJson == null) return null;
        String errorcode = errorJson.getAsString();
        if (!OK_MSG.equalsIgnoreCase(errorcode)) {//处理服务器返回的错误信息
            throw new ActionError(errorcode, getUrl());
        }
        return errorcode;
    }

    public Object handleResult(JsonObject json, Type type) throws JsonSyntaxException {
        if (type == null) {
            return json;
        } else {
            return new Gson().fromJson(json.get("result"), type);
        }
    }

    /**
     * 将运行在cacheDispatcher 和NetworkDispartcher 中
     * 用来解析缓存数据和解析结果
     * <p/>
     * 该过程执行时间影响Queue的效率
     * 如果抛出异常，将不会将结果传递到deliverResponse 而是deliverError
     */
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);
            entry.ttl = mTTL;
            entry.softTtl = mSoftTTL;
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(json, entry);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (OutOfMemoryError error) {
            return Response.error(new ParseError(error));
        }
    }

    /**
     * api缓存策略使用说明 1.客户端典型使用场景如下 1.1 考虑到访问本地数据比网络数据快很多，对于没有loadmore分页的接口
     * ----采用，先加载本地sdcard缓存数据，再获取网络最新数据。
     * <p/>
     * 1.2 对于create,delete,update,cancel等提交数据的GET或POST操作 ----采用，不需要缓存
     * <p/>
     * 1.3 对于有loadmore的地方，尽量避免使用策略1.1,防止一次请求，导致两次数据加载使得数据重复渲染。 ----采用，请求不走两次回调。如果本地有并且未过期就只是用本地的，不管网络的。如果本地没有或是已过期就从网络取
     * <p/>
     * 1.4 对于图片等资源型，宜采用永不过期策略，然后另启策略删除
     * <p/>
     * 1.5 对于没有网络的情况，我们需要针对1.1-1.4的策略忽略网络操作，将强制一些策略失效。 1.1-无网络环境下，即便缓存过期，也获取缓存数据
     * 1.2-无网络，无缓存，api提示网络错误 1.3-无网络，即便缓存过期，也获取缓存数据 1.4-无网络，无影响
     * <p/>
     * 1.6 数据异常的缓存处理 对于图片内容大小为0的图片数据，放弃缓存或设置为无效，防止从缓存中加载后显示空白图片 对于api返回结果errorcode不为ok的应该将缓存状态设置为无效
     * 对于api返回结果errorcode为ok,但是导致JSON解析出错的,应该将缓存状态置为无效
     * <p/>
     * 实现方案 mTTL 		表示 本地缓存的过期时间，如果mTTL过期,将需要从网络获取新数据，(不管本地有没有缓存数据) mSoftTTL		表示
     * 本地缓存距当前时间是否超过mSoftTTL，需要刷新数据。 走到mSoftTTL的判断，就一定表明，他本地是有缓存的。 而mSoftTTL就是用来到底是直接用本地缓存(mSoftTTL未过期)，还是用完之后还需要刷新网络数据(mSoftTTL过期)
     * mResponseTwice 表示一次请求，只返回一次。如果本地缓存有则返回本地缓存，如果本地没有则返回网络
     * <p/>
     * 1.1 setExpiredTime(10*1000).setSoftTTLTime(3000); 表示该接口缓存时间为10秒， 如果超过10秒之后，则直接请求网络数据
     * 如果超过3秒之后，再次访问此接口，则先返回本地缓存，同时再去请求网络数据，网络再返回一次 如果在3秒之内，再次访问此接口，则只返回 本地缓存。
     * <p/>
     * 1.2 setNoCache()，不设置缓存，永远走网络
     * <p/>
     * 1.3 setExpiredTime(10*1000).setSoftTTLTime(3000).neverResponseTwice(); 表示该接口缓存时间为10秒，
     * 如果超过10秒之后，则直接请求网络数据 如果超过3秒之后，再次访问此接口，则先返回本地缓存，（由于设置了neverResponseTwice,将不会再请求网络数据）
     * 如果在3秒之内，再次访问此接口，则只返回 本地缓存。 总之，只要本地返回了数据，将不会再请求网络了
     * <p/>
     * 1.4 setNotExpired(); 表示 接口永远不过期，只要本地有缓存，将不会再从网络上获取。 图片再HttpHeaderParser.parseIgnoreCacheHeaders(response)中将softTtl
     * ttl 设置为0，表示不过期
     * <p/>
     * TODO 1.5 ， 1.6 待完成
     */


    public GsonRequest setTTLTime(long ttl) {
        mTTL = System.currentTimeMillis() + ttl;
        return this;
    }

    public GsonRequest setSoftTTLTime(long time) {
        mSoftTTL = System.currentTimeMillis() + time;
        return this;
    }

    public GsonRequest setForceRefresh(boolean forceRefresh) {
        super.setForceRefresh(forceRefresh);
        return this;
    }

    public GsonRequest setNoCache() {
        mTTL = -1;
        if (NetworkMonitor.isNetworkAvailable()) {
            mTTL = 0;
        }
        mSoftTTL = -1;
        setShouldCache(false);
        return this;
    }

    public GsonRequest setSmartCache() {
        if (getParams().containsKey("start")) {
            if (!"0".equals(getParams().get("start"))) {
                setNoCache();
            }
        }
        return this;
    }

    public GsonRequest setNotExpired() {
        mTTL = 0;
        return this;
    }

    public GsonRequest neverResponseTwice() {
        mResponseTwice = false;
        return this;
    }

    @Override
    protected void finish(final String tag) {
        super.finish(tag);
        listener = null;
    }


}