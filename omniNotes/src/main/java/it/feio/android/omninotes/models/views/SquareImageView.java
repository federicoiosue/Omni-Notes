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

package it.feio.android.omninotes.models.views;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.ref.WeakReference;


public class SquareImageView extends ImageView {

    private WeakReference<AsyncTask<?, ?, ?>> mAsyncTaskReference;


    public SquareImageView(Context context) {
        super(context);
        setScaleType(ScaleType.CENTER_CROP);
    }


    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }


    public void setAsyncTask(AsyncTask<?, ?, ?> mAsyncTask) {
        this.mAsyncTaskReference = new WeakReference<AsyncTask<?, ?, ?>>(mAsyncTask);
    }


    public AsyncTask<?, ?, ?> getAsyncTask() {
        if (mAsyncTaskReference != null) {
            return mAsyncTaskReference.get();
        } else {
            return null;
        }
    }
}
