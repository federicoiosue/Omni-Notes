package it.feio.android.omninotes.widget;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.NoteAdapter;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListRemoteViewsFactory implements RemoteViewsFactory {

	private final int WIDTH = 80;
	private final int HEIGHT = 80;
	
	private static boolean showThumbnails = true;
	
	private OmniNotes app;
	private int appWidgetId;
	private List<Note> notes;
	private DbHelper db;

	
	public ListRemoteViewsFactory(Application app, Intent intent) {
		this.app = (OmniNotes) app;
		appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		db = new DbHelper(app);
	}

	
	@Override
	public void onCreate() {
		Log.d(Constants.TAG, "Created widget " + appWidgetId);
		String condition = PreferenceManager.getDefaultSharedPreferences(app)
				.getString(
						Constants.PREF_WIDGET_PREFIX
								+ String.valueOf(appWidgetId), "");
		notes = db.getNotes(condition, true);
	}

	
	@Override
	public void onDataSetChanged() {
		Log.d(Constants.TAG, "onDataSetChanged widget " + appWidgetId);
		String condition = PreferenceManager.getDefaultSharedPreferences(app)
				.getString(
						Constants.PREF_WIDGET_PREFIX
								+ String.valueOf(appWidgetId), "");
		notes = db.getNotes(condition, true);
	}
	

	@Override
	public void onDestroy() {
		PreferenceManager
				.getDefaultSharedPreferences(app)
				.edit()
				.remove(Constants.PREF_WIDGET_PREFIX
						+ String.valueOf(appWidgetId)).commit();
	}
	

	@Override
	public int getCount() {
		return notes.size();
	}

	
	@Override
	public RemoteViews getViewAt(int position) {
		RemoteViews row = new RemoteViews(app.getPackageName(), R.layout.note_layout_widget);
		
		Note note = notes.get(position);
		
		String[] titleAndContent = NoteAdapter.parseTitleAndContent(note);

		row.setTextViewText(R.id.note_title, Html.fromHtml(titleAndContent[0]));
		row.setTextViewText(R.id.note_content, Html.fromHtml(titleAndContent[1]));
		
		color(note, row);
		
		if (note.getAttachmentsList().size() > 0 && showThumbnails) {
			Attachment mAttachment = note.getAttachmentsList().get(0);
			// Fetch from cache if possible
			String cacheKey = mAttachment.getUri().getPath() + WIDTH + HEIGHT;
			Bitmap bmp = app.getBitmapFromCache(cacheKey);

			if (bmp == null) {
				bmp = BitmapHelper.getBitmapFromAttachment(app, mAttachment,
						WIDTH, HEIGHT);
			}
			row.setBitmap(R.id.attachmentThumbnail, "setImageBitmap", bmp);
			row.setInt(R.id.attachmentThumbnail, "setVisibility", View.VISIBLE);
		} else {
			row.setInt(R.id.attachmentThumbnail, "setVisibility", View.GONE);
		}
		
		row.setTextViewText(R.id.note_date, NoteAdapter.getDateText(app, note));

		// Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putParcelable(Constants.INTENT_NOTE, note);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        // Make it possible to distinguish the individual on-click
        // action of a given item
        row.setOnClickFillInIntent(R.id.root, fillInIntent);

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

	public static void updateConfiguration(Context mContext, int mAppWidgetId, String sqlCondition, boolean thumbnails) {
		Log.d(Constants.TAG, "Widget configuration updated");
		PreferenceManager
				.getDefaultSharedPreferences(mContext)
				.edit()
				.putString(
						Constants.PREF_WIDGET_PREFIX
								+ String.valueOf(mAppWidgetId), sqlCondition)
				.commit();
		showThumbnails = thumbnails;
	}
	
	
	private void color(Note note, RemoteViews row) {
		
		String colorsPref = PreferenceManager.getDefaultSharedPreferences(app)
				.getString("settings_colors_widget",
						Constants.PREF_COLORS_APP_DEFAULT);

		// Checking preference
		if (!colorsPref.equals("disabled")) {

			// Resetting transparent color to the view
			row.setInt(R.id.tag_marker, "setBackgroundColor", Color.parseColor("#00000000"));

			// If tag is set the color will be applied on the appropriate target
			if (note.getTag() != null && note.getTag().getColor() != null) {
				if (colorsPref.equals("list")) {
					row.setInt(R.id.card_layout, "setBackgroundColor", Integer.parseInt(note.getTag().getColor()));					
				} else {
					row.setInt(R.id.tag_marker, "setBackgroundColor", Integer.parseInt(note.getTag().getColor()));
				}
			} else {
				row.setInt(R.id.tag_marker, "setBackgroundColor", 0);
			}
		}
	}

}
