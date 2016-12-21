package com.cloudspace.jindun.net.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cloudspace.jindun.utils.ImageUtil;
import com.cloudspace.jindun.utils.StringUtil;

import java.io.File;

public class ImageManager {
    /**
     * Default on-disk cache directory for image request
     */
    public enum ImageType {
        /* No resize */ORIGINAL(".jpg"),
        /* 100x100 */SMALL("_100_100.jpg"),
        /* 200x200 */TINY("_200_200.jpg"),
        /* 320x320 */MEDIUM("_320_320.jpg"),
        /* 640x640 */LARGE("_640_640.jpg"),
        /* ChatImage */ChatImage("");
        private String holder;

        ImageType(String targetHolder) {
            this.holder = targetHolder;
        }

        public String getHolder() {
            return holder;
        }
    }

    public enum ImageRadius {
        ROUND(360), /* 圆形图片 */
        RADIUS_3(3), /* Radius=3的圆角矩形 */
        RADIUS_7(7), /* Radius=7的圆角矩形 */
        RADIUS_8(8), /* Radius=8的圆角矩形 */
        RADIUS_10(10); /* Radius=10的圆角矩形 */

        private int mRadius;

        ImageRadius(int radius) {
            this.mRadius = radius;
        }

        public int getRadius() {
            return this.mRadius;
        }
    }


    public static void loadCircleImage(final Context context, ImageView imageView, String url) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        // 小图标
        Glide.with(context.getApplicationContext()).load(url).asBitmap().centerCrop().into(new RoundBitmapImageViewTarget(imageView));
    }


    public static void loadCircleImage(final Context context, ImageView imageView, int resId) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        // 小图标
        Glide.with(context.getApplicationContext()).load(resId).asBitmap().centerCrop().into(new RoundBitmapImageViewTarget(imageView));
    }

    public static void loadCircleImage(final Context context, ImageView imageView, String url, @DrawableRes int placeHolder) {
        loadCircleImage(context, imageView, url, placeHolder, 0);
    }

    public static void loadCircleImage(final Context context, ImageView imageView, String url, @DrawableRes int placeHolder, final int radius){
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        // 小图标
        Glide.with(context.getApplicationContext()).load(url).asBitmap().centerCrop().placeholder(placeHolder).into(new RoundBitmapImageViewTarget(imageView,radius));
    }

    static class RoundBitmapImageViewTarget extends BitmapImageViewTarget{
        private int radius = 0;

        public RoundBitmapImageViewTarget(ImageView view) {
            super(view);
        }

        public RoundBitmapImageViewTarget(ImageView view, int radius){
            super(view);
            if (radius > 0){
                this.radius = radius;
            }
        }

        @Override
        protected void setResource(Bitmap resource) {
            if (getView() == null || getView().getContext() == null){
                return;
            }

            RoundedBitmapDrawable circularBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(getView().getContext().getResources(), resource);
            circularBitmapDrawable.setCircular(true);

            if (radius > 0){
                circularBitmapDrawable.setCornerRadius(radius);
            }

            getView().setImageDrawable(circularBitmapDrawable);
        }
    }

    public static void loadCircleImage(final Context context, final ImageView imageView, String url, final String path) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        Glide.with(context.getApplicationContext()).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                ImageUtil.saveBitmap(resource, path, Bitmap.CompressFormat.JPEG, 100);
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public static void loadCircleImage(Context context, ImageView imageView, String url, @DrawableRes int placeHolder, ImageManager.ImageType type) {
        loadCircleImage(context, imageView, StringUtil.resizeImageUrl(url, type), placeHolder);
    }

    public static void loadCircleImage(Context context, ImageView imageView, String url, ImageManager.ImageType type) {
        loadCircleImage(context, imageView, StringUtil.resizeImageUrl(url, type));
    }

    public static void loadCircleImage(Context context, ImageView imageView, File filepath) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        Glide.with(context.getApplicationContext()).load(filepath).asBitmap().centerCrop().into(new RoundBitmapImageViewTarget(imageView));
    }

    public static void loadImage(Context context, ImageView imageView, String url) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        String imgUrl = StringUtil.resizeImageUrl(url, ImageType.ORIGINAL);
        Glide.with(context.getApplicationContext()).load(imgUrl).into(imageView);
    }

    public static void loadImage(Context context, ImageView imageView, String url, ImageType type) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        Glide.with(context.getApplicationContext()).load(StringUtil.resizeImageUrl(url, type)).into(imageView);
    }

    public static void loadImage(Context context, String url, SimpleTarget<Bitmap> imageCallbck) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        Glide.with(context.getApplicationContext()).load(url).asBitmap().into(imageCallbck);
    }

    public static void pauseLoad(Context context) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        clearMemory(context.getApplicationContext());

        Glide.with(context.getApplicationContext()).pauseRequests();
    }

    public static void clearMemory(Context context){
        if (null == context){
            return;
        }

        Glide.get(context).clearMemory();
    }

    public static void resumeLoad(Context context) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        Glide.with(context.getApplicationContext()).resumeRequests();
    }

    public static void loadImage(Context context, ImageView imageView, String url, int defaultResourceId, ImageType type) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        Glide.with(context.getApplicationContext()).load(StringUtil.resizeImageUrl(url, type)).placeholder(defaultResourceId).into(imageView);
    }

    public static void loadImage(final Context context, ImageView imageView, String url, int placeHolder) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }
        Glide.with(context).load(url).asBitmap().centerCrop().placeholder(placeHolder).into(imageView);
    }

    public static void getImage(Context context, String url, ImageType type, SimpleTarget<Bitmap> imageCallbck) {
        if (null == context || context.getApplicationContext() == null){
            return;
        }

        Glide.with(context.getApplicationContext()).load(StringUtil.resizeImageUrl(url, type))
                .asBitmap()
                .into(imageCallbck);
    }
}
