package it.feio.android.omninotes;

import it.feio.android.omninotes.utils.ACRAPostSender;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.SimpleDiskCache;
import it.feio.android.omninotes.utils.SimpleDiskCache.BitmapEntry;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.DIALOG, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, resDialogText = R.string.crash_dialog_text)
public class OmniNotes extends Application {

	private final static String PREF_LANG = "settings_language";
	static SharedPreferences prefs;

	private LruCache<String, Bitmap> mMemoryCache;
	private SimpleDiskCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	@Override
	public void onCreate() {

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		HashMap<String, String> ACRAData = new HashMap<String, String>();
		ACRAData.put("my_app_info", "custom data");
		ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));

		// Get an instance of list thumbs cache
		InitCacheTask mInitCacheTask = new InitCacheTask();
		mInitCacheTask.execute(getExternalCacheDir());

		// Checks selected locale or default one
		updateLanguage(this, null);

		super.onCreate();
	}

	@Override
	// Used to restore user selected locale when configuration changes
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		String language = prefs.getString(PREF_LANG, "");
		super.onConfigurationChanged(newConfig);
		updateLanguage(this, language);
	}

	/**
	 * Updates default language with forced one
	 * 
	 * @param ctx
	 * @param lang
	 */
	public static void updateLanguage(Context ctx, String lang) {
		Configuration cfg = new Configuration();
		String language = prefs.getString(PREF_LANG, "");

		if (TextUtils.isEmpty(language) && lang == null) {
			cfg.locale = Locale.getDefault();

			String tmp = "";
			tmp = Locale.getDefault().toString().substring(0, 2);

			prefs.edit().putString(PREF_LANG, tmp).commit();

		} else if (lang != null) {
			cfg.locale = new Locale(lang);
			prefs.edit().putString(PREF_LANG, lang).commit();

		} else if (!TextUtils.isEmpty(language)) {
			cfg.locale = new Locale(language);
		}

		ctx.getResources().updateConfiguration(cfg, null);
	}

	
	
	class InitCacheTask extends AsyncTask<File, Void, Void> {
		@Override
		protected Void doInBackground(File... params) {

			// Get max available VM memory, exceeding this amount will throw an
			// OutOfMemory exception. Stored in kilobytes as LruCache takes an
			// int in its constructor.
			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			// Use 1/8th of the available memory for this memory cache.
			final int cacheSize = maxMemory / 8;
			mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
			
			synchronized (mDiskCacheLock) {
				int version = 0;
				try {
					version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				File cacheDir = params[0];
				try {
					mDiskLruCache = SimpleDiskCache.open(cacheDir, version 
							, DISK_CACHE_SIZE);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mDiskCacheStarting = false; // Finished initialization
				mDiskCacheLock.notifyAll(); // Wake any waiting threads
			}
			return null;
		}
	}
	

	public void addBitmapToCache(String key, Bitmap bitmap) {
		// Add to memory cache as before
		if (mMemoryCache.get(key) == null) {
			if (key != null && bitmap != null) {
				mMemoryCache.put(key, bitmap);
			}
		}

		// Also add to disk cache
		synchronized (mDiskCacheLock) {
			try {
				
				if (mDiskLruCache != null && !mDiskLruCache.contains(key)) {
					mDiskLruCache.put(key, BitmapHelper.getBitmapInputStream(bitmap));
				}
			} catch (IOException e) {
				
			}
		}
	}

//	public Bitmap getBitmapFromMemCache(String key) {
//		return mMemoryCache.get(key);
//	}

//	public Bitmap getBitmapFromDiskCache(String key) {
//		Bitmap bitmap = null;
//		synchronized (mDiskCacheLock) {
//			// Wait while disk cache is started from background thread
//			while (mDiskCacheStarting) {
//				try {
//					mDiskCacheLock.wait();
//					if (mDiskLruCache != null) {
//						BitmapEntry bmpEntry = mDiskLruCache.getBitmap(key);
//						bitmap = bmpEntry.getBitmap();
//					}
//				} catch (InterruptedException e) {} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		return bitmap;
//	}
	

	/**
	 * Retrieval of bitmap from chache
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromCache(String key) {
		// A first attempt is done with memory cache
		Bitmap bitmap = mMemoryCache.get(key);
		
		// If bitmap is not found a search into disk cache will be done
		if (bitmap == null) {

			synchronized (mDiskCacheLock) {
				// Wait while disk cache is started from background thread
				while (mDiskCacheStarting) {
					try {
						mDiskCacheLock.wait();
					} catch (InterruptedException e) {
					}
				}
				if (mDiskLruCache != null) {
					try {
						BitmapEntry bitmapEntry = mDiskLruCache.getBitmap(key);
						if (bitmapEntry != null) {
							bitmap = bitmapEntry.getBitmap();
						}
					} catch (IOException e) {
						Log.e(Constants.TAG,
								"Error retrieving bitmap from disk cache");
					}
				}
			}
			if (bitmap != null)
				addBitmapToCache(key, bitmap);
		}
		
		return bitmap;
	}
}