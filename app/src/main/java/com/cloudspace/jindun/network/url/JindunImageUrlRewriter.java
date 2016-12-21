package com.cloudspace.jindun.network.url;


import com.cloudspace.jindun.net.manager.ImageManager;
import com.cloudspace.jindun.network.NetworkMonitor;
import com.cloudspace.jindun.utils.AppUtil;

public class JindunImageUrlRewriter extends ProxyUrlRewriter {
    @Override
    public String rewriteUrl(String originalUrl) {
        return super.rewriteUrl(getHighQualityUrl(originalUrl));
    }

    public String getHighQualityUrl(String requestUrl) {
        boolean needHigherQuality = (NetworkMonitor.isWifi()) && AppUtil.hasGingerbread();
        String url = requestUrl;
        if (needHigherQuality) {
            if (requestUrl.contains(ImageManager.ImageType.TINY.getHolder())) {
                url = requestUrl.replace(ImageManager.ImageType.TINY.getHolder(), ImageManager.ImageType.MEDIUM.getHolder());
            } else if (requestUrl.contains(ImageManager.ImageType.MEDIUM.getHolder())) {
                url = requestUrl.replace(ImageManager.ImageType.MEDIUM.getHolder(), ImageManager.ImageType.LARGE.getHolder());
            }
        }
        return url;
    }
}