package it.feio.android.omninotes.models.views;

import android.os.Build;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.listeners.AbsListViewScrollDetector;
import it.feio.android.omninotes.models.listeners.OnFabItemClickedListener;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Display;
import it.feio.android.omninotes.utils.Navigation;

import static android.support.v4.view.ViewCompat.animate;

public class Fab {

    private FloatingActionsMenu fab;
    private boolean fabAllowed;
    private boolean fabHidden = true;
    private boolean fabExpanded = false;
    private final ListView listView;
    private boolean expandOnLongClick;

    OnFabItemClickedListener onFabItemClickedListener;


    public Fab(View fabView, ListView listView, boolean expandOnLongClick) {
        this.fab = (FloatingActionsMenu) fabView;
        this.listView = listView;
        this.expandOnLongClick = expandOnLongClick;
        init();
    }

    private void init() {
        AddFloatingActionButton fabAddButton = (AddFloatingActionButton) fab.findViewById(com.getbase
                .floatingactionbutton.R.id.fab_expand_menu_button);
        fabAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandOnLongClick) {
                    performAction(v);
                } else {
                    performToggle();
                }
            }
        });
        fabAddButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!expandOnLongClick) {
                    performAction(v);
                } else {
                    performToggle();
                }
                return true;
            }
        });
        listView.setOnScrollListener(
                new AbsListViewScrollDetector() {
                    public void onScrollUp() {
                        if (fab != null) {
                            fab.collapse();
                            hideFab();
                        }
                    }


                    public void onScrollDown() {
                        if (fab != null) {
                            fab.collapse();
                            showFab();
                        }
                    }
                });

        fab.findViewById(R.id.fab_checklist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabItemClickedListener.OnFabItemClick(v.getId());
            }
        });
        fab.findViewById(R.id.fab_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabItemClickedListener.OnFabItemClick(v.getId());
            }
        });

        // In KitKat bottom padding is added by navbar height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int navBarHeight = Display.getNavigationBarHeightKitkat(OmniNotes.getAppContext());
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                    navBarHeight + DensityUtil.pxToDp(params.bottomMargin, OmniNotes.getAppContext()));
            fab.setLayoutParams(params);
        }
    }

    private void performToggle() {
        fabExpanded = !fabExpanded;
        fab.toggle();
    }

    private void performAction(View v) {
        if (fabExpanded) {
            fab.toggle();
            fabExpanded = false;
        } else {
            onFabItemClickedListener.OnFabItemClick(v.getId());
        }
    }


    public void showFab() {
        if (fab != null && fabAllowed && isFabHidden()) {
            animateFab(0, View.VISIBLE, View.VISIBLE);
            fabHidden = false;
        }
    }


    public void hideFab() {
        if (fab != null && !isFabHidden()) {
            fab.collapse();
            animateFab(fab.getHeight() + getMarginBottom(fab), View.VISIBLE, View.INVISIBLE);
            fabHidden = true;
        }
    }


    public boolean isFabHidden() {
        return fabHidden;
    }


    public void animateFab(int translationY, final int visibilityBefore, final int visibilityAfter) {
        animate(fab).setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(Constants.FAB_ANIMATION_TIME)
                .translationY(translationY)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        fab.setVisibility(visibilityBefore);
                    }


                    @Override
                    public void onAnimationEnd(View view) {
                        fab.setVisibility(visibilityAfter);
                    }


                    @Override
                    public void onAnimationCancel(View view) {
                    }
                });
    }


    public void setFabAllowed(boolean allowed) {
        if (allowed) {
            boolean showFab = Navigation.checkNavigation(new Integer[]{Navigation.NOTES, Navigation.CATEGORY});
            if (showFab) {
                fabAllowed = true;
            }
        } else {
            fabAllowed = false;
        }
    }


    private int getMarginBottom(View view) {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    public void setOnFabItemClickedListener(OnFabItemClickedListener onFabItemClickedListener) {
        this.onFabItemClickedListener = onFabItemClickedListener;
    }
}
