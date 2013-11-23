package it.feio.android.omninotes.utils;

import java.util.Calendar;

import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.R;
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
		
		TextView datetime = (TextView) mActivity.findViewById(R.id.datetime);
		Calendar cal = DateHelper.getDateFromString(datetime.getText().toString(), Constants.DATE_FORMAT_SHORT);
//		int hour = mActivity.stringToTime(timer_label.getText().toString())[0];
//		int minute = mActivity.stringToTime(timer_label.getText().toString())[1];
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(mActivity, mListener, hour, minute, true);
	}

	// @Override
	// public void onTimeSet(TimePicker view, int hour, int minute) {
	// //
	// ((TextView)getActivity().findViewById(R.id.timer_label)).setText("fava");
	// // getActivity().seta
	// }
}