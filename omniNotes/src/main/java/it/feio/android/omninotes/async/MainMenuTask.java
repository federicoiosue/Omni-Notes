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

package it.feio.android.omninotes.async;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.NavigationItem;
import it.feio.android.omninotes.models.adapters.NavDrawerAdapter;
import it.feio.android.omninotes.models.listeners.OnNavigationItemClickedListener;
import it.feio.android.omninotes.models.misc.DynamicNavigationLookupTable;
import it.feio.android.omninotes.models.views.NonScrollableListView;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Navigation;
import roboguice.util.Ln;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainMenuTask extends AsyncTask<Void, Void, List<NavigationItem>> {

    private final WeakReference<Fragment> mFragmentWeakReference;
    private final MainActivity mainActivity;
    private final OnNavigationItemClickedListener onNavigationItemclicked;
    private NonScrollableListView mDrawerList;
    private NonScrollableListView mDrawerCategoriesList;


    public MainMenuTask(Fragment mFragment, OnNavigationItemClickedListener onNavigationItemclicked) {
        mFragmentWeakReference = new WeakReference<>(mFragment);
        this.mainActivity = (MainActivity) mFragment.getActivity();
        this.onNavigationItemclicked = onNavigationItemclicked;
    }


    @Override
    protected List<NavigationItem> doInBackground(Void... params) {
        return buildMainMenu();
    }


    @Override
    protected void onPostExecute(final List<NavigationItem> items) {
        if (isAlive()) {
            mDrawerList.setAdapter(new NavDrawerAdapter(mainActivity, items));

            // Sets click events
            mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    mainActivity.commitPending();
                    String navigation = mFragmentWeakReference.get().getResources().getStringArray(R.array
                            .navigation_list_codes)[items.get(position)
                            .getArrayIndex()];
                    onNavigationItemclicked.onNavigationItemclicked(mDrawerList.getItemAtPosition(position));
                    mainActivity.updateNavigation(navigation);
                    mDrawerList.setItemChecked(position, true);
                    if (mDrawerCategoriesList != null)
                        mDrawerCategoriesList.setItemChecked(0, false); // Called to force redraw
                    // Reset intent
                    mainActivity.getIntent().setAction(Intent.ACTION_MAIN);

                    // Call method to update notes list
                    mainActivity.initNotesList(mainActivity.getIntent());
                }
            });

            mDrawerList.justifyListViewHeightBasedOnChildren();
        }
    }


    private boolean isAlive() {
        return mFragmentWeakReference != null
                && mFragmentWeakReference.get() != null
                && mFragmentWeakReference.get().isAdded()
                && mFragmentWeakReference.get().getActivity() != null
                && !mFragmentWeakReference.get().getActivity().isFinishing();
    }


    private List<NavigationItem> buildMainMenu() {
        if (!isAlive()) {
            return new ArrayList<>();
        }
        // Sets the adapter for the MAIN navigation list view
        mDrawerList = (NonScrollableListView) mFragmentWeakReference.get().getView()
                .findViewById(R.id.drawer_nav_list);
        mDrawerCategoriesList = (NonScrollableListView) mFragmentWeakReference.get().getView()
                .findViewById(R.id.drawer_tag_list);

        String[] mNavigationArray = mFragmentWeakReference.get().getResources().getStringArray(R.array.navigation_list);
        TypedArray mNavigationIconsArray = mFragmentWeakReference.get().getResources().obtainTypedArray(R.array
                .navigation_list_icons);
        TypedArray mNavigationIconsSelectedArray = mFragmentWeakReference.get().getResources().obtainTypedArray(R.array
                .navigation_list_icons_selected);

        Ln.d("Starting lookup");
        DynamicNavigationLookupTable dynamicNavigationLookupTable = new DynamicNavigationLookupTable();
        dynamicNavigationLookupTable.init(mainActivity);
        Ln.d("Finished lookup");

        final List<NavigationItem> items = new ArrayList<>();
        for (int i = 0; i < mNavigationArray.length; i++) {
            if (!checkSkippableItem(dynamicNavigationLookupTable, i)) {
                NavigationItem item = new NavigationItem(i, mNavigationArray[i], mNavigationIconsArray.getResourceId(i,
                        0), mNavigationIconsSelectedArray.getResourceId(i, 0));
                items.add(item);
            }
        }
        return items;
    }


    private boolean checkSkippableItem(DynamicNavigationLookupTable dynamicNavigationLookupTable, int i) {
        boolean skippable = false;
        SharedPreferences prefs = mainActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
        boolean dynamicMenu = prefs.getBoolean(Constants.PREF_DYNAMIC_MENU, true);
        switch (i) {
            case Navigation.REMINDERS:
                if (dynamicMenu && dynamicNavigationLookupTable.getReminders() == 0)
                    skippable = true;
                break;
            case Navigation.UNCATEGORIZED:
                boolean showUncategorized = prefs.getBoolean(Constants.PREF_SHOW_UNCATEGORIZED, false);
                if (dynamicMenu && (!showUncategorized || dynamicNavigationLookupTable.getUncategorized() == 0))
                    skippable = true;
                break;
            case Navigation.ARCHIVE:
                if (dynamicMenu && dynamicNavigationLookupTable.getArchived() == 0)
                    skippable = true;
                break;
            case Navigation.TRASH:
                if (dynamicMenu && dynamicNavigationLookupTable.getTrashed() == 0)
                    skippable = true;
                break;
        }
        return skippable;
    }

}