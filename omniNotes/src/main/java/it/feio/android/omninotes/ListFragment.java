/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.afollestad.materialdialogs.MaterialDialog;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews.OnShowcaseAcknowledged;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.neopixl.pixlui.components.textview.TextView;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.async.NoteLoaderTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.*;
import it.feio.android.omninotes.models.adapters.NavDrawerCategoryAdapter;
import it.feio.android.omninotes.models.adapters.NoteAdapter;
import it.feio.android.omninotes.models.listeners.AbsListViewScrollDetector;
import it.feio.android.omninotes.models.listeners.OnNotesLoadedListener;
import it.feio.android.omninotes.models.listeners.OnViewTouchedListener;
import it.feio.android.omninotes.models.views.InterceptorLinearLayout;
import it.feio.android.omninotes.utils.*;
import it.feio.android.omninotes.utils.Display;
import it.feio.android.pixlui.links.RegexPatternsConstants;
import it.feio.android.pixlui.links.UrlCompleter;
import roboguice.util.Ln;

import java.util.*;
import java.util.regex.Matcher;

import static android.support.v4.view.ViewCompat.animate;


public class ListFragment extends Fragment implements OnNotesLoadedListener, OnViewTouchedListener, UndoBarController.UndoListener {

	static final int REQUEST_CODE_DETAIL = 1;
	private static final int REQUEST_CODE_CATEGORY = 2;
	private static final int REQUEST_CODE_CATEGORY_NOTES = 3;

	private DynamicListView list;
	private List<Note> selectedNotes = new ArrayList<Note>();
	private Note swipedNote;
    private List<Note> modifiedNotes = new ArrayList<Note>();
	private SearchView searchView;
    private MenuItem searchMenuItem;
	private TextView empyListItem;
	private AnimationDrawable jinglesAnimation;
	private int listViewPosition;
	private int listViewPositionOffset;
	private boolean sendToArchive;
	private SharedPreferences prefs;
	private ListFragment mFragment;
    private android.support.v7.view.ActionMode actionMode;

	// Undo archive/trash
	private boolean undoTrash = false;
	private boolean undoArchive = false;
	private boolean undoCategorize = false;
	private Category undoCategorizeCategory = null;
	// private Category removedCategory;
	private SparseArray<Note> undoNotesList = new SparseArray<Note>();
	// Used to remember removed categories from notes
	private Map<Note, Category> undoCategoryMap = new HashMap<Note, Category>();

	// Search variables
	private String searchQuery;
	private String searchTags;
	private boolean goBackOnToggleSearchLabel = false;
	private TextView listFooter;

//    private NoteCardArrayMultiChoiceAdapter listAdapter;
    private NoteAdapter listAdapter;
    private int layoutSelected;
    private UndoBarController ubc;

    //    Fab
    private FloatingActionsMenu fab;
    private FloatingActionButton fabAddNote;
    private FloatingActionButton fabAddChecklist;
    private boolean fabHidden;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFragment = this;
		prefs = ((MainActivity) getActivity()).prefs;

		setHasOptionsMenu(true);
		setRetainInstance(false);
	}


	@Override
	public void onStart() {
		// GA tracking
		OmniNotes.getGaTracker().set(Fields.SCREEN_NAME, getClass().getName());
		OmniNotes.getGaTracker().send(MapBuilder.createAppView().build());
		super.onStart();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("listViewPosition")) {
				listViewPosition = savedInstanceState.getInt("listViewPosition");
				listViewPositionOffset = savedInstanceState.getInt("listViewPositionOffset");
				searchQuery = savedInstanceState.getString("searchQuery");
				searchTags = savedInstanceState.getString("searchTags");
			}
		}
		return inflater.inflate(R.layout.fragment_list, container, false);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Restores savedInstanceState
		if (savedInstanceState != null) {
			((MainActivity) getActivity()).navigationTmp = savedInstanceState.getString("navigationTmp");
		}

		// Easter egg initialization
		initEasterEgg();

		// Listview initialization
		initListView();
