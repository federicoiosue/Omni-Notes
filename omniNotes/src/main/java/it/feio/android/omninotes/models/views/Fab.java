package it.feio.android.omninotes.models.views;

import android.os.Build;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
    private boolean fabHidden;
    private boolean fabExpanded;
    private final ListView listView;
    private boolean expandOnLongClick;
    private View overlay;

    OnFabItemClickedListener onFabItemClickedListener;


    public Fab(View fabView, ListView listView, boolean expandOnLongClick) {
        this.fab = (FloatingActionsMenu) fabView;
        this.listView = listView;
        this.expandOnLongClick = expandOnLongClick;
        init();
    }


    private void init() {
        this.fabHidden = true;
        this.fabExpanded = false;

        AddFloatingActionButton fabAddButton = (AddFloatingActionButton) fab.findViewById(R.id.fab_expand_menu_button);
        fabAddButton.setOnClickListener(v -> {
			if (!isExpanded() && expandOnLongClick) {
				performAction(v);
			} else {
				performToggle();
			}
		});
        fabAddButton.setOnLongClickListener(v -> {
			if (!expandOnLongClick) {
				performAction(v);
			} else {
				performToggle();
			}
			return true;
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

        fab.findViewById(R.id.fab_checklist).setOnClickListener(onClickListener);
        fab.findViewById(R.id.fab_camera).setOnClickListener(onClickListener);

        if (!expandOnLongClick) {
            View noteBtn = fab.findViewById(R.id.fab_note);
            noteBtn.setVisibility(View.VISIBLE);
            noteBtn.setOnClickListener(onClickListener);
        }

        // In KitKat bottom padding is added by navbar height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int navBarHeight = Display.getNavigationBarHeightKitkat(OmniNotes.getAppContext());
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                    navBarHeight + DensityUtil.pxToDp(params.bottomMargin, OmniNotes.getAppContext()));
            fab.setLayoutParams(params);
        }
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onFabItemClickedListener.OnFabItemClick(v.getId());
        }
    };

    public void performToggle() {
        fabExpanded = !fabExpanded;
        fab.toggle();
        toggleOverlay();
    }


    private void toggleOverlay() {
        if (this.overlay != null) {
            if (fabExpanded) {
                this.overlay.setVisibility(View.VISIBLE);
            } else {
                this.overlay.setVisibility(View.GONE);
            }
        }
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
        if (fab != null && fabAllowed && fabHidden) {
            animateFab(0, View.VISIBLE, View.VISIBLE);
            fabHidden = false;
        }
    }


    public void hideFab() {
        if (fab != null && !fabHidden) {
            fab.collapse();
            animateFab(fab.getHeight() + getMarginBottom(fab), View.VISIBLE, View.INVISIBLE);
            fabHidden = true;
            fabExpanded = false;
            toggleOverlay();
        }
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


    public void setAllowed(boolean allowed) {
        fabAllowed = allowed;
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


    public void setOverlay(View overlay) {
        this.overlay = overlay;
        this.overlay.setOnClickListener(v -> performToggle());
    }


    public void setOverlay(int colorResurce) {
        View overlayView = new View(OmniNotes.getAppContext());
        overlayView.setBackgroundResource(colorResurce);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                .MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        overlayView.setLayoutParams(params);
        overlayView.setVisibility(View.GONE);
        overlayView.setOnClickListener(v -> performToggle());
        ViewGroup parent = ((ViewGroup) fab.getParent());
        parent.addView(overlayView, parent.indexOfChild(fab));
        this.overlay = overlayView;
    }

    public boolean isExpanded() {
        return fabExpanded;
    }
}
