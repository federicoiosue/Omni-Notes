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

package it.feio.android.omninotes.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.adapters.NavDrawerCategoryAdapter;
import it.feio.android.omninotes.utils.Constants;


public class WidgetConfigurationActivity extends Activity {

    private Spinner categorySpinner;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String sqlCondition;
    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_widget_configuration);

        mRadioGroup = (RadioGroup) findViewById(R.id.widget_config_radiogroup);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.widget_config_notes:
                    categorySpinner.setEnabled(false);
                    break;

                    case R.id.widget_config_categories:
                        categorySpinner.setEnabled(true);
                        break;

					default:
						Log.e(Constants.TAG, "Wrong element choosen: " + checkedId);
                }
        });

        categorySpinner = (Spinner) findViewById(R.id.widget_config_spinner);
        categorySpinner.setEnabled(false);
        DbHelper db = DbHelper.getInstance();
        ArrayList<Category> categories = db.getCategories();
        categorySpinner.setAdapter(new NavDrawerCategoryAdapter(this, categories));

        Button configOkButton = (Button) findViewById(R.id.widget_config_confirm);
        configOkButton.setOnClickListener(v -> {

            if (mRadioGroup.getCheckedRadioButtonId() == R.id.widget_config_notes) {
                sqlCondition = " WHERE " + DbHelper.KEY_ARCHIVED + " IS NOT 1 AND " + DbHelper.KEY_TRASHED + " IS" +
                        " NOT 1 ";

            } else {
                Category tag = (Category) categorySpinner.getSelectedItem();
                sqlCondition = " WHERE " + DbHelper.TABLE_NOTES + "."
                        + DbHelper.KEY_CATEGORY + " = " + tag.getId()
                        + " AND " + DbHelper.KEY_ARCHIVED + " IS NOT 1"
                        + " AND " + DbHelper.KEY_TRASHED + " IS NOT 1";
            }

            CheckBox showThumbnailsCheckBox = (CheckBox) findViewById(R.id.show_thumbnails);
            CheckBox showTimestampsCheckBox = (CheckBox) findViewById(R.id.show_timestamps);

            // Updating the ListRemoteViewsFactory parameter to get the list
            // of notes
            ListRemoteViewsFactory.updateConfiguration(getApplicationContext(), mAppWidgetId,
                    sqlCondition, showThumbnailsCheckBox.isChecked(), showTimestampsCheckBox.isChecked());

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            finish();
        });

        // Checks if no tags are available and then disable that option
        if (categories.size() == 0) {
            mRadioGroup.setVisibility(View.GONE);
            categorySpinner.setVisibility(View.GONE);
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

}
