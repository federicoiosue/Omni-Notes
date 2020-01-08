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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.appeaser.sublimepickerlibrary.SublimePicker;
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import it.feio.android.omninotes.R;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class SublimePickerFragment extends DialogFragment {

  public static final String DEFAULT_TIME = "default_time";
  DateFormat mDateFormatter, mTimeFormatter;

  SublimePicker mSublimePicker;

  Callback mCallback;

  SublimeListenerAdapter mListener = new SublimeListenerAdapter() {
    @Override
    public void onCancelled () {
      if (mCallback != null) {
        mCallback.onCancelled();
      }
      dismiss();
    }

    @Override
    public void onDateTimeRecurrenceSet (SublimePicker sublimeMaterialPicker,
        SelectedDate selectedDate,
        int hourOfDay, int minute,
        SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
        String recurrenceRule) {
      if (mCallback != null) {
        mCallback.onDateTimeRecurrenceSet(selectedDate,
            hourOfDay, minute, recurrenceOption, recurrenceRule);
      }
      dismiss();
    }
  };

  public SublimePickerFragment () {
    mDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
    mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
    mTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
  }

  public void setCallback (Callback callback) {
    mCallback = callback;
  }

  @Nullable
  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mSublimePicker = (SublimePicker) getActivity().getLayoutInflater().inflate(R.layout.sublime_picker, container);

    Bundle arguments = getArguments();
    SublimeOptions options = null;

    if (arguments != null) {
      options = arguments.getParcelable("SUBLIME_OPTIONS");
    }

    mSublimePicker.initializePicker(options, mListener);
    return mSublimePicker;
  }

  public interface Callback {

    void onCancelled ();

    void onDateTimeRecurrenceSet (SelectedDate selectedDate,
        int hourOfDay, int minute,
        SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
        String recurrenceRule);
  }
}
