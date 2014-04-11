package it.feio.android.omninotes.utils.date;

import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.utils.Constants;

import java.util.Calendar;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

public class ReminderPickers implements OnDateSetListener, OnTimeSetListener {

	public static final int TYPE_GOOGLE = 0;
	public static final int TYPE_AOSP = 1;
	
	private FragmentActivity mActivity;
	private OnReminderPickedListener mOnReminderPickedListener;
	private int pickerType;

	private int reminderYear;
	private int reminderMonth;
	private int reminderDay;
	
	private boolean timePickerCalledAlready = false;
	private long presetDateTime;
	
	
	public ReminderPickers(FragmentActivity mActivity,
			OnReminderPickedListener mOnReminderPickedListener, int pickerType) {
		this.mActivity = mActivity;
		this.mOnReminderPickedListener = mOnReminderPickedListener;
		this.pickerType = pickerType;
	}

	
		
	public void pick(){
		pick(0);
	}
	
	
	public void pick(long presetDateTime){
		this.presetDateTime = DateHelper.getCalendar(presetDateTime).getTimeInMillis();
		if (pickerType == TYPE_AOSP) {
			timePickerCalledAlready = false;
			// Timepicker will be automatically called after date is inserted by user
			showDatePickerDialog(presetDateTime);					
		} else {
			showDateTimeSelectors(presetDateTime);					
		}
	}
	
	
	/**
	 * Show date and time pickers
	 */
	protected void showDateTimeSelectors(long reminder) {

		// Sets actual time or previously saved in note
		final Calendar now = DateHelper.getCalendar(reminder); 
		CalendarDatePickerDialog mCalendarDatePickerDialog = CalendarDatePickerDialog.newInstance(
				new CalendarDatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
						reminderYear = year;
						reminderMonth = monthOfYear;
						reminderDay = dayOfMonth;
						RadialTimePickerDialog mRadialTimePickerDialog = RadialTimePickerDialog.newInstance(
								new RadialTimePickerDialog.OnTimeSetListener() {

									@Override
									public void onTimeSet(
											RadialTimePickerDialog dialog,
											int hourOfDay, int minute) {
										// Setting alarm time in milliseconds
										Calendar c = Calendar.getInstance();
										c.set(reminderYear, reminderMonth, reminderDay, hourOfDay, minute);
										if (mOnReminderPickedListener != null) {
											mOnReminderPickedListener.onReminderPicked(c.getTimeInMillis());
										}	
									}
								}, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), DateHelper.is24HourMode(mActivity));
						mRadialTimePickerDialog.show(mActivity.getSupportFragmentManager(), Constants.TAG);
					}

				}, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		mCalendarDatePickerDialog.show(mActivity.getSupportFragmentManager(), Constants.TAG);
	}

	
	/**
	 * Shows fallback date and time pickers for smaller screens 
	 * 
	 * @param v
	 */

	public void showDatePickerDialog(long presetDateTime) {
		DatePickerFragment newFragment = new DatePickerFragment();
		Bundle bundle = new Bundle();		
		bundle.putLong(DatePickerFragment.DEFAULT_DATE, presetDateTime);
		newFragment.setArguments(bundle);
		newFragment.show(mActivity.getSupportFragmentManager(), Constants.TAG);
	}
	
	private void showTimePickerDialog(long presetDateTime) {
		TimePickerFragment newFragment = new TimePickerFragment();
		Bundle bundle = new Bundle();		
		bundle.putLong(TimePickerFragment.DEFAULT_TIME, presetDateTime);
		newFragment.setArguments(bundle);
		newFragment.show(mActivity.getSupportFragmentManager(), Constants.TAG);
	}

	@Override
	public void onDateSet(DatePicker v, int year, int monthOfYear, int dayOfMonth) {
		reminderYear = year;
		reminderMonth = monthOfYear;
		reminderDay = dayOfMonth;
		if (!timePickerCalledAlready) {	// Used to avoid native bug that calls onPositiveButtonPressed in the onClose()
			timePickerCalledAlready = true;
			showTimePickerDialog(presetDateTime);
		}
	}

	@Override
	public void onTimeSet(TimePicker v, int hourOfDay, int minute) {
		// Setting alarm time in milliseconds
		Calendar c = Calendar.getInstance();
		c.set(reminderYear, reminderMonth, reminderDay, hourOfDay, minute);
		if (mOnReminderPickedListener != null) {
			mOnReminderPickedListener.onReminderPicked(c.getTimeInMillis());
		}	
	}
}
