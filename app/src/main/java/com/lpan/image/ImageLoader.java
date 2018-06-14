package com.lpan.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.lpan.image.cache.ImageCache;
import com.lpan.image.cache.LoadBitmapCallback;
import com.lpan.image.cache.LoadResult;
import com.lpan.image_loader.R;


/**
 * Created by lpan on 2018/6/14.
 */

public class ImageLoader {

    private static ImageLoader mImageLoader;

    private static ImageCache mImageCache;

    public static final int LOAD_URL_TAG = R.id.image;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                LoadResult result = (LoadResult) msg.obj;
                ImageView imageView = result.getImageView();
                Bitmap bitmap = result.getBitmap();
                String url = result.getUrl();
                if (bitmap != null && url.equals(imageView.getTag(LOAD_URL_TAG))) {
                    imageView.setImageBitmap(bitmap);
                }
            } else if (msg.what == 2) {

            }
        }
    };

    private ImageLoader() {
    }

    public static ImageLoader getInstance(Context context) {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader();
            mImageCache = new ImageCache(context);
        }
        return mImageLoader;
    }

    public void display(String url, final ImageView imageView, int reqWidth, int reqHeight) {
        imageView.setTag(LOAD_URL_TAG, url);
        mImageCache.loadBitmap(url, reqWidth, reqHeight, new LoadBitmapCallback() {
            @Override
            public void onLoadSuccess(Bitmap bitmap, String url) {
                Log.d("ImageLoader", "onLoadSuccess--------thread=" + Thread.currentThread().getName() + "   " + url);
                Message message = Message.obtain();
                message.what = 1;
                message.obj = new LoadResult(imageView, url, bitmap);
                mHandler.sendMessage(message);
            }

            @Override
            public void onLoadFail(String url) {
                Log.d("ImageLoader", "onLoadFail--------thread=" + Thread.currentThread().getName() + "   " + url);
                Message message = Message.obtain();
                message.what = 2;
                message.obj = new LoadResult(imageView, url, null);
                mHandler.sendMessage(message);
            }
        });

    }
}
