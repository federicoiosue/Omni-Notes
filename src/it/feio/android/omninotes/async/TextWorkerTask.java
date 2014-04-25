package it.feio.android.omninotes.async;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.TextUtils;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;


public class TextWorkerTask extends AsyncTask<Note, Void, Spanned[]> {

	private final WeakReference<Activity> mActivityWeakReference;
	private Activity mActivity;
	private TextView titleTextView;
	private TextView contentTextView;
	private boolean expandedView;

	public TextWorkerTask(Activity activity, TextView titleTextView,
			TextView contentTextView, boolean expandedView) {
		mActivityWeakReference = new WeakReference<Activity>(activity);
		mActivity = activity;
		this.titleTextView = titleTextView;
		this.contentTextView = contentTextView;
		this.expandedView = expandedView;
	}

	
	@Override
	protected Spanned[] doInBackground(Note... params) {
		Note note = params[0];
		Spanned[] titleAndContent = TextUtils.parseTitleAndContent(mActivity, note);
		return titleAndContent;
	}
	

	@Override
	protected void onPostExecute(Spanned[] titleAndContent) {

		if (isAlive()) {
			titleTextView.setText(titleAndContent[0]);
			if (titleAndContent[1].length() > 0) {
				contentTextView.setText(titleAndContent[1]);
				contentTextView.setVisibility(View.VISIBLE);	
			} else {
				if (expandedView) {
					contentTextView.setVisibility(View.INVISIBLE);
				} else {
					contentTextView.setVisibility(View.GONE);
				}
			}
			return;
		}
	}

	/**
	 * Cheks if activity is still alive and not finishing
	 * 
	 * @param weakDetailFragmentReference
	 * @return True or false
	 */
	private boolean isAlive() {
		if (mActivityWeakReference != null
				&& mActivityWeakReference.get() != null) {
			return true;
		}

		return false;
	}

}