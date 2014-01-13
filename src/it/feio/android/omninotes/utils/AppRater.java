package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.R;

import com.neopixl.pixlui.components.button.Button;
import com.neopixl.pixlui.components.textview.TextView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class AppRater {
	private final static String APP_TITLE = "Omni Notes";
	private final static String APP_PNAME = "it.feio.android.omninotes";

	private final static int DAYS_UNTIL_PROMPT = 0;
	private final static int LAUNCHES_UNTIL_PROMPT = 7;

	public static void appLaunched(Activity mActivity, String message, String rateBtn, String dismissBtn, String laterBtn) {
		SharedPreferences prefs = mActivity.getSharedPreferences("apprater", 0);
		if (prefs.getBoolean("dontshowagain", false)) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}

		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateDialog(mActivity, message, rateBtn, dismissBtn, laterBtn, editor);
			}
		}

		editor.commit();
	}

//	private static void showRateDialog(final Context mActivity, String message, String rateBtn, String dismissBtn,
//			String laterBtn, final SharedPreferences.Editor editor) {
//		
//		final Dialog dialog = new Dialog(mActivity);
//		dialog.setTitle(APP_TITLE);
//		
//		LinearLayout ll = new LinearLayout(mActivity);
//		ll.setOrientation(LinearLayout.VERTICAL);
//		
//		TextView tv = new TextView(mActivity);
//		tv.setText(message);
//		tv.setWidth(200);
//		tv.setPadding(16, 16, 16, 16);
//		ll.addView(tv);
//		
//		Button b1 = new Button(mActivity);
//		b1.setText(rateBtn);
//		b1.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
//				dialog.dismiss();
//			}
//		});
//		b1.setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent));
//		ll.addView(b1);
//		
//		Button b2 = new Button(mActivity);
//		b2.setText(laterBtn);
//		b2.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//		ll.addView(b2);
//		
//		Button b3 = new Button(mActivity);
//		b3.setText(dismissBtn);
//		b3.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				if (editor != null) {
//					editor.putBoolean("dontshowagain", true);
//					editor.commit();
//				}
//				dialog.dismiss();
//			}
//		});
//		ll.addView(b3);
//		
//		dialog.setContentView(ll);
//		dialog.show();
//	}
	
	
	private static void showRateDialog(final Activity mActivity, String message, String rateBtn, String dismissBtn,
			String laterBtn, final SharedPreferences.Editor editor) {
		
		final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
		
		dialog.setTitle(APP_TITLE)
			.setMessage(message)
			.setPositiveButton(rateBtn, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
					dialog.dismiss();
				}
			})
			.setNeutralButton(laterBtn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					editor.putLong("date_firstlaunch", System.currentTimeMillis()).commit();
					dialog.dismiss();
				}
			})			
			.setNegativeButton(dismissBtn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (editor != null) {
						editor.putBoolean("dontshowagain", true).commit();
					}
					dialog.dismiss();
				}
			});
		
		dialog.show();
	}
}