//        list = (CardListView) getActivity().findViewById(R.id.list);

        initFab();

		// Activity title initialization
		initTitle();

		ubc = new UndoBarController(getActivity().findViewById(R.id.undobar), this);
	}

    private void initFab() {
        fab = (FloatingActionsMenu) getActivity().findViewById(R.id.fab);
        list.setOnScrollListener(
                new AbsListViewScrollDetector() {
                    public void onScrollUp() {
                        if (fab != null) {
                            fab.collapse();
                            showFab();
                        }
                    }
                    public void onScrollDown() {
                        if (fab != null) {
                            fab.collapse();
                            hideFab();
                        }
                    }
                });

        fabAddNote = (FloatingActionButton) fab.findViewById(R.id.fab_new_note);
        fabAddNote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editNote(new Note());
            }
        });
        fabAddChecklist = (FloatingActionButton) fab.findViewById(R.id.fab_new_checklist);
        fabAddChecklist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = new Note();
                note.setChecklist(true);
                editNote(note);
            }
        });

        // In KitKat bottom padding is added by navbar height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int navBarHeight = Display.getNavigationBarHeightKitkat(getActivity());
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin,params.rightMargin,
                    navBarHeight + DensityUtil.pxToDp(params.bottomMargin, getActivity()));
            fab.setLayoutParams(params);
        }
    }


    /**
	 * Activity title initialization based on navigation
	 */
	private void initTitle() {
		String[] navigationList = getResources().getStringArray(R.array.navigation_list);
		String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
		String navigation = prefs.getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		int index = Arrays.asList(navigationListCodes).indexOf(navigation);
		CharSequence title = "";
		// If is a traditional navigation item
		if (index >= 0 && index < navigationListCodes.length) {
			title = navigationList[index];
		} else {
			ArrayList<Category> categories = DbHelper.getInstance(getActivity()).getCategories();
			for (Category tag : categories) {
				if (navigation.equals(String.valueOf(tag.getId()))) title = tag.getName();
			}
		}

		title = title == null ? getString(R.string.title_activity_list) : title;
		((MainActivity) getActivity()).setActionBarTitle(title.toString());
	}


	/**
	 * Starts a little animation on Mr.Jingles!
	 */
	private void initEasterEgg() {
		empyListItem = (TextView) getActivity().findViewById(R.id.empty_list);
		empyListItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (jinglesAnimation == null) {
					jinglesAnimation = (AnimationDrawable) empyListItem.getCompoundDrawables()[1];
					empyListItem.post(new Runnable() {
						public void run() {
							if (jinglesAnimation != null) jinglesAnimation.start();
						}
					});
				} else {
					stopJingles();
				}
			}
		});
	}


	private void stopJingles() {
		if (jinglesAnimation != null) {
			jinglesAnimation.stop();
			jinglesAnimation = null;
			empyListItem.setCompoundDrawablesWithIntrinsicBounds(0, R.animator.jingles_animation, 0, 0);

		}
	}


	@Override
	public void onPause() {
		super.onPause();

		commitPending();
		stopJingles();
		Crouton.cancelAllCroutons();

		// Clears data structures
		// getSelectedNotes().clear();
//		if (listAdapter != null) {
//			listAdapter.clearSelectedItems();
//		}
		list.clearChoices();
		if (getActionMode() != null) {
			getActionMode().finish();
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		refreshListScrollPosition();
		outState.putInt("listViewPosition", listViewPosition);
		outState.putInt("listViewPositionOffset", listViewPositionOffset);
		outState.putString("searchQuery", searchQuery);
		outState.putString("searchTags", searchTags);
	}


	private void refreshListScrollPosition() {
		if (list != null) {
			listViewPosition = list.getFirstVisiblePosition();
			View v = list.getChildAt(0);
			listViewPositionOffset = (v == null) ? 0 : v.getTop();
		}
	}


	@SuppressWarnings("static-access")
	@Override
	public void onResume() {
		super.onResume();
		initNotesList(getActivity().getIntent());

		// Navigation drawer initialization to ensure data refresh
		((MainActivity) getActivity()).initNavigationDrawer();
		// Removes navigation drawer forced closed status
		if (((MainActivity) getActivity()).getDrawerLayout() != null) {
			((MainActivity) getActivity()).getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}

		// Restores again DefaultSharedPreferences too reload in case of data
		// erased from Settings
        prefs = getActivity().getSharedPreferences(Constants.PREFS_NAME, getActivity().MODE_MULTI_PROCESS);

		// Menu is invalidated to start again instructions tour if requested
		if (!prefs.getBoolean(Constants.PREF_TOUR_PREFIX + "list", false)) {
			getActivity().supportInvalidateOptionsMenu();
		}
	}

    private final class ModeCallback implements android.support.v7.view.ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate the menu for the CAB
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_list, menu);
			actionMode = mode;
            hideFab();
            return true;
		}


		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Here you can make any necessary updates to the activity when
			// the CAB is removed. By default, selected items are
			// deselected/unchecked.
			for (int i = 0; i < listAdapter.getSelectedItems().size(); i++) {
				int key = listAdapter.getSelectedItems().keyAt(i);
				View v = list.getChildAt(key - list.getFirstVisiblePosition());
				if (listAdapter.getCount() > key && listAdapter.getItem(key) != null && v != null) {
					listAdapter.restoreDrawable(listAdapter.getItem(key), v.findViewById(R.id.card_layout));
				}
			}

            // Backups modified notes in another structure to perform post-elaborations
            modifiedNotes = new ArrayList<Note>(getSelectedNotes());

			// Clears data structures
			selectedNotes.clear();
			listAdapter.clearSelectedItems();
			list.clearChoices();

            showFab();

            actionMode = null;
			Ln.d("Closed multiselection contextual menu");

			// Updates app widgets
			BaseActivity.notifyAppWidgets(getActivity());
		}


		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			prepareActionModeMenu();
			return true;
		}


		@Override
		public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            Integer[] protectedActions = {R.id.menu_select_all, R.id.menu_merge};
            if (!Arrays.asList(protectedActions).contains(item.getItemId())) {
                ((MainActivity)getActivity()).requestPassword(getActivity(), getSelectedNotes(), new PasswordValidator() {
                    @Override
                    public void onPasswordValidated(boolean passwordConfirmed) {
                        if (passwordConfirmed) {
                            performAction(item, mode);
                        }
                    }
                });
            } else {
                performAction(item, mode);
            }
			return true;
		}
	}


    private void showFab() {
        if (fab != null) {
            int translationY = fabHidden ? 0 : fab.getHeight() + getMarginBottom(fab);
            animate(fab).setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(Constants.FAB_ANIMATION_TIME)
                    .translationY(translationY);
            fabHidden = false;
        }
    }


    private void hideFab() {
        if (fab != null) {
            fab.collapse();
            int translationY = fabHidden ? 0 : fab.getHeight() + getMarginBottom(fab);
            animate(fab).setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(Constants.FAB_ANIMATION_TIME)
                    .translationY(translationY);
            fabHidden = true;
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


//    public void onCABItemClicked(final MenuItem item) {
//        Integer[] protectedActions = {R.id.menu_select_all, R.id.menu_merge};
//        if (!Arrays.asList(protectedActions).contains(item.getItemId())) {
//            ((MainActivity) getActivity()).requestPassword(getActivity(), getSelectedNotes(), new PasswordValidator() {
//                @Override
//                public void onPasswordValidated(boolean passwordConfirmed) {
//                    if (passwordConfirmed) {
//                        performAction(item, getActionMode());
//                    }
//                }
//            });
//        } else {
//            performAction(item, getActionMode());
//        }
//    }

	public void finishActionMode() {
		if (getActionMode() != null) {
            getActionMode().finish();
		}
	}


	/**
	 * Manage check/uncheck of notes in list during multiple selection phase
	 */
	private void toggleListViewItem(View view, int position) {
		Note note = listAdapter.getItem(position);
		LinearLayout v = (LinearLayout) view.findViewById(R.id.card_layout);
		if (!getSelectedNotes().contains(note)) {
			getSelectedNotes().add(note);
			listAdapter.addSelectedItem(position);
			v.setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
		} else {
			getSelectedNotes().remove(note);
			listAdapter.removeSelectedItem(position);
			listAdapter.restoreDrawable(note, v);
		}
		prepareActionModeMenu();

		// Close CAB if no items are selected
		if (getSelectedNotes().size() == 0) {
			finishActionMode();
		}

	}


	/**
	 * Notes list initialization. Data, actions and callback are defined here.
	 */
	private void initListView() {
		list = (DynamicListView) getActivity().findViewById(R.id.list);

		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		list.setItemsCanFocus(false);

		// If device runs KitKat a footer is added to list to avoid
		// navigation bar transparency covering items
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int navBarHeight = Display.getNavigationBarHeightKitkat(getActivity());
			listFooter = new TextView(getActivity().getApplicationContext());
			listFooter.setHeight(navBarHeight + 5);
			// To avoid useless events on footer
			listFooter.setOnClickListener(null);
			list.addFooterView(listFooter);
		}

		// Note long click to start CAB mode
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (view.equals(listFooter)) return true;
				if (getActionMode() != null) { return false; }
				// Start the CAB using the ActionMode.Callback defined above
                ((MainActivity)getActivity()).startSupportActionMode(new ModeCallback());
				toggleListViewItem(view, position);
				setCabTitle();
				return true;
			}
		});

		// Note single click listener managed by the activity itself
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (view.equals(listFooter)) return;
				if (getActionMode() == null) {
					Note note = listAdapter.getItem(position);
					editNote(note);
					return;
				}
				// If in CAB mode
				toggleListViewItem(view, position);
				setCabTitle();
			}
		});

		((InterceptorLinearLayout) getActivity().findViewById(R.id.list_root))
				.setOnViewTouchedListener(this);
	}

    @Override
    public void onViewTouchOccurred(MotionEvent ev) {
        commitPending();
    }


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
		// Initialization of SearchView
		initSearchView(menu);
