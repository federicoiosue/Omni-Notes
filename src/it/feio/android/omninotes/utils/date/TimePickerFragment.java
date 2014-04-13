/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.utils.date;

import it.feio.android.omninotes.R;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

public class TimePickerFragment extends DialogFragment {

	public static final String DEFAULT_TIME = "default_time";
	
	TextView timer_label;
	private Activity mActivity;
	private OnTimeSetListener mListener;
	private Long defaultTime = null;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (Activity) activity;
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
		
		Calendar cal = DateHelper.getCalendar(defaultTime);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		boolean is24HourMode = DateHelper.is24HourMode(mActivity);
		TimePickerDialog tpd = new TimePickerDialog(mActivity, R.style.Theme_AppCompat_Dialog_NoBackgroundOrDim, mListener, hour, minute, is24HourMode);
		tpd.setTitle("");
		return tpd;
	}

}
