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
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews.OnShowcaseAcknowledged;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.melnykov.fab.FloatingActionButton;
import com.neopixl.pixlui.components.textview.TextView;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.omninotes.async.NoteLoaderTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.models.UndoBarController;
import it.feio.android.omninotes.models.UndoBarController.UndoListener;
import it.feio.android.omninotes.models.adapters.NavDrawerCategoryAdapter;
import it.feio.android.omninotes.models.adapters.NoteAdapter;
import it.feio.android.omninotes.models.listeners.OnNotesLoadedListener;
import it.feio.android.omninotes.models.listeners.OnViewTouchedListener;
import it.feio.android.omninotes.models.views.InterceptorLinearLayout;
import it.feio.android.omninotes.utils.AppTourHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Display;
import it.feio.android.omninotes.utils.KeyboardUtils;
import it.feio.android.omninotes.utils.Navigation;
import it.feio.android.pixlui.links.RegexPatternsConstants;
import it.feio.android.pixlui.links.UrlCompleter;
import roboguice.util.Ln;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public class ListFragment extends Fragment implements UndoListener, OnNotesLoadedListener, OnViewTouchedListener {

	static final int REQUEST_CODE_DETAIL = 1;
	private static final int REQUEST_CODE_CATEGORY = 2;
	private static final int REQUEST_CODE_CATEGORY_NOTES = 3;

	private ListView listView;
    private NoteAdapter mAdapter;
    private ActionMode mActionMode;
	private List<Note> selectedNotes = new ArrayList<Note>();
    private List<Note> modifiedNotes = new ArrayList<Note>();
	private SearchView searchView;
    private MenuItem searchMenuItem;
	private TextView empyListItem;
	private AnimationDrawable jinglesAnimation;
	private int listViewPosition;
	private int listViewPositionOffset;
	private UndoBarController ubc;
	private boolean sendToArchive;
	private SharedPreferences prefs;
	private ListFragment mFragment;

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

    // Floating actioon button
    private FloatingActionButton fab;


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

		// Activity title initialization
		initTitle();

		ubc = new UndoBarController(getActivity().findViewById(R.id.undobar), this);
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
		// selectedNotes.clear();
		if (mAdapter != null) {
			mAdapter.clearSelectedItems();
		}
		listView.clearChoices();
		if (mActionMode != null) {
			mActionMode.finish();
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
		if (listView != null) {
			listViewPosition = listView.getFirstVisiblePosition();
			View v = listView.getChildAt(0);
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

    private final class ModeCallback implements Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate the menu for the CAB
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_list, menu);
			mActionMode = mode;
			return true;
		}


		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Here you can make any necessary updates to the activity when
			// the CAB is removed. By default, selected items are
			// deselected/unchecked.
			for (int i = 0; i < mAdapter.getSelectedItems().size(); i++) {
				int key = mAdapter.getSelectedItems().keyAt(i);
				View v = listView.getChildAt(key - listView.getFirstVisiblePosition());
				if (mAdapter.getCount() > key && mAdapter.getItem(key) != null && v != null) {
					mAdapter.restoreDrawable(mAdapter.getItem(key), v.findViewById(R.id.card_layout));
				}
			}

            // Backups modified notes in another structure to perform post-elaborations
            modifiedNotes = new ArrayList<Note>(selectedNotes);

			// Clears data structures
			selectedNotes.clear();
			mAdapter.clearSelectedItems();
			listView.clearChoices();

			mActionMode = null;
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
                ((MainActivity)getActivity()).requestPassword(getActivity(), selectedNotes, new PasswordValidator() {
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


	public void finishActionMode() {
		selectedNotes.clear();
        modifiedNotes.clear();
		if (mActionMode != null) {
			mActionMode.finish();
		}
	}


	/**
	 * Manage check/uncheck of notes in list during multiple selection phase
	 */
	private void toggleListViewItem(View view, int position) {
		Note note = mAdapter.getItem(position);
		LinearLayout v = (LinearLayout) view.findViewById(R.id.card_layout);
		if (!selectedNotes.contains(note)) {
			selectedNotes.add(note);
			mAdapter.addSelectedItem(position);
			v.setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
		} else {
			selectedNotes.remove(note);
			mAdapter.removeSelectedItem(position);
			mAdapter.restoreDrawable(note, v);
		}
		prepareActionModeMenu();

		// Close CAB if no items are selected
		if (selectedNotes.size() == 0) {
			finishActionMode();
		}

	}


	/**
	 * Manages logic to show or not menu entries during action mode
	 */
	private void prepareActionModeMenu() {
		Menu menu = mActionMode.getMenu();
		int navigation = Navigation.getNavigation();
		boolean showArchive = navigation == Navigation.NOTES || navigation == Navigation.REMINDERS
				|| navigation == Navigation.CATEGORY;
		boolean showUnarchive = navigation == Navigation.ARCHIVED || navigation == Navigation.CATEGORY;

		if (navigation == Navigation.TRASH) {
			menu.findItem(R.id.menu_untrash).setVisible(true);
			menu.findItem(R.id.menu_delete).setVisible(true);
		} else {
			if (selectedNotes.size() == 1) {
				menu.findItem(R.id.menu_share).setVisible(true);
				menu.findItem(R.id.menu_merge).setVisible(false);
				menu.findItem(R.id.menu_archive)
						.setVisible(showArchive && !selectedNotes.get(0).isArchived());
				menu.findItem(R.id.menu_unarchive)
						.setVisible(showUnarchive && selectedNotes.get(0).isArchived());
			} else {
				menu.findItem(R.id.menu_share).setVisible(false);
				menu.findItem(R.id.menu_merge).setVisible(true);
				menu.findItem(R.id.menu_archive).setVisible(showArchive);
				menu.findItem(R.id.menu_unarchive).setVisible(showUnarchive);

			}
			menu.findItem(R.id.menu_category).setVisible(true);
			menu.findItem(R.id.menu_tags).setVisible(true);
			menu.findItem(R.id.menu_trash).setVisible(true);
//			menu.findItem(R.id.menu_synchronize).setVisible(true);
		}
		menu.findItem(R.id.menu_select_all).setVisible(true);
	}


	/**
	 * Notes list initialization. Data, actions and callback are defined here.
	 */
	private void initListView() {
		listView = (ListView) getActivity().findViewById(R.id.notes_list);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setItemsCanFocus(false);

		// If device runs KitKat a footer is added to list to avoid
		// navigation bar transparency covering items
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int navBarHeight = Display.getNavigationBarHeightKitkat(getActivity());
			listFooter = new TextView(getActivity().getApplicationContext());
			listFooter.setHeight(navBarHeight + 5);
			// To avoid useless events on footer
			listFooter.setOnClickListener(null);
			listView.addFooterView(listFooter);
		}

		// Note long click to start CAB mode
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (view.equals(listFooter)) return true;
				if (mActionMode != null) { return false; }
				// Start the CAB using the ActionMode.Callback defined above
				((MainActivity) getActivity()).startSupportActionMode(new ModeCallback());
				toggleListViewItem(view, position);
				setCabTitle();
				return true;
			}
		});

		// Note single click listener managed by the activity itself
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (view.equals(listFooter)) return;
				if (mActionMode == null) {
					Note note = mAdapter.getItem(position);
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

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.attachToListView(listView);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.hide();
                editNote(new Note());
            }
        });
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
		initShowCase();
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
            fab.show();
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.hide();
            fab.setVisibility(View.GONE);
        }
		menu.findItem(R.id.menu_sort).setVisible(!drawerOpen);
