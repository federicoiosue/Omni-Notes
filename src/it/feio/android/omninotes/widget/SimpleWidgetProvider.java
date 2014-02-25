package it.feio.android.omninotes.widget;


import it.feio.android.omninotes.R;
import android.app.PendingIntent;
import android.content.Context;
import android.util.SparseArray;
import android.widget.RemoteViews;

public class SimpleWidgetProvider extends WidgetProvider {
	
	
	@Override
	protected RemoteViews getRemoteViews(Context mContext, int widgetId, boolean isSmall, boolean isSingleLine, SparseArray<PendingIntent> pendingIntentsMap) {
		RemoteViews views;
		if (isSmall) {
			views = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_small);
			views.setOnClickPendingIntent(R.id.list, pendingIntentsMap.get(R.id.list));
		} else {
			views = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout);
			views.setOnClickPendingIntent(R.id.add, pendingIntentsMap.get(R.id.add));
			views.setOnClickPendingIntent(R.id.list, pendingIntentsMap.get(R.id.list));
			views.setOnClickPendingIntent(R.id.camera, pendingIntentsMap.get(R.id.camera));
		}
		return views;
	}
}