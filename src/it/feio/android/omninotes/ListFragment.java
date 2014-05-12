/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import it.feio.android.omninotes.async.DeleteNoteTask;
import it.feio.android.omninotes.async.NoteLoaderTask;
import it.feio.android.omninotes.async.UpdaterTask;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
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
import com.neopixl.pixlui.components.textview.TextView;
import com.neopixl.pixlui.links.UrlCompleter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ListFragment extends Fragment implements UndoListener, OnNotesLoadedListener {

	static final int REQUEST_CODE_DETAIL = 1;
	private static final int REQUEST_CODE_CATEGORY = 2;
	private static final int REQUEST_CODE_CATEGORY_NOTES = 3;

	private ListView listView;
	NoteAdapter mAdapter;
	ActionMode mActionMode;
	ArrayList<Note> selectedNotes = new ArrayList<Note>();
	private SearchView searchView;
	MenuItem searchMenuItem;
	private TextView empyListItem;
	private AnimationDrawable jinglesAnimation;
	private int listViewPosition;
	private int listViewPositionOffset;
	private boolean undoTrash = false, undoArchive = false;
	private UndoBarController ubc;
	private boolean sendToArchive;
	private MainActivity mActivity;
	private SharedPreferences prefs;
	private DbHelper db;
	private ListFragment mFragment;
	private String searchQuery;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = (MainActivity) getActivity();	
		mFragment = this;
		prefs = mActivity.prefs;
		db = mActivity.db;
		
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
			}			
		}		
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Restores savedInstanceState
		if (savedInstanceState != null) {
			mActivity.navigationTmp = savedInstanceState.getString("navigationTmp");
		}

		// Easter egg initialization
		initEasterEgg();

		// Listview initialization
		initListView();

		// Activity title initialization
		initTitle();

		// Launching update task
		UpdaterTask task = new UpdaterTask(mActivity);
		task.execute();

		ubc = new UndoBarController(mActivity.findViewById(R.id.undobar), this);
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
			ArrayList<Category> categories = db.getCategories();
			for (Category tag : categories) {
				if (navigation.equals(String.valueOf(tag.getId())))
					title = tag.getName();
			}
		}

		title = title == null ? getString(R.string.title_activity_list) : title;
		mActivity.setActionBarTitle(title.toString());
	}

	/**
	 * Starts a little animation on Mr.Jingles!
	 */
	private void initEasterEgg() {
		empyListItem = (TextView) mActivity.findViewById(R.id.empty_list);
		empyListItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (jinglesAnimation == null) {
					jinglesAnimation = (AnimationDrawable) empyListItem.getCompoundDrawables()[1];
					empyListItem.post(new Runnable() {
						public void run() {
							if (jinglesAnimation != null)
								jinglesAnimation.start();
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
		selectedNotes.clear();
		mAdapter.clearSelectedItems();
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
		Log.v(Constants.TAG, "OnResume");
		initNotesList(mActivity.getIntent());
		
		// Navigation drawer initialization to ensure data refresh 
		mActivity.initNavigationDrawer();
		// Removes navigation drawer forced closed status
		if (mActivity.getDrawerLayout() != null) {
			mActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}

		// Restores again DefaultSharedPreferences too reload in case of data
		// erased from Settings
		prefs = mActivity.getSharedPreferences(Constants.PREFS_NAME, mActivity.MODE_MULTI_PROCESS);

		// Menu is invalidated to start again instructions tour if requested
		if (!prefs.getBoolean(Constants.PREF_TOUR_PREFIX + "list", false)) {
			mActivity.supportInvalidateOptionsMenu();
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

			// Clears data structures
			// selectedNotes.clear();
			mAdapter.clearSelectedItems();
			listView.clearChoices();

			mActionMode = null;
			Log.d(Constants.TAG, "Closed multiselection contextual menu");

			// Updates app widgets
			BaseActivity.notifyAppWidgets(mActivity);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			boolean notes = getResources().getStringArray(R.array.navigation_list_codes)[0]
					.equals(mActivity.navigation);
			boolean archive = getResources().getStringArray(R.array.navigation_list_codes)[1]
					.equals(mActivity.navigation);
			boolean trash = getResources().getStringArray(R.array.navigation_list_codes)[3]
					.equals(mActivity.navigation);

			if (trash) {
				menu.findItem(R.id.menu_untrash).setVisible(true);
				menu.findItem(R.id.menu_delete).setVisible(true);
			} else {
				menu.findItem(R.id.menu_archive).setVisible(notes);
				menu.findItem(R.id.menu_unarchive).setVisible(archive);
				menu.findItem(R.id.menu_category).setVisible(true);
				menu.findItem(R.id.menu_trash).setVisible(true);
				menu.findItem(R.id.menu_settings).setVisible(false);
			}
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// Respond to clicks on the actions in the CAB
			switch (item.getItemId()) {
			case R.id.menu_category:
				categorizeSelectedNotes();
				return true;
			case R.id.menu_share:
				share();
				return true;
			case R.id.menu_merge:
				merge();
				return true;
			case R.id.menu_archive:
				archiveSelectedNotes(true);
				mode.finish(); 
				return true;
			case R.id.menu_unarchive:
				archiveSelectedNotes(false);
				mode.finish();
				return true;
			case R.id.menu_trash:
				trashSelectedNotes(true);
				return true;
			case R.id.menu_untrash:
				trashSelectedNotes(false);
				return true;
			case R.id.menu_delete:
				deleteSelectedNotes();
				return true;
			default:
				return false;
			}
		}
	};


	
	public void finishActionMode() {
		selectedNotes.clear();
		if (mActionMode != null) {
			mActionMode.finish();
		}
	}
	
	

	/**
	 * Manage check/uncheck of notes in list during multiple selection phase
	 * 
	 * @param view
	 * @param position
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
		
		// Edit menu
		boolean singleSelection = selectedNotes.size() == 1;
		
		// Checks if we're in trash
		boolean trash = getResources().getStringArray(R.array.navigation_list_codes)[3]
				.equals(mActivity.navigation);
		if (!trash) {
			mActionMode.getMenu().findItem(R.id.menu_share).setVisible(singleSelection);
			mActionMode.getMenu().findItem(R.id.menu_merge).setVisible(!singleSelection);
		}
		
		// Close CAB if no items are selected
		if (selectedNotes.size() == 0) {
			finishActionMode();
		}
		
	}

	/**
	 * Notes list initialization. Data, actions and callback are defined here.
	 */
	private void initListView() {
		listView = (ListView) mActivity.findViewById(R.id.notes_list);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setItemsCanFocus(false);
		
		// If device runs KitKat a footer is added to list to avoid 
		// navigation bar transparency covering items
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			TextView footer = new TextView(mActivity);
			footer.setHeight(Display.getNavigationBarHeight(listView));
			listView.addFooterView(footer);
		}

		// Note long click to start CAB mode
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (mActionMode != null) {
					return false;
				}
				// Start the CAB using the ActionMode.Callback defined above
				mActivity.startSupportActionMode(new ModeCallback());
				toggleListViewItem(view, position);
				setCabTitle();
				return true;
			}
		});

		// Note single click listener managed by the activity itself
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
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

		// listView.setOnTouchListener(screenTouches);
		((InterceptorLinearLayout) mActivity.findViewById(R.id.list_root)).setOnViewTouchedListener(screenTouches);
	}

	OnViewTouchedListener screenTouches = new OnViewTouchedListener() {
		@Override
		public void onViewTouchOccurred(MotionEvent ev) {
			commitPending();
		}
	};

	

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
		boolean drawerOpen = (mActivity.getDrawerLayout() != null && mActivity.getDrawerLayout().isDrawerOpen(GravityCompat.START)) ? true : false;		
		boolean expandedView = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true);
		// "Add" item must be shown only from main navigation
		String navArchived = getResources().getStringArray(R.array.navigation_list_codes)[1];
		String navReminders = getResources().getStringArray(R.array.navigation_list_codes)[2];
		String navTrash = getResources().getStringArray(R.array.navigation_list_codes)[3];
		boolean showAdd = !navArchived.equals(mActivity.navigation) && !navReminders.equals(mActivity.navigation) && !navTrash.equals(mActivity.navigation);

		menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_add).setVisible(!drawerOpen && showAdd);
		menu.findItem(R.id.menu_sort).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_add_category).setVisible(drawerOpen);
