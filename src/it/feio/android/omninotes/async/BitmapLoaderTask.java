package it.feio.android.omninotes.async;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.utils.BitmapDecoder;
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
				bmp = BitmapDecoder.decodeSampledFromUri(mContext, mAttachment.getUri(), reqWidth, reqHeight);
			
			// Audio
			} else if (Constants.MIME_TYPE_AUDIO.equals(mAttachment.getMime_type())) {
				bmp = ThumbnailUtils.extractThumbnail(
						BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play),
						Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE);
//				Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_play);
			}
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

		canvas.drawBitmap(video, 0, 0, null);
		canvas.drawBitmap(mark, 0, 0, null);

		return thumbnail;
	}
}