//		initShowCase();
	}


	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// Defines the conditions to set actionbar items visible or not
		boolean drawerOpen = (((MainActivity) getActivity()).getDrawerLayout() != null && ((MainActivity) getActivity())
                .getDrawerLayout().isDrawerOpen(GravityCompat.START));
		boolean expandedView = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true);
		// "Add" item must be shown only from main navigation;
		boolean showAdd = Navigation.checkNavigation(new Integer[] { Navigation.NOTES, Navigation.CATEGORY });

		menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
        if (!drawerOpen && showAdd) {
            showFab();
        } else {
            hideFab();
        }
		menu.findItem(R.id.menu_sort).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_expanded_view).setVisible(!drawerOpen && !expandedView);
		menu.findItem(R.id.menu_contracted_view).setVisible(!drawerOpen && expandedView);
		menu.findItem(R.id.menu_empty_trash).setVisible(!drawerOpen && Navigation.checkNavigation(Navigation.TRASH));
	}

    private void prepareActionModeMenu() {
        Menu menu = getActionMode().getMenu();
        int navigation = Navigation.getNavigation();
        boolean showArchive = navigation == Navigation.NOTES || navigation == Navigation.REMINDERS
                || navigation == Navigation.CATEGORY;
        boolean showUnarchive = navigation == Navigation.ARCHIVE || navigation == Navigation.CATEGORY;

        if (navigation == Navigation.TRASH) {
            menu.findItem(R.id.menu_untrash).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
        } else {
            if (getSelectedCount() == 1) {
                menu.findItem(R.id.menu_share).setVisible(true);
                menu.findItem(R.id.menu_merge).setVisible(false);
                menu.findItem(R.id.menu_archive)
                        .setVisible(showArchive && !getSelectedNotes().get(0).isArchived
                                ());
                menu.findItem(R.id.menu_unarchive)
                        .setVisible(showUnarchive && getSelectedNotes().get(0).isArchived
                                ());
            } else {
                menu.findItem(R.id.menu_share).setVisible(false);
                menu.findItem(R.id.menu_merge).setVisible(true);
                menu.findItem(R.id.menu_archive).setVisible(showArchive);
                menu.findItem(R.id.menu_unarchive).setVisible(showUnarchive);

            }
            menu.findItem(R.id.menu_category).setVisible(true);
            menu.findItem(R.id.menu_tags).setVisible(true);
            menu.findItem(R.id.menu_trash).setVisible(true);
        }
        menu.findItem(R.id.menu_select_all).setVisible(true);

        setCabTitle();
    }


    private int getSelectedCount() {
        return getSelectedNotes().size();
    }


    private void setCabTitle() {
        if (getActionMode() != null) {
            int title = getSelectedCount();
            getActionMode().setTitle(String.valueOf(title));
        }
    }


	/**
	 * SearchView initialization. It's a little complex because it's not using SearchManager but is implementing on its
	 * own.
	 */
	@SuppressLint("NewApi")
	private void initSearchView(final Menu menu) {

		// Save item as class attribute to make it collapse on drawer opening
		searchMenuItem = menu.findItem(R.id.menu_search);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
		searchView
				.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

		// Expands the widget hiding other actionbar icons
		searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                menu.findItem(R.id.menu_sort).setVisible(!hasFocus);
                menu.findItem(R.id.menu_contracted_view).setVisible(!hasFocus);
                menu.findItem(R.id.menu_expanded_view).setVisible(!hasFocus);
                menu.findItem(R.id.menu_tags).setVisible(hasFocus);
            }
        });

		MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Reinitialize notes list to all notes when search is
                // collapsed
                searchQuery = null;
                if (getActivity().findViewById(R.id.search_layout).getVisibility() == View.VISIBLE) {
                    toggleSearchLabel(false);
                }
                getActivity().getIntent().setAction(Intent.ACTION_MAIN);
                initNotesList(getActivity().getIntent());
                return true;
            }


            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(new OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String arg0) {
                        return prefs.getBoolean("settings_instant_search", false);
                    }

                    @Override
                    public boolean onQueryTextChange(String pattern) {
                        View searchLayout = getActivity().findViewById(R.id.search_layout);
                        if (prefs.getBoolean("settings_instant_search", false) && searchLayout != null) {
                            searchTags = null;
                            searchQuery = pattern;
                            NoteLoaderTask mNoteLoaderTask = new NoteLoaderTask(mFragment, mFragment);
                            mNoteLoaderTask.execute("getNotesByPattern", pattern);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                return true;
            }
        });
	}


	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
        Integer[] protectedActions = {R.id.menu_empty_trash};
        if (Arrays.asList(protectedActions).contains(item.getItemId())) {
            ((MainActivity)getActivity()).requestPassword(getActivity(), getSelectedNotes(), new PasswordValidator() {
                @Override
                public void onPasswordValidated(boolean passwordConfirmed) {
                    if (passwordConfirmed) {
                        performAction(item, null);
                    }
                }
            });
        } else {
            performAction(item, null);
        }
		return super.onOptionsItemSelected(item);
	}


    /**
     * Performs one of the ActionBar button's actions after checked notes protection
     */
    public boolean performAction(MenuItem item, ActionMode actionMode) {
        if (actionMode == null) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    if (((MainActivity) getActivity()).getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                        ((MainActivity) getActivity()).getDrawerLayout().closeDrawer(GravityCompat.START);
                    } else {
                        ((MainActivity) getActivity()).getDrawerLayout().openDrawer(GravityCompat.START);
                    }
                    break;
                case R.id.menu_tags:
                    filterByTags();
                    break;
                case R.id.menu_sort:
                    sortNotes();
                    break;
                case R.id.menu_expanded_view:
                    switchNotesView();
                    break;
                case R.id.menu_contracted_view:
                    switchNotesView();
                    break;
                case R.id.menu_empty_trash:
                    emptyTrash();
                    break;
            }
        } else {
            switch (item.getItemId()) {
                case R.id.menu_category:
                    categorizeNotes();
                    break;
                case R.id.menu_tags:
                    tagNotes();
                    break;
                case R.id.menu_share:
                    share();
                    break;
                case R.id.menu_merge:
                    merge();
                    break;
                case R.id.menu_archive:
                    archiveNotes(true);
//                    actionMode.finish();
                    break;
                case R.id.menu_unarchive:
                    archiveNotes(false);
//                    actionMode.finish();
                    break;
                case R.id.menu_trash:
                    trashNotes(true);
                    break;
                case R.id.menu_untrash:
                    trashNotes(false);
                    break;
                case R.id.menu_delete:
                    deleteNotes();
                    break;
                case R.id.menu_select_all:
                    selectAllNotes();
                    break;
//                case R.id.menu_synchronize:
//                    synchronizeSelectedNotes();
//                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


	private void switchNotesView() {
		boolean expandedView = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true);
		prefs.edit().putBoolean(Constants.PREF_EXPANDED_VIEW, !expandedView).commit();
		// Change list view
		initNotesList(getActivity().getIntent());
		// Called to switch menu voices
		getActivity().supportInvalidateOptionsMenu();
	}


	void editNote(final Note note) {
        fab.collapse();
		if (note.isLocked() && !prefs.getBoolean("settings_password_access", false)) {
			BaseActivity.requestPassword(getActivity(), new PasswordValidator() {
				@Override
				public void onPasswordValidated(boolean passwordConfirmed) {
					if (passwordConfirmed) {
						note.setPasswordChecked(true);
						editNote2(note);
					}
				}
			});
		} else {
			editNote2(note);
		}
	}


	void editNote2(Note note) {

		if (note.get_id() == 0) {
			Ln.d("Adding new note");
			// if navigation is a tag it will be set into note
			try {
				int tagId;
				if (!TextUtils.isEmpty(((MainActivity) getActivity()).navigationTmp)) {
					tagId = Integer.parseInt(((MainActivity) getActivity()).navigationTmp);
				} else {
					tagId = Integer.parseInt(((MainActivity) getActivity()).navigation);
				}
				note.setCategory(DbHelper.getInstance(getActivity()).getCategory(tagId));
			} catch (NumberFormatException e) {}
		} else {
			Ln.d("Editing note with id: " + note.get_id());
		}

		// Current list scrolling position is saved to be restored later
		refreshListScrollPosition();

		// Fragments replacing
		((MainActivity) getActivity()).switchToDetail(note);
	}


	@Override
	public// Used to show a Crouton dialog after saved (or tried to) a note
	void onActivityResult(int requestCode, final int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
			case REQUEST_CODE_DETAIL:
				if (intent != null) {

					String intentMsg = intent.getStringExtra(Constants.INTENT_DETAIL_RESULT_MESSAGE);
					// If no message is returned nothing will be shown
					if (!TextUtils.isEmpty(intentMsg)) {
						final String message = intent.getStringExtra(Constants.INTENT_DETAIL_RESULT_MESSAGE);
						// Dialog retarded to give time to activity's views of being
						// completely initialized
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								// The dialog style is choosen depending on result code
								switch (resultCode) {
									case Activity.RESULT_OK:
										Crouton.makeText(getActivity(), message, ONStyle.CONFIRM)
												.show();
										break;
									case Activity.RESULT_FIRST_USER:
										Crouton.makeText(getActivity(), message, ONStyle.INFO).show();
										break;
									case Activity.RESULT_CANCELED:
										Crouton.makeText(getActivity(), message, ONStyle.ALERT).show();
										break;

									default:
										break;
								}
							}
						}, 800);
					}
				}
				break;

			case REQUEST_CODE_CATEGORY:
				// Dialog retarded to give time to activity's views of being
				// completely initialized
				// The dialog style is choosen depending on result code
				switch (resultCode) {
					case Activity.RESULT_OK:
						Crouton.makeText(getActivity(), R.string.category_saved, ONStyle.CONFIRM).show();
						((MainActivity) getActivity()).initNavigationDrawer();
						break;
					case Activity.RESULT_CANCELED:
						Crouton.makeText(getActivity(), R.string.category_deleted, ONStyle.ALERT)
								.show();
						break;

					default:
						break;
				}

				break;

			case REQUEST_CODE_CATEGORY_NOTES:
				if (intent != null) {
					Category tag = intent.getParcelableExtra(Constants.INTENT_TAG);
					categorizeNotesExecute(tag);
				}
				break;

			default:
				break;
		}

	}


	private void sortNotes() {
		// Two array are used, one with db columns and a corrispective with
		// column names human readables
		final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
		final String[] arrayDialog = getResources().getStringArray(R.array.sortable_columns_human_readable);

		int selected = Arrays.asList(arrayDb).indexOf(prefs.getString(Constants.PREF_SORTING_COLUMN, arrayDb[0]));

		// Dialog and events creation
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
		builder.title(R.string.select_sorting_column)
//              .setSingleChoiceItems(arrayDialog, selected,
//				new DialogInterface.OnClickListener() {
//
//					// On choosing the new criteria will be saved into
//					// preferences and listview redesigned
//					public void onClick(DialogInterface dialog, int which) {
//						prefs.edit().putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which]).commit();
//						initNotesList(getActivity().getIntent());
//						// Resets list scrolling position
//						listViewPositionOffset = 0;
//						listViewPosition = 0;
//						list.setSelectionFromTop(listViewPosition, listViewPositionOffset);
//						// Updates app widgets
//						BaseActivity.notifyAppWidgets(getActivity());
//						dialog.dismiss();
//					}
//				});
                .items(arrayDialog)
                .itemsCallbackSingleChoice(selected, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        // On choosing the new criteria will be saved into
                        // preferences and listview redesigned
                        prefs.edit().putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which]).commit();
                        initNotesList(getActivity().getIntent());
                        // Resets list scrolling position
                        listViewPositionOffset = 0;
                        listViewPosition = 0;
                        list.setSelectionFromTop(listViewPosition, listViewPositionOffset);
                        // Updates app widgets
                        BaseActivity.notifyAppWidgets(getActivity());
                        dialog.dismiss();
                    }
                })
                .show();
	}


	/**
	 * Empties trash deleting all the notes
	 */
	private void emptyTrash() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setMessage(R.string.empty_trash_confirmation)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < listAdapter.getCount(); i++) {
                            getSelectedNotes().add(getSelectedNotes().get(i));
                        }
                        deleteNotesExecute();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
		alertDialogBuilder.create().show();
	}


	/**
	 * Notes list adapter initialization and association to view
	 */
	void initNotesList(Intent intent) {
		Ln.d("initNotesList intent: " + intent.getAction());

		NoteLoaderTask mNoteLoaderTask = new NoteLoaderTask(mFragment, mFragment);

		// Search for a tag
		// A workaround to simplify it's to simulate normal search
		if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getCategories() != null
				&& intent.getCategories().contains(Intent.CATEGORY_BROWSABLE)) {
			searchTags = intent.getDataString().replace(UrlCompleter.HASHTAG_SCHEME, "");
			goBackOnToggleSearchLabel = true;
		}

		// Searching
		if (searchTags != null || searchQuery != null || Intent.ACTION_SEARCH.equals(intent.getAction())) {

			// Using tags
			if (searchTags != null && intent.getStringExtra(SearchManager.QUERY) == null) {
				searchQuery = searchTags;
				mNoteLoaderTask.execute("getNotesByTag", searchQuery);
			} else {
				// Get the intent, verify the action and get the query
				if (intent.getStringExtra(SearchManager.QUERY) != null) {
					searchQuery = intent.getStringExtra(SearchManager.QUERY);
					searchTags = null;
				}
				if (((MainActivity) getActivity()).loadNotesSync) {
					onNotesLoaded((ArrayList<Note>) DbHelper.getInstance(getActivity()).getNotesByPattern(searchQuery));
				} else {
					mNoteLoaderTask.execute("getNotesByPattern", searchQuery);
				}
				((MainActivity) getActivity()).loadNotesSync = Constants.LOAD_NOTES_SYNC;
			}

			toggleSearchLabel(true);

		} else {
			// Check if is launched from a widget with categories to set tag
			if ((Constants.ACTION_WIDGET_SHOW_LIST.equals(intent.getAction()) && intent
					.hasExtra(Constants.INTENT_WIDGET))
					|| !TextUtils.isEmpty(((MainActivity) getActivity()).navigationTmp)) {
				String widgetId = intent.hasExtra(Constants.INTENT_WIDGET) ? intent.getExtras()
						.get(Constants.INTENT_WIDGET).toString() : null;
				if (widgetId != null) {
					String sqlCondition = prefs.getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
					String pattern = DbHelper.KEY_CATEGORY + " = ";
					if (sqlCondition.lastIndexOf(pattern) != -1) {
						String tagId = sqlCondition.substring(sqlCondition.lastIndexOf(pattern) + pattern.length())
								.trim();
						((MainActivity) getActivity()).navigationTmp = !TextUtils.isEmpty(tagId) ? tagId : null;
					}
				}
				intent.removeExtra(Constants.INTENT_WIDGET);
				if (((MainActivity) getActivity()).loadNotesSync) {
					onNotesLoaded((ArrayList<Note>) DbHelper.getInstance(getActivity()).getNotesByCategory(
							((MainActivity) getActivity()).navigationTmp));
				} else {
					mNoteLoaderTask.execute("getNotesByTag", ((MainActivity) getActivity()).navigationTmp);
				}
				((MainActivity) getActivity()).loadNotesSync = Constants.LOAD_NOTES_SYNC;

				// Gets all notes
			} else {
				if (((MainActivity) getActivity()).loadNotesSync) {
					onNotesLoaded((ArrayList<Note>) DbHelper.getInstance(getActivity()).getAllNotes(true));
				} else {
					mNoteLoaderTask.execute("getAllNotes", true);
				}
				((MainActivity) getActivity()).loadNotesSync = Constants.LOAD_NOTES_SYNC;
			}
		}
	}


	public void toggleSearchLabel(boolean activate) {
		View searchLabel = getActivity().findViewById(R.id.search_layout);
		boolean isActive = searchLabel.getVisibility() == View.VISIBLE;
		if (activate) {
			((android.widget.TextView) getActivity().findViewById(R.id.search_query)).setText(Html.fromHtml("<i>"
					+ getString(R.string.search) + ":</i> " + searchQuery));
			searchLabel.setVisibility(View.VISIBLE);
			getActivity().findViewById(R.id.search_cancel).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSearchLabel(false);
                }
            });
		}

		else {
            if (isActive) {
                getActivity().findViewById(R.id.search_layout).setVisibility(View.GONE);
                searchTags = null;
                searchQuery = null;
                if (!goBackOnToggleSearchLabel) {
                    getActivity().getIntent().setAction(Intent.ACTION_MAIN);
                    if (searchView != null) {
                        MenuItemCompat.collapseActionView(searchMenuItem);
                    }
                    initNotesList(getActivity().getIntent());
                } else {
                    getActivity().onBackPressed();
                }
                goBackOnToggleSearchLabel = false;
                if (Intent.ACTION_VIEW.equals(getActivity().getIntent().getAction())) {
                    getActivity().getIntent().setAction(null);
                }
            }
        }
	}


	@Override
	public void onNotesLoaded(ArrayList<Note> notes) {
		layoutSelected = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true) ? R.layout.note_layout_expanded
				: R.layout.note_layout;

