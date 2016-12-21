package com.cloudspace.jindun.activity;

import android.view.View;

import com.cloudspace.jindun.R;
import com.cloudspace.jindun.ui.LoadingDialog;
import com.cloudspace.jindun.ui.MyTitleBar;

public class CustomTitleBaseActivity extends JindunBaseActivity {
    /**
     * 标题栏布局.
     */
    private MyTitleBar mAbTitleBar = null;

    private LoadingDialog mLoadingDialog;

    public LoadingDialog getLoadingDialog() {
        if (null == mLoadingDialog) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setCancelable(true);
        }
        return mLoadingDialog;
    }

    public LoadingDialog showLoadingDialog(String message) {
        try {
            getLoadingDialog().setMessage(message);
            getLoadingDialog().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getLoadingDialog();
    }

    public void showLoadingDialog() {
        getLoadingDialog().show();
    }

    public void hideLoadingDialog() {
        try {
            if (null != mLoadingDialog && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                mLoadingDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setContentView(int resId, boolean showTitle) {
        isToolBarVisible = showTitle;
        setContentView(resId);
    }


    public MyTitleBar getTitleBar() {
        if (mAbTitleBar == null) {
            mAbTitleBar = (MyTitleBar) toolbar.findViewById(R.id.act_titlebar);
        }
        if (mAbTitleBar == null) {//just for deal with error when no title ,and call this method
            mAbTitleBar = new MyTitleBar(this);
            mAbTitleBar.setVisibility(View.GONE);
        }
        return mAbTitleBar;
    }

    public void setSuperContentView(int resId) {
        super.setContentView(resId);
    }

    public Object getTag() {
        return this;
    }
}
