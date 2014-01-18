/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
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

import java.util.Calendar;

import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.utils.Constants;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

public class TimePickerFragment extends DialogFragment {

	TextView timer_label;
	private DetailActivity mActivity;
	private OnTimeSetListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (DetailActivity) activity;

		try {
			mListener = (OnTimeSetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTimeSetListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Calendar cal = DateHelper.getDateFromString(mActivity.getAlarmTime(), Constants.DATE_FORMAT_SHORT_TIME);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(mActivity, mListener, hour, minute, true);
	}

}
