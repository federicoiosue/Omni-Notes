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
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {

	public static final String DEFAULT_DATE = "default_date";
	
	private Activity mActivity;
	private OnDateSetListener mListener;
	private Long defaultDate;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (Activity)activity;
		if (getArguments().containsKey(DEFAULT_DATE)) {
			this.defaultDate = getArguments().getLong(DEFAULT_DATE);
		}

		try {
			mListener = (OnDateSetListener) mActivity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDateSetListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the current date as the default date in the picker
		Calendar cal = DateHelper.getCalendar(defaultDate);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(mActivity, R.style.Theme_AppCompat_Dialog_NoBackgroundOrDim ,mListener, year, month, day);
	}
}
