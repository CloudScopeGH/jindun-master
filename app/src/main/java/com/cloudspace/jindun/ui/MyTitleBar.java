/*
 * Copyright (C) 2012 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudspace.jindun.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudspace.jindun.R;
import com.cloudspace.jindun.module.ActionItem;
import com.cloudspace.jindun.utils.Convert;


/**
 * 描述：标题栏实现.
 */
public class MyTitleBar extends RelativeLayout implements TitleBar {
    private Context mContext;

    /**
     * 标题布局.
     */
    protected LinearLayout middleLayout = null;
    /**
     * 显示标题文字的View.
     */
    protected TextView titleTextBtn = null;
    /**
     * 左侧的Logo图标View.
     */
    protected TextView leftView = null;
    /** 左侧的Logo图标View.
     protected ImageView logoView2 = null;*/


    /**
     * 左侧的Logo图标右边的分割线View.
     */
    protected TextView rightView = null;
    protected TextView rightView2 = null;
    private ImageView rightBadgeIV = null;

    private boolean isFreezing = false;//冻结，true的话，titlebar的左中右布局样式将无法改变

    /**
     * Instantiates a new ab title bar.
     *
     * @param context the context
     */
    public MyTitleBar(Context context) {
        super(context);
        init(context);

    }

    /**
     * Instantiates a new ab title bar.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public MyTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Inin title bar.
     *
     * @param context the context
     */
    public void init(Context context) {

        mContext = context;
        // 水平排列
//		this.setId(R.id.my_titlebar);

        /*添加左边第一个view*/
        leftView = new TextView(context);
        leftView.setId(R.id.my_lefttview);
        leftView.setVisibility(View.GONE);
        final float textSize = Convert.getFloatFromResource(getContext(),
                R.dimen.basic_text_size_float);
        leftView.setTextSize(textSize);
        final int padding = Convert.getDimensionPixelFromResource(getContext(), R.dimen.my_title_bar_padding);
        final int padding2 = Convert.getDimensionPixelFromResource(getContext(), R.dimen.my_title_bar_padding_2);
        final int padding3 = Convert.getDimensionPixelFromResource(getContext(), R.dimen.my_title_bar_padding_3);
        leftView.setPadding(padding2, padding, padding + padding2, padding);
        leftView.setTextColor(getResources().getColorStateList(R.color.base_red_text_color));
        leftView.setGravity(Gravity.CENTER_VERTICAL);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);

        addView(leftView, params);

		/*添加右边默认 text view*/
        rightView = new TextView(context);
        rightView.setId(R.id.my_rightview);

        rightView.setVisibility(View.GONE);
        rightView.setTextSize(textSize);
        rightView.setCompoundDrawablePadding(padding);
        rightView.setPadding(padding3, padding, padding3, padding);
        rightView.setTextColor(getResources().getColorStateList(R.color.base_red_text_color));
        rightView.setGravity(Gravity.CENTER_VERTICAL);


        params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.setMargins(0, 0, 10, 0);
        addView(rightView, params);

		/*添加中间Title view*/
        middleLayout = new LinearLayout(context);
        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        middleLayout.setOrientation(LinearLayout.VERTICAL);
        middleLayout.setGravity(Gravity.CENTER);
        addView(middleLayout, params);

        setBackgroundResource(R.drawable.titlebar_layout_bg);

