package it.feio.android.omninotes;

import it.feio.android.omninotes.models.ThumbnailLruCache;
import it.feio.android.omninotes.utils.ACRAPostSender;

import java.util.HashMap;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

@ReportsCrashes(formKey = "", 
				mode = ReportingInteractionMode.DIALOG,
				resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
				resDialogText = R.string.crash_dialog_text
				)
public class OmniNotes extends Application {
	
	private LruCache<String, Bitmap> mMemoryCache;
	
	private final static String PREF_LANG = "settings_language";
	static SharedPreferences prefs;
	
	private ThumbnailLruCache mThumbnailLruCache;
	
	@Override
	public void onCreate() {
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
				
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		HashMap<String, String> ACRAData = new HashMap<String, String>();
		ACRAData.put("my_app_info", "custom data");
		ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));
		
		// Get an instance of list thumbs cache
		mThumbnailLruCache = ThumbnailLruCache.getInstance();
		initCache();

		// Checks selected locale or default one
		updateLanguage(this,null);

		super.onCreate();
	}
	
	
	@Override
	// Used to restore user selected locale when configuration changes
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String language = prefs.getString(PREF_LANG, "");             
        super.onConfigurationChanged(newConfig);
        updateLanguage(this,language);
    }
	
	
	/**
	 * Updates default language with forced one
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
	
	
	
	public ThumbnailLruCache getThumbnailLruCache() {
		return mThumbnailLruCache;
	}
	
	
	
	private void initCache() {
		// Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = maxMemory / 8;

	    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
//	        @Override
//	        protected int sizeOf(String key, Bitmap bitmap) {
//	            // The cache size will be measured in kilobytes rather than
//	            // number of items.
//	            return bitmap.getByteCount() / 1024;
//	        }
	    };
	}
	
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}
}