//        initCards(notes);


		listAdapter = new NoteAdapter(getActivity(), layoutSelected, notes);

		// A specifical behavior is performed basing on navigation
//		SwipeDismissAdapter adapter = new SwipeDismissAdapter(listAdapter, new OnDismissCallback() {
//			@Override
//            public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] reverseSortedPositions) {

        list.enableSwipeToDismiss(new OnDismissCallback() {
            @Override
            public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] reverseSortedPositions) {

				// Avoids conflicts with action mode
				finishActionMode();

				for (int position : reverseSortedPositions) {
					Note note = listAdapter.getItem(position);
					getSelectedNotes().add(note);

					// Depending on settings and note status this action will...
					// ...restore
					if (Navigation.checkNavigation(Navigation.TRASH)) {
						trashNotes(false);
					}
					// ...removes category
					else if (Navigation.checkNavigation(Navigation.CATEGORY)) {
						categorizeNotesExecute(null);
					} else {
						// ...trash
						if (prefs.getBoolean("settings_swipe_to_trash", false)
								|| Navigation.checkNavigation(Navigation.ARCHIVE)) {
							trashNotes(true);
                        // ...archive
						} else {
							archiveNotes(true);
						}
					}
				}
			}
		});
//		adapter.setAbsListView(list);
		list.setAdapter(listAdapter);

		// Replace listview with Mr. Jingles if it is empty
		if (notes.size() == 0) list.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Restores listview position when turning back to list
		if (list != null && notes.size() > 0) {
			if (list.getCount() > listViewPosition) {
				list.setSelectionFromTop(listViewPosition, listViewPositionOffset);
			} else {
				list.setSelectionFromTop(0, 0);
			}
		}

		// Fade in the list view
		animate(list).setDuration(getResources().getInteger(R.integer.list_view_fade_anim)).alpha(1);
	}


