package it.feio.android.omninotes.async;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.widget.ImageView;

public class BitmapLoaderTask extends AsyncTask<Attachment, Void, Bitmap> {

	private final Context mContext;
	private final WeakReference<ImageView> imageViewReference;
	private int reqWidth;
	private int reqHeight;

	public BitmapLoaderTask(Context ctx, ImageView imageView) {
		this.mContext = ctx;
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(Attachment... params) {
		Bitmap bmp = null;
		Attachment mAttachment = params[0];
		reqWidth = Constants.THUMBNAIL_SIZE;
		reqHeight = Constants.THUMBNAIL_SIZE;

		try {
			// Video
			if (Constants.MIME_TYPE_VIDEO.equals(mAttachment.getMime_type())) {
				// Tries to retrieve full path from ContentResolver if is a new video
				String path = StorageManager.getRealPathFromURI(mContext, mAttachment.getUri());
				// .. or directly from local directory otherwise
				if (path == null) 
					path = mAttachment.getUri().getPath();
				bmp = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
				bmp = createVideoThumbnail(bmp);
			
			// Image
			} else if (Constants.MIME_TYPE_IMAGE.equals(mAttachment.getMime_type())) {				
				try {
					bmp = checkIfBroken(BitmapHelper.decodeSampledFromUri(mContext, mAttachment.getUri(), reqWidth, reqHeight));
				} catch (NullPointerException e) {
					bmp = checkIfBroken(null);
				}
			
			// Audio
			} else if (Constants.MIME_TYPE_AUDIO.equals(mAttachment.getMime_type())) {
				bmp = ThumbnailUtils.extractThumbnail(
						BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play),
						Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE);
				bmp = BitmapHelper.drawTextToBitmap(mContext, bmp, 
							mAttachment.getUri().getLastPathSegment(), null, -10, 
							10, mContext.getResources().getColor(R.color.text_gray));
			}
		} catch (FileNotFoundException e) {
			Log.e(Constants.TAG, "Image not found");
		}
		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference.get() != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	/**
	 * Draws a watermark on ImageView to highlight videos
	 * 
	 * @param bmp
	 * @param overlay
	 * @return
	 */
	public Bitmap createVideoThumbnail(Bitmap video) {
		Bitmap mark = ThumbnailUtils.extractThumbnail(
				BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play_white), Constants.THUMBNAIL_SIZE,
				Constants.THUMBNAIL_SIZE);
		Bitmap thumbnail = Bitmap.createBitmap(Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(thumbnail);
		Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		
		canvas.drawBitmap(checkIfBroken(video), 0, 0, null);
		canvas.drawBitmap(mark, 0, 0, null);

		return thumbnail;
	}

	private Bitmap checkIfBroken(Bitmap bmp) {

		// In case no thumbnail can be extracted from video 
		if (bmp == null) {
			bmp = ThumbnailUtils.extractThumbnail(
					BitmapFactory.decodeResource(mContext.getResources(), R.drawable.attachment_broken), Constants.THUMBNAIL_SIZE,
					Constants.THUMBNAIL_SIZE);
		}
		return bmp;
	}
	
	
	
	
}