package com.cloudspace.jindun.network.url;

import com.android.volley.toolbox.http.HttpStack;

public class ProxyUrlRewriter implements HttpStack.UrlRewriter {
    @Override
    public String rewriteUrl(String originalUrl) {
        return redirectProxyUrl(originalUrl);
    }

    // TODO 默认该接口中访问的网络状态时 实时正确的
    public static String redirectProxyUrl(String url) {
        return url;
    }
}