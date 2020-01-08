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

package it.feio.android.omninotes.async;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import androidx.fragment.app.Fragment;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.SettingsActivity;
import it.feio.android.omninotes.async.bus.NavigationUpdatedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.adapters.CategoryBaseAdapter;
import it.feio.android.omninotes.models.views.NonScrollableListView;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;


public class CategoryMenuTask extends AsyncTask<Void, Void, List<Category>> {

  private final WeakReference<Fragment> mFragmentWeakReference;
  private final MainActivity mainActivity;
  private NonScrollableListView mDrawerCategoriesList;
  private View settingsView;
  private View settingsViewCat;
  private NonScrollableListView mDrawerList;


  public CategoryMenuTask (Fragment mFragment) {
    mFragmentWeakReference = new WeakReference<>(mFragment);
    this.mainActivity = (MainActivity) mFragment.getActivity();
  }


  @Override
  protected void onPreExecute () {
    super.onPreExecute();
    mDrawerList = mainActivity.findViewById(R.id.drawer_nav_list);
    LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

    settingsView = mainActivity.findViewById(R.id.settings_view);

    // Settings view when categories are available
    mDrawerCategoriesList = mainActivity.findViewById(R.id.drawer_tag_list);
    if (mDrawerCategoriesList.getAdapter() == null && mDrawerCategoriesList.getFooterViewsCount() == 0) {
      settingsViewCat = inflater.inflate(R.layout.drawer_category_list_footer, null);
      mDrawerCategoriesList.addFooterView(settingsViewCat);
    } else {
      settingsViewCat = mDrawerCategoriesList.getChildAt(mDrawerCategoriesList.getChildCount() - 1);
    }

  }


  @Override
  protected List<Category> doInBackground (Void... params) {
    if (isAlive()) {
      return buildCategoryMenu();
    } else {
      cancel(true);
      return Collections.emptyList();
    }
  }


  @Override
  protected void onPostExecute (final List<Category> categories) {
    if (isAlive()) {
      mDrawerCategoriesList.setAdapter(new CategoryBaseAdapter(mainActivity, categories,
          mainActivity.getNavigationTmp()));
      if (categories.isEmpty()) {
        setWidgetVisibility(settingsViewCat, false);
        setWidgetVisibility(settingsView, true);
      } else {
        setWidgetVisibility(settingsViewCat, true);
        setWidgetVisibility(settingsView, false);
      }
      mDrawerCategoriesList.justifyListViewHeightBasedOnChildren();
    }
  }


  private void setWidgetVisibility (View view, boolean visible) {
    if (view != null) {
      view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
  }


  private boolean isAlive () {
    return mFragmentWeakReference.get() != null
        && mFragmentWeakReference.get().isAdded()
        && mFragmentWeakReference.get().getActivity() != null
        && !mFragmentWeakReference.get().getActivity().isFinishing();
  }


  private List<Category> buildCategoryMenu () {

    List<Category> categories = DbHelper.getInstance().getCategories();

    View settings = categories.isEmpty() ? settingsView : settingsViewCat;
    if (settings == null) {
      return categories;
    }

    mainActivity.runOnUiThread(() -> {
      settings.setOnClickListener(v -> {
        Intent settingsIntent = new Intent(mainActivity, SettingsActivity.class);
        mainActivity.startActivity(settingsIntent);
      });

      buildCategoryMenuClickEvent();

      buildCategoryMenuLongClickEvent();

    });

    return categories;
  }

  private void buildCategoryMenuLongClickEvent () {
    mDrawerCategoriesList.setOnItemLongClickListener((arg0, view, position, arg3) -> {
      if (mDrawerCategoriesList.getAdapter() != null) {
        Object item = mDrawerCategoriesList.getAdapter().getItem(position);
        // Ensuring that clicked item is not the ListView header
        if (item != null) {
          mainActivity.editTag((Category) item);
        }
      } else {
        mainActivity.showMessage(R.string.category_deleted, ONStyle.ALERT);
      }
      return true;
    });
  }

  private void buildCategoryMenuClickEvent () {
    mDrawerCategoriesList.setOnItemClickListener((arg0, arg1, position, arg3) -> {

      Object item = mDrawerCategoriesList.getAdapter().getItem(position);
      if (mainActivity.updateNavigation(String.valueOf(((Category) item).getId()))) {
        mDrawerCategoriesList.setItemChecked(position, true);
        // Forces redraw
        if (mDrawerList != null) {
          mDrawerList.setItemChecked(0, false);
          EventBus.getDefault().post(new NavigationUpdatedEvent(mDrawerCategoriesList.getItemAtPosition
              (position)));
        }
      }
    });
  }

}
