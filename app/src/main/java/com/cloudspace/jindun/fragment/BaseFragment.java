package com.cloudspace.jindun.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.cloudspace.jindun.activity.CustomTitleBaseActivity;
import com.cloudspace.jindun.network.HttpManager;
import com.cloudspace.jindun.ui.LoadingDialog;

import java.lang.reflect.Field;

public abstract class BaseFragment extends Fragment {

    private boolean wasCreated, wasInterrupted;

    protected boolean isAutoReCreate() {
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    final public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.wasCreated = true;
        if (isAutoReCreate() || mRootView == null) {
            mRootView = createView(inflater, container, savedInstanceState);
        }

        ViewParent parent = mRootView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(mRootView);
        }

        return mRootView;
    }

    @Override
    final public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mViewCreated && !isAutoReCreate() && mRootView != null) return;
        initView(view, savedInstanceState);
    }

    @Override
    final public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        onFragmentCreated(savedInstanceState);

        mViewCreated = true;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        wasInterrupted = true;
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onPause() {
        wasCreated = wasInterrupted = false;
        super.onPause();
    }

    @Override
    public void onDetach() {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Handler.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Handler handler = (Handler) field.get(this);
                    handler.removeCallbacksAndMessages(null);
                    handler = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        HttpManager.cancelAllRequests(this);
    }

    private LoadingDialog mProgressDialog;

    public LoadingDialog getLoadingDialog() {
        if (getActivity() instanceof CustomTitleBaseActivity) {
            return ((CustomTitleBaseActivity) getActivity()).getLoadingDialog();
        }
        if (null == mProgressDialog) {
            mProgressDialog = new LoadingDialog(getActivity());
            mProgressDialog.setCancelable(true);
        }
        return mProgressDialog;
    }

    public LoadingDialog getLoadingDialog(boolean isOutsideCanceled) {
        mProgressDialog = getLoadingDialog();
        mProgressDialog.setCanceledOnTouchOutside(isOutsideCanceled);
        return mProgressDialog;
    }


    public void showProgressDialog() {
        showProgressDialog(null);
    }

    public void showProgressDialog(String message) {
        getLoadingDialog().setMessage(message);
        getLoadingDialog().show();
    }

    public void showProgressDialog(String message, boolean isOutsideCanceled) {
        getLoadingDialog(isOutsideCanceled).setMessage(message);
        getLoadingDialog(isOutsideCanceled).show();
    }

    public void hideProgressDialog() {
        try {
            LoadingDialog mProgressDialog = getLoadingDialog();
            if (null != mProgressDialog && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected View mRootView;

    protected boolean mViewCreated = false;

    public boolean isRestoring() {
        return wasInterrupted;
    }

    public boolean isResuming() {
        return !wasCreated;
    }

    public boolean isLaunching() {
        return !wasInterrupted && wasCreated;
    }

    //创建View
    protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    //初始化View
    protected void initView(View view, Bundle savedInstanceState) {
    }

    //view初始化之后 需要更新的静态视图或是 参数获取处理等简单逻辑
    public abstract void onFragmentCreated(Bundle savedInstanceState);

    //根据业务需求，请求需要的业务数据
    public abstract void updateContent();

    protected boolean isAlive() {
        return (isAdded() && getActivity() != null);
    }

    public boolean isOnGestureBack(MotionEvent event) {
        return true;
    }

}
