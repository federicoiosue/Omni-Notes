package it.feio.android.omninotes.receiver;

import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.ListActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	private static final String ACTION_CLICK = "ACTION_CLICK";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				WidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			// create some random data
//			int number = (new Random().nextInt(100));
//
//			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
//					R.layout.widget_layout);
//			Log.w("WidgetExample", String.valueOf(number));
//			// Set the text
//			remoteViews.setTextViewText(R.id.update, String.valueOf(number));
//
//			// Register an onClickListener
//			Intent intent = new Intent(context, WidgetProvider.class);
//
//			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
//
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
//					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
//			appWidgetManager.updateAppWidget(widgetId, remoteViews);
			
			// Create an Intent to launch ExampleActivity
            Intent intentDetail = new Intent(context, DetailActivity.class);
            intentDetail.putExtra(Constants.INTENT_NOTE, new Note());
            PendingIntent pendingIntentDetail = PendingIntent.getActivity(context, 0, intentDetail, 0);
            
            Intent intentList = new Intent(context, ListActivity.class);
            PendingIntent pendingIntentList = PendingIntent.getActivity(context, 0, intentList, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.add, pendingIntentDetail);
            views.setOnClickPendingIntent(R.id.list, pendingIntentList);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(widgetId, views);
			
		}
	}
}