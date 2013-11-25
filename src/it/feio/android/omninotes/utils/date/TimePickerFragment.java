package it.feio.android.omninotes.utils.date;

import java.util.Calendar;

import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.R;
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