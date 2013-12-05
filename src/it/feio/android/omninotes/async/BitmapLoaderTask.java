package it.feio.android.omninotes.async;

import it.feio.android.omninotes.utils.BitmapDecoder;
import it.feio.android.omninotes.utils.Constants;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class BitmapLoaderTask extends AsyncTask<Uri, Void, Bitmap> {

	private final Context mContext;
	private final WeakReference<ImageView> imageViewReference;
	private int reqWidth;
	private int reqHeight;

	public BitmapLoaderTask(Context ctx, ImageView imageView) {
		this.mContext = ctx;
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(Uri... params) {
		Bitmap bmp = null;
		Uri uri = params[0];
		reqWidth = Constants.THUMBNAIL_SIZE;
		reqHeight = Constants.THUMBNAIL_SIZE;

		try {
			bmp = BitmapDecoder.decodeSampledFromUri(mContext, uri, reqWidth,
					reqHeight);
		} catch (FileNotFoundException e) {
			Log.e(Constants.TAG, "Image not found");
		}
		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}
}