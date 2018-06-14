package com.lpan.image.cache;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by lpan on 2018/6/14.
 */

public class LoadResult {
    private ImageView mImageView;

    private String mUrl;

    private Bitmap mBitmap;

    public LoadResult(ImageView imageView, String url, Bitmap bitmap) {
        mImageView = imageView;
        mUrl = url;
        mBitmap = bitmap;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }
}
