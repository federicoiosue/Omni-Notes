package it.feio.android.omninotes.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import it.feio.android.omninotes.MainActivity;

import java.util.Calendar;


/**
 * Created by fede on 29/05/15.
 */
public class MiscUtils {

	/**
	 * Performs a full app restart
	 */
	public static void restartApp(final Context mContext, Class activityClass) {
//		Intent intent = new Intent(mContext, activityClass);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		int mPendingIntentId = Long.valueOf(Calendar.getInstance().getTimeInMillis()).intValue();
//		PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, intent,
//				PendingIntent.FLAG_CANCEL_CURRENT);
//		AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		System.exit(0);
	}


	/**
	 * Searches for Google Play app package
	 * @param mActivity
	 * @return
	 */
	public static boolean isGooglePlayAvailable(Activity mActivity) {
		try {
			mActivity.getPackageManager().getPackageInfo("com.android.vending", 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
}