//    private void initCards(final ArrayList<Note> notes) {
//
//        // If device runs KitKat a footer is added to list to avoid
//        // navigation bar transparency covering items
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            int navBarHeight = Display.getNavigationBarHeightKitkat(getActivity());
//            listFooter = new TextView(getActivity().getApplicationContext());
//            listFooter.setHeight(navBarHeight + 5);
//            // To avoid useless events on footer
//            listFooter.setOnClickListener(null);
//            list.addFooterView(listFooter);
//        }
//
//        ArrayList<Card> cards = new ArrayList<Card>();
//        for (int i = 0; i < notes.size(); i++) {
//            cards.add(initCard(notes.get(i)));
//        }
//
//        listAdapter = new NoteCardArrayMultiChoiceAdapter(getActivity(), cards, notes);
//        listAdapter.setUndoBarUIElements(new UndoBarController.DefaultUndoBarUIElements(){
//            @Override
//            public int getUndoBarId() {
//                return R.id.list_card_undobar;
//            }
//
//            @Override
//            public int getUndoBarMessageId() {
//                return R.id.list_card_undobar_message;
//            }
//
//            @Override
//            public int getUndoBarButtonId() {
//                return R.id.list_card_undobar_button;
//            }
//            @Override
//            public SwipeDirectionEnabled isEnabledUndoBarSwipeAction() {
//                return SwipeDirectionEnabled.TOPBOTTOM;
//            }
//            @Override
//            public AnimationType getAnimationType() {
//                return AnimationType.TOPBOTTOM;
//            }
//        });
//        //Enable undo controller!
//        listAdapter.setEnableUndo(true);
//
//        if (list !=null){
//            list.setAdapter(listAdapter);
//            list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//            listAdapter.setOnActionItemClickedListener(this);
//        }
//
//		// Replace listview with Mr. Jingles if it is empty
//		if (notes.size() == 0) list.setEmptyView(getActivity().findViewById(R.id.empty_list));
//
//		// Restores listview position when turning back to list
//		if (list != null && notes.size() > 0) {
//			if (list.getCount() > listViewPosition) {
//				list.setSelectionFromTop(listViewPosition, listViewPositionOffset);
//			} else {
//				list.setSelectionFromTop(0, 0);
//			}
//		}
//
//        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//        list.setMultiChoiceModeListener(listAdapter);
//
////        ((InterceptorLinearLayout) getActivity().findViewById(R.id.list_root)).setOnViewTouchedListener(this);
//    }
//
//
//    private NoteCard initCard(Note note) {
//        NoteCard card = new NoteCard(getActivity(), note, layoutSelected);
//        card.setOnClickListener(new Card.OnCardClickListener() {
//            @Override
//            public void onClick(Card card, View view) {
//                if (getActionMode() == null) {
//                    editNote(((NoteCard)card).getNote());
//                } else {
//                    listAdapter.toggleSelection(((NoteCard)card).getNote());
//                }
//            }
//        });
//        card.setOnLongClickListener(new Card.OnLongCardClickListener() {
//            @Override
//            public boolean onLongClick(Card card, View view) {
//                if (getActionMode() != null) {
//                    listAdapter.toggleSelection(((NoteCard)card).getNote());
//                    return false;
//                }
//                getActivity().findViewById(R.id.toolbar).startActionMode(listAdapter);
//                listAdapter.toggleSelection(((NoteCard) card).getNote());
//
//                fab.setVisibility(View.GONE);
//                view.setSelected(true);
//                return true;
//            }
//        });
//        card.setSwipeable(true);
//        card.setOnSwipeListener(new Card.OnSwipeListener() {
//            @Override
//            public void onSwipe(Card card) {
//                ListFragment.this.onSwipe(card);
//            }
//        });
//        card.setOnUndoSwipeListListener(new Card.OnUndoSwipeListListener() {
//            @Override
//            public void onUndoSwipe(Card card) {
//                ListFragment.this.onUndoSwipe(card);
//            }
//        });
//        card.setOnUndoHideSwipeListListener(new Card.OnUndoHideSwipeListListener() {
//            @Override
//            public void onUndoHideSwipe(Card card) {
//                ListFragment.this.onUndoHideSwipe(card);
//            }
//        });
//        return card;
//    }
//
//
//    private void onUndoHideSwipe(Card card) {
//        Note note = ((NoteCard) card).getNote();
//        DbHelper.getInstance(getActivity()).updateNote(note, false);
//    }
//
//
//    private void onSwipe(Card card) {
//        // Avoids conflicts with action mode
//        finishActionMode();
//
//        Note note = ((NoteCard) card).getNote();
////        undoNotesList.put(listAdapter.getPosition(note) + undoNotesList.size(), note);
////        modifiedNotes.add(note);
////        swipedNote = note;
//
//        // Depending on settings and note status this action will...
//        // ...restore
//        if (Navigation.checkNavigation(Navigation.TRASH)) {
////            trashNotes(false);
//            ((NoteCard) card).getNote().setTrashed(false);
//        }
//        // ...removes category
//        else if (Navigation.checkNavigation(Navigation.CATEGORY)) {
////            categorizeNotesExecute(null);
//            undoCategoryMap.put(note, note.getCategory());
//            ((NoteCard) card).getNote().setCategory(null);
//        } else {
//            // ...trash
//            if (prefs.getBoolean("settings_swipe_to_trash", false)
//                    || Navigation.checkNavigation(Navigation.ARCHIVE)) {
////                trashNotes(true);
//                ((NoteCard) card).getNote().setTrashed(true);
//                // ...archive
//            } else {
////                archiveNotes2(true);
//                ((NoteCard) card).getNote().setArchived(true);
//            }
//        }
//    }
//
//
//    private void onUndoSwipe(Card card) {
//        Note note = ((NoteCard) card).getNote();
//        // Depending on settings and note status this action will...
//        // ...restore
//        if (Navigation.checkNavigation(Navigation.TRASH)) {
//            ((NoteCard) card).getNote().setTrashed(true);
//        }
//        // ...removes category
//        else if (Navigation.checkNavigation(Navigation.CATEGORY)) {
//            for (Map.Entry<Note, Category> noteCategoryEntry : undoCategoryMap.entrySet()) {
//                if (noteCategoryEntry.getKey().get_id() == note.get_id()) {
//                    ((NoteCard) card).getNote().setCategory(noteCategoryEntry.getValue());
//                    undoCategoryMap.remove(noteCategoryEntry);
//                }
//            }
//        } else {
//            // ...trash
//            if (prefs.getBoolean("settings_swipe_to_trash", false)
//                    || Navigation.checkNavigation(Navigation.ARCHIVE)) {
//                ((NoteCard) card).getNote().setTrashed(false);
//                // ...archive
//            } else {
//                ((NoteCard) card).getNote().setArchived(false);
//            }
//        }
//    }


    /**
	 * Batch note trashing
	 */
	public void trashNotes(boolean trash) {
        int selectedNotesSize = getSelectedNotes().size();
		for (Note note : getSelectedNotes()) {
			// Restore it performed immediately, otherwise undo bar
			if (!trash) {
				trashNote(note, false);
				((MainActivity) getActivity()).initNavigationDrawer();
			} else {
				// Saves notes to be eventually restored at right position
				undoNotesList.put(listAdapter.getPosition(note) + undoNotesList.size(), note);
                modifiedNotes.add(note);
			}
			// Removes note adapter
			listAdapter.remove(note);
		}

		// If list is empty again Mr Jingles will appear again
		if (listAdapter.getCount() == 0)
			list.setEmptyView(getActivity().findViewById(R.id.empty_list));

		finishActionMode();

		// Advice to user
		if (trash) {
			Crouton.makeText(getActivity(), getString(R.string.note_trashed), ONStyle.WARN).show();
		} else {
			Crouton.makeText(getActivity(), getString(R.string.note_untrashed), ONStyle.INFO).show();
		}

		// Creation of undo bar
		if (trash) {
            ubc.showUndoBar(false, selectedNotesSize + " " + getString(R.string.trashed), null);
			undoTrash = true;
		} else {
			getSelectedNotes().clear();
		}
	}

    private android.support.v7.view.ActionMode getActionMode() {
        return actionMode;
    }


    private List<Note> getSelectedNotes() {
//        return listAdapter.getSelectedNotes();
        return selectedNotes;
    }


	/**
	 * Single note logical deletion
	 *
	 * @param note
	 *            Note to be deleted
	 */
	@SuppressLint("NewApi")
	protected void trashNote(Note note, boolean trash) {
		DbHelper.getInstance(getActivity()).trashNote(note, trash);
		// Update adapter content
		listAdapter.remove(note);
		// Informs about update
		Ln.d("Trashed/restored note with id '" + note.get_id() + "'");
	}


	/**
	 * Selects all notes in list
	 */
	private void selectAllNotes() {
		for (int i = 0; i < list.getChildCount(); i++) {
			LinearLayout v = (LinearLayout) list.getChildAt(i).findViewById(R.id.card_layout);
			// Checks null to avoid the footer
			if (v != null) {
				v.setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
			}
		}
		selectedNotes.clear();
		for (int i = 0; i < listAdapter.getCount(); i++) {
			selectedNotes.add(listAdapter.getItem(i));
            listAdapter.addSelectedItem(i);
		}
		prepareActionModeMenu();
		setCabTitle();
	}


	/**
	 * Batch note permanent deletion
	 */
	private void deleteNotes() {
		// Confirm dialog creation
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setMessage(R.string.delete_note_confirmation)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
                        ((MainActivity)getActivity()).requestPassword(getActivity(), getSelectedNotes(), new PasswordValidator() {
                            @Override
                            public void onPasswordValidated(boolean passwordConfirmed) {
                                if (passwordConfirmed) {
                                    deleteNotesExecute();
                                }
                            }
                        });
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {}
				});
		alertDialogBuilder.create().show();
	}


	/**
	 * Performs notes permanent deletion after confirmation by the user
	 */
	private void deleteNotesExecute() {
		for (Note note : getSelectedNotes()) {
			listAdapter.remove(note);
			((MainActivity) getActivity()).deleteNote(note);
		}

		// Clears data structures
//		listAdapter.clearSelectedItems();
		list.clearChoices();

		finishActionMode();

		// If list is empty again Mr Jingles will appear again
		if (listAdapter.getCount() == 0)
			list.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Advice to user
		Crouton.makeText(getActivity(), R.string.note_deleted, ONStyle.ALERT).show();
	}


    /**
     * Batch note archiviation
     */
    public void archiveNotes(boolean archive) {
        int selectedNotesSize = getSelectedNotes().size();
        // Used in undo bar commit
        sendToArchive = archive;

        for (Note note : getSelectedNotes()) {
            // If is restore it will be done immediately, otherwise the undo bar
            // will be shown
            if (!archive) {
                archiveNote(note, false);
            } else {
                // Saves notes to be eventually restored at right position
                undoNotesList.put(listAdapter.getPosition(note) + undoNotesList.size(), note);
                modifiedNotes.add(note);
            }

            // Updates adapter content. If actual navigation is a category
            // the item will not be removed but replaced to fit the new state
            if (!Navigation.checkNavigation(Navigation.CATEGORY)) {
                listAdapter.remove(note);
            } else {
                note.setArchived(archive);
                listAdapter.replace(note, listAdapter.getPosition(note));
            }
        }

        finishActionMode();

        // If list is empty again Mr Jingles will appear again
        if (listAdapter.getCount() == 0) list.setEmptyView(getActivity().findViewById(R.id.empty_list));

        // Advice to user
        int msg = archive ? R.string.note_archived : R.string.note_unarchived;
        Style style = archive ? ONStyle.WARN : ONStyle.INFO;
        Crouton.makeText(getActivity(), msg, style).show();

        // Creation of undo bar
        if (archive) {
            ubc.showUndoBar(false, selectedNotesSize + " " + getString(R.string.archived), null);
            undoArchive = true;
        } else {
            getSelectedNotes().clear();
        }
    }


    public void archiveNotes2(boolean archive) {
        // Used in undo bar commit
        sendToArchive = archive;

        // Updates adapter content. If actual navigation is a category
        // the item will not be removed but replaced to fit the new state
        if (Navigation.checkNavigation(Navigation.CATEGORY)) {
            swipedNote.setArchived(archive);
            listAdapter.replace(swipedNote, listAdapter.getPosition(swipedNote));
        }

        // If list is empty again Mr Jingles will appear again
        if (listAdapter.getCount() == 0) list.setEmptyView(getActivity().findViewById(R.id.empty_list));

        // Advice to user
        int msg = archive ? R.string.note_archived : R.string.note_unarchived;
        Style style = archive ? ONStyle.WARN : ONStyle.INFO;
        Crouton.makeText(getActivity(), msg, style).show();

        // Creation of undo bar
        if (archive) {
            undoArchive = true;
        } else {
            getSelectedNotes().clear();
        }
    }


	private void archiveNote(Note note, boolean archive) {
		// Deleting note using DbHelper
		DbHelper.getInstance(getActivity()).archiveNote(note, archive);
		// Update adapter content
		if (!Navigation.checkNavigation(Navigation.CATEGORY)) {
			listAdapter.remove(note);
		}
		// Informs the user about update
		BaseActivity.notifyAppWidgets(getActivity());
		Ln.d("Note with id '" + note.get_id() + "' " + (archive ? "archived" : "restored from archive"));
	}


	/**
	 * Categories addition and editing
	 */
	void editCategory(Category category) {
		Intent categoryIntent = new Intent(getActivity(), CategoryActivity.class);
		categoryIntent.putExtra(Constants.INTENT_TAG, category);
		startActivityForResult(categoryIntent, REQUEST_CODE_CATEGORY);
	}


	/**
	 * Associates to or removes categories
	 */
	private void categorizeNotes() {
		// Retrieves all available categories
		final ArrayList<Category> categories = DbHelper.getInstance(getActivity()).getCategories();

        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
				.title(R.string.categorize_as)
				.adapter(new NavDrawerCategoryAdapter(getActivity(), categories))
                .positiveText(R.string.add_category)
//                .neutralText(R.string.cancel)
                .negativeText(R.string.remove_category)
                .callback(new MaterialDialog.Callback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(getActivity(), CategoryActivity.class);
                        intent.putExtra("noHome", true);
                        startActivityForResult(intent, REQUEST_CODE_CATEGORY_NOTES);
                    }
//                    @Override
//                    public void onNegative(MaterialDialog materialDialog) {
//                        selectedNotes.clear();
//                        finishActionMode();
//                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        categorizeNotesExecute(null);
                    }
                }).build();

        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                categorizeNotesExecute(categories.get(position));
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                selectedNotes.clear();
                finishActionMode();
            }
        });


