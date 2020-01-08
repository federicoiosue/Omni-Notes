/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import it.feio.android.omninotes.helpers.date.RecurrenceHelper;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import java.util.Calendar;


public class ReminderPickers {

  private FragmentActivity mActivity;
  private OnReminderPickedListener mOnReminderPickedListener;


  public ReminderPickers(FragmentActivity mActivity,
      OnReminderPickedListener mOnReminderPickedListener) {
    this.mActivity = mActivity;
    this.mOnReminderPickedListener = mOnReminderPickedListener;
  }

  public void pick(Long presetDateTime, String recurrenceRule) {
    showDateTimeSelectors(DateUtils.getCalendar(presetDateTime), recurrenceRule);
  }


  /**
   * Show date and time pickers
   */
  private void showDateTimeSelectors(Calendar reminder, String recurrenceRule) {
    SublimePickerFragment pickerFrag = new SublimePickerFragment();
    pickerFrag.setCallback(new SublimePickerFragment.Callback() {
      @Override
      public void onCancelled() {
        // Nothing to do
      }

      @Override
      public void onDateTimeRecurrenceSet(SelectedDate selectedDate, int hourOfDay, int minute, SublimeRecurrencePicker.RecurrenceOption recurrenceOption, String recurrenceRule) {
        Calendar reminder = selectedDate.getFirstDate();
        reminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
        reminder.set(Calendar.MINUTE, minute);

        mOnReminderPickedListener.onReminderPicked(reminder.getTimeInMillis());
        mOnReminderPickedListener.onRecurrenceReminderPicked(
            RecurrenceHelper.buildRecurrenceRuleByRecurrenceOptionAndRule(recurrenceOption, recurrenceRule));
      }
    });

    int displayOptions = 0;
    displayOptions |= SublimeOptions.ACTIVATE_DATE_PICKER;
    displayOptions |= SublimeOptions.ACTIVATE_TIME_PICKER;
    displayOptions |= SublimeOptions.ACTIVATE_RECURRENCE_PICKER;

    SublimeOptions sublimeOptions = new SublimeOptions();
    sublimeOptions.setPickerToShow(SublimeOptions.Picker.TIME_PICKER);
    sublimeOptions.setDisplayOptions(displayOptions);
    sublimeOptions.setDateParams(reminder);
    sublimeOptions.setRecurrenceParams(SublimeRecurrencePicker.RecurrenceOption.CUSTOM, recurrenceRule);
    sublimeOptions.setTimeParams(reminder.get(Calendar.HOUR_OF_DAY), reminder.get(Calendar.MINUTE), DateUtils.is24HourMode(mActivity));

    Bundle bundle = new Bundle();
    bundle.putParcelable("SUBLIME_OPTIONS", sublimeOptions);
    pickerFrag.setArguments(bundle);

    pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    pickerFrag.show(mActivity.getSupportFragmentManager(), "SUBLIME_PICKER");
  }

}
