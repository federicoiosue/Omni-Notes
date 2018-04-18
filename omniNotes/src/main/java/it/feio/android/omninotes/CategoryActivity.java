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

package it.feio.android.omninotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.CategoriesUpdatedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.simplegallery.util.BitmapUtils;

import static java.lang.Integer.parseInt;


public class CategoryActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback{

    @BindView(R.id.category_title) EditText title;
    @BindView(R.id.category_description) EditText description;
    @BindView(R.id.delete) Button deleteBtn;
    @BindView(R.id.save) Button saveBtn;
    @BindView(R.id.color_chooser) ImageView colorChooser;

    Category category;
    private int selectedColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        ButterKnife.bind(this);

        category = getIntent().getParcelableExtra(Constants.INTENT_CATEGORY);

        if (category == null) {
            Log.d(Constants.TAG, "Adding new category");
            category = new Category();
            category.setColor(String.valueOf(getRandomPaletteColor()));
        } else {
            Log.d(Constants.TAG, "Editing category " + category.getName());
        }
        selectedColor = parseInt(category.getColor());
        populateViews();
    }


    private int getRandomPaletteColor() {
        int[] paletteArray = getResources().getIntArray(R.array.material_colors);
        return paletteArray[new Random().nextInt((paletteArray.length))];
    }


    @OnClick(R.id.color_chooser)
    public void showColorChooserCustomColors() {

        new ColorChooserDialog.Builder(this, R.string.colors)
                .dynamicButtonColor(false)
                .preselect(selectedColor)
                .show();
    }


    @Override
    public void onColorSelection(ColorChooserDialog colorChooserDialog, int color) {
        BitmapUtils.changeImageViewDrawableColor(colorChooser, color);
        selectedColor = color;
    }


    private void populateViews() {
        title.setText(category.getName());
        description.setText(category.getDescription());
        // Reset picker to saved color
        String color = category.getColor();
        if (color != null && color.length() > 0) {
            colorChooser.getDrawable().mutate().setColorFilter(parseInt(color), PorterDuff.Mode.SRC_ATOP);
        }
        deleteBtn.setVisibility(TextUtils.isEmpty(category.getName()) ? View.INVISIBLE : View.VISIBLE);
    }


    /**
     * Category saving
     */
    @OnClick(R.id.save)
    public void saveCategory() {

        if (title.getText().toString().length() == 0) {
            title.setError(getString(R.string.category_missing_title));
            return;
        }

		Long id = category.getId() != null ? category.getId() : Calendar.getInstance().getTimeInMillis();
		category.setId(id);
        category.setName(title.getText().toString());
        category.setDescription(description.getText().toString());
        if (selectedColor != 0 || category.getColor() == null) {
            category.setColor(String.valueOf(selectedColor));
        }

        // Saved to DB and new id or update result catched
        DbHelper db = DbHelper.getInstance();
        category = db.updateCategory(category);

        // Sets result to show proper message
        getIntent().putExtra(Constants.INTENT_CATEGORY, category);
        setResult(RESULT_OK, getIntent());
        finish();
    }


    @OnClick(R.id.delete)
    public void deleteCategory() {

        new MaterialDialog.Builder(this)
				.title(R.string.delete_unused_category_confirmation)
                .content(R.string.delete_category_confirmation)
                .positiveText(R.string.confirm)
                .positiveColorRes(R.color.colorAccent)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // Changes navigation if actually are shown notes associated with this category
                        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
                        String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
                        String navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
                        if (String.valueOf(category.getId()).equals(navigation))
                            prefs.edit().putString(Constants.PREF_NAVIGATION, navNotes).apply();
                        // Removes category and edit notes associated with it
                        DbHelper db = DbHelper.getInstance();
                        db.deleteCategory(category);

                        EventBus.getDefault().post(new CategoriesUpdatedEvent());
                        BaseActivity.notifyAppWidgets(OmniNotes.getAppContext());

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
            Log.d(Constants.TAG, "Bitmap not found", e);
        }
    }
}
