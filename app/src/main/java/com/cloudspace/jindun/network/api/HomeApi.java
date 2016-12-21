package com.cloudspace.jindun.network.api;

import com.cloudspace.jindun.model.Student;
import com.cloudspace.jindun.network.HttpManager;
import com.cloudspace.jindun.network.base.ApiCallback;
import com.cloudspace.jindun.network.base.GsonRequest;
import com.cloudspace.jindun.network.base.RequestFactory;

/**
 * Created by zengxianhua on 16/12/11.
 */

public class HomeApi extends BaseAPI{

    public  void getUser(Object tag, int id, ApiCallback<Student> apiCallback) {
        String url = "http://www.cloudscope.cn/jindun/user/stu/query/1";
        GsonRequest request = RequestFactory.newInstance()
                .create(url, Student.class, apiCallback)
                .setNoCache();
        HttpManager.addRequest(request, tag);
    }
}
