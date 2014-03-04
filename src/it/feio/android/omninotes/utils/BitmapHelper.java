/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.utils.date.DateHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;


public class BitmapHelper {

	/**
	 * Decodifica ottimizzata per la memoria dei bitmap
	 * 
	 * @param uri
	 *            URI bitmap
	 * @param reqWidth
	 *            Larghezza richiesta
	 * @param reqHeight
	 *            Altezza richiesta
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Bitmap decodeSampledFromUri(Context ctx, Uri uri, int reqWidth, int reqHeight)
			throws FileNotFoundException {

		// Decoding con inJustDecodeBounds=true per controllare le dimensioni evitando errori di memoria
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, options);

		// Calcolo dell'inSampleSize e delle nuove dimensioni proporzionate
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {			
			final int halfHeight = height / 2;
	        final int halfWidth = width / 2;

	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
		}

		// Setting decode options
		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;

		// Bitmap is now decoded for real using calculated inSampleSize
		Bitmap bmp = BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, options);
		return bmp;
	}
	
	
	public static Uri getUri(Context mContext, int resource_id) {
		Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + resource_id);
		return uri;
	}
	
	
	
	/**
	 * Creates a thumbnail of requested size by doing a first sampled decoding of the bitmap to optimize memory
	 * @param ctx
	 * @param uri
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Bitmap getThumbnail(Context ctx, Uri uri, int reqWidth, int reqHeight) {
		Bitmap srcBmp;
		Bitmap dstBmp = null;
		try {
			srcBmp = decodeSampledFromUri(ctx, uri, reqWidth, reqHeight);
			// Cropping
			int x = ( srcBmp.getWidth() - reqWidth )/2;
			int y = ( srcBmp.getHeight() - reqHeight )/2;
			if (x > 0 && y > 0) {
				dstBmp = Bitmap.createBitmap(srcBmp, x, y, reqWidth, reqHeight);		
			} else {
				dstBmp = ThumbnailUtils.extractThumbnail(srcBmp, reqWidth, reqHeight);
			}
		} catch (FileNotFoundException e) {
			Log.e(Constants.TAG, "Missing attachment file: " + uri.getPath());
		}
		
		return dstBmp;
	}
	
	
	
	/**
	 * To avoid problems with rotated videos retrieved from camera
	 * @param bitmap
	 * @param filePath
	 * @return
	 */
	public static Bitmap rotateImage(Bitmap bitmap, String filePath) {
		Bitmap resultBitmap = bitmap;

		try {
			ExifInterface exifInterface = new ExifInterface(filePath);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

			Matrix matrix = new Matrix();

			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				matrix.postRotate(ExifInterface.ORIENTATION_ROTATE_90);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				matrix.postRotate(ExifInterface.ORIENTATION_ROTATE_180);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				matrix.postRotate(ExifInterface.ORIENTATION_ROTATE_270);
			}

			// Rotate the bitmap
			resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		} catch (Exception exception) {
			Log.d(Constants.TAG, "Could not rotate the image");
		}
		return resultBitmap;
	}
	

	
	
	/**
	 * Draws text on a bitmap
	 * 
	 * @param mContext Context
	 * @param bitmap Bitmap to draw on
	 * @param text Text string to be written 
	 * @return
	 */
	public static Bitmap drawTextToBitmap(Context mContext, Bitmap bitmap,
			String text, Integer offsetX, Integer offsetY, float textSize, Integer textColor) {
		Resources resources = mContext.getResources();
		float scale = resources.getDisplayMetrics().density;
		// Bitmap bitmap =
		// BitmapFactory.decodeResource(resources, gResId);

		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		// set default bitmap config if none
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		// if bitmap is not mutable a copy is done
		if (!bitmap.isMutable())
			bitmap = bitmap.copy(bitmapConfig, true);

		Canvas canvas = new Canvas(bitmap);
		// new antialised Paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// text color - #3D3D3D
		paint.setColor(textColor);
		// text size in pixels is converted as follows:
		// 1. multiplied for scale to obtain size in dp
		// 2. multiplied for bitmap size to maintain proportionality
		// 3. divided for a constant (300) to assimilate input size with android text sizes
		textSize = (int) (textSize * scale	* bitmap.getWidth() / 100);
		// If is too big it will be limited
		textSize = textSize < 15 ? textSize : 15;
		paint.setTextSize(textSize);
		// text shadow
		paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

		// Preparing text paint bounds
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		
		// Calculating position
		int x, y;
		// If no offset are set default is center of bitmap
		if (offsetX == null) {
			x = (bitmap.getWidth() - bounds.width()) / 2;
		} else {
			// If is a positive offset is set position is calculated 
			// starting from left limit of bitmap
			if (offsetX >= 0) {
				x = offsetX;
			// Otherwise if negative offset is set position is calculated
			// starting from right limit of bitmap
			} else {
				x = bitmap.getWidth() - bounds.width() - offsetX;
			}
		}
		// If no offset are set default is center of bitmap
		if (offsetY == null) {
			y = (bitmap.getHeight() - bounds.height()) / 2;
		} else {
			// If is a positive offset is set position is calculated 
			// starting from top limit of bitmap
			if (offsetY >= 0) {
				y = offsetY;
			// Otherwise if negative offset is set position is calculated
			// starting from bottom limit of bitmap
			} else {
				y = bitmap.getHeight() - bounds.height() + offsetY;
			}
		}

		// Drawing text
		canvas.drawText(text, x, y, paint);

		return bitmap;
	}
	
	
	
	
	
	public static InputStream getBitmapInputStream(Bitmap bitmap) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos); 
		byte[] bitmapdata = bos.toByteArray();
		ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
		return bs;
	}
	
	
	
	/**
	 * Retrieves a the bitmap relative to attachment based on mime type
	 * @param mContext
	 * @param mAttachment
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getBitmapFromAttachment(Context mContext, Attachment mAttachment, int width, int height) {
		Bitmap bmp = null;
		String path = mAttachment.getUri().getPath();	
		
		// Video
		if (Constants.MIME_TYPE_VIDEO.equals(mAttachment.getMime_type())) {
			// Tries to retrieve full path from ContentResolver if is a new
			// video
			path = StorageManager.getRealPathFromURI(mContext,
					mAttachment.getUri());
			// .. or directly from local directory otherwise
			if (path == null) {
				path = mAttachment.getUri().getPath();
			}

			bmp = ThumbnailUtils.createVideoThumbnail(path,
					Thumbnails.MINI_KIND);
			bmp = createVideoThumbnail(mContext, bmp, width, height);

			// Image
		} else if (Constants.MIME_TYPE_IMAGE.equals(mAttachment.getMime_type())) {
			try {
				bmp = checkIfBroken(mContext, BitmapHelper.getThumbnail(mContext,
						mAttachment.getUri(), width, height), width, height);
			} catch (NullPointerException e) {
				bmp = checkIfBroken(mContext, null, height, height);
			}

			// Audio
		} else if (Constants.MIME_TYPE_AUDIO.equals(mAttachment.getMime_type())) {
//			String text = "";
//			text = DateHelper.getLocalizedDateTime(mContext, mAttachment
//					.getUri().getLastPathSegment().split("\\.")[0],
//					Constants.DATE_FORMAT_SORTABLE);
//		
//			if (text == null) {
//				text = mContext.getString(R.string.attachment);
//			}
					
//			bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(
//					mContext.getResources(), R.drawable.play), width, height);
			bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.play), width, height);
//			bmp = BitmapHelper.drawTextToBitmap(mContext, bmp, text, null, -20,
//					3.3f, mContext.getResources().getColor(R.color.text_gray));
		}
		
		return bmp;
	}

	
	/**
	 * Draws a watermark on ImageView to highlight videos
	 * 
	 * @param bmp
	 * @param overlay
	 * @return
	 */
	public static Bitmap createVideoThumbnail(Context mContext, Bitmap video, int width, int height) {
//		Bitmap mark = ThumbnailUtils.extractThumbnail(
//				BitmapFactory.decodeResource(mContext.getResources(),
//						R.drawable.play_white), width, height);
		Bitmap mark = ThumbnailUtils.extractThumbnail(
				BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.play), width, height);
		Bitmap thumbnail = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(thumbnail);
		Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

		canvas.drawBitmap(checkIfBroken(mContext, video, height, height), 0, 0, null);
		canvas.drawBitmap(mark, 0, 0, null);

		return thumbnail;
	}
	
	


	
	/**
	 * Checks if a bitmap is null and returns a placeholder in its place
	 * @param mContext
	 * @param bmp
	 * @param width
	 * @param height
	 * @return
	 */
	private static Bitmap checkIfBroken(Context mContext, Bitmap bmp, int width, int height) {
		// In case no thumbnail can be extracted from video
		if (bmp == null) {
			bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.attachment_broken),
					width, height);
		}
		return bmp;
	}
	
}
