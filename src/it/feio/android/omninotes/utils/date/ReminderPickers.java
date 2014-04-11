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
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

public class ReminderPickers implements OnDateSetListener, OnTimeSetListener {

	public static final int TYPE_GOOGLE = 0;
	public static final int TYPE_AOSP = 1;
	
	private static volatile ReminderPickers instance = null;
	private FragmentActivity mActivity;
	private OnReminderPickedListener mOnReminderPickedListener;
	private int pickerType;

	private int reminderYear;
	private int reminderMonth;
	private int reminderDay;
	private String alarmDate;
	private String alarmTime;
	
	private boolean timePickerCalledAlready = false;
	
	
	private ReminderPickers(FragmentActivity mActivity,
			OnReminderPickedListener mOnReminderPickedListener, int pickerType) {
		this.mActivity = mActivity;
		this.mOnReminderPickedListener = mOnReminderPickedListener;
		this.pickerType = pickerType;
	}

	
	public static ReminderPickers getInstance(FragmentActivity mActivity,
			OnReminderPickedListener mOnReminderPickedListener, int pickerType) {
		if (instance == null) {
			instance = new ReminderPickers(mActivity,
					mOnReminderPickedListener, pickerType);
		}
		return instance;
	}
	
	
	
	public void pick(){
		pick(0);
	}
	
	
	public void pick(long presetDateTime){
		if (pickerType == TYPE_AOSP) {
			timePickerCalledAlready = false;
			// Timepicker will be automatically called after date is inserted by user
			showDatePickerDialog();					
		} else {
			showDateTimeSelectors(presetDateTime);					
		}
	}
	
	
	/**
	 * Show date and time pickers
	 */
	protected void showDateTimeSelectors(long reminder) {

		// Sets actual time or previously saved in note
		Calendar cal = Calendar.getInstance();
		if (reminder != 0)
			cal.setTimeInMillis(reminder);
		final Calendar now = cal; 
		CalendarDatePickerDialog mCalendarDatePickerDialog = CalendarDatePickerDialog.newInstance(
				new CalendarDatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
						reminderYear = year;
						reminderMonth = monthOfYear;
						reminderDay = dayOfMonth;
						alarmDate = DateHelper.onDateSet(year, monthOfYear, dayOfMonth,
								Constants.DATE_FORMAT_SHORT_DATE);
						RadialTimePickerDialog mRadialTimePickerDialog = RadialTimePickerDialog.newInstance(
								new RadialTimePickerDialog.OnTimeSetListener() {

									@Override
									public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
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

	public void showDatePickerDialog() {
		DatePickerFragment newFragment = new DatePickerFragment();
		Bundle bundle = new Bundle();		
		bundle.putString(DatePickerFragment.DEFAULT_DATE, alarmDate);
		newFragment.setArguments(bundle);
		newFragment.show(mActivity.getSupportFragmentManager(), Constants.TAG);
	}
	
	private void showTimePickerDialog() {
		TimePickerFragment newFragment = new TimePickerFragment();
		Bundle bundle = new Bundle();		
		bundle.putString(TimePickerFragment.DEFAULT_TIME, alarmTime);
		newFragment.setArguments(bundle);
		newFragment.show(mActivity.getSupportFragmentManager(), Constants.TAG);
	}

	@Override
	public void onDateSet(DatePicker v, int year, int monthOfYear, int dayOfMonth) {
		reminderYear = year;
		reminderMonth = monthOfYear;
		reminderDay = dayOfMonth;
		alarmDate = DateHelper.onDateSet(year, monthOfYear, dayOfMonth, Constants.DATE_FORMAT_SHORT_DATE);
		if (!timePickerCalledAlready) {	// Used to avoid native bug that calls onPositiveButtonPressed in the onClose()
			timePickerCalledAlready = true;
			showTimePickerDialog();
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
