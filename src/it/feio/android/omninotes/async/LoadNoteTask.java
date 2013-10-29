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