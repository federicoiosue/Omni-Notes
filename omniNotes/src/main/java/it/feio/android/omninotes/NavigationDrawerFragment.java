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

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import it.feio.android.omninotes.async.CategoryMenuTask;
import it.feio.android.omninotes.async.MainMenuTask;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.NavigationItem;
import it.feio.android.omninotes.models.listeners.OnNavigationItemClickedListener;
import it.feio.android.omninotes.utils.Display;
import roboguice.util.Ln;


/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements OnNavigationItemClickedListener {

    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    private MainActivity mActivity;
    private CharSequence mTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        //prefs = mActivity.prefs;
    }


    /**
     * Initialization of compatibility navigation drawer
     */
    public void init() {
        Ln.d("Started navigation drawer initialization");

        mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        mDrawerLayout.setFocusableInTouchMode(false);

        // Setting specific bottom margin for Kitkat with translucent nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View leftDrawer = getView().findViewById(R.id.left_drawer);
            int leftDrawerBottomPadding = Display.getNavigationBarHeightKitkat(getActivity());
            leftDrawer.setPadding(leftDrawer.getPaddingLeft(), leftDrawer.getPaddingTop(),
                    leftDrawer.getPaddingRight(), leftDrawerBottomPadding);
        }

        buildMainMenu();
        Ln.d("Finished main menu initialization");
        buildCategoriesMenu();
        Ln.d("Finished categories menu initialization");

        // ActionBarDrawerToggle± ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(mActivity,
                mDrawerLayout,
                getMainActivity().getToolbar(),
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                // Restore title
                mActivity.getSupportActionBar().setTitle(mTitle);
                // Call to onPrepareOptionsMenu()
                mActivity.supportInvalidateOptionsMenu();
            }


            public void onDrawerOpened(View drawerView) {
                // Commits all pending actions
                mActivity.commitPending();
                // Finishes action mode
                mActivity.finishActionMode();
                mTitle = mActivity.getSupportActionBar().getTitle();
                mActivity.getSupportActionBar().setTitle(mActivity.getApplicationContext().getString(R.string
                        .app_name));
                mActivity.supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                // Show instructions on first launch
//				final String instructionName = Constants.PREF_TOUR_PREFIX + "navdrawer";
//				if (AppTourHelper.isStepTurn(mActivity, instructionName)) {
//					ArrayList<Integer[]> list = new ArrayList<Integer[]>();
////					list.add(new Integer[] { R.id.menu_add_category, R.string.tour_listactivity_tag_title,
////							R.string.tour_listactivity_tag_detail, ShowcaseView.ITEM_ACTION_ITEM });
//					mActivity.showCaseView(list, new OnShowcaseAcknowledged() {
//						@Override
//						public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
//							AppTourHelper.completeStep(mActivity, instructionName);
//							mDrawerLayout.closeDrawer(GravityCompat.START);
//
//							// Attaches a dummy image as example
//							Note note = new Note();
//							Attachment attachment = new Attachment(BitmapHelper.getUri(mActivity,
//									R.drawable.ic_launcher), Constants.MIME_TYPE_IMAGE);
//							note.getAttachmentsList().add(attachment);
//							note.setTitle("http://www.opensource.org");
//							mActivity.editNote(note);
//						}
//					});
//				}
            }
        };

        // Styling options
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        Ln.d("Finished navigation drawer initialization");
    }


    private void buildCategoriesMenu() {
        CategoryMenuTask task = new CategoryMenuTask(this, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void buildMainMenu() {
        MainMenuTask task = new MainMenuTask(this, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onNavigationItemclicked(Object navigationItem) {
        if (navigationItem.getClass().isAssignableFrom(NavigationItem.class)) {
            mTitle = ((NavigationItem) navigationItem).getText();
            // Is a category
        } else {
            mTitle = ((Category) navigationItem).getName();
        }
        // Navigation drawer is closed after a while to avoid lag
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivity.getSupportActionBar().setTitle(mTitle);
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }, 500);
    }


    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
