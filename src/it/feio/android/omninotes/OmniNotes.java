package it.feio.android.omninotes;

import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.SimpleDiskCache;
import it.feio.android.omninotes.utils.SimpleDiskCache.BitmapEntry;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

@ReportsCrashes(
				formKey = "", 
						
				httpMethod = Method.PUT,
			    reportType = Type.JSON,
			    formUri = "http://feio.cloudant.com/acra-omninotes/_design/acra-storage/_update/report",
			    formUriBasicAuthLogin = "thelescivessiandesedclik",
			    formUriBasicAuthPassword = "uScXIHpchNKfuCdgbm3nHTjo",
						
				mode = ReportingInteractionMode.DIALOG, 
				resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, 
				resDialogText = R.string.crash_dialog_text
				)

public class OmniNotes extends Application {
	
	private static Context mContext;

	private final static String PREF_LANG = "settings_language";
	static SharedPreferences prefs;
	private static Tracker mTracker;
	private static GoogleAnalytics mGa;

	private LruCache<String, Bitmap> mMemoryCache;
	private SimpleDiskCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

	@Override
	public void onCreate() {
//		Log.d(Constants.TAG, "App onCreate()");
		
		mContext = getApplicationContext();
		
		prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

		// The following line triggers the initialization of ACRA
		ACRA.init(this);

		// Get an instance of list thumbs cache
		InitCacheTask mInitCacheTask = new InitCacheTask();
		mInitCacheTask.execute(getExternalCacheDir());

		// Checks selected locale or default one
		updateLanguage(this, null);
		
		// Google Analytics initialization
	    initializeGa();

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
	
	


    public static Context getAppContext() {
        return OmniNotes.mContext;
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
			// Checks country
			if (lang.contains("_")) {
				cfg.locale = new Locale(lang.split("_")[0], lang.split("_")[1]);
			} else {
				cfg.locale = new Locale(lang);				
			}
			prefs.edit().putString(PREF_LANG, lang).commit();

		} else if (!TextUtils.isEmpty(language)) {
			// Checks country
			if (language.contains("_")) {
				cfg.locale = new Locale(language.split("_")[0], language.split("_")[1]);
			} else {
				cfg.locale = new Locale(language);				
			}
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
				} catch (NameNotFoundException e) {
					Log.e(Constants.TAG, "Error retrieving package name", e);
				}
				File cacheDir = params[0];
				try {
					mDiskLruCache = SimpleDiskCache.open(cacheDir, version 
							, DISK_CACHE_SIZE);
				} catch (IOException e) {
					Log.e(Constants.TAG, "Error retrieving disk cache", e);
				} catch (NullPointerException e) {
					Log.e(Constants.TAG, "Error retrieving disk cache", e);
				}
				mDiskCacheStarting = false; // Finished initialization
				mDiskCacheLock.notifyAll(); // Wake any waiting threads
			}
			return null;
		}
	}
	

	public void addBitmapToCache(String key, Bitmap bitmap) {
		// Add to memory cache as before
		if (mMemoryCache != null && mMemoryCache.get(key) == null) {
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
	
	

	/**
	 * Retrieval of bitmap from chache
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromCache(String key) {
		
		if (mMemoryCache == null) {
			return null;
		}
		
		// A first attempt is done with memory cache
		Bitmap bitmap = mMemoryCache.get(key);
		
		// If bitmap is not found a search into disk cache will be done
		if (bitmap == null) {

			synchronized (mDiskCacheLock) {
				// Wait while disk cache is started from background thread
				while (mDiskCacheStarting) {
					try {
						mDiskCacheLock.wait();
					} catch (InterruptedException e) {}
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
	
	
	/*
	 * Method to handle basic Google Analytics initialization. This call will
	 * not block as all Google Analytics work occurs off the main thread.
	 */
	private void initializeGa() {
		mGa = GoogleAnalytics.getInstance(this);
		mTracker = mGa.getTracker("UA-45502770-1");

//		// Set dispatch period.
//		GAServiceManager.getInstance().setLocalDispatchPeriod(
//				GA_DISPATCH_PERIOD);
//
//		// Set dryRun flag.
//		mGa.setDryRun(GA_IS_DRY_RUN);
//
//		// Set Logger verbosity.
//		mGa.getLogger().setLogLevel(GA_LOG_VERBOSITY);
//
//		// Set the opt out flag when user updates a tracking preference.
//		SharedPreferences userPrefs = PreferenceManager
//				.getDefaultSharedPreferences(this);
//		userPrefs
//				.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
//					@Override
//					public void onSharedPreferenceChanged(
//							SharedPreferences sharedPreferences, String key) {
//						if (key.equals(TRACKING_PREF_KEY)) {
//							GoogleAnalytics
//									.getInstance(getApplicationContext())
//									.setAppOptOut(
//											sharedPreferences.getBoolean(key,
//													false));
//						}
//					}
//				});
	}

	/*
	 * Returns the Google Analytics tracker.
	 */
	public static Tracker getGaTracker() {
		return mTracker;
	}

	/*
	 * Returns the Google Analytics instance.
	 */
	public static GoogleAnalytics getGaInstance() {
		return mGa;
	}
	
	
	
	
	/**
	 * Performs a full app restart
	 */
	public static void restartApp(Context mContext) {
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		NavUtils.navigateUpFromSameTask(this);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(mContext,
				mPendingIntentId, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
				mPendingIntent);
		System.exit(0);
		
//		Process.killProcess(Process.myPid());
	}

	
}