package com.cloudspace.jindun.network.url;

public class JindunUrlRewriter extends ProxyUrlRewriter {
    public static String getRewriteUrl(String originalUrl) {
        return ProxyUrlRewriter.redirectProxyUrl(originalUrl);
    }
}