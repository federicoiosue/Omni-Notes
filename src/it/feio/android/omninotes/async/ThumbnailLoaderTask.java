package it.feio.android.omninotes.async;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;
import it.feio.android.omninotes.utils.date.DateHelper;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.widget.ImageView;

public class ThumbnailLoaderTask extends
		AsyncTask<Attachment, Void, Bitmap> {

	private final int FADE_IN_TIME = 150;
	
	private final Activity mActivity;
	private final WeakReference<ImageView> imageViewReference;
	private int width;
	private int height;
	private boolean wasCached = true;

	public ThumbnailLoaderTask(Activity activity, ImageView imageView,
			int width, int height) {
		this.mActivity = activity;
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.width = width;
		this.height = height;
	}

	@Override
	protected Bitmap doInBackground(Attachment... params) {
		Bitmap bmp = null;
		Attachment mAttachment = params[0];

		String path = mAttachment.getUri().getPath();		
		// Creating a key based on path and thumbnail size to re-use the same
		// AsyncTask both for list and detail
		String cacheKey = path + width + height;

		// Requesting from Application an instance of the cache
		OmniNotes app = ((OmniNotes) mActivity.getApplication());


		// Fetch from cache if possible
		bmp = app.getBitmapFromCache(cacheKey);

		// Creates thumbnail	
		if (bmp == null) {		
			wasCached = false;
			bmp = BitmapHelper.getBitmapFromAttachment(mActivity, mAttachment, width, height);

			app.addBitmapToCache(cacheKey, bmp);			
		}

		return bmp;
	}

	
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		// The bitmap will now be attached to view
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				
				// If the bitmap was already cached it will be directly attached to view
				if (wasCached) {
					imageView.setImageBitmap(bitmap);				
				} 
				
				// Otherwise a fading transaction will be used to shot it
				else { 
					// Transition with transparent drawabale and the final bitmap
					final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
							new ColorDrawable(Color.TRANSPARENT), new BitmapDrawable(mActivity.getResources(), bitmap) });	
					if (td != null) {
						imageView.setImageDrawable(td);
						td.startTransition(FADE_IN_TIME);
					}
				}
			}
		}
	}
	

}