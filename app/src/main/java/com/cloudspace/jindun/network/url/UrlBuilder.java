package com.cloudspace.jindun.network.url;


public class UrlBuilder {

    private String host;
    private String path;
    private String action;

    public UrlBuilder host(String host) {
        this.host = host;
        return this;
    }

    public UrlBuilder path(String path) {
        this.path = path;
        return this;
    }

    public UrlBuilder action(String action) {
        this.action = action;
        return this;
    }

    public StringBuilder build() {
        StringBuilder sb = new StringBuilder();

        sb.append(host).append(path).append(action).append("/");

        return sb;
    }


    public static String create(String host, String path, String action) {
        return new UrlBuilder()
                .host(host)
                .path(path)
                .action(action)
                .build().toString();
    }
}
