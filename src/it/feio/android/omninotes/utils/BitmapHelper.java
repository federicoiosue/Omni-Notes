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
	public static Bitmap getThumbnail(Context ctx, Uri uri, int reqWidth, int reqHeight) throws FileNotFoundException {
		Bitmap srcBmp = decodeSampledFromUri(ctx, uri, reqWidth, reqHeight);
		Bitmap dstBmp;
		// Cropping
		int x = ( srcBmp.getWidth() - reqWidth )/2;
		int y = ( srcBmp.getHeight() - reqHeight )/2;
		if (x > 0 && y > 0) {
			dstBmp = Bitmap.createBitmap(srcBmp, x, y, reqWidth, reqHeight);		
		} else {
			dstBmp = ThumbnailUtils.extractThumbnail(srcBmp, reqWidth, reqHeight);
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
	
}
