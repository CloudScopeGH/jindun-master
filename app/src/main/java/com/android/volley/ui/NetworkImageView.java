/**
 * Copyright (C) 2013 The Android Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.volley.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

import com.android.ui.drawable.AlphableBitmapDrawable;
import com.android.volley.Response.Listener;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.Utils;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.SimpleImageLoader;
import com.cloudspace.jindun.utils.ImageUtil;

/**
 *
 *
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class NetworkImageView extends RecyclingImageView {
    public static final int TAG_URL = 0x20FF0001;
    private static final ColorDrawable transparentDrawable = new ColorDrawable(
            android.R.color.transparent);
    private static final int HALF_FADE_IN_TIME = Utils.ANIMATION_FADE_IN_TIME / 2;
    /** The URL of the network image to load */
    protected String mUrl;

    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    public int mDefaultImageId;
    public boolean mNeedStates = true;

    /**
     * Resource ID of the image to be used if the network response fails.
     */
    protected int mErrorImageId;

    /** Local copy of the ImageLoader. */
    protected ImageLoader mImageLoader;

    /** Current ImageContainer. (either in-flight or finished) */
    protected ImageContainer mImageContainer;

    private boolean mFadeIn = false;
    private int mMaxImageHeight = 0;
    private int mMaxImageWidth = 0;

    public boolean isNeedImmediate;

    private Listener<BitmapDrawable> mListener;

    private int mRadius;
    private float mRect;

    //    private BitmapShader mBitmapShader;
