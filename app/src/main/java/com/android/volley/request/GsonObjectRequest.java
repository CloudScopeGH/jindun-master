package com.android.volley.request;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.error.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;

public class GsonObjectRequest extends JsonRequest<JsonObject> {

    private long mTTL;

    private long mSoftTTL = -1;

    public GsonObjectRequest(String url, Listener<JsonObject> listener,
                             Response.ErrorListener errorListener) {
        this(Request.Method.GET, url, null, listener, errorListener);
    }

    public GsonObjectRequest(int method, String url, String requestBody,
                             Listener<JsonObject> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
        this.setShouldCache(true);
    }

    @Override
    protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JsonElement jsonEle = new JsonParser().parse(jsonString);
            JsonObject json = jsonEle.getAsJsonObject();
            Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);
            entry.ttl = mTTL;
            entry.softTtl = mSoftTTL;
            return Response.success(json, entry);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
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

    public GsonObjectRequest setTTLTime(long ttl) {
        mTTL = System.currentTimeMillis() + ttl;
        return this;
    }

    public GsonObjectRequest setSoftTTLTime(long time) {
        mSoftTTL = System.currentTimeMillis() + time;
        return this;
    }

    public GsonObjectRequest setNoCache() {
        mTTL = -1;
        mSoftTTL = -1;
        return this;
    }

    public GsonObjectRequest setNotExpired() {
        mTTL = 0;
        return this;
    }

    public GsonObjectRequest neverResponseTwice() {
        mResponseTwice = false;
        return this;
    }
}