//        }).setNeutralButton(R.string.remove_category, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int id) {
//                        categorizeNotesExecute(null);
//					}
//				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int id) {
//
//					}
//				});

        dialog.show();
	}


	private void categorizeNotesExecute(Category category) {
		for (Note note : getSelectedNotes()) {
			// If is restore it will be done immediately, otherwise the undo bar
			// will be shown
			if (category != null) {
				categorizeNote(note, category);
			} else {
				// Saves categories associated to eventually undo
				undoCategoryMap.put(note, note.getCategory());
				// Saves notes to be eventually restored at right position
				undoNotesList.put(listAdapter.getPosition(note) + undoNotesList.size(), note);
                modifiedNotes.add(note);
			}
			// Update adapter content if actual navigation is the category
			// associated with actually cycled note
			if (Navigation.checkNavigation(Navigation.CATEGORY) && !Navigation.checkNavigationCategory(category)) {
				listAdapter.remove(note);
			} else {
				note.setCategory(category);
				listAdapter.replace(note, listAdapter.getPosition(note));
			}
		}

		// Clears data structures
//		listAdapter.clearSelectedItems();
//		list.clearChoices();
        finishActionMode();

		// Refreshes list
//		list.invalidateViews();

		// If list is empty again Mr Jingles will appear again
		if (listAdapter.getCount() == 0)
			list.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Refreshes navigation drawer if is set to show categories count numbers
		if (prefs.getBoolean("settings_show_category_count", false)) {
			((MainActivity) getActivity()).initNavigationDrawer();
		}

		if (getActionMode() != null) {
			getActionMode().finish();
		}

		// Advice to user
		String msg;
		if (category != null) {
			msg = getResources().getText(R.string.notes_categorized_as) + " '" + category.getName() + "'";
		} else {
			msg = getResources().getText(R.string.notes_category_removed).toString();
		}
		Crouton.makeText(getActivity(), msg, ONStyle.INFO).show();

		// Creation of undo bar
		if (category == null) {
			ubc.showUndoBar(false, getString(R.string.notes_category_removed), null);
			undoCategorize = true;
			undoCategorizeCategory = category;
		} else {
			getSelectedNotes().clear();
		}
	}


	private void categorizeNote(Note note, Category category) {
		note.setCategory(category);
		DbHelper.getInstance(getActivity()).updateNote(note, false);
	}


    /**
     * Bulk tag selected notes
     */
    private void tagNotes() {

        // Retrieves all available tags
        final List<String> tags = DbHelper.getInstance(getActivity()).getTags();

        // If there is no tag a message will be shown
        if (tags.size() == 0) {
            Crouton.makeText(getActivity(), R.string.no_tags_created, ONStyle.WARN).show();
            return;
        }

        Integer[] preselectedTags;
        if(selectedNotes.size() == 1) {
            List<Integer> t = new ArrayList<Integer>();
            List<String> noteTags = DbHelper.getInstance(getActivity()).getTags(selectedNotes.get(0));
            for (String noteTag : noteTags) {
                t.add(tags.indexOf(noteTag));
            }
            preselectedTags = t.toArray(new Integer[t.size()]);
        } else {
            preselectedTags = new Integer[]{};
        }

        // Selected tags
        final boolean[] selectedTags = new boolean[tags.size()];
        Arrays.fill(selectedTags, Boolean.FALSE);
        Integer[] multichoidSelected;

        String[] tagsArray = tags.toArray(new String[tags.size()]);

        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.select_tags)
                .items(tagsArray)
                .positiveText(R.string.ok)
                .itemsCallbackMultiChoice(preselectedTags, new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        dialog.dismiss();
                        for (Integer integer : which) {
                            selectedTags[integer] = true;
                        }
                        tagNotesExecute(tags, selectedTags);
                    }
                }).build();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                selectedNotes.clear();
                finishActionMode();
            }
        });

        dialog.show();
    }


	private void tagNotesExecute(List<String> tags, boolean[] selectedTags) {

		// Retrieves selected tags
		for (Note note : getSelectedNotes()) {

			HashMap<String, Boolean> tagsMap = new HashMap<String, Boolean>();
			Matcher matcher = RegexPatternsConstants.HASH_TAG.matcher(note.getTitle() + " " + note.getContent());
			while (matcher.find()) {
				tagsMap.put(matcher.group().trim(), true);
			}

			// String of choosen tags in order of selection
			StringBuilder sbTags = new StringBuilder();
			for (int i = 0; i < selectedTags.length; i++) {
				if (!selectedTags[i] || tagsMap.containsKey(tags.get(i))) continue;
				// To divide tags a head space is inserted
				if (sbTags.length() > 0) {
					sbTags.append(" ");
				}
				sbTags.append(tags.get(i));
			}

			sbTags.insert(0, System.getProperty("line.separator")).insert(0, System.getProperty("line.separator"));

			if (note.isChecklist()) {
				note.setTitle(note.getTitle() + sbTags);
			} else {
				note.setContent(note.getContent() + sbTags);
			}
			DbHelper.getInstance(getActivity()).updateNote(note, false);
		}

		// Clears data structures
//		listAdapter.clearSelectedItems();
		list.clearChoices();

		// Refreshes list
		list.invalidateViews();

		// If list is empty again Mr Jingles will appear again
		if (listAdapter.getCount() == 0)
			list.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Refreshes navigation drawer if is set to show categories count numbers
		if (prefs.getBoolean("settings_show_category_count", false)) {
			((MainActivity) getActivity()).initNavigationDrawer();
		}

		if (getActionMode() != null) {
			getActionMode().finish();
		}

		Crouton.makeText(getActivity(), R.string.tags_added, ONStyle.INFO).show();
	}


