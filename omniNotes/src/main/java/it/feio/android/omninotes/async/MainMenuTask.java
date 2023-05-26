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

package it.feio.android.omninotes.async;

import static it.feio.android.omninotes.utils.ConstantsBase.PREF_DYNAMIC_MENU;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_SHOW_UNCATEGORIZED;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import androidx.fragment.app.Fragment;
import com.pixplicity.easyprefs.library.Prefs;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.bus.NavigationUpdatedEvent;
import it.feio.android.omninotes.databinding.FragmentNavigationDrawerBinding;
import it.feio.android.omninotes.models.NavigationItem;
import it.feio.android.omninotes.models.adapters.NavDrawerAdapter;
import it.feio.android.omninotes.models.misc.DynamicNavigationLookupTable;
import it.feio.android.omninotes.utils.Navigation;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainMenuTask extends AsyncTask<Void, Void, List<NavigationItem>> {

  private final WeakReference<Fragment> fragmentWeakReference;
  private final MainActivity mainActivity;

  FragmentNavigationDrawerBinding navDrawer;

  public MainMenuTask(Fragment fragment) {
    fragmentWeakReference = new WeakReference<>(fragment);
    mainActivity = (MainActivity) fragment.getActivity();
    navDrawer = FragmentNavigationDrawerBinding.inflate(fragment.getLayoutInflater());
  }

  @Override
  protected List<NavigationItem> doInBackground(Void... params) {
    return buildMainMenu();
  }

  @Override
  protected void onPostExecute(final List<NavigationItem> items) {
    if (isAlive()) {
      navDrawer.drawerNavList.setAdapter(new NavDrawerAdapter(mainActivity, items));
      navDrawer.drawerNavList.setOnItemClickListener((arg0, arg1, position, arg3) -> {
        String navigation = fragmentWeakReference.get().getResources().getStringArray(R.array
            .navigation_list_codes)[items.get(position).getArrayIndex()];
        updateNavigation(position, navigation);
      });
      navDrawer.drawerNavList.justifyListViewHeightBasedOnChildren();
    }
  }

  private void updateNavigation(int position, String navigation) {
    if (mainActivity.updateNavigation(navigation)) {
      navDrawer.drawerNavList.setItemChecked(position, true);
      navDrawer.drawerTagList.setItemChecked(0, false); // Called to force redraw
      mainActivity.getIntent().setAction(Intent.ACTION_MAIN);
      EventBus.getDefault()
          .post(new NavigationUpdatedEvent(navDrawer.drawerNavList.getItemAtPosition(position)));
    }
  }

  private boolean isAlive() {
    return fragmentWeakReference.get() != null
        && fragmentWeakReference.get().isAdded()
        && fragmentWeakReference.get().getActivity() != null
        && !fragmentWeakReference.get().getActivity().isFinishing();
  }

  private List<NavigationItem> buildMainMenu() {
    if (!isAlive()) {
      return new ArrayList<>();
    }

    String[] mNavigationArray = mainActivity.getResources().getStringArray(R.array.navigation_list);
    TypedArray mNavigationIconsArray = mainActivity.getResources()
        .obtainTypedArray(R.array.navigation_list_icons);
    TypedArray mNavigationIconsSelectedArray = mainActivity.getResources().obtainTypedArray(R.array
        .navigation_list_icons_selected);

    final List<NavigationItem> items = new ArrayList<>();
    for (int i = 0; i < mNavigationArray.length; i++) {
      if (!checkSkippableItem(i)) {
        NavigationItem item = new NavigationItem(i, mNavigationArray[i],
            mNavigationIconsArray.getResourceId(i,
                0), mNavigationIconsSelectedArray.getResourceId(i, 0));
        items.add(item);
      }
    }
    return items;
  }

  private boolean checkSkippableItem(int i) {
    boolean skippable = false;
    boolean dynamicMenu = Prefs.getBoolean(PREF_DYNAMIC_MENU, true);
    DynamicNavigationLookupTable dynamicNavigationLookupTable = null;
    if (dynamicMenu) {
      dynamicNavigationLookupTable = DynamicNavigationLookupTable.getInstance();
    }
    switch (i) {
      case Navigation.REMINDERS:
        if (dynamicMenu && dynamicNavigationLookupTable.getReminders() == 0) {
          skippable = true;
        }
        break;
      case Navigation.UNCATEGORIZED:
        boolean showUncategorized = Prefs.getBoolean(PREF_SHOW_UNCATEGORIZED, false);
        if (!showUncategorized || (dynamicMenu
            && dynamicNavigationLookupTable.getUncategorized() == 0)) {
          skippable = true;
        }
        break;
      case Navigation.ARCHIVE:
        if (dynamicMenu && dynamicNavigationLookupTable.getArchived() == 0) {
          skippable = true;
        }
        break;
      case Navigation.TRASH:
        if (dynamicMenu && dynamicNavigationLookupTable.getTrashed() == 0) {
          skippable = true;
        }
        break;
    }
    return skippable;
  }

}