//		menu.findItem(R.id.menu_add_category).setVisible(drawerOpen);
		menu.findItem(R.id.menu_expanded_view).setVisible(!drawerOpen && !expandedView);
		menu.findItem(R.id.menu_contracted_view).setVisible(!drawerOpen && expandedView);
		menu.findItem(R.id.menu_empty_trash).setVisible(!drawerOpen && Navigation.checkNavigation(Navigation.TRASH));
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
                        if (prefs.getBoolean("settings_instant_search", false)) {
                            getActivity().findViewById(R.id.search_layout).setVisibility(View.GONE);
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
            ((MainActivity)getActivity()).requestPassword(getActivity(), selectedNotes, new PasswordValidator() {
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
//                case R.id.menu_add_category:
//                    editCategory(null);
//                    break;
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
                    actionMode.finish();
                    break;
                case R.id.menu_unarchive:
                    archiveNotes(false);
                    actionMode.finish();
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


	private void setCabTitle() {
		if (mActionMode == null) return;
		switch (selectedNotes.size()) {
			case 0:
				mActionMode.setTitle(null);
				break;
			default:
				mActionMode.setTitle(String.valueOf(selectedNotes.size()));
				break;
		}
	}




	void editNote(final Note note) {
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
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.select_sorting_column).setSingleChoiceItems(arrayDialog, selected,
				new DialogInterface.OnClickListener() {

					// On choosing the new criteria will be saved into
					// preferences and listview redesigned
					public void onClick(DialogInterface dialog, int which) {
						prefs.edit().putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which]).commit();
						initNotesList(getActivity().getIntent());
						// Resets list scrolling position
						listViewPositionOffset = 0;
						listViewPosition = 0;
						listView.setSelectionFromTop(listViewPosition, listViewPositionOffset);
						// Updates app widgets
						BaseActivity.notifyAppWidgets(getActivity());
						dialog.dismiss();
					}
				});
		builder.create().show();
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
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            selectedNotes.add(mAdapter.getItem(i));
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
		int layout = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true) ? R.layout.note_layout_expanded
				: R.layout.note_layout;
		mAdapter = new NoteAdapter(getActivity(), layout, notes);

		// A specifical behavior is performed basing on navigation
		SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, new OnDismissCallback() {
			@Override
			public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {

				// Avoids conflicts with action mode
				finishActionMode();

				for (int position : reverseSortedPositions) {
					Note note = mAdapter.getItem(position);
					selectedNotes.add(note);

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
								|| Navigation.checkNavigation(Navigation.ARCHIVED)) {
							trashNotes(true);
                        // ...archive
						} else {
							archiveNotes(true);
						}
					}
				}
			}
		});
