package com.lpan.image.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;


import com.lpan.image.ImageResize;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lpan on 2018/6/14.
 */

public class ImageCache {
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private ImageResize mImageResize;
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int MAX_DISK_SIZE = 50 * 1024 * 1024;
    private static final int DISK_CACHE_INDEX = 0;

    private ExecutorService mExecutor;

    public ImageCache(Context context) {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mImageResize = new ImageResize();
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        File file = CacheUtil.getDiskCacheDir(context);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (CacheUtil.getStorageAvailableSize() >= MAX_DISK_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(file, 1, 1, MAX_DISK_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mExecutor = Executors.newCachedThreadPool();
    }

    public void loadBitmap(final String url, final int reqWidth, final int reqHeight, final LoadBitmapCallback callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String key = CacheUtil.stringToMD5(url);
        Bitmap bitmapMemoryCache = getBitmapMemoryCache(key);
        if (bitmapMemoryCache != null) {
            if (callback != null) {
                Log.d("ImageCache", "loadBitmap--------load from memory---" + url);
                callback.onLoadSuccess(bitmapMemoryCache, url);
            }
        } else {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (callback != null) {
                            Bitmap bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
                            if (bitmap == null) {
                                Log.d("ImageCache", "loadBitmap--------load from http---" + url);
                                bitmap = loadBitmapFromHttp(url, reqWidth, reqHeight);
                            } else {
                                Log.d("ImageCache", "loadBitmap--------load from disk---" + url);
                            }
                            if (bitmap != null) {
                                callback.onLoadSuccess(bitmap, url);
                            } else {
                                callback.onLoadFail(url);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            mExecutor.execute(task);
        }
    }


    private void addBitmapToMemory(String key, Bitmap bitmap) {
        if (getBitmapMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        if (mDiskLruCache == null) {
            return null;
        }
        String key = CacheUtil.stringToMD5(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downloadToStream(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        String key = CacheUtil.stringToMD5(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResize.decodeBitmap(fileDescriptor, reqWidth, reqHeight);
            if (bitmap != null) {
                addBitmapToMemory(key, bitmap);
            }
        }
        return bitmap;
    }

    private boolean downloadToStream(String url, OutputStream outputStream) {
        HttpURLConnection connection = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            URL urlStr = new URL(url);
            connection = (HttpURLConnection) urlStr.openConnection();
            in = new BufferedInputStream(connection.getInputStream());
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ImageCache", "downloadToStream--------" + e.getMessage() + "  url=" + url);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            CacheUtil.closeQuietly(in);
            CacheUtil.closeQuietly(out);

        }
        return false;
    }

}
