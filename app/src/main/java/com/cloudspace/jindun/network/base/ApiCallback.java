package com.cloudspace.jindun.network.base;

import android.app.Activity;

import com.android.volley.Response.ErrorListener;
import com.android.volley.error.NoConnectionError;
import com.android.volley.error.VolleyError;
import com.android.volley.error.VolleyErrorHelper;
import com.cloudspace.jindun.R;
import com.cloudspace.jindun.utils.ToastMaker;

import java.lang.ref.WeakReference;
import java.util.Map;

public abstract class ApiCallback<K> implements ErrorListener {

    protected boolean mToaskError;
    private boolean mUiResponse = true;
    private boolean mIsRequestCanceled = false;

    private Object tag;

    private WeakReference<Activity> weakActivityRef;

    public ApiCallback() {

    }

    public ApiCallback(Activity activity) {
        weakActivityRef = new WeakReference<Activity>(activity);
    }

    public ApiCallback<K> toastActionError() {
        this.mToaskError = true;
        return this;
    }

    public ApiCallback<K> setUiResponse(boolean uiResponse) {
        this.mUiResponse = uiResponse;
        return this;
    }

    public ApiCallback<K> setRequestState(boolean mIsRequestCanceled) {
        this.mIsRequestCanceled = mIsRequestCanceled;
        return this;
    }

    public boolean isUiResponse() {
        return mUiResponse;
    }

    public boolean isRequestCanceled() {
        return mIsRequestCanceled;
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
        if (mToaskError) {
            if (error instanceof NoConnectionError) {
                ToastMaker.showToastLong(R.string.error_network_simple);
            } else {
                ToastMaker.showToastLong(VolleyErrorHelper.getErrorActionMessage(error));
            }
        }
        if (!mIsRequestCanceled) {
            if (weakActivityRef == null || isActivity()) {
                handleResult(null, error);
            }
        }
    }

    public void onSuccess(final K result, Map<String, String> params) {
        if (!mIsRequestCanceled) {
            if (weakActivityRef == null || isActivity()) {
                handleResult(result, null);
            }
        }
    }

    public abstract void handleResult(K result, VolleyError error);

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    boolean isActivity() {
        return weakActivityRef != null && weakActivityRef.get() != null && !weakActivityRef.get().isFinishing();
    }
}