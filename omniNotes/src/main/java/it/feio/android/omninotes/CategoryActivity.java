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

package it.feio.android.omninotes;

import static it.feio.android.omninotes.utils.Constants.PREFS_NAME;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_CATEGORY;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_NAVIGATION;
import static java.lang.Integer.parseInt;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.CategoriesUpdatedEvent;
import it.feio.android.omninotes.databinding.ActivityCategoryBinding;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Category;
import it.feio.android.simplegallery.util.BitmapUtils;
import java.util.Calendar;
import java.util.Random;


public class CategoryActivity extends AppCompatActivity implements
    ColorChooserDialog.ColorCallback {

  private ActivityCategoryBinding binding;

  Category category;
  private int selectedColor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityCategoryBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    category = getIntent().getParcelableExtra(INTENT_CATEGORY);

    if (category == null) {
      LogDelegate.d("Adding new category");
      category = new Category();
      category.setColor(String.valueOf(getRandomPaletteColor()));
    } else {
      LogDelegate.d("Editing category " + category.getName());
    }
    selectedColor = parseInt(category.getColor());
    populateViews();
  }

  private int getRandomPaletteColor() {
    int[] paletteArray = getResources().getIntArray(R.array.material_colors);
    return paletteArray[new Random().nextInt((paletteArray.length))];
  }

  public void showColorChooserCustomColors() {

    new ColorChooserDialog.Builder(this, R.string.colors)
        .dynamicButtonColor(false)
        .preselect(selectedColor)
        .show(this);
  }

  @Override
  public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, int color) {
    BitmapUtils.changeImageViewDrawableColor(binding.colorChooser, color);
    selectedColor = color;
  }

  @Override
  public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    // Nothing to do
  }

  @Override
  public void onPointerCaptureChanged(boolean hasCapture) {
    // Nothing to do
  }

  private void populateViews() {
    binding.categoryTitle.setText(category.getName());
    binding.categoryDescription.setText(category.getDescription());
    // Reset picker to saved color
    String color = category.getColor();
    if (color != null && color.length() > 0) {
      binding.colorChooser.getDrawable().mutate()
          .setColorFilter(parseInt(color), PorterDuff.Mode.SRC_ATOP);
    }
    binding.delete
        .setVisibility(TextUtils.isEmpty(category.getName()) ? View.INVISIBLE : View.VISIBLE);

    binding.save.setOnClickListener(v -> saveCategory());
    binding.delete.setOnClickListener(v -> deleteCategory());
    binding.colorChooser.setOnClickListener(v -> showColorChooserCustomColors());
  }

  public void saveCategory() {

    if (binding.categoryTitle.getText().toString().length() == 0) {
      binding.categoryTitle.setError(getString(R.string.category_missing_title));
      return;
    }

    Long id =
        category.getId() != null ? category.getId() : Calendar.getInstance().getTimeInMillis();
    category.setId(id);
    category.setName(binding.categoryTitle.getText().toString());
    category.setDescription(binding.categoryDescription.getText().toString());
    if (selectedColor != 0 || category.getColor() == null) {
      category.setColor(String.valueOf(selectedColor));
    }

    // Saved to DB and new ID or update result catched
    DbHelper db = DbHelper.getInstance();
    category = db.updateCategory(category);

    // Sets result to show proper message
    getIntent().putExtra(INTENT_CATEGORY, category);
    setResult(RESULT_OK, getIntent());
    finish();
  }

  public void deleteCategory() {

    new MaterialDialog.Builder(this)
        .title(R.string.delete_unused_category_confirmation)
        .content(R.string.delete_category_confirmation)
        .positiveText(R.string.confirm)
        .positiveColorRes(R.color.colorAccent)
        .onPositive((dialog, which) -> {
          // Changes navigation if actually are shown notes associated with this category
          SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
          String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
          String navigation = prefs.getString(PREF_NAVIGATION, navNotes);
          if (String.valueOf(category.getId()).equals(navigation)) {
            prefs.edit().putString(PREF_NAVIGATION, navNotes).apply();
          }
          // Removes category and edit notes associated with it
          DbHelper db = DbHelper.getInstance();
          db.deleteCategory(category);

          EventBus.getDefault().post(new CategoriesUpdatedEvent());
          BaseActivity.notifyAppWidgets(OmniNotes.getAppContext());

          setResult(RESULT_FIRST_USER);
          finish();
        }).build().show();
  }

}
