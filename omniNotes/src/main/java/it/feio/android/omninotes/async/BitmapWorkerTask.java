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

package it.feio.android.omninotes.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.models.views.SquareImageView;
import it.feio.android.omninotes.utils.BitmapHelper;

import java.lang.ref.WeakReference;


public class BitmapWorkerTask extends AsyncTask<Attachment, Void, Bitmap> {

    private final int FADE_IN_TIME = 200;
    private final int QUALITY_FACTOR = 90;

    private final Context context;
    private final WeakReference<SquareImageView> imageViewReference;
    private int width;
    private int height;
    private boolean wasCached = true;
    private OnAttachingFileListener mOnAttachingFileErrorListener;
    private Attachment mAttachment;


    public BitmapWorkerTask(Context activity, SquareImageView imageView, int width, int height) {
        this.context = activity;
        imageViewReference = new WeakReference<>(imageView);
        this.width = width / 100 * QUALITY_FACTOR;
        this.height = height / 100 * QUALITY_FACTOR;
    }


    @Override
    protected Bitmap doInBackground(Attachment... params) {
        mAttachment = params[0];

        String path = mAttachment.getUri().getPath();
        // Creating a key based on path and thumbnail size to re-use the same
        // AsyncTask both for list and detail
        String cacheKey = path + width + height;

        // Fetch from cache if possible
        Bitmap bmp = OmniNotes.getBitmapCache().getBitmap(cacheKey);

        // Creates thumbnail
        if (bmp == null) {
            wasCached = false;
            bmp = BitmapHelper.getBitmapFromAttachment(context, mAttachment, width, height);
            if (bmp != null) {
                OmniNotes.getBitmapCache().addBitmap(cacheKey, bmp);
            }
        }
        return bmp;
    }


    // @Override
    // protected void onCancelled() {
    // // TODO Auto-generated method stub
    // super.onCancelled();
    // }

    // @Override
    // protected void onCancelled(Bitmap result) {
    // // TODO Auto-generated method stub
    // super.onCancelled(result);
    // }


    @Override
    protected void onPostExecute(Bitmap bitmap) {

        // Checks if has been canceled due to imageView recycling
        if (isCancelled()) {
            // bitmap = null;
            return;
        }

        // The bitmap will now be attached to view
        if (imageViewReference != null && bitmap != null) {
            final SquareImageView imageView = imageViewReference.get();

            // Checks if is still the task in charge to load image on that imageView
            if (imageView != null && this == imageView.getAsyncTask()) {

                // If the bitmap was already cached it will be directly attached to view
                if (wasCached) {
                    imageView.setImageBitmap(bitmap);
                }

                // Otherwise a fading transaction will be used to shot it
                else {
                    // Transition with transparent drawabale and the final bitmap
                    final TransitionDrawable td = new TransitionDrawable(
                            new Drawable[]{new ColorDrawable(Color.TRANSPARENT),
                                    new BitmapDrawable(context.getResources(), bitmap)});
                    if (td != null) {
                        imageView.setImageDrawable(td);
                        td.startTransition(FADE_IN_TIME);
                    }
                }
            }
        } else {
            if (this.mOnAttachingFileErrorListener != null) {
                mOnAttachingFileErrorListener.onAttachingFileErrorOccurred(mAttachment);
            }
        }
    }


    public void setOnErrorListener(OnAttachingFileListener listener) {
        this.mOnAttachingFileErrorListener = listener;
    }


    public Attachment getAttachment() {
        return mAttachment;
    }

}
