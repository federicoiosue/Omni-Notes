
package it.feio.android.omninotes.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;


/**
 * Various utility methods
 */
public class SystemHelper {

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
	 * Performs closure of multiple closeables objects
	 *
	 * @param closeables Objects to close
	 */
	public static void closeCloseable(Closeable... closeables) {
		for (Closeable closeable : closeables) {
			if (closeable != null) {
				try {
					closeable.close();
				} catch (IOException e) {
					Log.w(Constants.TAG, "Can't close " + closeable, e);
				}
			}
		}
	}


	public static void copyToClipboard(Context context, String text) {
		android.content.ClipboardManager clipboard =
				(android.content.ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
		android.content.ClipData clip = android.content.ClipData.newPlainText("text label", text);
		clipboard.setPrimaryClip(clip);
	}
}