//		adapter.setAbsListView(listView);
		listView.setAdapter(adapter);

		// Replace listview with Mr. Jingles if it is empty
		if (notes.size() == 0) listView.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Restores listview position when turning back to list
		if (listView != null && notes.size() > 0) {
			if (listView.getCount() > listViewPosition) {
				listView.setSelectionFromTop(listViewPosition, listViewPositionOffset);
			} else {
				listView.setSelectionFromTop(0, 0);
			}
		}

		// Fade in the list view
		animate(listView).setDuration(getResources().getInteger(R.integer.list_view_fade_anim)).alpha(1);
	}


	/**
	 * Batch note trashing
	 */
	public void trashNotes(boolean trash) {
		int selectedNotesSize = selectedNotes.size();
		for (Note note : selectedNotes) {
			// Restore it performed immediately, otherwise undo bar
			if (!trash) {
				trashNote(note, false);
				((MainActivity) getActivity()).initNavigationDrawer();
			} else {
				// Saves notes to be eventually restored at right position
				undoNotesList.put(mAdapter.getPosition(note) + undoNotesList.size(), note);
                modifiedNotes.add(note);
			}
			// Removes note adapter
			mAdapter.remove(note);
		}

		// If list is empty again Mr Jingles will appear again
		if (mAdapter.getCount() == 0)
			listView.setEmptyView(getActivity().findViewById(R.id.empty_list));

		if (mActionMode != null) {
			mActionMode.finish();
		}

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
			selectedNotes.clear();
		}
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
		mAdapter.remove(note);
		// Informs about update
		Ln.d("Trashed/restored note with id '" + note.get_id() + "'");
	}


	/**
	 * Selects all notes in list
	 */
	private void selectAllNotes() {
		for (int i = 0; i < listView.getChildCount(); i++) {
			LinearLayout v = (LinearLayout) listView.getChildAt(i).findViewById(R.id.card_layout);
			// Checks null to avoid the footer
			if (v != null) {
				v.setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
			}
		}
		selectedNotes.clear();
		for (int i = 0; i < mAdapter.getCount(); i++) {
			selectedNotes.add(mAdapter.getItem(i));
			mAdapter.addSelectedItem(i);
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
                        ((MainActivity)getActivity()).requestPassword(getActivity(), selectedNotes, new PasswordValidator() {
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
		for (Note note : selectedNotes) {
			mAdapter.remove(note);
			((MainActivity) getActivity()).deleteNote(note);
		}

		// Clears data structures
		mAdapter.clearSelectedItems();
		listView.clearChoices();

		finishActionMode();

		// If list is empty again Mr Jingles will appear again
		if (mAdapter.getCount() == 0)
			listView.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Advice to user
		Crouton.makeText(getActivity(), R.string.note_deleted, ONStyle.ALERT).show();
	}


    /**
     * Batch note archiviation
     */
    public void archiveNotes(boolean archive) {
        // Used in undo bar commit
        sendToArchive = archive;

        for (Note note : selectedNotes) {
            // If is restore it will be done immediately, otherwise the undo bar
            // will be shown
            if (!archive) {
                archiveNote(note, false);
            } else {
                // Saves notes to be eventually restored at right position
                undoNotesList.put(mAdapter.getPosition(note) + undoNotesList.size(), note);
                modifiedNotes.add(note);
            }

            // Updates adapter content. If actual navigation is a category
            // the item will not be removed but replaced to fit the new state
            if (!Navigation.checkNavigation(Navigation.CATEGORY)) {
                mAdapter.remove(note);
            } else {
                note.setArchived(archive);
                mAdapter.replace(note, mAdapter.getPosition(note));
            }
        }

        // Clears data structures
        mAdapter.clearSelectedItems();
        listView.clearChoices();

        // Refresh view
        listView.invalidateViews();

        // If list is empty again Mr Jingles will appear again
        if (mAdapter.getCount() == 0) listView.setEmptyView(getActivity().findViewById(R.id.empty_list));

        // Advice to user
        int msg = archive ? R.string.note_archived : R.string.note_unarchived;
        Style style = archive ? ONStyle.WARN : ONStyle.INFO;
        Crouton.makeText(getActivity(), msg, style).show();

        // Creation of undo bar
        if (archive) {
            ubc.showUndoBar(false, selectedNotes.size() + " " + getString(R.string.archived), null);
            undoArchive = true;
        } else {
            selectedNotes.clear();
        }
    }


	private void archiveNote(Note note, boolean archive) {
		// Deleting note using DbHelper
		DbHelper.getInstance(getActivity()).archiveNote(note, archive);
		// Update adapter content
		if (!Navigation.checkNavigation(Navigation.CATEGORY)) {
			mAdapter.remove(note);
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
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

		// Retrieves all available categories
		final ArrayList<Category> categories = DbHelper.getInstance(getActivity()).getCategories();

		alertDialogBuilder
				.setTitle(R.string.categorize_as)
				.setAdapter(new NavDrawerCategoryAdapter(getActivity(), categories),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// Moved to other method, ((MainActivity)getActivity()) way the same code
								// block can be called
								// also by onActivityResult when a new tag is created
                                categorizeNotesExecute(categories.get(which));
							}
						}).setPositiveButton(R.string.add_category, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(getActivity(), CategoryActivity.class);
						intent.putExtra("noHome", true);
						startActivityForResult(intent, REQUEST_CODE_CATEGORY_NOTES);
					}
				}).setNeutralButton(R.string.remove_category, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
                        categorizeNotesExecute(null);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						selectedNotes.clear();
						if (mActionMode != null) {
							mActionMode.finish();
						}
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}


	private void categorizeNotesExecute(Category category) {
		for (Note note : selectedNotes) {
			// If is restore it will be done immediately, otherwise the undo bar
			// will be shown
			if (category != null) {
				categorizeNote(note, category);
			} else {
				// Saves categories associated to eventually undo
				undoCategoryMap.put(note, note.getCategory());
				// Saves notes to be eventually restored at right position
				undoNotesList.put(mAdapter.getPosition(note) + undoNotesList.size(), note);
                modifiedNotes.add(note);
			}
			// Update adapter content if actual navigation is the category
			// associated with actually cycled note
			if (Navigation.checkNavigation(Navigation.CATEGORY) && !Navigation.checkNavigationCategory(category)) {
				mAdapter.remove(note);
			} else {
				note.setCategory(category);
				mAdapter.replace(note, mAdapter.getPosition(note));
			}
		}

		// Clears data structures
		mAdapter.clearSelectedItems();
		listView.clearChoices();

		// Refreshes list
		listView.invalidateViews();

		// If list is empty again Mr Jingles will appear again
		if (mAdapter.getCount() == 0)
			listView.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Refreshes navigation drawer if is set to show categories count numbers
		if (prefs.getBoolean("settings_show_category_count", false)) {
			((MainActivity) getActivity()).initNavigationDrawer();
		}

		if (mActionMode != null) {
			mActionMode.finish();
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
			selectedNotes.clear();
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
                tagNotesExecute(tags, selectedTags);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedNotes.clear();
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        });
        builder.create().show();
    }


	private void tagNotesExecute(List<String> tags, boolean[] selectedTags) {

		// Retrieves selected tags
		for (Note note : selectedNotes) {

			HashMap<String, Boolean> tagsMap = new HashMap<String, Boolean>();
			Matcher matcher = RegexPatternsConstants.HASH_TAG.matcher(note.getContent());
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
		mAdapter.clearSelectedItems();
		listView.clearChoices();

		// Refreshes list
		listView.invalidateViews();

		// If list is empty again Mr Jingles will appear again
		if (mAdapter.getCount() == 0)
			listView.setEmptyView(getActivity().findViewById(R.id.empty_list));

		// Refreshes navigation drawer if is set to show categories count numbers
		if (prefs.getBoolean("settings_show_category_count", false)) {
			((MainActivity) getActivity()).initNavigationDrawer();
		}

		if (mActionMode != null) {
			mActionMode.finish();
		}

		Crouton.makeText(getActivity(), R.string.tags_added, ONStyle.INFO).show();
	}


//	private void synchronizeSelectedNotes() {
//		new DriveSyncTask(getActivity()).execute(new ArrayList<Note>(selectedNotes));
//		// Clears data structures
//		mAdapter.clearSelectedItems();
//		listView.clearChoices();
//		finishActionMode();
//	}


	@Override
	public void onUndo(Parcelable token) {

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
				mAdapter.replace(note, mAdapter.getPosition(note));
			// Manages trash undo
			} else {
				mAdapter.insert(note, undoNotesList.keyAt(undoNotesList.indexOfValue(note)));
			}
		}

		listView.invalidateViews();

		undoNotesList.clear();
        modifiedNotes.clear();

		undoTrash = false;
		undoArchive = false;
		undoCategorize = false;
		undoNotesList.clear();
		undoCategoryMap.clear();
		undoCategorizeCategory = null;
		Crouton.cancelAllCroutons();

		if (mActionMode != null) {
			mActionMode.finish();
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
			mAdapter.clearSelectedItems();
			listView.clearChoices();

			ubc.hideUndoBar(false);
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
			list.add(new Integer[] { R.id.fab, R.string.tour_listactivity_actions_title,
					R.string.tour_listactivity_actions_detail, null });
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
		for (final Note note : selectedNotes) {
            ((MainActivity) getActivity()).shareNote(note);
		}

		selectedNotes.clear();
		if (mActionMode != null) {
			mActionMode.finish();
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

		for (Note note : selectedNotes) {

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

		selectedNotes.clear();
		if (mActionMode != null) {
			mActionMode.finish();
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
}
