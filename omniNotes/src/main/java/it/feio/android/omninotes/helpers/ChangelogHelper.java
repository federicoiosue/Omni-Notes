/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers;

import android.app.Activity;
import android.content.Context;
import com.afollestad.materialdialogs.MaterialDialog;
import it.feio.android.omninotes.R;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ChangelogHelper {

  public static void showChangelog(Activity activity) {
    new MaterialDialog.Builder(activity)
        .customView(R.layout.activity_changelog, false)
        .positiveText(R.string.ok)
        .build().show();


  }

}