//	private void synchronizeSelectedNotes() {
//		new DriveSyncTask(getActivity()).execute(new ArrayList<Note>(getSelectedNotes()));
//		// Clears data structures
//		listAdapter.clearSelectedItems();
//		list.clearChoices();
//		finishActionMode();
//	}



    @Override
    public void onUndo(Parcelable undoToken) {
		// Cycles removed items to re-insert into adapter
		for (Note note : modifiedNotes) {
			//   Manages uncategorize or archive  undo
			if ( (undoCategorize && !Navigation.checkNavigationCategory(undoCategoryMap.get(note)))
				|| undoArchive && Navigation.checkNavigation(Navigation.CATEGORY)){
				if (undoCategorize) {
					note.setCategory(undoCategoryMap.get(note));
                } else if (undoArchive) {
					note.setArchived(false);
				}
				listAdapter.replace(note, listAdapter.getPosition(note));
			// Manages trash undo
			} else {
                list.insert(undoNotesList.keyAt(undoNotesList.indexOfValue(note)), note);
			}
		}

        selectedNotes.clear();
		undoNotesList.clear();
        modifiedNotes.clear();

		undoTrash = false;
		undoArchive = false;
		undoCategorize = false;
		undoNotesList.clear();
		undoCategoryMap.clear();
		undoCategorizeCategory = null;
		Crouton.cancelAllCroutons();

		if (getActionMode() != null) {
			getActionMode().finish();
		}
		ubc.hideUndoBar(false);
	}


	void commitPending() {
		if (undoTrash || undoArchive || undoCategorize) {

			for (Note note : modifiedNotes) {
				if (undoTrash)
					trashNote(note, true);
				else if (undoArchive)
					archiveNote(note, sendToArchive);
				else if (undoCategorize) categorizeNote(note, undoCategorizeCategory);
			}
			// Refreshes navigation drawer if is set to show categories count numbers
			if (prefs.getBoolean("settings_show_category_count", false)) {
				((MainActivity) getActivity()).initNavigationDrawer();
			}

			undoTrash = false;
			undoArchive = false;
			undoCategorize = false;
			undoCategorizeCategory = null;

			// Clears data structures
            modifiedNotes.clear();
			undoNotesList.clear();
			undoCategoryMap.clear();
//			listAdapter.clearSelectedItems();
			list.clearChoices();

//			ubc.hideUndoBar(false);
		}
	}


	private void initShowCase() {
		// Show instructions on first launch
		final String instructionName = Constants.PREF_TOUR_PREFIX + "list";
		if (AppTourHelper.isStepTurn(getActivity(), instructionName)) {
            ((MainActivity) getActivity()).getDrawerLayout().closeDrawer(GravityCompat.START);
			ArrayList<Integer[]> list = new ArrayList<Integer[]>();
			list.add(new Integer[] { 0, R.string.tour_listactivity_intro_title,
					R.string.tour_listactivity_intro_detail, ShowcaseView.ITEM_TITLE });
//			list.add(new Integer[] { R.id.fab, R.string.tour_listactivity_actions_title,
//					R.string.tour_listactivity_actions_detail, null });
			list.add(new Integer[] { 0, R.string.tour_listactivity_home_title, R.string.tour_listactivity_home_detail,
					ShowcaseView.ITEM_ACTION_HOME });
			((MainActivity) getActivity()).showCaseView(list, new OnShowcaseAcknowledged() {
				@Override
				public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
					AppTourHelper.completeStep(getActivity(), instructionName);
					((MainActivity) getActivity()).getDrawerLayout().openDrawer(GravityCompat.START);
				}
			});
		}

		// Show instructions on first launch
		final String instructionName2 = Constants.PREF_TOUR_PREFIX + "list2";
		if (AppTourHelper.isStepTurn(getActivity(), instructionName2)) {
			ArrayList<Integer[]> list = new ArrayList<Integer[]>();
			list.add(new Integer[] { null, R.string.tour_listactivity_final_title,
					R.string.tour_listactivity_final_detail, null });
			((MainActivity) getActivity()).showCaseView(list, new OnShowcaseAcknowledged() {
				@Override
				public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
					AppTourHelper.completeStep(getActivity(), instructionName2);
                    AppTourHelper.complete(getActivity());
				}
			});
		}
	}


	/**
	 * Shares the selected note from the list
	 */
	private void share() {
		// Only one note should be selected to perform sharing but they'll be cycled anyhow
		for (final Note note : getSelectedNotes()) {
            ((MainActivity) getActivity()).shareNote(note);
		}

		getSelectedNotes().clear();
		if (getActionMode() != null) {
			getActionMode().finish();
		}
	}


	/**
	 * Merges all the selected notes
	 */
	public void merge() {

		Note mergedNote = null;
		boolean locked = false;
		StringBuilder content = new StringBuilder();
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();

		for (Note note : getSelectedNotes()) {

			if (mergedNote == null) {
				mergedNote = new Note();
				mergedNote.setTitle(note.getTitle());
				content.append(note.getContent());

			} else {
				if (content.length() > 0
						&& (!TextUtils.isEmpty(note.getTitle()) || !TextUtils.isEmpty(note.getContent()))) {
					content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"))
							.append("----------------------").append(System.getProperty("line.separator"))
							.append(System.getProperty("line.separator"));
				}
				if (!TextUtils.isEmpty(note.getTitle())) {
					content.append(note.getTitle());
				}
				if (!TextUtils.isEmpty(note.getTitle()) && !TextUtils.isEmpty(note.getContent())) {
					content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
				}
				if (!TextUtils.isEmpty(note.getContent())) {
					content.append(note.getContent());
				}
			}

			locked = locked || note.isLocked();
			attachments.addAll(note.getAttachmentsList());
		}

		// Resets all the attachments id to force their note re-assign when saved
		for (Attachment attachment : attachments) {
			attachment.setId(0);
		}

		// Sets content text and attachments list
		mergedNote.setContent(content.toString());
		mergedNote.setLocked(locked);
		mergedNote.setAttachmentsList(attachments);

		getSelectedNotes().clear();
		if (getActionMode() != null) {
			getActionMode().finish();
		}

		// Sets the intent action to be recognized from DetailFragment and switch fragment
		getActivity().getIntent().setAction(Constants.ACTION_MERGE);
		((MainActivity) getActivity()).switchToDetail(mergedNote);
	}


	/**
	 * Search notes by tags
	 */
	private void filterByTags() {

		// Retrieves all available categories
		final List<String> tags = DbHelper.getInstance(getActivity()).getTags();

		// If there is no category a message will be shown
		if (tags.size() == 0) {
			Crouton.makeText(getActivity(), R.string.no_tags_created, ONStyle.WARN).show();
			return;
		}

		// Selected tags
		final boolean[] selectedTags = new boolean[tags.size()];
		Arrays.fill(selectedTags, Boolean.FALSE);

		// Dialog and events creation
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final String[] tagsArray = tags.toArray(new String[tags.size()]);
		builder.setTitle(R.string.select_tags)
				.setMultiChoiceItems(tagsArray, selectedTags, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						selectedTags[which] = isChecked;
					}
				}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Retrieves selected tags
						for (int i = 0; i < selectedTags.length; i++) {
							if (!selectedTags[i]) {
								tags.remove(tagsArray[i]);
							}
						}

						// Saved here to allow persisting search
						searchTags = tags.toString().substring(1, tags.toString().length() - 1).replace(" ", "");
						Intent intent = getActivity().getIntent();

                        // Hides keyboard
                        searchView.clearFocus();
                        KeyboardUtils.hideKeyboard(searchView);

						intent.removeExtra(SearchManager.QUERY);
						initNotesList(intent);

						// Fires an intent to search related notes
						// NoteLoaderTask mNoteLoaderTask = new NoteLoaderTask(mFragment, mFragment);
						// mNoteLoaderTask.execute("getNotesByTag", searchQuery);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
		builder.create().show();
	}


    public MenuItem getSearchMenuItem() {
        return searchMenuItem;
    }


//    @Override
//    public void onUndoBarHide(boolean undoOccurred) {
//        if (undoOccurred) {
//            onUndo(getSelectedNotes().get(0));
//        } else {
//            commitPending();
//        }
//    }


}
