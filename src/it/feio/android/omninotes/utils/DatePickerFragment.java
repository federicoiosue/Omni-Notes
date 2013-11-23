package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.DetailActivity;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

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
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(mActivity, mListener, year, month, day);
	}
}