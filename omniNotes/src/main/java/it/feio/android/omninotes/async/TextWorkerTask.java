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

package it.feio.android.omninotes.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.TextHelper;


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
		Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mActivity, note);
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