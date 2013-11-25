package it.feio.android.omninotes.utils;

import java.io.FileNotFoundException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
		bmp = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
		return bmp;
	}
}
