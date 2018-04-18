/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.models;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;


public class ThumbnailLruCache extends LruCache<String, Bitmap> {

    // Calculates available device memory
    final static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    final static int cacheSize = maxMemory / 8;

    private static ThumbnailLruCache instance = null;

    private static LruCache<String, Bitmap> mMemoryCache;


    public ThumbnailLruCache(int maxSize) {
        super(maxSize);
    }


    public static synchronized ThumbnailLruCache getInstance() {
        if (instance == null) {
            instance = new ThumbnailLruCache(cacheSize);
        }

        mMemoryCache = new LruCache<>(cacheSize);

        return instance;
    }


    public void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }


    public Bitmap getBitmap(String key) {
        return mMemoryCache.get(key);
    }

}
