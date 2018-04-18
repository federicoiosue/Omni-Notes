/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.utils.date;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import it.feio.android.omninotes.R;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment {

    public static final String DEFAULT_TIME = "default_time";

    TextView timer_label;
    private Activity mActivity;
    private OnTimeSetListener mListener;
    private Long defaultTime = null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        if (getArguments().containsKey(DEFAULT_TIME)) {
            this.defaultTime = getArguments().getLong(DEFAULT_TIME);
        }

        try {
            mListener = (OnTimeSetListener) mActivity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTimeSetListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar cal = DateUtils.getCalendar(defaultTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        boolean is24HourMode = DateUtils.is24HourMode(mActivity);
        TimePickerDialog tpd = new TimePickerDialog(mActivity, R.style.Theme_AppCompat_Dialog_NoBackgroundOrDim, mListener, hour, minute, is24HourMode);
        tpd.setTitle("");
        return tpd;
    }

}
