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
package it.feio.android.omninotes.async;

import it.feio.android.omninotes.utils.Constants;
import java.lang.ref.WeakReference;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;


public class LoadNoteTask extends AsyncTask<String, Void, Bitmap> {

	private final Context ctx;
	private final WeakReference<ImageView> imageViewReference;
	private String fotoString;
	private int reqWidth;
	private int reqHeight;

	public LoadNoteTask(Context ctx, ImageView imageView) {
		this.ctx = ctx;
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bmp = null;
	
		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
//				Log.i(Constants.TAG, "Immagine caricata: " + "img_" + fq.getData() + fq.getAds() + fq.getAttivita() + fq.getKpi());
				Log.i(Constants.TAG, "Immagine caricata: " + fotoString);
			}
		}
	}
}
