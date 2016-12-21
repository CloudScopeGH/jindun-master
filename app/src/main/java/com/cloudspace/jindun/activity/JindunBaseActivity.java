package com.cloudspace.jindun.activity;

import android.annotation.TargetApi;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;

import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.constants.Constants;
import com.cloudspace.jindun.log.APPLog;
import com.cloudspace.jindun.network.HttpManager;
import com.cloudspace.jindun.sip.CallReceiver;
import com.cloudspace.jindun.titlestyle.ToolBarHelper;
import com.cloudspace.jindun.utils.ActivityUtil;
import com.cloudspace.jindun.utils.Device;

public class JindunBaseActivity extends AppCompatActivity {
    public final static String TAG = JindunBaseActivity.class.getSimpleName();

    private CallReceiver callReceiver;

    protected void registerSipCallReceiver(){
        IntentFilter filter = new IntentFilter(Constants.SIP_INCOMING_CALL_ACTION);
        callReceiver = new CallReceiver();
        registerReceiver(callReceiver, filter);
    }

    protected void unRegisterSipCallReceiver(){
        if (callReceiver != null){
            unregisterReceiver(callReceiver);
        }
    }

    private ToolBarHelper mToolBarHelper;
    public Toolbar toolbar;

    protected static long activityCnt = 0;

    protected boolean isToolBarVisible = true;      //是否显示自定义toolbar

    @Override
    public void setContentView(int layoutResID) {
        mToolBarHelper = new ToolBarHelper(this, layoutResID, isToolBarVisible);
        toolbar = mToolBarHelper.getToolBar();
        setContentView(mToolBarHelper.getContentView());
        setSupportActionBar(toolbar);
        onCreateCustomToolBar(toolbar);

        setToolbarVisible(isToolBarVisible);
    }

    private void setToolbarVisible(boolean isToolBarVisible) {
        if (isToolBarVisible) {
            return;
        }

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            toolbar.setTranslationY(-actionBarHeight);
        }
    }

    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setContentInsetsRelative(0, 0);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.onActivityCreate(this);
        ((UCAPIApp)getApplication()).addActivity(this);

        registerSipCallReceiver();
    }

    @Override
    protected void onDestroy() {
        ((UCAPIApp)getApplication()).removeActivity(this);

        unRegisterSipCallReceiver();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ++activityCnt;
    }

    public long getActiveActivityCnt(){
        return activityCnt;
    }


    @Override
    protected void onStop() {
        super.onStop();

        --activityCnt;
        APPLog.w(TAG, "activityCnt:" + activityCnt);

        HttpManager.cancelAllRequests(this);
    }


    /**
     * @return true:foreground
     */
    public static boolean isForeground() {
        return activityCnt > 0;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        if (Device.isPreM()) return true;
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission(String permission, int requestCode) {
        if (shouldShowRequestPermissionRationale(permission)) {
            //TODO explain permission
        }
        requestPermissions(new String[]{permission}, requestCode);
    }

    public boolean checkPermission(String permission, int requestCode) {
        if (!hasPermission(permission)) {
            requestPermission(permission, requestCode);
            return false;
        }
        return true;
    }

}
