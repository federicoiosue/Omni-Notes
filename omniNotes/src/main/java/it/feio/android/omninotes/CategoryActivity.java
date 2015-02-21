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

package it.feio.android.omninotes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.utils.Constants;
import roboguice.util.Ln;

import java.io.File;
import java.io.FileOutputStream;


public class CategoryActivity extends Activity {

    private final float SATURATION = 0.4f;
    private final float VALUE = 0.9f;

    Category category;
    EditText title;
    EditText description;
    ColorPicker picker;
    Button deleteBtn;
    Button saveBtn;
    private CategoryActivity mActivity;
    private boolean colorChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mActivity = this;

        // Retrieving intent
        category = getIntent().getParcelableExtra(Constants.INTENT_TAG);

        // Getting Views from layout
        initViews();

        if (category == null) {
            Ln.d("Adding new category");
            category = new Category();
        } else {
            Ln.d("Editing category " + category.getName());
            populateViews();
        }
    }


    private void initViews() {
        title = (EditText) findViewById(R.id.category_title);
        description = (EditText) findViewById(R.id.category_description);
        picker = (ColorPicker) findViewById(R.id.colorpicker_category);
        picker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                picker.setOldCenterColor(picker.getColor());
                colorChanged = true;
            }
        });
        // Long click on color picker to remove color
        picker.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                picker.setColor(Color.WHITE);
                return true;
            }
        });
        picker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.setColor(Color.WHITE);
            }
        });

        // Added invisible saturation and value bars to get achieve pastel colors
        SaturationBar saturationbar = (SaturationBar) findViewById(R.id.saturationbar_category);
        saturationbar.setSaturation(SATURATION);
        picker.addSaturationBar(saturationbar);
        ValueBar valuebar = (ValueBar) findViewById(R.id.valuebar_category);
        valuebar.setValue(VALUE);
        picker.addValueBar(valuebar);

        deleteBtn = (Button) findViewById(R.id.delete);
        saveBtn = (Button) findViewById(R.id.save);
//		discardBtn = (Button) findViewById(R.id.discard);

        // Buttons events
        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCategory();
            }
        });
        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // In case category name is not compiled a message will be shown
                if (title.getText().toString().length() > 0) {
                    saveCategory();
                } else {
                    title.setError(getString(R.string.category_missing_title));
                }
            }
        });
    }


    private void populateViews() {
        title.setText(category.getName());
        description.setText(category.getDescription());
        // Reset picker to saved color
        String color = category.getColor();
        if (color != null && color.length() > 0) {
            picker.setColor(Integer.parseInt(color));
            picker.setOldCenterColor(Integer.parseInt(color));
        }
        deleteBtn.setVisibility(View.VISIBLE);
    }


    /**
     * Category saving
     */
    private void saveCategory() {
        category.setName(title.getText().toString());
        category.setDescription(description.getText().toString());
        if (colorChanged || category.getColor() == null)
            category.setColor(String.valueOf(picker.getColor()));

        // Saved to DB and new id or update result catched
        DbHelper db = DbHelper.getInstance(this);
        category = db.updateCategory(category);

        // Sets result to show proper message
        getIntent().putExtra(Constants.INTENT_TAG, category);
        setResult(RESULT_OK, getIntent());
        finish();
    }


    private void deleteCategory() {

        // Retrieving how many notes are categorized with category to be deleted
        DbHelper db = DbHelper.getInstance(this);
        int count = db.getCategorizedCount(category);
        String msg;
        if (count > 0)
            msg = getString(R.string.delete_category_confirmation).replace("$1$", String.valueOf(count));
        else
            msg = getString(R.string.delete_unused_category_confirmation);

        // Showing dialog
        new MaterialDialog.Builder(this)
                .content(msg)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.SimpleCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // Changes navigation if actually are shown notes associated with this category
                        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
                        String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
                        String navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
                        if (String.valueOf(category.getId()).equals(navigation))
                            prefs.edit().putString(Constants.PREF_NAVIGATION, navNotes).apply();
                        // Removes category and edit notes associated with it
                        DbHelper db = DbHelper.getInstance(mActivity);
                        db.deleteCategory(category);

                        // Sets result to show proper message
                        setResult(RESULT_FIRST_USER);
                        finish();
                    }
                }).build().show();
    }


    public void goHome() {
        // In this case the caller activity is DetailActivity
        if (getIntent().getBooleanExtra("noHome", false)) {
            setResult(RESULT_OK);
            super.finish();
        }
        NavUtils.navigateUpFromSameTask(this);
    }


    public void save(Bitmap bitmap) {
        if (bitmap == null) {
            setResult(RESULT_CANCELED);
            super.finish();
        }

        try {
            Uri uri = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            File bitmapFile = new File(uri.getPath());
            FileOutputStream out = new FileOutputStream(bitmapFile);
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

            if (bitmapFile.exists()) {
                Intent localIntent = new Intent().setData(Uri
                        .fromFile(bitmapFile));
                setResult(RESULT_OK, localIntent);
            } else {
                setResult(RESULT_CANCELED);
            }
            super.finish();

        } catch (Exception e) {
            Ln.d("Bitmap not found", e);
        }
    }
}
