package it.feio.android.omninotes.async;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.OnAttachingFileErrorListener;
import it.feio.android.omninotes.models.SquareImageView;
import it.feio.android.omninotes.utils.BitmapHelper;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapWorkerTask extends
		AsyncTask<Attachment, Void, Bitmap> {

	private final int FADE_IN_TIME = 170;
	
	private final Activity mActivity;
	private final WeakReference<SquareImageView> imageViewReference;
	private int width;
	private int height;
	private boolean wasCached = true;
	private OnAttachingFileErrorListener mOnAttachingFileErrorListener;
	private Attachment mAttachment;
	

	public BitmapWorkerTask(Activity activity, SquareImageView imageView,
			int width, int height) {
		this.mActivity = activity;
		imageViewReference = new WeakReference<SquareImageView>(imageView);
		this.width = width;
		this.height = height;
	}

	@Override
	protected Bitmap doInBackground(Attachment... params) {
		Bitmap bmp = null;
		mAttachment = params[0];
		
		// If possible, auto-calculated sized replace the requested
//		if (imageViewReference.get() != null) {
//			int widthCalc = imageViewReference.get().getWidth();
//			int heightCalc = imageViewReference.get().getHeight();
//			this.width = widthCalc != 0 ? widthCalc : width;
//			this.height = heightCalc != 0 ? heightCalc : height;			
//		}
		
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
			if (bmp != null) {
				app.addBitmapToCache(cacheKey, bmp);
			}
		}

		return bmp;
	}

	
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		
		// Checks if has been canceled due to imageView recycling
		if (isCancelled()) {
//			bitmap = null;
            return;
        }
		
		// The bitmap will now be attached to view
		if (imageViewReference != null && bitmap != null) {
			final SquareImageView imageView = imageViewReference.get();
			
			// Checks if is still the task in charge to load image on that imageView
			if (imageView != null && this == imageView.getBitmapWorkerTask()) {
				
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
		}  else {
			if (this.mOnAttachingFileErrorListener != null) {
				mOnAttachingFileErrorListener.onAttachingFileErrorOccurred(mAttachment);
			}
		}
	}
	
	
	
	
	public void setOnErrorListener(OnAttachingFileErrorListener listener) {
		this.mOnAttachingFileErrorListener = listener;
	}

	public Attachment getAttachment() {
		return mAttachment;
	}

}