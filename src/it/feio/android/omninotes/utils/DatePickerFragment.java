package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.DetailActivity;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {
	private DetailActivity mActivity;
	private OnDateSetListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (DetailActivity) activity;

		try {
			mListener = (OnDateSetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDateSetListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the current date as the default date in the picker
		Calendar cal = DateHelper.getDateFromString(mActivity.getAlarmDate(), Constants.DATE_FORMAT_SHORT_DATE);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(mActivity, mListener, year, month, day);
	}
}