//		menu.findItem(R.id.menu_tags).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_expanded_view).setVisible(!drawerOpen && !expandedView);
		menu.findItem(R.id.menu_contracted_view).setVisible(!drawerOpen && expandedView);	
		menu.findItem(R.id.menu_settings).setVisible(!drawerOpen);
	}

	
	
	/**
	 * SearchView initialization. It's a little complex because it's not using
	 * SearchManager but is implementing on its own.
	 * 
	 * @param menu
	 */
//	@SuppressLint("NewApi")
	@SuppressLint("NewApi")
	private void initSearchView(final Menu menu) {

		// Save item as class attribute to make it collapse on drawer opening
		searchMenuItem = menu.findItem(R.id.menu_search);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(mActivity.getComponentName()));
		searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

		// Expands the widget hiding other actionbar icons
		searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
//				Log.d(Constants.TAG, "Search focus");
//				 searchView.setIconified(!hasFocus);
//				MenuItemCompat.collapseActionView(searchMenuItem);
				menu.findItem(R.id.menu_add).setVisible(!hasFocus);
				menu.findItem(R.id.menu_sort).setVisible(!hasFocus);
				menu.findItem(R.id.menu_contracted_view).setVisible(!hasFocus);
				menu.findItem(R.id.menu_expanded_view).setVisible(!hasFocus);
				menu.findItem(R.id.menu_tags).setVisible(hasFocus);
				menu.findItem(R.id.menu_settings).setVisible(!hasFocus);
			}
		});

		MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// Reinitialize notes list to all notes when search is
				// collapsed
				searchQuery = null;
