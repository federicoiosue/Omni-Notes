/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
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

import java.io.FileNotFoundException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;


public class BitmapDecoder {

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
		float inSampleSize = 1;
		int newWidth = width;
		int newHeight = height;

		if (height > reqHeight || width > reqWidth) {
			final float heightRatio = (float) height / (float) reqHeight;
			final float widthRatio = (float) width / (float) reqWidth;

			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
			newWidth = Math.round(width / inSampleSize);
			newHeight = Math.round(height / inSampleSize);
		}

		// Impostazione delle opzioni per la decodifica
		options.inJustDecodeBounds = false;
		options.inSampleSize = Math.round(inSampleSize);

		// Ulteriore ottimizzazione a scapito della fedeltà dei colori e trasparenze
		// options.inPreferredConfig = Bitmap.Config.RGB_565;

		// Decodifica dell'immagine con inSampleSize e ridimensionamento
		Bitmap bmp = BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, options);
//		bmp = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
		bmp = ThumbnailUtils.extractThumbnail(bmp, reqWidth, reqHeight);
		return bmp;
	}
	
	public static Uri getUri(Context mContext, int resource_id) {
		Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + resource_id);
		return uri;
	}
}
