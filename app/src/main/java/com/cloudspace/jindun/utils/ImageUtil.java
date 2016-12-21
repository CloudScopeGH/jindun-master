package com.cloudspace.jindun.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.view.View;

import com.cloudspace.jindun.R;
import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.ext.aq.AQUtility;
import com.cloudspace.jindun.net.manager.ImageManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {
    public final static int IMAGE_MAX_SIZE = 320;

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static BitmapDrawable byteArrayToDrawable(byte[] image) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length, opts);
        return new BitmapDrawable(bmp);
    }

    public static void measureView(View v) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] result = baos.toByteArray();
        try {
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] bitmap2PngBytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] result = baos.toByteArray();
        try {
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setBackgroundColor(UCAPIApp.getInstance().getResources().getColor(android.R.color.transparent));
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        Bitmap btm = null;
        if (bitmap != null) {
            btm = bitmap.copy(Config.ARGB_8888, false);
            bitmap.recycle();
        }
        view.destroyDrawingCache();
        return btm;
    }

    public static Bitmap getBitmapFromView2(View v) {
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        // creates immutable clone
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());

        v.setDrawingCacheEnabled(false); // clear drawing cache
        return b;
    }
    /*public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}*/

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static byte[] fileToByteArray(String jpgPath) {
        byte[] imgbuff = null;
        try {
            FileInputStream is = new FileInputStream(jpgPath);
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            is.close();
            imgbuff = outSteam.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgbuff;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius, float rect) {
        if (bitmap != null && rect > 0 && rect < 1) {//图片是矩形 长度:高度< rect的时候
            int startX = 0;
            int startY = 0;
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            if (height <= width && height > width * rect) {
                startY = (int) (height * (1 - rect) / 3);
                height = (int) (height * rect);
                bitmap = Bitmap.createBitmap(bitmap, startX, startY, width, height);
            } else if (width <= height && width > height * rect) {
                startX = (int) (width * (1 - rect) / 3);
                width = (int) (width * rect);
                bitmap = Bitmap.createBitmap(bitmap, startX, startY, width, height);
            }
        }
        return getRoundedCornerBitmap(bitmap, radius);
    }


    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        BitmapShader mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final float roundPx = bitmap.getWidth() / 2;
        paint.setAntiAlias(true);
        paint.setShader(mBitmapShader);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        return output;
    }

    //radiusRate 0 -100 代表圆弧直径 .基准图片的长宽的 200

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radiusRate) {
        if (radiusRate == 360) {
            return getRoundedBitmap(bitmap);
        }
        if (bitmap == null) {
            return null;
        }
        if (radiusRate <= 0) {
            return bitmap;
        }
        try {
            int width = 0;
            int height = 0;
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            if (radiusRate > 50 || width < 60 || height < 60) {//radius 比较大，图片越园。长方形图片剪切成正方形。图片过小小于60*60像素，剪切成正方形
                int detal = width - height;
                int startX = 0;
                int startY = 0;
                if (detal > 0) {
                    width = height;
                    startX = detal / 2;
                    bitmap = Bitmap.createBitmap(bitmap, startX, startY, width, height);
                } else if (detal < 0) {
                    height = width;
                    startY = Math.abs(detal / 2);
                    bitmap = Bitmap.createBitmap(bitmap, startX, startY, width, height);
                }
            }
            Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            if (output == null) {
                return bitmap;
            }
            Canvas canvas = new Canvas(output);

            final int color = UCAPIApp.getInstance().getResources()
                    .getColor(R.color.base_color_gray2);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, width, height);
            final RectF rectF = new RectF(rect);
            final float roundPx = Math.min(height, width) * radiusRate / 100;//roundPx 水平圆角半径和垂直圆角半径

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 回收图片
     *
     * @param bitmap 需要被回收的图片
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * 保存图片
     *
     * @param bitmap          保存的图
     * @param savePath        保存的路径
     * @param format          保存的格式，png和jpg两种
     * @param compressQuality 保存的质量 0到100，100为最佳
     */
    public static byte[] saveBitmap(Bitmap bitmap, String savePath, Bitmap.CompressFormat format, int compressQuality) {
        FileOutputStream fos = null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (bitmap != null) {
                bitmap.compress(format, compressQuality, baos);
                byte[] result = baos.toByteArray();
                //写文件
                fos = new FileOutputStream(savePath, false);
                if (fos != null) {
                    fos.write(result);
                }
                //返回数据
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                    fos = null;
                }
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 从文件解析出Bitmap格式的图片 并压缩
     *
     * @param path
     * @return
     */
    public static Bitmap compressImage(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        Bitmap image = BitmapFactory.decodeFile(path,
//                options);// 此时返回bm为空

        options.inJustDecodeBounds = false;
        Bitmap image = BitmapFactory.decodeFile(path, options);
        if (image == null) return null;

        Bitmap bitmap = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int option = 100;
            int bytes = baos.toByteArray().length / 1024;

            if (bytes > 800) {
                int rate = (int) ((800.0f / bytes) * 100);
                baos.reset();
                image.compress(Bitmap.CompressFormat.JPEG, rate, baos);//这里压缩options%，把压缩后的数据存放到baos中
            }
            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
            bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        } catch (Exception e) {
            bitmap = image;
        }
        return bitmap;
    }

    /*
     *
     * 读取并获得适合大小的图片,降低分辨率避免图片太大
     */
    public static Bitmap getBitmapByUrl(String imagePath) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeFile(imagePath, options);
            if (options.outWidth < 0) {
                return bitmap;
            }
            options.inSampleSize = sampleSize(options.outWidth, IMAGE_MAX_SIZE);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(imagePath, options);

        } catch (OutOfMemoryError err) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } catch (Exception e) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        return bitmap;
    }

    public static void updateSysContentURI(Context context, File file) {
        if (file == null) return;
        try {
            ContentValues values = new ContentValues(7);
            values.put(Images.Media.TITLE, file.getName());
            values.put(Images.Media.DISPLAY_NAME, file.getName());
            values.put(Images.Media.MIME_TYPE, "image/png");
            values.put(Images.Media.DATA, file.getAbsolutePath());
            values.put(Images.Media.SIZE, file.length());
            context.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public static boolean isVaildBitmap(Bitmap bmp) {
        if (bmp == null || bmp.isRecycled()) {
            return false;
        }
        return true;
    }

    public static Bitmap decodeFile(String path, BitmapFactory.Options options) {

        Bitmap result = null;

        if (options == null) {
            options = new BitmapFactory.Options();
        }

        options.inInputShareable = true;
        options.inPurgeable = true;


        FileInputStream fis = null;

        try {

            fis = new FileInputStream(path);

            FileDescriptor fd = fis.getFD();

            //AQUtility.debug("decoding file");
            //AQUtility.time("decode file");

            result = BitmapFactory.decodeFileDescriptor(fd, null, options);

            //AQUtility.timeEnd("decode file", 0);
        } catch (IOException e) {
            AQUtility.report(e);
        } finally {
            AQUtility.close(fis);
        }

        return result;

    }

    public static int sampleSize(int width, int target) {

        int result = 1;

        for (int i = 0; i < 10; i++) {

            if (width < target * 2) {
                break;
            }

            width = width / 2;
            result = result * 2;

        }
        return result;
    }

    public static Bitmap cropBitmap(Bitmap firstBitmap, Bitmap secondBitmap) {
        int width = firstBitmap.getWidth() > secondBitmap.getWidth() ? firstBitmap.getWidth() : secondBitmap.getWidth();
        int height = firstBitmap.getHeight() > secondBitmap.getHeight() ? firstBitmap.getHeight() : secondBitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, firstBitmap.getConfig());
        Bitmap newFirst = Bitmap.createBitmap(firstBitmap, firstBitmap.getWidth() / 4, 0, firstBitmap.getWidth() / 2, firstBitmap.getHeight());
        Bitmap newSecond = Bitmap.createBitmap(secondBitmap, secondBitmap.getWidth() / 4, 0, secondBitmap.getWidth() / 2, secondBitmap.getHeight());
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(newFirst, 0, 0, null);
        canvas.drawBitmap(newSecond, width / 2, 0, null);
        return newBitmap;
    }

    public static Bitmap cropBitmap(Bitmap source, int vwidth, int vheight) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (vwidth == 0) {
            return source;
        }
        int toheight = vheight * width / vwidth;
        if (height <= 0 || width <= 0) {
            return source;
        }
        if (toheight > height) {
            return source;
        }
        source = Bitmap.createBitmap(source, 0, 0, width, toheight, null, false);
        return source;
    }


    public static Bitmap getThumbnail(String path) {
        // 以下是把图片转化为缩略图再加载
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回bitmap为空
        options.inJustDecodeBounds = false;
        int be = options.outWidth > (UCAPIApp.getInstance().getScreenWidth() / 2) ? 5 : 2;
        options.inSampleSize = be;
        return BitmapFactory.decodeFile(path, options); // 返回缩略图
    }

    public static Bitmap getScaledBitmap(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = UCAPIApp.getInstance().getScreenHeight();
        float ww = UCAPIApp.getInstance().getScreenWidth();
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置采样率
        //其实是无效的,大家尽管尝试
        return BitmapFactory.decodeFile(srcPath, newOpts); // 返回缩略图
    }


    public static Bitmap getGuassianBlurBitmapOptimized(Bitmap bitmap, int x, int y, int width, int height,
                                                        boolean hasSemitransparentBlack) {
        return getGuassianBlurBitmapOptimizedWithAlpha(bitmap, x, y, width, height, hasSemitransparentBlack, 0.8f);
    }

    public static Bitmap getGuassianBlurBitmapOptimizedWithAlpha(Bitmap bitmap, int x, int y, int width, int height,
                                                                 boolean hasSemitransparentBlack, float alpha) {
        // long start = System.currentTimeMillis();
        Bitmap output = null;
        try {
            if (bitmap == null || bitmap.isRecycled()) {
                throw new IllegalArgumentException("Bitmap is invliad!");
            }
            if ((x < 0 || y < 0) || (width <= 0 || height <= 0)
                    || (x + width > bitmap.getWidth() || y + height > bitmap.getHeight())) {
                throw new IllegalArgumentException("One or more bitmap parameters are invliad!");
            }
            Bitmap src = null;
            float weight[] = null;
            boolean isGreaterThanHDPI = UCAPIApp.getInstance().getResources()
                    .getDisplayMetrics().densityDpi > DisplayMetrics.DENSITY_HIGH;// XHDPI及以上
            if (isGreaterThanHDPI) {
                src = bitmap;
                weight = new float[]{1.0f / 262144.0f, 18.0f / 262144.0f, 153.0f / 262144.0f, 816.0f / 262144.0f,
                        3060.0f / 262144.0f, 8568.0f / 262144.0f, 18564.0f / 262144.0f, 31824.0f / 262144.0f,
                        43758.0f / 262144.0f, 48620.0f / 262144.0f, 43758.0f / 262144.0f, 31824.0f / 262144.0f,
                        18564.0f / 262144.0f, 8568.0f / 262144.0f, 3060.0f / 262144.0f, 816.0f / 262144.0f,
                        153.0f / 262144.0f, 18.0f / 262144.0f, 1.0f / 262144.0f};
            } else {
                int scaleFactor = 2;
                width /= scaleFactor;
                height /= scaleFactor;
                x /= scaleFactor;
                y /= scaleFactor;

                src = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / scaleFactor, bitmap.getHeight()
                        / scaleFactor, true);
                weight = new float[]{3.0f / 16.0f, 3.0f / 16.0f, 4.0f / 16.0f, 3.0f / 16.0f, 3.0f / 16.0f,};
            }
            output = Bitmap.createBitmap(width, height, Config.ARGB_8888);

            final int sampleRadius = weight.length / 2;

            int[] pixels = new int[width * height * 4];
            int srcColor = 0;
            int finalColor = 0;
            int index = 0;
            int weightIndex = 0;

            // Horizontal PASS
            for (int j = y; j < y + height; j++) {
                for (int i = x; i < x + width; i++) {
                    finalColor = 0;
                    index = (j - y) * width + (i - x);
                    for (int k = i - sampleRadius; k <= i + sampleRadius; k++) {
                        int newk = k;
                        // If index out of bound,reset the index to its
                        // symmetrical value
                        if (k < x || k >= x + width) {
                            newk = 2 * i - k;
                        }

                        weightIndex = k - i + sampleRadius;
                        srcColor = src.getPixel(newk, j);
                        finalColor += adjustARGB8888Luminance(srcColor, weight[weightIndex], false);
                    }
                    pixels[index] = finalColor;
                }
            }

            output.setPixels(pixels, 0, width, 0, 0, width, height);

            // Vertical PASS
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    finalColor = 0;
                    index = j * width + i;
                    for (int k = j - sampleRadius; k <= j + sampleRadius; k++) {
                        int newk = k;
                        // If index out of bound,reset the index to its
                        // symmetrical value
                        if (k < 0 || k >= height) {
                            newk = 2 * j - k;
                        }

                        weightIndex = k - j + sampleRadius;

                        finalColor += adjustARGB8888Luminance(output.getPixel(i, newk), weight[weightIndex], false);
                    }
                    if (hasSemitransparentBlack) {
                        pixels[index] = adjustARGB8888Luminance(finalColor, alpha, true);
                    } else {
                        pixels[index] = finalColor;
                    }
                    // pixels[index] = finalColor;
                }
            }

            output.setPixels(pixels, 0, width, 0, 0, width, height);
            pixels = null;
            // MLog.w("Util#####", "getGuassianBlurBitmap time=" +
            // (System.currentTimeMillis() - start));
        } catch (Exception e) {
        } catch (OutOfMemoryError e) {

        }
        return output;
    }

    public static int adjustARGB8888Luminance(int color, float factor, boolean isAlphaIgnored) {
        int a = (color >> 24) & 0xff;
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;

        if (!isAlphaIgnored) {
            a *= factor;
        }
        r *= factor;
        g *= factor;
        b *= factor;

        color = (a << 24) | (r << 16) | (g << 8) | b;
        return color;
    }


    public static Bitmap getExportedBitmap(Bitmap bitmap, float rect, int radius) {
        if (radius != ImageManager.ImageRadius.ROUND.getRadius()) {
            return getRoundedCornerBitmap(bitmap, radius, rect);
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            left = 0;
            top = 0;
            right = width;
            bottom = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        if (output == null) {
            return bitmap;
        }
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);// 设置画笔无锯齿

        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
        paint.setColor(color);

        // 以下有两种方法画圆,drawRounRect和drawCircle
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
//        canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output;
    }
}
