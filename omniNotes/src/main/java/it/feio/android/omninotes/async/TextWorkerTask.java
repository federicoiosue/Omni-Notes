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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.view.View;
import android.widget.TextView;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.TextHelper;

import java.lang.ref.WeakReference;


public class TextWorkerTask extends AsyncTask<Note, Void, Spanned[]> {

    private final WeakReference<Activity> mActivityWeakReference;
    private Activity mActivity;
    private TextView titleTextView;
    private TextView contentTextView;
    private boolean expandedView;


    public TextWorkerTask(Activity activity, TextView titleTextView,
                          TextView contentTextView, boolean expandedView) {
        mActivityWeakReference = new WeakReference<>(activity);
        mActivity = activity;
        this.titleTextView = titleTextView;
        this.contentTextView = contentTextView;
        this.expandedView = expandedView;
    }


    @Override
    protected Spanned[] doInBackground(Note... params) {
        return TextHelper.parseTitleAndContent(mActivity, params[0]);
    }


    @Override
    protected void onPostExecute(Spanned[] titleAndContent) {
        if (isAlive()) {
            titleTextView.setText(titleAndContent[0]);
            if (titleAndContent[1].length() > 0) {
                contentTextView.setText("✓ ");
                contentTextView.measure(0, 0);

                contentTextView.setText(createIndentedText(SpannableString.valueOf(titleAndContent[1]), 0, contentTextView.getMeasuredWidth()));contentTextView.setVisibility(View.VISIBLE);
            } else {
                if (expandedView) {
                    contentTextView.setVisibility(View.INVISIBLE);
                } else {
                    contentTextView.setVisibility(View.GONE);
                }
            }
        }
    }


    /**
     * Checks if activity is still alive and not finishing
     *
     * @return True or false
     */
    private boolean isAlive() {
        return mActivityWeakReference != null
                && mActivityWeakReference.get() != null;

    }

    static SpannableString createIndentedText(SpannableString text, int marginFirstLine, int marginNextLines) {
        final int color = Color.rgb(128, 128, 128);

        text.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines),0,text.length(),0);

        String t = text.toString();
        int index = 0;
        while (index >= 0) {
            ForegroundColorSpan fcs = new ForegroundColorSpan(color);
            text.setSpan(fcs, index, index + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            index = t.indexOf('\n', index + 2);
        }


        return text;
    }

}