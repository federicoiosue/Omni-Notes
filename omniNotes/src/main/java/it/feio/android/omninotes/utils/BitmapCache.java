/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import it.feio.android.omninotes.utils.SimpleDiskCache.BitmapEntry;
import roboguice.util.Ln;

import java.io.File;
import java.io.IOException;


public class BitmapCache extends LruCache<String, Bitmap> {

    /**
     * Get max available VM memory, exceeding this amount will throw an OutOfMemory exception. Stored in kilobytes as
     * LruCache takes an int in its constructor. Use 1/4th of the available memory for this memory cache.
     */
    public static final int MEMORY_CACHE_DEFAULT_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024) / 4;

    /**
     * Default size of space used for store data on physical disk cache. 20 Megabytes.
     */
    private static final int DISK_CACHE_DEFAULT_SIZE = 1024 * 1024 * 20;

    private Context mContext;
    private SimpleDiskCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;


    /**
     * @param mContext           Context of the application.
     * @param maxMemorySize      Memory in kilobytes to be used. Negative values will allow the class to use its 
     *                           *                           defaults.
     * @param maxDiskSize        Memory in megabytes to be used. Negative values will allow the class to use its 
     *                           *                           defaults.
     * @param diskCacheDirectory Directory to use for disk cache data storage.
     */
    public BitmapCache(Context mContext, int maxMemorySize, int maxDiskSize, File diskCacheDirectory) {
        super(maxMemorySize <= 0 ? MEMORY_CACHE_DEFAULT_SIZE : maxMemorySize);
        this.mContext = mContext;
        InitCacheTask mInitCacheTask = new InitCacheTask(maxDiskSize <= 0 ? DISK_CACHE_DEFAULT_SIZE : maxDiskSize);
        mInitCacheTask.execute(diskCacheDirectory);
    }


    private class InitCacheTask extends AsyncTask<File, Void, Void> {

        private int maxDiskSize;


        public InitCacheTask(int maxDiskSize) {
            this.maxDiskSize = maxDiskSize;
        }


        @Override
        protected Void doInBackground(File... params) {

            synchronized (mDiskCacheLock) {
                int version = 0;
                try {
                    version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
                } catch (NameNotFoundException e) {
                    Ln.e("Error retrieving package name", e);
                }
                try {
                    mDiskLruCache = SimpleDiskCache.open(params[0], version, maxDiskSize);
                } catch (IOException | NullPointerException e) {
                    Ln.e("Error retrieving disk cache", e);
                }
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }


    @SuppressLint("NewApi")
    @Override
    protected int sizeOf(String key, Bitmap value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return value.getByteCount();
        }
        return value.getRowBytes() * value.getHeight();
    }


    public void addBitmap(String key, Bitmap bitmap) {
        // Add to memory cache as before
        if (get(key) == null) {
            if (key != null && bitmap != null) {
                put(key, bitmap);
            }
        }
        // Also add to disk cache
        synchronized (mDiskCacheLock) {
            try {
                if (mDiskLruCache != null && !mDiskLruCache.contains(key)) {
                    mDiskLruCache.put(key, BitmapHelper.getBitmapInputStream(bitmap));
                }
            } catch (IOException e) {
                Ln.e("Error managing diskk cache", e);
            }
        }
    }


    /**
     * Retrieval of bitmap from chache
     */
    public Bitmap getBitmap(String key) {

        // A first attempt is done with memory cache
        Bitmap bitmap = get(key);

        // If bitmap is not found a search into disk cache will be done
        if (bitmap == null) {

            synchronized (mDiskCacheLock) {
                // Wait while disk cache is started from background thread
                while (mDiskCacheStarting) {
                    try {
                        mDiskCacheLock.wait();
                    } catch (InterruptedException e) {
                        Ln.e("Error managing diskk cache", e);
                    }
                }
                if (mDiskLruCache != null) {
                    try {
                        BitmapEntry bitmapEntry = mDiskLruCache.getBitmap(key);
                        if (bitmapEntry != null) {
                            bitmap = bitmapEntry.getBitmap();
                        }
                    } catch (IOException e) {
                        Ln.e("Error retrieving bitmap from disk cache");
                    }
                }
            }
            if (bitmap != null) addBitmap(key, bitmap);
        }

        return bitmap;
    }
}
