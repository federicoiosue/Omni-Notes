/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;


/**
 * <p>This class provides a usable {@link android.app.DatePickerDialog} wrapped as a {@link android.support.v4.app
 * * .DialogFragment},
 * using the compatibility package v4. Its main advantage is handling Issue 34833
 * automatically for you.</p>
 * <p/>
 * <p>Current implementation (because I wanted that way =) ):</p>
 * <p/>
 * <ul>
 * <li>Only two buttons, a {@code BUTTON_POSITIVE} and a {@code BUTTON_NEGATIVE}.
 * <li>Buttons labeled from {@code android.R.string.ok} and {@code android.R.string.cancel}.
 * </ul>
 * <p/>
 * <p><strong>Usage sample:</strong></p>
 * <p/>
 * <pre>class YourActivity extends Activity implements OnDateSetListener
 * <p/>
 * // ...
 * <p/>
 * Bundle b = new Bundle();
 * b.putInt(DatePickerDialogFragment.YEAR, 2012);
 * b.putInt(DatePickerDialogFragment.MONTH, 6);
 * b.putInt(DatePickerDialogFragment.DATE, 17);
 * DialogFragment picker = new DatePickerDialogFragment();
 * picker.setArguments(b);
 * picker.show(getActivity().getSupportFragmentManager(), "fragment_date_picker");</pre>
 *
 * @author davidcesarino@gmail.com
 * @version 2012.0828
 * @see <a href="http://code.google.com/p/android/issues/detail?id=34833">Android Issue 34833</a>
 * @see <a href="http://stackoverflow.com/q/11444238/489607"
 * >Jelly Bean DatePickerDialog — is there a way to cancel?</a>
 */
public class DatePickerDialogFragment extends DialogFragment {

    public static final String DEFAULT_DATE = "default_date";

    private OnDateSetListener mListener;
    private Long defaultDate;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getArguments().containsKey(DEFAULT_DATE)) {
            this.defaultDate = getArguments().getLong(DEFAULT_DATE);
        }

        try {
            mListener = (OnDateSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDateSetListener");
        }
    }


    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Calendar cal = DateUtils.getCalendar(defaultDate);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        // Jelly Bean introduced a bug in DatePickerDialog (and possibly 
        // TimePickerDialog as well), and one of the possible solutions is 
        // to postpone the creation of both the listener and the BUTTON_* .
        // 
        // Passing a null here won't harm because DatePickerDialog checks for a null
        // whenever it reads the listener that was passed here. >>> This seems to be 
        // true down to 1.5 / API 3, up to 4.1.1 / API 16. <<< No worries. For now.
        //
        // See my own question and answer, and details I included for the issue:
        //
        // http://stackoverflow.com/a/11493752/489607
        // http://code.google.com/p/android/issues/detail?id=34833
        //
        // Of course, suggestions welcome.

        final DatePickerDialog picker = new DatePickerDialog(getActivity(), DatePickerDialog.THEME_HOLO_LIGHT,
                mListener, y, m, d);
        picker.setTitle("");

		picker.setButton(DialogInterface.BUTTON_POSITIVE,
				getActivity().getString(android.R.string.ok),
				(dialog, which) -> {
					DatePicker dp = picker.getDatePicker();
					mListener.onDateSet(dp,
							dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
				});
		picker.setButton(DialogInterface.BUTTON_NEGATIVE,
				getActivity().getString(android.R.string.cancel),
				(dialog, which) -> {});

        return picker;
    }

}
