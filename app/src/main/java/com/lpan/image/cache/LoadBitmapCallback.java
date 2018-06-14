package com.lpan.image.cache;

import android.graphics.Bitmap;

/**
 * Created by lpan on 2018/6/14.
 */

public interface LoadBitmapCallback {

    void onLoadSuccess(Bitmap bitmap, String url);

    void onLoadFail(String url);
}
