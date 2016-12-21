package com.android.ui.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;

public class AlphableBitmapDrawable extends BitmapDrawable {

    private static final int MASK_COLOR = 0x77000000;
    private boolean isEnable = true;

    public AlphableBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
        this.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_selected, android.R.attr.state_enabled});
    }

    public AlphableBitmapDrawable(Resources res, Bitmap bitmap, boolean enable) {
        this(res, bitmap);
        isEnable = enable;
    }

    @Override
    protected boolean onStateChange(int[] states) {
        if (isEnable) {
            for (int state : states) {
                if (state == android.R.attr.state_pressed) {
                    setColorFilter(new PorterDuffColorFilter(MASK_COLOR, PorterDuff.Mode.SRC_ATOP));
                } else if (state == android.R.attr.state_selected) {
                    setColorFilter(null);
                } else if (state == android.R.attr.state_enabled) {
                    setColorFilter(null);
                }
            }
        }
        return true;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    public void setEnableAlapha(boolean flag) {
        isEnable = flag;
    }
}
