package com.cloudspace.jindun.network.api;

import com.cloudspace.jindun.rongyun.RongyunApi;

/**
 * Created by zengxianhua on 16/12/15.
 */

public class API {
    private static API api;

    private HomeApi homeApi;
    private RongyunApi rongyunApi;

    public static API getInstance() {
        if (api == null) {
            api = new API();
        }
        return api;
    }

    public synchronized HomeApi getHomeApi() {
        if (homeApi == null) {
            homeApi = new HomeApi();
        }
        return homeApi;
    }

    public  synchronized RongyunApi getRongyunApi(){
        if (rongyunApi == null){
            rongyunApi = new RongyunApi();
        }
        return rongyunApi;
    }
}
