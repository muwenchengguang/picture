package com.peng.jpegviewer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import java.io.InputStream;

/**
 * Created by richie on 2017/8/7.
 */

public class BitmapUtils {
    private static float STANDARD_HEIGHT = 1920.0f;

    public static float retriveScreenRatio(View view) {
        int widthPixels = view.getResources().getDisplayMetrics().widthPixels;
        int heightPixels = view.getResources().getDisplayMetrics().heightPixels;
        float actualHeight = (widthPixels > heightPixels ? widthPixels : heightPixels);
        return (actualHeight/STANDARD_HEIGHT);
    }

    public static String retriveImageScreenResolution(View view, int resId) {
        return retriveImageScreenResolution(view, view.getResources().openRawResource(resId));
    }

    public static String retriveImageScreenResolution(View view, BitmapDrawable drawable) {
        float ratio = retriveScreenRatio(view);
        float density = view.getResources().getDisplayMetrics().density;
        int srcWidth =  drawable.getBitmap().getWidth();
        int srcHeight = drawable.getBitmap().getHeight();
        int dw = (int) (srcWidth*ratio/density);
        int dh = (int) (srcHeight*ratio/density);
        return "" + dw + "x" + dh;
    }

    private static String retriveImageScreenResolution(View view, InputStream is) {
        float ratio = retriveScreenRatio(view);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;
        int dw = (int) (srcWidth*ratio);
        int dh = (int) (srcHeight*ratio);
        return "" + dw + "x" + dh;
    }

    private static Bitmap decode(InputStream is, BitmapFactory.Options options) {
        Bitmap sampledSrcBitmap;
        sampledSrcBitmap = BitmapFactory.decodeStream(is, null, options);
        return sampledSrcBitmap;
    }

    public static Bitmap decode(InputStream is) {
        return decode(is, null);
    }

    public static Bitmap decode(InputStream is, int width, int height) {
        BitmapFactory.Options options = generateSuitableOption(is, width, height);
        return decode(is, options);
    }

    private static BitmapFactory.Options generateSuitableOption(InputStream is, int dstWidth, int dstHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;
        if (srcWidth <= 0 || srcHeight <= 0) {
            return null;
        }
        float pictureRatio = srcWidth/(float)srcHeight;
        boolean horizontal = (pictureRatio >= 1f?true:false);
        int maxWidth = horizontal?dstWidth:dstHeight;
        int maxHeight = horizontal?dstHeight:dstWidth;
        float decentRatio = horizontal?(maxWidth/(float)maxHeight):(maxHeight/(float)maxWidth);
        boolean needScale = false;
        if (srcWidth > maxWidth || srcHeight > maxHeight) {
            needScale = true;
        }
        int inSampleSize = 1;
        int scaleW = srcWidth;
        int scaleH = srcHeight;
        if (needScale) {
            if (pictureRatio > decentRatio) {
                scaleW = maxWidth;
                scaleH = (int)(scaleW/pictureRatio);
            } else {
                scaleH = maxHeight;
                scaleW = (int)(scaleH*pictureRatio);
            }
            while (srcWidth > scaleW) {
                srcWidth /= 2;
                inSampleSize *= 2;
            }
        }

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPurgeable = true;
        return options;
    }
}
