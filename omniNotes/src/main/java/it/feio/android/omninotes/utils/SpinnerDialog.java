/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class SpinnerDialog extends DialogFragment {

    public SpinnerDialog() {
        // use empty constructors. If something is needed use onCreate's
    }


    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getActivity());
//		this.setStyle(STYLE_NO_TITLE, getTheme()); // You can use styles or
//													// inflate a view
//		dialog.setMessage("Spinning.."); // set your messages if not inflated
        // from XML
//		dialog.setView(new Spinner(getActivity(), STYLE_NO_TITLE));
        dialog.setCancelable(false);

        return dialog;
    }
}