//    protected Paint mBitmapPaint;
//    private final Matrix mShaderMatrix = new Matrix();
//    
    public NetworkImageView(Context context) {
        this(context, null);
        if (isInEditMode()) {
            return;
        }
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (isInEditMode()) {
            return;
        }
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }
// 		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.networkImageView);
//        if(a.hasValue(R.styleable.networkImageView_defaultImage)){
//        	mDefaultImageId = a.getResourceId(R.styleable.networkImageView_defaultImage,0);
//        }
//        a.recycle();
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
     *
     * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)} and
     * {@link NetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url The URL that should be loaded into this ImageView.
     * @param imageLoader ImageLoader that will be used to make the request.
     */
    public void setImageUrl(String url, ImageLoader imageLoader) {
        mUrl = url;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    public void setResetImageUrl(String url, ImageLoader imageLoader) {
        mImageContainer = null;
        mUrl = url;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    public void setDefaultImageResId(int defaultImage) {
        mDefaultImageId = defaultImage;
    }

    public void setStateEnable(boolean enable) {
        mNeedStates = enable;
    }

    public void setImageRect(float rect) {
        mRect = rect;
    }

    public void setImageRadius(int radius) {
        mRadius = radius;
    }

    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage) {
        mErrorImageId = errorImage;
    }

    public void setMaxImageSize(int maxImageWidth, int maxImageHeight) {
        mMaxImageWidth = maxImageWidth;
        mMaxImageHeight = maxImageHeight;
    }

    public void setMaxImageSize(int maxImageSize) {
        setMaxImageSize(maxImageSize, maxImageSize);
    }

    public void setFadeInImage(boolean fadeInImage) {
        mFadeIn = fadeInImage;
    }

    public void setImageListener(Listener<BitmapDrawable> listener) {
        mListener = listener;
    }

    public Listener<BitmapDrawable> getImageListener() {
        return mListener;
    }

//    private Paint getBitmapPaint() {
//        if (mBitmapPaint == null) {
//            mBitmapPaint = new Paint();
//            mBitmapPaint.setAntiAlias(true);
//        }
//        return mBitmapPaint;
//    }
//    
//    private BitmapShader getBitmapShader(Bitmap bitmap) {
//        if (mBitmapShader == null && bitmap != null) {
//        	mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            getBitmapPaint().setShader(mBitmapShader);
//        }
//        return mBitmapShader;
//    }

    /**
     * Loads the image for the view if it isn't already loaded.
     * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
     */
    protected void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = getWidth();
        int height = getHeight();

        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
        }
        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            if (mImageContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mImageContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }

        int maxWidth = 0;
        int maxHeight = 0;
        if (mImageLoader instanceof SimpleImageLoader) {
            final SimpleImageLoader loader = (SimpleImageLoader) mImageLoader;
            maxWidth = mMaxImageWidth == 0 ? loader.getMaxImageWidth() : mMaxImageWidth;
            maxHeight = mMaxImageHeight == 0 ? loader.getMaxImageHeight() : mMaxImageHeight;
        } else {
            // Calculate the max image width / height to use while ignoring WRAP_CONTENT dimens.
            maxWidth = wrapWidth ? 0 : width;
            maxHeight = wrapHeight ? 0 : height;
        }

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        ImageContainer newContainer = mImageLoader.get(mUrl,
                new ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mErrorImageId != 0) {
                            setImageResource(mErrorImageId);
//                            setTag(TAG_URL,null);
                        }
                    }

                    @Override
                    public void onResponse(final ImageContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        if (response.getBitmapDrawable() != null) {
                            if (isNeedImmediate) {
                                if (null != mListener) {
                                    mListener.onResponse(response.getBitmapDrawable());
                                }
                            } else {
                                if (mFadeIn) {
                                    setAnimateImageBitmap(response.getBitmapDrawable());
                                } else {
                                    setImageDrawable(response.getBitmapDrawable(), mNeedStates);
                                }
                                if (null != mListener) {
                                    mListener.onResponse(response.getBitmapDrawable());
                                }
                            }
//                        	setTag(TAG_URL,mUrl);
                        } else if (mDefaultImageId != 0) {
                            setImageResource(mDefaultImageId);
//                            setTag(TAG_URL,null);//???
                        }
                    }
                }, maxWidth, maxHeight, mNeedStates);

        // update the ImageContainer to be the new bitmap container.
        mImageContainer = newContainer;
    }

    @SuppressLint("NewApi")
    private void setAnimateImageBitmap(final BitmapDrawable bitmap) {
        // If we're fading in and on HC MR1+
        if (Utils.hasHoneycombMR1()) {
            // Use ViewPropertyAnimator to run a simple fade in + fade out animation to update the ImageView
            animate().scaleY(0.95f)
                    .scaleX(0.95f)
                    .alpha(0f)
                    .setDuration(getDrawable() == null ? 0 : HALF_FADE_IN_TIME)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setImageDrawable(bitmap);
                            setImageDrawable(null);
                            animate()
                                    .alpha(1f)
                                    .scaleY(1f)
                                    .scaleX(1f)
                                    .setDuration(HALF_FADE_IN_TIME)
                                    .setListener(null);
                        }
                    });
        } else {
            // Otherwise use a TransitionDrawable to fade in
            Drawable initialDrawable = (getDrawable() != null) ? getDrawable() : transparentDrawable;
            // Use TransitionDrawable to fade in
            final TransitionDrawable td = new TransitionDrawable(new Drawable[]{initialDrawable, bitmap});
            setImageDrawable(td);
            td.startTransition(Utils.ANIMATION_FADE_IN_TIME);
        }
    }

    protected void setDefaultImageOrNull() {
        if (mDefaultImageId != 0) {
            setImageResource(mDefaultImageId);
        } else {
            setImageDrawable(null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mImageLoader != null) {
            loadImageIfNecessary(true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
//            setImageDrawable(null);
//            postDelayed(runnable, 100);
            // also clear out the container so we can reload the image if necessary.
            mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    public void setImageResource(int resId) {
        if (mRadius > 0) {
            Drawable d = null;
            try {
                d = getResources().getDrawable(resId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setImageDrawable(d);
            return;
        }
        super.setImageResource(resId);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null && drawable != getDrawable()) {
            Bitmap bitmap = null;
            if (drawable instanceof BitmapDrawable)
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                bitmap = ImageUtil.getExportedBitmap(bitmap, mRect, mRadius);
                drawable = new AlphableBitmapDrawable(getResources(), bitmap);
            }
        }
        super.setImageDrawable(drawable);
    }

    public void setImageDrawable(Drawable drawable, boolean needStates) {
        if (drawable != null && drawable != getDrawable()) {
            Bitmap bitmap = null;
            if (drawable instanceof BitmapDrawable)
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                bitmap = ImageUtil.getExportedBitmap(bitmap, mRect, mRadius);
                drawable = new AlphableBitmapDrawable(getResources(), bitmap, needStates);
            }
        }
        super.setImageDrawable(drawable);
    }
}