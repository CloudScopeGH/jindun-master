package com.cloudspace.jindun.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.TextView;

import java.text.DecimalFormat;

public final class Convert {

    public static final long ONE_KB = 1024;

    public static final long ONE_MB = ONE_KB * ONE_KB;

    public static final long ONE_GB = ONE_KB * ONE_MB;

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + .5);
    }

    public static int dip2px(float dpValue) {
        final float scale = Global.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + .5);
    }

    public static int px2dip(float pxValue) {
        final float scale = Global.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + .5);
    }

    public final static int cm2px(float cmValue) {
        return (int) (cmValue * Global.getContext().getResources()
                .getDisplayMetrics().densityDpi / 2.54f);
    }

    public static int getColor(int res) {
        return Global.getContext().getResources().getColor(res);
    }

    public static String size2String(long size) {
        String displaySize;
        DecimalFormat df = new DecimalFormat("0.0");

        if (size / ONE_GB > 0) {
            displaySize = df.format(size * 1.0 / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = df.format(size * 1.0 / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = df.format(size * 1.0 / ONE_KB) + " KB";
        } else {
            displaySize = df.format(size * 1.0) + "B";
        }
        return displaySize;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static int getDimensionPixelFromResource(Context context, int resID) {
        return context.getResources().getDimensionPixelSize(resID);
    }

    public static float getFloatFromResource(Context context, int resId) {
        TypedValue outValue = new TypedValue();
        context.getResources().getValue(resId, outValue, true);
        float value = outValue.getFloat();

        return value;
    }

    public static final void setTextView(TextView tv, CharSequence text) {
    }


}
