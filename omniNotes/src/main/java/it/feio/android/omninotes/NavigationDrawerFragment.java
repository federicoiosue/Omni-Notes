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

import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.CategoryMenuTask;
import it.feio.android.omninotes.async.MainMenuTask;
import it.feio.android.omninotes.async.bus.*;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.NavigationItem;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Display;


public class NavigationDrawerFragment extends Fragment {

    static final int BURGER = 0;
    static final int ARROW = 1;

    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    private MainActivity mActivity;
    private boolean alreadyInitialized;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        initImage();
    }


    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }


    public void onEvent(DynamicNavigationReadyEvent event) {
        if (alreadyInitialized) {
            alreadyInitialized = false;
        } else {
            init();
        }
    }


    public void onEvent(CategoriesUpdatedEvent event) {
        init();
    }


    public void onEvent(NotesUpdatedEvent event) {
        alreadyInitialized = false;
    }


    public void onEvent(NotesLoadedEvent event) {
        // Removes navigation drawer forced closed status
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        init();
        alreadyInitialized = true;
    }


    public void onEvent(SwitchFragmentEvent event) {
        switch (event.direction) {
            case CHILDREN:
                animateBurger(ARROW);
                break;
            default:
                animateBurger(BURGER);
        }
    }


    public void onEvent(NavigationUpdatedEvent navigationUpdatedEvent) {
        if (navigationUpdatedEvent.navigationItem.getClass().isAssignableFrom(NavigationItem.class)) {
            mActivity.getSupportActionBar().setTitle(((NavigationItem) navigationUpdatedEvent.navigationItem).getText());
        } else {
            mActivity.getSupportActionBar().setTitle(((Category) navigationUpdatedEvent.navigationItem).getName());
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            new Handler().postDelayed(() -> EventBus.getDefault().post(new NavigationUpdatedNavDrawerClosedEvent
                    (navigationUpdatedEvent.navigationItem)), 400);
        }
    }


    public void init() {
        Log.d(Constants.TAG, "Started navigation drawer initialization");

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
        Log.d(Constants.TAG, "Finished main menu initialization");
        buildCategoriesMenu();
        Log.d(Constants.TAG, "Finished categories menu initialization");

        // ActionBarDrawerToggle± ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(mActivity,
                mDrawerLayout,
                getMainActivity().getToolbar(),
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                mActivity.supportInvalidateOptionsMenu();
            }


            public void onDrawerOpened(View drawerView) {
                // Commits all pending actions
                mActivity.commitPending();
                // Finishes action mode
                mActivity.finishActionMode();

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

        Log.d(Constants.TAG, "Finished navigation drawer initialization");
    }


    private void initImage() {
        ImageView navDrawerImage = (ImageView) getActivity().findViewById(R.id.navdrawer_image);
        ViewGroup.LayoutParams params = navDrawerImage.getLayoutParams();
        int statusBarHeight = Display.getStatusBarHeight(getActivity());
        params.height = params.height + statusBarHeight;
        navDrawerImage.setLayoutParams(params);
    }


    private void buildCategoriesMenu() {
        CategoryMenuTask task = new CategoryMenuTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void buildMainMenu() {
        MainMenuTask task = new MainMenuTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    void animateBurger(int targetShape) {
        if (mDrawerToggle != null) {
            if (targetShape != BURGER && targetShape != ARROW)
                return;
            ValueAnimator anim = ValueAnimator.ofFloat((targetShape + 1) % 2, targetShape);
            anim.addUpdateListener(valueAnimator -> {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                mDrawerToggle.onDrawerSlide(mDrawerLayout, slideOffset);
            });
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(500);
            anim.start();
        }
    }
}
