package com.cloudspace.jindun.titlestyle;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cloudspace.jindun.R;


public class ToolBarHelper {
    private Context mContext;
    private FrameLayout mContentView;
    private View mUserView;
    private Toolbar mToolBar;
    private LayoutInflater mInflater;

    /*
    * 两个属性
    * 1、toolbar是否悬浮在窗口之上
    * 2、toolbar的高度获取
    * */
    private static int[] ATTRS = {
            R.attr.windowActionBarOverlay,
            R.attr.actionBarSize
    };

    public ToolBarHelper(Context context, int layoutId, boolean isVisible) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        initContentView(); /*初始化用户定义的布局*/
        initUserView(layoutId, isVisible); /*初始化toolbar*/
        initToolBar();
    }

    private void initContentView() {
        /*直接创建一个帧布局，作为视图容器的父容器*/
        mContentView = new FrameLayout(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContentView.setLayoutParams(params);

    }

    private void initToolBar() { /*通过inflater获取toolbar的布局文件*/
        View toolbar = mInflater.inflate(R.layout.custom_toolbar, mContentView);
        mToolBar = (Toolbar) toolbar.findViewById(R.id.id_tool_bar);
    }

    @SuppressWarnings("ResourceType")
    private void initUserView(int id, boolean isVisible) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(ATTRS); /*获取主题中定义的悬浮标志*/
        boolean overly = typedArray.getBoolean(0, false); /*获取主题中定义的toolbar的高度*/
        int toolBarSize = (int) typedArray.getDimension(1,  mContext.getResources().getDimension(R.dimen.abc_action_bar_default_height_material));
        typedArray.recycle(); /*如果是悬浮状态，则不需要设置间距*/
        params.topMargin = (!isVisible || overly) ? 0 : toolBarSize;

        mUserView = mInflater.inflate(id, null);
        mContentView.addView(mUserView, params);

//        mInflater.inflate(id, mContentView, true);

    }

    public FrameLayout getContentView() {
        return mContentView;
    }

    public Toolbar getToolBar() {
        return mToolBar;
    }
}