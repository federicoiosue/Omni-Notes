/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.widget;


import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.RemoteViews;

import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;
import roboguice.util.Ln;


public abstract class WidgetProvider extends AppWidgetProvider {

    public static String EXTRA_WORD = "it.feio.android.omninotes.widget.WORD";
    public static String TOAST_ACTION = "it.feio.android.omninotes.widget.NOTE";
    public static String EXTRA_ITEM = "it.feio.android.omninotes.widget.EXTRA_FIELD";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, getClass());
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int appWidgetId : allWidgetIds) {
            Ln.d("WidgetProvider onUpdate() widget " + appWidgetId);
            // Get the layout for and attach an on-click listener to views
            setLayout(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {
        Ln.d("Widget size changed");
        setLayout(context, appWidgetManager, appWidgetId);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setLayout(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        // Create an Intent to launch DetailActivity
        Intent intentDetail = new Intent(context, MainActivity.class);
        intentDetail.setAction(Constants.ACTION_WIDGET);
        intentDetail.putExtra(Constants.INTENT_WIDGET, widgetId);
        PendingIntent pendingIntentDetail = PendingIntent.getActivity(context, widgetId, intentDetail,
                Intent.FLAG_ACTIVITY_NEW_TASK);

        // Create an Intent to launch ListActivity
        Intent intentList = new Intent(context, MainActivity.class);
        intentList.setAction(Constants.ACTION_WIDGET_SHOW_LIST);
        intentList.putExtra(Constants.INTENT_WIDGET, widgetId);
        PendingIntent pendingIntentList = PendingIntent.getActivity(context, widgetId, intentList,
                Intent.FLAG_ACTIVITY_NEW_TASK);

        // Create an Intent to launch DetailActivity to take a photo
        Intent intentDetailPhoto = new Intent(context, MainActivity.class);
        intentDetailPhoto.setAction(Constants.ACTION_TAKE_PHOTO);
        intentDetailPhoto.putExtra(Constants.INTENT_WIDGET, widgetId);
        PendingIntent pendingIntentDetailPhoto = PendingIntent.getActivity(context, widgetId, intentDetailPhoto,
                Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check various dimensions aspect of widget to choose between layouts
        boolean isSmall = false;
        boolean isSingleLine = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
            // Width check
            isSmall = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < 110;
            // Height check
            isSingleLine = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) < 110;
        }

        // Creation of a map to associate PendingIntent(s) to views
        SparseArray<PendingIntent> map = new SparseArray<>();
        map.put(R.id.list, pendingIntentList);
        map.put(R.id.add, pendingIntentDetail);
        map.put(R.id.camera, pendingIntentDetailPhoto);

        RemoteViews views = getRemoteViews(context, widgetId, isSmall, isSingleLine, map);

        // Tell the AppWidgetManager to perform an update on the current app
        // widget
        appWidgetManager.updateAppWidget(widgetId, views);
    }


    abstract protected RemoteViews getRemoteViews(Context context, int widgetId, boolean isSmall, boolean isSingleLine, SparseArray<PendingIntent> pendingIntentsMap);

}