        leftView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }
        });
    }

    /**
     * 描述：标题栏的背景图.
     */
    public void setTitleBarBackground(int resId) {
        this.setBackgroundResource(resId);
    }

    /**
     * 描述：设置标题栏
     */
    public void setTitleText(CharSequence text) {
        setTitleText(text, false);
    }

    public void setTitleText(CharSequence text, boolean isHasEmoji) {
        if (!TextUtils.isEmpty(text)) {
            getTitle().setVisibility(View.VISIBLE);
            if (isHasEmoji) {
                Convert.setTextView(getTitle(), text);
            } else {
                getTitle().setText(text);
            }
        }
    }

    public void setTitleColor(int color) {
        getTitle().setTextColor(color);
    }

    public TextView getTitle() {
        if (titleTextBtn == null) {
            middleLayout.removeAllViews();
             /*添加中间默认 text view*/
            titleTextBtn = createTitleTextView();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;

            middleLayout.addView(titleTextBtn, params);
        }
        return titleTextBtn;
    }

    /**
     * 描述：获取左边控件.
     *
     * @return the left view
     */
    public TextView getLeftView() {
        return leftView;
    }

    public MyTitleBar setLeftView(int resId) {
        leftView.setVisibility(View.VISIBLE);
        leftView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
        return this;
    }

    public MyTitleBar setLeftText(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            leftView.setVisibility(View.VISIBLE);
            leftView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        leftView.setText(text);
        return this;
    }

    public MyTitleBar setLeftOnClickListener(OnClickListener listener) {
        leftView.setOnClickListener(listener);
        return this;
    }

    /**
     * 描述：获取右边第一个控件.
     *
     * @return the right view
     */
    public TextView getRightView() {
        return rightView;
    }

    public MyTitleBar setRightView(int resId) {
        rightView.setVisibility(View.VISIBLE);
        rightView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
        return this;
    }

    public MyTitleBar setRightText(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            rightView.setVisibility(View.VISIBLE);
            rightView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        rightView.setText(text);
        return this;
    }

    public MyTitleBar setRightColor(int color) {
        rightView.setTextColor(color);
        return this;
    }

    public MyTitleBar setRightTextSize(int size) {
        rightView.setTextSize(size);
        return this;
    }

    public MyTitleBar setRightOnClickListener(OnClickListener listener) {
        rightView.setOnClickListener(listener);
        return this;
    }

    public MyTitleBar setRightOnLongClickListener(OnLongClickListener listener) {
        rightView.setOnLongClickListener(listener);
        return this;
    }

    public ImageView getRightBadgeIV() {
        if (rightBadgeIV == null) {
            ImageView view = new ImageView(mContext);
            view.setVisibility(View.GONE);
            view.setImageResource(R.drawable.live_private_chat_new);
            LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (rightView != null && rightView.getVisibility() == VISIBLE) {
                params.addRule(RelativeLayout.RIGHT_OF, R.id.my_rightview);
            } else {
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }
            final int marginleft = Convert.getDimensionPixelFromResource(getContext(), R.dimen.my_title_bar_badge_margin_right);
            final int margintop = Convert.getDimensionPixelFromResource(getContext(), R.dimen.my_title_bar_badge_margin_top);
            params.setMargins(-marginleft, margintop, 0, 0);
            rightBadgeIV = view;
            addView(view, params);
        }
        return rightBadgeIV;
    }

    private TextView createTitleTextView() {
        TextView titleTextBtn = new TextView(getContext());
        titleTextBtn.setId(R.id.my_titleview);
        titleTextBtn.setTextColor(getResources().getColor(R.color.base_txt_gray1));
        titleTextBtn.setTextSize(Convert.getFloatFromResource(getContext(), R.dimen.title_text_size_float));
        int padding = Convert.getDimensionPixelFromResource(getContext(), R.dimen.my_title_bar_middle_padding);
        titleTextBtn.setPadding(padding, 0, padding, 0);
        titleTextBtn.setSingleLine(true);
        titleTextBtn.setEllipsize(TextUtils.TruncateAt.END);
        titleTextBtn.setGravity(Gravity.CENTER_HORIZONTAL);
        return titleTextBtn;
    }

    /**
     * ************************ 工具型 **********************************
     */
    private ActionItem defultLeft = new ActionItem(null, R.drawable.backbtn_black, new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mContext instanceof Activity) {
                ((Activity) mContext).finish();
            }
        }
    });


    /**
     * 典型的左返回，中间Title,右播放
     */
    public void setSimpleMode(CharSequence titleText) {

        setSimpleMode(titleText, null);
    }

    /**
     * 典型的左返回，中间Title,右边自定义行为
     * 如果右边是 ActionItem();则什么不显示
     * 如果是其他，则图示自定义行为和显示图片文字
     */
    public void setSimpleMode(CharSequence titleText, ActionItem right) {

        setSimpleMode(titleText, defultLeft, right);
    }


    /**
     * 典型的左返回，中间Title,右边自定义行为
     * 如果右边是 ActionItem();则什么不显示
     * 如果是其他，则图示自定义行为和显示图片文字
     */
    public void setSimpleMode(CharSequence titleText, ActionItem left, ActionItem right) {
        if (isFreezing) {
            return;
        }
        if (left != null) {
            setLeftText(left.text);
            setLeftView(left.resId);
            setLeftOnClickListener(left.clickListener);
        }

        setTitleText(titleText);

        if (right != null) {
            setRightView(right.resId);
            setRightText(right.text);
            setRightOnClickListener(right.clickListener);
            if (right.longClickListener != null) {
                setRightOnLongClickListener(right.longClickListener);
            }
        } else {
            setRightView(0);
            setRightText(null);
            setRightOnClickListener(null);
        }
    }


    @Override
    public void setVisibility(final int visibility) {
        if (getParent() instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup) getParent();
            if (parent.getId() == getTitleContainerId()) {
                parent.setVisibility(visibility);
            }
        }
        super.setVisibility(visibility);
    }

    public static Object reflactFiled(String className, String filedName) {
        Object result = null;
        try {
            result = Class.forName(className).getField(filedName).get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected int getTitleContainerId() {
        Object obj = reflactFiled("com.android.internal.R$id", "title_container");
        if (obj != null) {
            return (Integer) obj;
        } else {
            return -1;
        }
    }

    @Override
    public void setTitle(String title) {
        setTitleText(title);
    }
}
