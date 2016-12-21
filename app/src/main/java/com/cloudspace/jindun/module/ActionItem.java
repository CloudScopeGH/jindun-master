package com.cloudspace.jindun.module;

import android.view.View;

import java.io.Serializable;

public class ActionItem implements Serializable {
    public final CharSequence text;
    public final View.OnClickListener clickListener;
    public final int resId;
    public final View.OnLongClickListener longClickListener;

    public ActionItem() {//什么行为都没有，不显示
        this(null, 0, null);
    }

    public ActionItem(int resId, View.OnClickListener listener) {//有图片，有行为
        this(null, resId, listener);
    }

    public ActionItem(CharSequence text, View.OnClickListener listener) {//有文字，有行为
        this(text, 0, listener);
    }

    public ActionItem(CharSequence text, int resId, View.OnClickListener clickListener) {//有文字，有图片，有行为
        this(text, resId, clickListener, null);
    }

    public ActionItem(CharSequence text, int resId, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {//有文字，有图片，有行为，长按行为
        this.text = text;
        this.resId = resId;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }
}