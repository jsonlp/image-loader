package com.lpan.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * Created by lpan on 2018/6/14.
 */

public class ImageResize {

    public Bitmap decodeBitmap(Resources resources, int resId, int requireWidth, int requireHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);
        options.inSampleSize = calculateInSampleSize(options, requireWidth, requireHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    public Bitmap decodeBitmap(FileDescriptor file, int requireWidth, int requireHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(file, null, options);
        options.inSampleSize = calculateInSampleSize(options, requireWidth, requireHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(file, null, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int requireWidth, int requireHeight) {
        if (requireWidth == 0 || requireHeight == 0) {
            return 1;
        }
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        Log.d("ImageResize", "calculateInSampleSize--------origin width=" + outWidth + "  height=" + outHeight);
        int inSampleSize = 1;

        if (outWidth > requireWidth || outHeight > requireHeight) {
            int halfWidth = outWidth / 2;
            int halfHeight = outHeight / 2;
            while (halfWidth / inSampleSize >= requireWidth && halfHeight / inSampleSize >= requireHeight) {
                inSampleSize *= 2;
            }

        }
        Log.d("ImageResize", "calculateInSampleSize--------inSampleSize=" + inSampleSize);
        return inSampleSize;
    }
}