//				Log.i(Constants.TAG, "onMenuItemActionCollapse " + item.getItemId());
				mActivity.getIntent().setAction(Intent.ACTION_MAIN);
				initNotesList(mActivity.getIntent());
				return true;
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
//				Log.i(Constants.TAG, "onMenuItemActionExpand " + item.getItemId());
				
				searchView.setOnQueryTextListener(new OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String arg0) {
						if (prefs.getBoolean("settings_instant_search", false)) {
							return true;
						} else {
							return false;
						}
					}

					@Override
					public boolean onQueryTextChange(String pattern) {
						if (prefs.getBoolean("settings_instant_search", false) && pattern.length() > 0) {
//								Intent i = new Intent(mActivity, MainActivity.class);
//								i.setAction(Intent.ACTION_SEARCH);
//								i.putExtra(SearchManager.QUERY, pattern);
//								startActivity(i);
							searchQuery = pattern;
							NoteLoaderTask mNoteLoaderTask = new NoteLoaderTask(mFragment, mFragment);
							mNoteLoaderTask.execute("getMatchingNotes", pattern);
							return true;
						} else {
							return false;
						}
					}
				});
				return true;
			}
		});
		
		// A previous search has been performed and SearchView has still not
		// been manually closed so it must be re-expanded and filled with query 
