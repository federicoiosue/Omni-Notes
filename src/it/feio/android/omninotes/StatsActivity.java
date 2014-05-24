package it.feio.android.omninotes;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Stats;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class StatsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stats);
		initData();
	}
	

	@SuppressLint("NewApi")
	private void initData() {
		class StatsTask extends AsyncTask<Void, Void, Stats> {

			private Context mContext;

			public StatsTask(Context mContext) {
				this.mContext = mContext;

			}

			@Override
			protected Stats doInBackground(Void... params) {
				Stats mStats = (DbHelper.getInstance(mContext)).getStats();
				return mStats;
			}

			@Override
			protected void onPostExecute(Stats result) {
				populateViews(result);
			}
		}

		StatsTask task = new StatsTask(this);
		if (Build.VERSION.SDK_INT >= 11) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	
	
	
	private void populateViews(Stats mStats) {
		((TextView)findViewById(R.id.stat_notes_total)).setText(String.valueOf(mStats.getNotesTotalNumber()));
		((TextView)findViewById(R.id.stat_notes_active)).setText(String.valueOf(mStats.getNotesActive()));
		((TextView)findViewById(R.id.stat_notes_archived)).setText(String.valueOf(mStats.getNotesArchived()));
		((TextView)findViewById(R.id.stat_notes_trashed)).setText(String.valueOf(mStats.getNotesTrashed()));
		((TextView)findViewById(R.id.stat_reminders)).setText(String.valueOf(mStats.getReminders()));
		((TextView)findViewById(R.id.stat_reminders_futures)).setText(String.valueOf(mStats.getRemindersFutures()));
		((TextView)findViewById(R.id.stat_checklists)).setText(String.valueOf(mStats.getNotesChecklist()));
		((TextView)findViewById(R.id.stat_masked)).setText(String.valueOf(mStats.getNotesMasked()));
		((TextView)findViewById(R.id.stat_categories)).setText(String.valueOf(mStats.getCategories()));
		((TextView)findViewById(R.id.stat_tags)).setText(String.valueOf(mStats.getTags()));

		((TextView)findViewById(R.id.stat_attachments)).setText(String.valueOf(mStats.getAttachments()));
		((TextView)findViewById(R.id.stat_attachments_images)).setText(String.valueOf(mStats.getImages()));
		((TextView)findViewById(R.id.stat_attachments_videos)).setText(String.valueOf(mStats.getVideos()));
		((TextView)findViewById(R.id.stat_attachments_audiorecordings)).setText(String.valueOf(mStats.getAudioRecordings()));
		((TextView)findViewById(R.id.stat_attachments_sketches)).setText(String.valueOf(mStats.getSketches()));
		((TextView)findViewById(R.id.stat_attachments_files)).setText(String.valueOf(mStats.getFiles()));
		
		((TextView)findViewById(R.id.stat_words)).setText(String.valueOf(mStats.getWords()));
		((TextView)findViewById(R.id.stat_chars)).setText(String.valueOf(mStats.getChars()));
	}
}
