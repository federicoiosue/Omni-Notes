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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.SettingsActivity;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.NavigationItem;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.adapters.NavDrawerCategoryAdapter;
import it.feio.android.omninotes.models.views.NonScrollableListView;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Fonts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class CategoryMenuTask extends AsyncTask<Void, Void, List<Category>> {

    private final WeakReference<Fragment> mFragmentWeakReference;
    private final MainActivity mainActivity;
    private NonScrollableListView mDrawerCategoriesList;
    private View settingsView;
    private NonScrollableListView mDrawerList;


    public CategoryMenuTask(Fragment mFragment) {
        mFragmentWeakReference = new WeakReference<>(mFragment);
        this.mainActivity = (MainActivity) mFragment.getActivity();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDrawerList = (NonScrollableListView) mFragmentWeakReference.get().getView()
                .findViewById(R.id.drawer_nav_list);
    }


    @Override
    protected List<Category> doInBackground(Void... params) {
        return buildCategoryMenu();
    }


    @Override
    protected void onPostExecute(final List<Category> categories) {
        if (isAlive()) {
            mDrawerCategoriesList.setAdapter(new NavDrawerCategoryAdapter(mainActivity, categories,
                    mainActivity.getNavigationTmp()));

            // Sets click events
            mDrawerCategoriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                    // Commits pending deletion or archiviation
                    mainActivity.commitPending();
                    // Stops search service
                    if (mainActivity.getSearchMenuItem() != null && MenuItemCompat.isActionViewExpanded(mainActivity
                            .getSearchMenuItem()))
                        MenuItemCompat.collapseActionView(mainActivity.getSearchMenuItem());

                    Object item = mDrawerCategoriesList.getAdapter().getItem(position);
                    // Ensuring that clicked item is not the ListView header
                    if (item != null) {
                        Category tag = (Category) item;
                        selectNavigationItem(mDrawerCategoriesList, position);
                        mainActivity.updateNavigation(String.valueOf(tag.getId()));
                        mDrawerCategoriesList.setItemChecked(position, true);
                        if (mDrawerList != null)
                            mDrawerList.setItemChecked(0, false); // Forces redraw
                        mainActivity.initNotesList(mainActivity.getIntent());
                    }
                }
            });

            // Sets long click events
            mDrawerCategoriesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
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
                }
            });

//        // Restores listview position when turning back to list
//        if (mDrawerCategoriesList != null && categories.size() > 0) {
//            if (mDrawerCategoriesList.getCount() > listViewPosition) {
//                mDrawerCategoriesList.setSelectionFromTop(listViewPosition, listViewPositionOffset);
//            } else {
//                mDrawerCategoriesList.setSelectionFromTop(0, 0);
//            }
//        }

            mDrawerCategoriesList.justifyListViewHeightBasedOnChildren();
        }
    }


    private boolean isAlive() {
        if (mFragmentWeakReference != null
                && mFragmentWeakReference.get() != null
                && mFragmentWeakReference.get().isAdded()
                && mFragmentWeakReference.get().getActivity() != null
                && !mFragmentWeakReference.get().getActivity().isFinishing()) {
            return true;
        }
        return false;
    }


    private List<Category> buildCategoryMenu() {
        // Retrieves data to fill tags list
        ArrayList<Category> categories = DbHelper.getInstance(mainActivity).getCategories();

        // Inflater used for header and footer
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // Inflation of header view
        View categoriesListHeader = inflater.inflate(R.layout.drawer_category_list_header, null);

        // Inflation of Settings view
        settingsView = ((ViewStub) mFragmentWeakReference.get().getView().findViewById(R.id.settings_placeholder));
        if (settingsView != null) {
            ((ViewStub) settingsView).inflate();
            Fonts.overrideTextSize(mainActivity,
                    mainActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS),
                    settingsView);
            settingsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent settingsIntent = new Intent(mainActivity, SettingsActivity.class);
                    mainActivity.startActivity(settingsIntent);
                }
            });
        } else {
            settingsView = mFragmentWeakReference.get().getView().findViewById(R.id.settings_view);
        }

        View settingsViewCat = inflater.inflate(R.layout.drawer_category_list_footer, null);
        Fonts.overrideTextSize(mainActivity,
                mainActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS),
                settingsViewCat);
        settingsViewCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(mainActivity, SettingsActivity.class);
                mainActivity.startActivity(settingsIntent);
            }
        });

        mDrawerCategoriesList = (NonScrollableListView) mFragmentWeakReference.get().getView().findViewById(R.id
                .drawer_tag_list);

        if (mDrawerCategoriesList.getAdapter() == null) {
            mDrawerCategoriesList.addFooterView(settingsViewCat);
        }
        if (categories.size() == 0) {
            categoriesListHeader.setVisibility(View.GONE);
            settingsViewCat.setVisibility(View.GONE);
            settingsView.setVisibility(View.VISIBLE);
        } else if (categories.size() > 0) {
            categoriesListHeader.setVisibility(View.VISIBLE);
            settingsViewCat.setVisibility(View.VISIBLE);
            settingsView.setVisibility(View.GONE);
        }

        return categories;
    }


    /**
     * Swaps fragments in the main content view
     */
    private void selectNavigationItem(ListView list, int position) {
        Object itemSelected = list.getItemAtPosition(position);
        final String mTitle;
        if (itemSelected.getClass().isAssignableFrom(NavigationItem.class)) {
            mTitle = ((NavigationItem) itemSelected).getText();
            // Is a category
        } else {
            mTitle = ((Category) itemSelected).getName();
        }
        // Navigation drawer is closed after a while to avoid lag
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mainActivity.getSupportActionBar().setTitle(mTitle);
                mainActivity.getDrawerLayout().closeDrawer(GravityCompat.START);
            }
        }, 500);
    }

}