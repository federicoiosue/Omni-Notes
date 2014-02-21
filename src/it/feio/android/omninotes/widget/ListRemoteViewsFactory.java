package it.feio.android.omninotes.widget;

import java.util.List;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListRemoteViewsFactory implements RemoteViewsFactory {
	private static final String[] items = { "lorem", "ipsum", "dolor", "sit", "amet", "consectetuer", "adipiscing",
			"elit", "morbi", "vel", "ligula", "vitae", "arcu", "aliquet", "mollis", "etiam", "vel", "erat", "placerat",
			"ante", "porttitor", "sodales", "pellentesque", "augue", "purus" };
	private Context mContext;
	private int appWidgetId;
	private List<Note> notes;

	public ListRemoteViewsFactory(Context mContext, Intent intent) {
		this.mContext = mContext;
		appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onCreate() {
		DbHelper db = new DbHelper(mContext);
		notes = db.getAllNotes(false);

	}

	@Override
	public void onDataSetChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCount() {
		return (items.length);
	}

	@Override
	public RemoteViews getViewAt(int position) {
//		RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.note_layout);
		RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.row);

//		row.setTextViewText(R.id.note_title, items[position]);
//		row.setTextViewText(R.id.text1, items[position]);
		row.setTextViewText(R.id.text1, notes.get(position).getTitle());

		Intent i = new Intent();
		Bundle extras = new Bundle();

		extras.putString(WidgetProvider.EXTRA_WORD, items[position]);
		i.putExtras(extras);
//		row.setOnClickFillInIntent(R.id.text1, i);
//		row.setOnClickFillInIntent(R.id.text1, i);

		return (row);
	}

	@Override
	public RemoteViews getLoadingView() {
		return (null);
	}

	@Override
	public int getViewTypeCount() {
		return (1);
	}

	@Override
	public long getItemId(int position) {
		return (position);
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

}