//		if (searchQuery != null) {
//			MenuItemCompat.expandActionView(searchMenuItem);
//		}
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mActivity.getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
				mActivity.getDrawerLayout().closeDrawer(GravityCompat.START);
			} else {
				mActivity.getDrawerLayout().openDrawer(GravityCompat.START);
			}
			break;
		case R.id.menu_tags:
			filterByTags();
			break;
		case R.id.menu_add:
			editNote(new Note());
			break;
		case R.id.menu_sort:
			sortNotes();
			break;
		case R.id.menu_add_category:
			editCategory(null);
			break;
		case R.id.menu_expanded_view:
			switchNotesView();
			break;
		case R.id.menu_contracted_view:
			switchNotesView();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void switchNotesView() {
		boolean expandedView = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true);
		prefs.edit().putBoolean(Constants.PREF_EXPANDED_VIEW, !expandedView).commit();

		// Change list view
		initNotesList(mActivity.getIntent());

		// Called to switch menu voices
		mActivity.supportInvalidateOptionsMenu();
	}

	private void setCabTitle() {
		if (mActionMode == null)
			return;
		switch (selectedNotes.size()) {
		case 0:
			mActionMode.setTitle(null);
			break;
		default:
			mActionMode.setTitle(String.valueOf(selectedNotes.size()));
			break;
		}

	}

	void editNote(Note note) {
		
		if (note.get_id() == 0) {
			Log.d(Constants.TAG, "Adding new note");
			// if navigation is a tag it will be set into note
			try {
				int tagId;
				if (!TextUtils.isEmpty(mActivity.navigationTmp)) {
					tagId = Integer.parseInt(mActivity.navigationTmp);
				} else {
					tagId = Integer.parseInt(mActivity.navigation);
				}
				note.setCategory(db.getCategory(tagId));
			} catch (NumberFormatException e) {
			}
		} else {
			Log.d(Constants.TAG, "Editing note with id: " + note.get_id());
		}

		//Current list scrolling position is saved to be restored later
		refreshListScrollPosition();
		
		// Fragments replacing
		FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
		mActivity.animateTransition(transaction, mActivity.TRANSITION_HORIZONTAL);
		DetailFragment mDetailFragment = new DetailFragment();
		Bundle b = new Bundle();
		b.putParcelable(Constants.INTENT_NOTE, note);
		mDetailFragment.setArguments(b);
		transaction.replace(R.id.fragment_container, mDetailFragment, mActivity.FRAGMENT_DETAIL_TAG).addToBackStack(mActivity.FRAGMENT_LIST_TAG).commit();
		mActivity.getDrawerToggle().setDrawerIndicatorEnabled(false);
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
					final String message = intentMsg != null ? intent
							.getStringExtra(Constants.INTENT_DETAIL_RESULT_MESSAGE) : "";
					// Dialog retarded to give time to activity's views of being
					// completely initialized
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							// The dialog style is choosen depending on result
							// code
							switch (resultCode) {
							case Activity.RESULT_OK:
								Crouton.makeText(mActivity, message, ONStyle.CONFIRM).show();
								break;
							case Activity.RESULT_FIRST_USER:
								Crouton.makeText(mActivity, message, ONStyle.INFO).show();
								break;
							case Activity.RESULT_CANCELED:
								Crouton.makeText(mActivity, message, ONStyle.ALERT).show();
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
				Crouton.makeText(mActivity, R.string.category_saved, ONStyle.CONFIRM).show();
				mActivity.initNavigationDrawer();
				break;
			case Activity.RESULT_CANCELED:
				Crouton.makeText(mActivity, R.string.category_deleted, ONStyle.ALERT).show();
				break;

			default:
				break;
			}

			break;

		case REQUEST_CODE_CATEGORY_NOTES:
			if (intent != null) {
				Category tag = intent.getParcelableExtra(Constants.INTENT_TAG);
				categorizeSelectedNotes2(tag);
			}
			break;

		default:
			break;
		}

	}
	

	private void sortNotes() {
//		onCreateDialog().show();
		// Two array are used, one with db columns and a corrispective with
		// column names human readables
		final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
		final String[] arrayDialog = getResources().getStringArray(R.array.sortable_columns_human_readable);

		int selected = Arrays.asList(arrayDb).indexOf(prefs.getString(Constants.PREF_SORTING_COLUMN, arrayDb[0]));

		// Dialog and events creation
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.select_sorting_column)
				.setSingleChoiceItems(arrayDialog, selected, new DialogInterface.OnClickListener() {

					// On choosing the new criteria will be saved into
					// preferences and listview redesigned
					public void onClick(DialogInterface dialog, int which) {
						prefs.edit().putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which]).commit();
						initNotesList(mActivity.getIntent());
						// Updates app widgets
						BaseActivity.notifyAppWidgets(mActivity);
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

//	/**
//	 * Creation of a dialog for choose sorting criteria
//	 * 
//	 * @return
//	 */
//	public Dialog onCreateDialog() {
//		// Two array are used, one with db columns and a corrispective with
//		// column names human readables
//		final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
//		final String[] arrayDialog = getResources().getStringArray(R.array.sortable_columns_human_readable);
//
//		int selected = Arrays.asList(arrayDb).indexOf(prefs.getString(Constants.PREF_SORTING_COLUMN, arrayDb[0]));
//
//		// Dialog and events creation
//		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//		builder.setTitle(R.string.select_sorting_column)
//				.setSingleChoiceItems(arrayDialog, selected, new DialogInterface.OnClickListener() {
//
//					// On choosing the new criteria will be saved into
//					// preferences and listview redesigned
//					public void onClick(DialogInterface dialog, int which) {
//						prefs.edit().putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which]).commit();
//						initNotesList(mActivity.getIntent());
//						// Updates app widgets
//						BaseActivity.notifyAppWidgets(mActivity);
//						dialog.dismiss();
//					}
//				});
//		return builder.create();
//	}
	
	
	
	/**
	 * Notes list adapter initialization and association to view
	 */
	void initNotesList(Intent intent) {
		
		NoteLoaderTask mNoteLoaderTask = new NoteLoaderTask(mFragment, mFragment);
		
		// Search for a tag
		// A workaround to simplify it's to simulate normal search
		if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getCategories().contains(Intent.CATEGORY_BROWSABLE)) {
//			mNoteLoaderTask.execute("getNotesByTag", intent.getDataString().replace(UrlCompleter.HASHTAG_SCHEME, ""));
			searchQuery = intent.getDataString().replace(UrlCompleter.HASHTAG_SCHEME, "");
		}

		// Searching		
		if (searchQuery != null || Intent.ACTION_SEARCH.equals(intent.getAction())) {
			intent.setAction(null);
			// Get the intent, verify the action and get the query
			if (searchQuery == null) {
				searchQuery = intent.getStringExtra(SearchManager.QUERY);
			}
			if (mActivity.loadNotesSync) {
				DbHelper db = new DbHelper(mActivity);
				onNotesLoaded((ArrayList<Note>) db.getMatchingNotes(searchQuery));
			} else {
				mNoteLoaderTask.execute("getMatchingNotes", searchQuery);
			}
			mActivity.loadNotesSync = Constants.LOAD_NOTES_SYNC;
			
			toggleSearchLabel(true);

		} else {
			// Check if is launched from a widget with categories to set tag
			if ((Constants.ACTION_WIDGET_SHOW_LIST.equals(intent.getAction()) && intent.hasExtra(Constants.INTENT_WIDGET)) 
					|| !TextUtils.isEmpty(mActivity.navigationTmp)) {
				String widgetId = intent.hasExtra(Constants.INTENT_WIDGET) ? intent.getExtras()
						.get(Constants.INTENT_WIDGET).toString() : null;
				if (widgetId != null) {
					String sqlCondition = prefs.getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
					String pattern = DbHelper.KEY_CATEGORY + " = ";
					if (sqlCondition.lastIndexOf(pattern) != -1) {
						String tagId = sqlCondition.substring(sqlCondition.lastIndexOf(pattern) + pattern.length())
								.trim();
						mActivity.navigationTmp = !TextUtils.isEmpty(tagId) ? tagId : null;
					}
				}
				intent.removeExtra(Constants.INTENT_WIDGET);
				if (mActivity.loadNotesSync) {
					DbHelper db = new DbHelper(mActivity);
					onNotesLoaded((ArrayList<Note>) db.getNotesByCategory(mActivity.navigationTmp));
				} else {
					mNoteLoaderTask.execute("getNotesWithTag", mActivity.navigationTmp);
				}
				mActivity.loadNotesSync = Constants.LOAD_NOTES_SYNC;
				
			// Gets all notes
			} else {
				if (mActivity.loadNotesSync) {
					DbHelper db = new DbHelper(mActivity);
					onNotesLoaded((ArrayList<Note>) db.getAllNotes(true));
				} else {
					mNoteLoaderTask.execute("getAllNotes", true);
				}
				mActivity.loadNotesSync = Constants.LOAD_NOTES_SYNC;
			}
		}
	}
	
	
	
	public void toggleSearchLabel(boolean active) {
		if (active) {
			((android.widget.TextView) mActivity.findViewById(R.id.search_query)).setText(Html.fromHtml("<i>" + getString(R.string.search) + ":</i> " + searchQuery.toString()));
			mActivity.findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
			mActivity.findViewById(R.id.search_cancel).setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					mActivity.findViewById(R.id.search_layout).setVisibility(View.GONE);
					searchQuery = null;
					mActivity.getIntent().setAction(Intent.ACTION_MAIN);
					initNotesList(mActivity.getIntent());
				}
			});
		} else {
			mActivity.findViewById(R.id.search_layout).setVisibility(View.GONE);
			searchQuery = null;
			mActivity.getIntent().setAction(Intent.ACTION_MAIN);
			initNotesList(mActivity.getIntent());
		}
	}
	
	


	@Override
	public void onNotesLoaded(ArrayList<Note> notes) {
		int layout = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true) ? R.layout.note_layout_expanded
				: R.layout.note_layout;
		mAdapter = new NoteAdapter(mActivity, layout, notes);

		SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter,
				new OnDismissCallback() {
					@Override
					public void onDismiss(AbsListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							Note note = mAdapter.getItem(position);
							selectedNotes.add(note);
							mAdapter.remove(note);
							listView.invalidateViews();

							// Advice to user
							Crouton.makeText(mActivity, R.string.note_trashed,
									ONStyle.WARN).show();

							// Creation of undo bar
							ubc.showUndoBar(false,
									getString(R.string.note_trashed), null);
							undoTrash = true;
						}
					}
				});
		adapter.setAbsListView(listView);
		listView.setAdapter(adapter);

		// Replace listview with Mr. Jingles if it is empty
		if (notes.size() == 0)
			listView.setEmptyView(mActivity.findViewById(R.id.empty_list));

		// Restores listview position when turning back to list
		if (listView != null && notes.size() > 0) {
			if (listView.getCount() > listViewPosition) {
				listView.setSelectionFromTop(listViewPosition,
						listViewPositionOffset);
			} else {
				listView.setSelectionFromTop(0, 0);
			}
		}

		// Fade in the list view
		animate(listView).setDuration(
				getResources().getInteger(R.integer.list_view_fade_anim))
				.alpha(1);
	}
		

	
	/**
	 * Batch note trashing
	 */
	public void trashSelectedNotes(boolean trash) {
		int selectedNotesSize = selectedNotes.size();
		for (Note note : selectedNotes) {
			mAdapter.remove(note);
			// Restore it performed immediately, otherwise undo bar
			if (!trash) {
				trashNote(note, false);
			}
		}
		// Refresh view
		ListView l = (ListView) mActivity.findViewById(R.id.notes_list);
		l.invalidateViews();

		// If list is empty again Mr Jingles will appear again
		if (l.getCount() == 0)
			listView.setEmptyView(mActivity.findViewById(R.id.empty_list));

		if (mActionMode != null) {
			mActionMode.finish();
		}

		// Advice to user
		if (trash) {
			Crouton.makeText(mActivity, getString(R.string.note_trashed), ONStyle.WARN).show();
		} else {
			Crouton.makeText(mActivity, getString(R.string.note_untrashed), ONStyle.INFO).show();
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
		if (trash) {
			db.trashNote(note);
		} else {
			db.untrashNote(note);
		}
		// Update adapter content
		mAdapter.remove(note);
		// Informs about update
		Log.d(Constants.TAG, "Trashed/restored note with id '" + note.get_id() + "'");
	}
	

	
	/**
	 * Batch note permanent deletion
	 */
	private void deleteSelectedNotes() {
		// Confirm dialog creation
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
		alertDialogBuilder.setMessage(R.string.delete_note_confirmation)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						deleteSelectedNotes2();
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
	private void deleteSelectedNotes2() {
		for (Note note : selectedNotes) {
			mAdapter.remove(note);
			deleteNote(note);
		}	
		
		finishActionMode();
		
		// Refresh view
		ListView l = (ListView) mActivity.findViewById(R.id.notes_list);
		l.invalidateViews();
	
		// If list is empty again Mr Jingles will appear again
		if (l.getCount() == 0)
			listView.setEmptyView(mActivity.findViewById(R.id.empty_list));
	
		// Advice to user
		Crouton.makeText(mActivity, R.string.note_deleted, ONStyle.ALERT).show();
	}

		
	/**
	 * Single note permanent deletion
	 * 
	 * @param note
	 *            Note to be deleted
	 */
	@SuppressLint("NewApi")
	protected void deleteNote(Note note) {

		// Saving changes to the note
		DeleteNoteTask deleteNoteTask = new DeleteNoteTask(mActivity);
		// Forcing parallel execution disabled by default
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			deleteNoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
		} else {
			deleteNoteTask.execute(note);
		}

		// Informs about update
		Log.d(Constants.TAG, "Deleted permanently note with id '" + note.get_id() + "'");
	}

	
	/**
	 * Batch note archiviation
	 */
	public void archiveSelectedNotes(boolean archive) {
		// Used in undo bar commit
		sendToArchive = archive;
		String archivedStatus = archive ? getResources().getText(R.string.note_archived).toString() : getResources()
				.getText(R.string.note_unarchived).toString();

		for (Note note : selectedNotes) {
			// Update adapter content
			mAdapter.remove(note);
			// If is restore it will be done immediately, otherwise the undo bar
			// will be shown
			if (!archive) {
				archiveNote(note, false);
			}
		}

		// Clears data structures
		mAdapter.clearSelectedItems();
		listView.clearChoices();

		// Refresh view
		((ListView) mActivity.findViewById(R.id.notes_list)).invalidateViews();
		// Advice to user
		Crouton.makeText(mActivity, archivedStatus, ONStyle.INFO).show();

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
		DbHelper db = new DbHelper(mActivity);
		note.setArchived(archive);
		db.updateNote(note, true);

		// Update adapter content
		mAdapter.remove(note);

		// Informs the user about update
		BaseActivity.notifyAppWidgets(mActivity);
		Log.d(Constants.TAG, "Note with id '" + note.get_id() + "' " + (archive ? "archived" : "restored from archive"));
	}

	
	/**
	 * Tags addition and editing
	 * 
	 * @param tag
	 */
	void editCategory(Category category) {
		Intent categoryIntent = new Intent(mActivity, CategoryActivity.class);
		categoryIntent.putExtra(Constants.INTENT_TAG, category);
		startActivityForResult(categoryIntent, REQUEST_CODE_CATEGORY);
	}

	
	/**
	 * Tag selected notes
	 */
	private void categorizeSelectedNotes() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

		// Retrieves all available categories
		final ArrayList<Category> categories = db.getCategories();

		// A single choice dialog will be displayed
		final String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
		final String navigation = prefs.getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);

		alertDialogBuilder.setTitle(R.string.categorize_as)
				.setAdapter(new NavDrawerCategoryAdapter(mActivity, categories), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Moved to other method, mActivity way the same code
						// block can be called
						// also by onActivityResult when a new tag is created
						categorizeSelectedNotes2(categories.get(which));
					}
				}).setPositiveButton(R.string.add_category, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(mActivity, CategoryActivity.class);
						intent.putExtra("noHome", true);
						startActivityForResult(intent, REQUEST_CODE_CATEGORY_NOTES);
					}
				}).setNeutralButton(R.string.remove_category, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						for (Note note : selectedNotes) {
							// Update adapter content if actual navigation is
							// the category
							// associated with actually cycled note
							if (note.getCategory() != null && navigation.equals(String.valueOf(note.getCategory().getId()))) {
								mAdapter.remove(note);
							}
							note.setCategory(null);
							db.updateNote(note, false);
						}
						// Refresh view
						((ListView) mActivity.findViewById(R.id.notes_list)).invalidateViews();
						// Advice to user
						Crouton.makeText(mActivity, R.string.notes_category_removed, ONStyle.INFO).show();
						selectedNotes.clear();
						if (mActionMode != null) {
							mActionMode.finish();
						}
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

	private void categorizeSelectedNotes2(Category category) {
		final String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
		final String navigation = prefs.getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		for (Note note : selectedNotes) {
			// Update adapter content if actual navigation is the category
			// associated with actually cycled note
			if (!Arrays.asList(navigationListCodes).contains(navigation) && !navigation.equals(category.getId())) {
				mAdapter.remove(note);
			}
			note.setCategory(category);
			db.updateNote(note, false);
		}
		// Refresh view
		((ListView) mActivity.findViewById(R.id.notes_list)).invalidateViews();
		// Advice to user
		String msg = getResources().getText(R.string.notes_categorized_as) + " '" + category.getName() + "'";
		Crouton.makeText(mActivity, msg, ONStyle.INFO).show();
		finishActionMode();
	}
	

	@Override
	public void onUndo(Parcelable token) {
		undoTrash = false;
		undoArchive = false;
		Crouton.cancelAllCroutons();
		selectedNotes.clear();
		if (mActionMode != null) {
			mActionMode.finish();
		}
		ubc.hideUndoBar(false);
		initNotesList(mActivity.getIntent());
	}

	
	void commitPending() {
		if (undoTrash || undoArchive) {

			for (Note note : selectedNotes) {
				if (undoTrash)
					trashNote(note, true);
				else if (undoArchive)
					archiveNote(note, sendToArchive);
			}

			undoTrash = false;
			undoArchive = false;

			// Clears data structures
			selectedNotes.clear();
			mAdapter.clearSelectedItems();
			listView.clearChoices();

			ubc.hideUndoBar(false);
		}
	}
	
	
	
	private void initShowCase() {

		// Show instructions on first launch
		final String instructionName = Constants.PREF_TOUR_PREFIX + "list";
		if (AppTourHelper.isMyTurn(mActivity, instructionName)) {
			ArrayList<Integer[]> list = new ArrayList<Integer[]>();
			list.add(new Integer[] { 0, R.string.tour_listactivity_intro_title,
					R.string.tour_listactivity_intro_detail, ShowcaseView.ITEM_TITLE });
			list.add(new Integer[] { R.id.menu_add, R.string.tour_listactivity_actions_title,
					R.string.tour_listactivity_actions_detail, ShowcaseView.ITEM_ACTION_ITEM });
			list.add(new Integer[] { 0, R.string.tour_listactivity_home_title, R.string.tour_listactivity_home_detail,
					ShowcaseView.ITEM_ACTION_HOME });
			mActivity.showCaseView(list, new OnShowcaseAcknowledged() {
				@Override
				public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
					AppTourHelper.complete(mActivity, instructionName);
					mActivity.getDrawerLayout().openDrawer(GravityCompat.START);
				}
			});
		}
		
		// Show instructions on first launch
		final String instructionName2 = Constants.PREF_TOUR_PREFIX + "list2";
		if (AppTourHelper.isMyTurn(mActivity, instructionName2)) {
			ArrayList<Integer[]> list = new ArrayList<Integer[]>();
			list.add(new Integer[] { null, R.string.tour_listactivity_final_title,
					R.string.tour_listactivity_final_detail, null });
			mActivity.showCaseView(list, new OnShowcaseAcknowledged() {
				@Override
				public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
					AppTourHelper.complete(mActivity, instructionName2);
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
			if (note.isLocked()) {
				BaseActivity.requestPassword(mActivity, new PasswordValidator() {					
					@Override
					public void onPasswordValidated(boolean result) {
						if (result) {
							mActivity.shareNote(note);
						} 
					}
				});
			} else {
				mActivity.shareNote(note);
			}		
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
						&& (!TextUtils.isEmpty(note.getTitle()) 
							|| !TextUtils.isEmpty(note.getContent()))) {
					content.append(System.getProperty("line.separator"))
							.append(System.getProperty("line.separator"))
							.append("----------------------")
							.append(System.getProperty("line.separator"))
							.append(System.getProperty("line.separator"));
				}
				if (!TextUtils.isEmpty(note.getTitle())) {
					content.append(note.getTitle());
				}
				if (!TextUtils.isEmpty(note.getTitle())
						&& !TextUtils.isEmpty(note.getContent())) {
					content.append(System.getProperty("line.separator"))
							.append(System.getProperty("line.separator"));
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
		mActivity.getIntent().setAction(Constants.ACTION_MERGE);		
		mActivity.switchToDetail(mergedNote);
	}
	
	
	
	/**
	 * Search notes by tags
	 */
	private void filterByTags() {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

			// Retrieves all available categories
			final List<String> tags = db.getTags();

			// If there is no category a message will be shown
			if (tags.size() == 0) {
				Crouton.makeText(mActivity, R.string.no_tags_created, ONStyle.WARN).show();
				return;
			}

			// Selected tags
			final boolean[] selectedTags = new boolean[tags.size()];
			Arrays.fill(selectedTags, Boolean.FALSE);

			// Dialog and events creation
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			final String[] tagsArray = tags.toArray(new String[tags.size()]);
			builder
				.setTitle(R.string.select_tags)
				.setMultiChoiceItems(tagsArray, selectedTags, new DialogInterface.OnMultiChoiceClickListener() {						
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						selectedTags[which] = isChecked;
					}
				})
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Retrieves selected tags
						for (int i = 0; i < selectedTags.length; i++) {
							if (!selectedTags[i]) {
								tags.remove(tagsArray[i]);
							}
						}
						
						// Fires an intent to search related notes
						NoteLoaderTask mNoteLoaderTask = new NoteLoaderTask(mFragment, mFragment);
						mNoteLoaderTask.execute("getNotesByTag", tags.toString().substring(1, tags.toString().length()-1).replace(" ", ""));
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
			builder.create().show();
		}

}
