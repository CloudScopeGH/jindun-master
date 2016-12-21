package com.cloudspace.jindun.network.api;

import android.os.Build;

import com.android.volley.GlobalExecutor;
import com.android.volley.error.VolleyError;
import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.network.base.GsonRequest;
import com.cloudspace.jindun.network.base.RequeuePolicy;
import com.cloudspace.jindun.network.url.UrlBuilder;
import com.cloudspace.jindun.utils.AppUtil;
import com.cloudspace.jindun.utils.Device;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class BaseAPI {
    //TODO 公共变量参数等可以在这里设置
    //clientid,deviceid ,address,wifi mode ,number,token ,userid ,location,  version
    // mobile type ,sdk ,sysversion

    public final static long NO_CACHE = -1; // expire = -1
    public final static long PER_SECOND = 1000; // 一秒
    public final static long PER_MINUTE = PER_SECOND * 60; // 一分钟
    public final static long PER_HOUR = PER_MINUTE * 60; // 一小时
    public final static long PER_DAY_EXPIRE = PER_MINUTE * 60 * 24; // 一天 for

    public final static long DEFAULT_EXPIRE_BOARD = PER_SECOND * 10;
    public final static long DEFAULT_EXPIRE = PER_SECOND * 5;
    public long expire = 20 * PER_SECOND; // 默认20s


    public static String HOST = "http://www.cloudscope.cn/jindun";
    public final static String PATH = "/user/stu/";
    public static String TEST_ACTION = "query";
    public static String HOSTS = "https://apis.jindun.com";

    public final static String PATH_HTTPS = "/apis.php";
    public final static String PATH_API = "/api.php";

    public final static String CHANNRL_SRC_KEY = "channelsrc";
    public final static String VERSION_KEY = "version";
    public final static String DEVICEID_KEY = "deviceid";
    public final static String MACADDRESS = "macaddress";
    public final static String BLESS = "bless";
    public final static String MODEL = "model";
    public final static String DEVICE = "device";
    public final static String OS = "systemversion";
    public final static String MANU = "manufacturer";

    public static final String ACCESS_TOKEN_INVALID = "ACCESS_TOKEN_INVALID";//服务器返回错误代码，表示token过期

    protected final ReloginRequeuePolicy reloginRequeuePolicy = new ReloginRequeuePolicy();

    private final class ReloginRequeuePolicy implements RequeuePolicy {

        private ReloginRequeuePolicy() {
        }

        @Override
        public boolean shouldRequeue(final VolleyError error) {
            return ACCESS_TOKEN_INVALID.equalsIgnoreCase(error.getMessage());
        }

        @Override
        public void executeBeforeRequeueing(final GsonRequest request/* ,final ApiCallback callback*/) {
            GlobalExecutor.postUI(new Runnable() {
                @Override
                public void run() {
                   //// TODO: 16/12/11 invalid token
                }
            });
        }
    }


    protected String getShostUrlBuilder(String action) {
        return UrlBuilder.create(HOST, PATH_HTTPS, action);
    }

    protected String getUrlBuilder(String action) {
        return UrlBuilder.create(HOST, PATH, action);
    }

    protected String getHttpsUrlBuilder(String action) {
        return UrlBuilder.create(HOSTS, PATH_HTTPS, action);
    }

    protected String getAPIUrlBuilder(String action) {
        return UrlBuilder.create(HOST, PATH_API, action);
    }

    protected String getRoomAPIUrlBuilder(String action) {
        return UrlBuilder.create(HOST, PATH, action);
    }

    protected String getStatsAPIUrlBuilder(String action) {
        return UrlBuilder.create(HOST, PATH_API, action);
    }

    public static Map<String, String> getDefaultParamsMap() {
        Map<String, String> map = getUserParamsMap();
        map.putAll(getDeviceParamsMap());
        return map;
    }

    public static Map<String, String> getDeviceParamsMap() {
        Map<String, String> map = new HashMap<String, String>();

        map.put(VERSION_KEY, AppUtil.getAppVersionName());
        map.put(MACADDRESS, AppUtil.getMacAddress());
        map.put(CHANNRL_SRC_KEY, AppUtil.getChannelSource(UCAPIApp.getInstance()));
        map.put(DEVICEID_KEY, Device.DEVICE_ID);
        map.put(BLESS, "1");
        map.put(DEVICE, Build.BRAND);
        map.put(OS, Build.VERSION.RELEASE + "");
        map.put(MODEL, Build.MODEL);
        map.put(MANU, Build.MANUFACTURER);

        return map;
    }

    public static Map<String, String> getUserParamsMap() {
        Map<String, String> map = new HashMap<String, String>();
        //// TODO: 16/12/11 userid
        return map;
    }

    public static String getBaseParams() {
        return mapToString(getDefaultParamsMap());
    }

    public static String mapToString(Map<String, String> params) {
        StringBuilder paramsStr = new StringBuilder();
        if (params == null) {
            return paramsStr.toString();
        }
        for (Map.Entry<String, String> e : params.entrySet()) {
            try {
                paramsStr.append(e.getKey())
                        .append("=")
                        .append(URLEncoder
                                .encode((e.getValue() == null ? "" : e.getValue()), "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
        return paramsStr.toString();
    }
}

