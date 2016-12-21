package com.cloudspace.jindun.rongyun;

import com.cloudspace.jindun.model.User;
import com.cloudspace.jindun.network.HttpManager;
import com.cloudspace.jindun.network.base.ApiCallback;
import com.cloudspace.jindun.network.base.GsonRequest;
import com.cloudspace.jindun.network.base.RequestFactory;

/**
 * Created by zengxianhua on 16/12/17.
 */

public class RongyunApi {
    @Deprecated
    public void getToken(String tag, String userId, String name, String portraitUri, ApiCallback<User> callback){
        String url = "http://api.cn.ronghub.com/user/getToken.json";
        GsonRequest request = RequestFactory.newInstance()
                .create(url, User.class, callback)
                .setParams("userId", userId)
                .setParams("name", name)
                .setParams("portraitUri", portraitUri)
                .setNoCache();
        HttpManager.addRequest(request, tag);
    }
}
