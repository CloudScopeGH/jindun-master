package com.cloudspace.jindun.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.log.APPLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ActivityUtil {
    private static List<Class<?>> limitActivity;
    private static final int MAX_INSTANCE_NUM = 3;
    private static LinkedList<WeakReference<Activity>> limitQuene;

    // call in application's onCreate
    public static void init() {
        if (null == limitActivity) {
            limitActivity = new ArrayList<Class<?>>();
        }
        if (null == limitQuene) {
            limitQuene = new LinkedList<WeakReference<Activity>>();
        }
    }

    public static void onNewActivityStart(Intent intent) {
        int size = limitQuene.size();
        while (size > MAX_INSTANCE_NUM) {
            size--;
            Activity activity = limitQuene.poll().get();
            if (null == activity) {
                APPLog.i("test", "activity has been recycled.");
                continue;
            }
            APPLog.i("test", "activity finish: " + activity.getClass().getSimpleName());
            activity.finish();
        }
    }

    public static void onActivityCreate(Activity activity) {
        if (null != activity && isLimitInstanceActivity(activity)) {
            limitQuene.offer(new WeakReference<Activity>(activity));
        }
        UCAPIApp.getInstance().initScreenParams(activity.getWindowManager().getDefaultDisplay());
    }

    public static boolean isLimitInstanceActivity(Intent intent) {
        if (null != intent) {
            String s = intent.getComponent().getClassName();
            if (limitActivity != null) {
                for (Class<?> clazz : limitActivity) {
                    if (clazz.getName().equals(s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isLimitInstanceActivity(Activity activity) {
        if (null != activity) {
            if (limitActivity.contains(activity.getClass())) {
                return true;
            }
        }
        return false;
    }

    public static void showSms(Context mContext, String smsto, String sms_body) {
        showSms(mContext, smsto, sms_body, 0);
    }

    public static void showSms(Context mContext, String smsto, String sms_body, int requestCode) {
        try {
            StringBuilder sb = new StringBuilder("smsto:");
            if (smsto != null) {
                sb.append(smsto);
            }
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(sb.toString()));
            intent.putExtra("sms_body", sms_body);
            if (requestCode > 0 && mContext instanceof Activity) {
                ((Activity) mContext).startActivityForResult(intent, requestCode);
            } else {
                mContext.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();

        } catch (Exception e) {
            if (e instanceof ActivityNotFoundException) {//用于android pad的判断
                // ToastMaker.showToastLong(mContext.getString(R.string.not_support_sms));
            }
            e.printStackTrace();
        }
    }

    public static void startApp(Context mContext, String url) {
        Intent intent = new Intent();
        if (url.startsWith("com.")) {
            intent = UCAPIApp.getInstance().getPackageManager().getLaunchIntentForPackage(url);
        } else if (url.contains("://")) {
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            intent.setAction(Intent.ACTION_VIEW);
        }
        mContext.startActivity(intent);
    }

    public static boolean isActivityValid(Activity activity) {
        if (activity == null) {
            return false;
        }

        if (activity.isFinishing()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return false;
            }
        }

        return true;
    }
}
