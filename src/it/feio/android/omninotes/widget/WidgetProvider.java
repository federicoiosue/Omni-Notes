package it.feio.android.omninotes.widget;

import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.ListActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	public static String EXTRA_WORD=
		    "it.feio.android.omninotes.widget.WORD";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int appWidgetId : allWidgetIds) {

			// Create an Intent to launch DetailActivity
			Intent intentDetail = new Intent(context, DetailActivity.class);
			PendingIntent pendingIntentDetail = PendingIntent.getActivity(context, 0, intentDetail,
					Intent.FLAG_ACTIVITY_NEW_TASK);

			// Create an Intent to launch ListActivity
			Intent intentList = new Intent(context, ListActivity.class);
			PendingIntent pendingIntentList = PendingIntent.getActivity(context, 0, intentList, 0);

			// Create an Intent to launch DetailActivity to take a photo
			Intent intentDetailPhoto = new Intent(context, DetailActivity.class);
			intentDetailPhoto.setAction(Intent.ACTION_PICK);
			PendingIntent pendingIntentDetailPhoto = PendingIntent.getActivity(context, 0, intentDetailPhoto,
					Intent.FLAG_ACTIVITY_NEW_TASK);

			// Get the layout for and attach an on-click listener to views

			setLayout(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {
		Log.d(Constants.TAG, "Widget size changed");

		setLayout(context, appWidgetManager, appWidgetId);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setLayout(Context context, AppWidgetManager appWidgetManager, int widgetId) {

		// Create an Intent to launch DetailActivity
		Intent intentDetail = new Intent(context, DetailActivity.class);
		PendingIntent pendingIntentDetail = PendingIntent.getActivity(context, 0, intentDetail,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		// Create an Intent to launch ListActivity
		Intent intentList = new Intent(context, ListActivity.class);
		PendingIntent pendingIntentList = PendingIntent.getActivity(context, 0, intentList, 0);

		// Create an Intent to launch DetailActivity to take a photo
		Intent intentDetailPhoto = new Intent(context, DetailActivity.class);
		intentDetailPhoto.setAction(Intent.ACTION_PICK);
		PendingIntent pendingIntentDetailPhoto = PendingIntent.getActivity(context, 0, intentDetailPhoto,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		boolean isSmall = false;
		if (Build.VERSION.SDK_INT >= 16) {
			Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
			isSmall = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < 70;
		} 
		
		
		
		RemoteViews views;
		if (isSmall) {
			views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
			views.setOnClickPendingIntent(R.id.list, pendingIntentList);
		} else {
			views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_list);
			views.setOnClickPendingIntent(R.id.add, pendingIntentDetail);
			views.setOnClickPendingIntent(R.id.list, pendingIntentList);
			views.setOnClickPendingIntent(R.id.camera, pendingIntentDetailPhoto);
			
			// Set up the intent that starts the ListViewService, which will
	        // provide the views for this collection.
	        Intent intent = new Intent(context, ListWidgetService.class);
	        // Add the app widget ID to the intent extras.
	        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
	        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
	        // Instantiate the RemoteViews object for the app widget layout.
			views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_list);
			
			views.setRemoteAdapter(R.id.widget_list, intent);
		}

		
		
		// Tell the AppWidgetManager to perform an update on the current app
		// widget
		appWidgetManager.updateAppWidget(widgetId, views);
	}
}