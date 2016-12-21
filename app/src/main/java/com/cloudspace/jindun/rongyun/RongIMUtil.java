package com.cloudspace.jindun.rongyun;

import com.cloudspace.jindun.config.Configs;
import com.cloudspace.jindun.log.APPLog;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * Created by zengxianhua on 16/12/15.
 */

public class RongIMUtil {
    public static void connect(){
        RongIM.connect(Configs.USER_TOKEN, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                APPLog.d("connect TokenIncorrect");
            }

            @Override
            public void onSuccess(String s) {
                APPLog.d("connect success");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                APPLog.d("connect error:" + errorCode);
            }
        });
    }
}
