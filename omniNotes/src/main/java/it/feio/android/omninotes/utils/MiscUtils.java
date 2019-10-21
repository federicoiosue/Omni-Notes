package it.feio.android.omninotes.utils;

import android.content.Context;
import android.content.pm.PackageManager;


public class MiscUtils {

    /**
     * Performs a full app restart
     */
    public static void restartApp(final Context mContext, Class activityClass) {
//        Intent intent = new Intent(mContext, activityClass);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        int mPendingIntentId = Long.valueOf(Calendar.getInstance().getTimeInMillis()).intValue();
//        PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, intent,
//                PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }


    /**
     * Checks Google Play Store availability
     * @param context Application context
     * @return True if Play Store is installed on the device
     */
    public static boolean isGooglePlayAvailable(Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.android.vending", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}