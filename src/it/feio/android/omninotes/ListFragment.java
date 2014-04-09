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
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.models.UndoBarController;
import it.feio.android.omninotes.models.UndoBarController.UndoListener;
import it.feio.android.omninotes.models.adapters.NavDrawerTagAdapter;
import it.feio.android.omninotes.models.adapters.NoteAdapter;
import it.feio.android.omninotes.models.listeners.OnNotesLoadedListener;
import it.feio.android.omninotes.models.listeners.OnViewTouchedListener;
import it.feio.android.omninotes.models.views.InterceptorLinearLayout;
import it.feio.android.omninotes.utils.AppTourHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.neopixl.pixlui.components.textview.TextView;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ListFragment extends Fragment implements UndoListener, OnNotesLoadedListener {

	static final int REQUEST_CODE_DETAIL = 1;
	private static final int REQUEST_CODE_TAG = 2;
	private static final int REQUEST_CODE_TAG_NOTES = 3;

	private ListView listView;
	NoteAdapter mAdapter;
	ActionMode mActionMode;
	HashSet<Note> selectedNotes = new HashSet<Note>();
	private SearchView searchView;
	MenuItem searchMenuItem;
	private TextView empyListItem;
	private AnimationDrawable jinglesAnimation;
	private int listViewPosition;
	private int listViewPositionOffset;
	private boolean undoDelete = false, undoArchive = false;
	private UndoBarController ubc;
	private boolean sendToArchive;
	private MainActivity mActivity;
	private SharedPreferences prefs;
	private DbHelper db;
	private ListFragment mFragment;

	
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
		
		// Show the Up button in the action bar.
//		if (mActivity.getSupportActionBar() != null) {
//			mActivity.getDrawerToggle().setDrawerIndicatorEnabled(true);
//			mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
//			mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//		}

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
			ArrayList<Tag> tags = db.getTags();
			for (Tag tag : tags) {
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
//		if (searchView != null && searchView.isEnabled()) {
//			MenuItemCompat.collapseActionView(searchMenuItem);
//		}
		commitPending();
		stopJingles();
		Crouton.cancelAllCroutons();
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
		prefs = mActivity.getSharedPreferences(Constants.PREFS_NAME, Activity.MODE_MULTI_PROCESS);

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
			inflater.inflate(R.menu.menu, menu);
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
			mActivity.notifyAppWidgets(mActivity);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// Here you can perform updates to the CAB due to
			// an invalidate() request
			Log.d(Constants.TAG, "CAB preparation");
			boolean notes = getResources().getStringArray(R.array.navigation_list_codes)[0]
					.equals(mActivity.navigation);
			boolean archived = getResources().getStringArray(R.array.navigation_list_codes)[1]
					.equals(mActivity.navigation);

			menu.findItem(R.id.menu_archive).setVisible(notes);
			menu.findItem(R.id.menu_unarchive).setVisible(archived);
			menu.findItem(R.id.menu_tag).setVisible(true);
			menu.findItem(R.id.menu_delete).setVisible(true);
			menu.findItem(R.id.menu_settings).setVisible(false);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// Respond to clicks on the actions in the CAB
			switch (item.getItemId()) {
			case R.id.menu_delete:
				deleteSelectedNotes();
				return true;
			case R.id.menu_archive:
				archiveSelectedNotes(true);
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.menu_unarchive:
				archiveSelectedNotes(false);
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.menu_tag:
				tagSelectedNotes();
				return true;
			default:
				return false;
			}
		}
	};

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
		if (selectedNotes.size() == 0) {
			selectedNotes.clear();
			mActionMode.finish();
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
		public void onViewTouchOccurred() {
			commitPending();
		}
	};

	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
		
//		if (mActivity == null) {
//			mActivity = (MainActivity)getActivity();
//		}

		// Setting the conditions to show determinate items in CAB
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen;
		if (mActivity.getDrawerLayout() != null) {
			drawerOpen = mActivity.getDrawerLayout().isDrawerOpen(GravityCompat.START);
		} else {
			drawerOpen = false;
		}

		// If archived or reminders notes are shown the "add new note" item must
		// be hidden
		String navArchived = getResources().getStringArray(R.array.navigation_list_codes)[1];
		String navReminders = getResources().getStringArray(R.array.navigation_list_codes)[2];
		boolean showAdd = !navArchived.equals(mActivity.navigation) && !navReminders.equals(mActivity.navigation);

		menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_add).setVisible(!drawerOpen && showAdd);
		menu.findItem(R.id.menu_sort).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_add_tag).setVisible(drawerOpen);
		menu.findItem(R.id.menu_settings).setVisible(true);

		// Initialization of SearchView
		initSearchView(menu);

		initShowCase();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean expandedView = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true);
		menu.findItem(R.id.menu_expanded_view).setVisible(!expandedView);
		menu.findItem(R.id.menu_contracted_view).setVisible(expandedView);
	}

	/**
	 * SearchView initialization. It's a little complex because it's not using
	 * SearchManager but is implementing on its own.
	 * 
	 * @param menu
	 */
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
				Log.d(Constants.TAG, "Search focus");
				menu.findItem(R.id.menu_add).setVisible(!hasFocus);
				menu.findItem(R.id.menu_sort).setVisible(!hasFocus);
				// searchView.setIconified(!hasFocus);
			}
		});

		// Sets events on searchView closing to restore full notes list
		MenuItem menuItem = menu.findItem(R.id.menu_search);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {

				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					// Reinitialize notes list to all notes when search is
					// collapsed
					Log.i(Constants.TAG, "onMenuItemActionCollapse " + item.getItemId());
					mActivity.getIntent().setAction(Intent.ACTION_MAIN);
					initNotesList(mActivity.getIntent());
					return true;
				}

				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					Log.i(Constants.TAG, "onMenuItemActionExpand " + item.getItemId());

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
		} else {
			// Do something for phones running an SDK before froyo
			searchView.setOnCloseListener(new OnCloseListener() {

				@Override
				public boolean onClose() {
					Log.i(Constants.TAG, "mSearchView on close ");
					mActivity.getIntent().setAction(Intent.ACTION_MAIN);
					initNotesList(mActivity.getIntent());
					return false;
				}
			});
		}

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
		case R.id.menu_add:
			editNote(new Note());
			break;
		case R.id.menu_sort:
			sortNotes();
			break;
		case R.id.menu_add_tag:
			editTag(null);
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
				note.setTag(db.getTag(tagId));
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

		case REQUEST_CODE_TAG:
			// Dialog retarded to give time to activity's views of being
			// completely initialized
//			new Handler().postDelayed(new Runnable() {
//				@Override
//				public void run() {
					// The dialog style is choosen depending on result code
					switch (resultCode) {
					case Activity.RESULT_OK:
						Crouton.makeText(mActivity, R.string.tag_saved, ONStyle.CONFIRM).show();
						mActivity.initNavigationDrawer();
						break;
					case Activity.RESULT_CANCELED:
						Crouton.makeText(mActivity, R.string.tag_deleted, ONStyle.ALERT).show();
						break;

					default:
						break;
					}
//				}
//			}, 800);

			break;

		case REQUEST_CODE_TAG_NOTES:
			if (intent != null) {
				Tag tag = intent.getParcelableExtra(Constants.INTENT_TAG);
				tagSelectedNotes2(tag);
			}
			break;

		default:
			break;
		}

	}
	

	private void sortNotes() {
		onCreateDialog().show();
	}

	/**
	 * Creation of a dialog for choose sorting criteria
	 * 
	 * @return
	 */
	public Dialog onCreateDialog() {
		// Two array are used, one with db columns and a corrispective with
		// column names human readables
		final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
		final String[] arrayDialog = getResources().getStringArray(R.array.sortable_columns_human_readable);

		int selected = Arrays.asList(arrayDb).indexOf(prefs.getString(Constants.PREF_SORTING_COLUMN, arrayDb[0]));

		// Dialog and events creation
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.select_sorting_column)
		// .setItems(arrayDialog,
				.setSingleChoiceItems(arrayDialog, selected, new DialogInterface.OnClickListener() {

					// On choosing the new criteria will be saved into
					// preferences and listview redesigned
					public void onClick(DialogInterface dialog, int which) {
						prefs.edit().putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which]).commit();
						initNotesList(mActivity.getIntent());
						// Updates app widgets
						mActivity.notifyAppWidgets(mActivity);
						dialog.dismiss();
					}
				});
		return builder.create();
	}

	
	
//	/**
//	 * Notes list adapter initialization and association to view
//	 */
//	void initNotesList(Intent intent) {
//
//		Log.v(Constants.TAG, "initNotesList: intent action " + intent.getAction());
//
//		List<Note> notes;
//		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//			notes = handleIntent(intent);
//			intent.setAction(null);
//		} else {
//			DbHelper db = new DbHelper(mActivity.getApplicationContext());
//			// Check if is launched from a widget with tags to set tag
//			if ((Constants.ACTION_WIDGET_SHOW_LIST.equals(intent.getAction()) && intent.hasExtra(Constants.INTENT_WIDGET)) 
//					|| !TextUtils.isEmpty(mActivity.navigationTmp)) {
//				String widgetId = intent.hasExtra(Constants.INTENT_WIDGET) ? intent.getExtras()
//						.get(Constants.INTENT_WIDGET).toString() : null;
//				if (widgetId != null) {
//					String sqlCondition = prefs.getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
//					String pattern = DbHelper.KEY_TAG + " = ";
//					if (sqlCondition.lastIndexOf(pattern) != -1) {
//						String tagId = sqlCondition.substring(sqlCondition.lastIndexOf(pattern) + pattern.length())
//								.trim();
//						mActivity.navigationTmp = !TextUtils.isEmpty(tagId) ? tagId : null;
//					}
//				}
//				notes = db.getNotesWithTag(mActivity.navigationTmp);
//				intent.removeExtra(Constants.INTENT_WIDGET);
//
//			} else {
//				notes = db.getAllNotes(true);
//			}
//		}
//		int layout = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true) ? R.layout.note_layout_expanded
//				: R.layout.note_layout;
//		mAdapter = new NoteAdapter(mActivity, layout, notes);
//
//		// if (prefs.getBoolean("settings_enable_animations", true) &&
//		// showListAnimation) {
//		// SwingBottomInAnimationAdapter swingInAnimationAdapter = new
//		// SwingBottomInAnimationAdapter(mAdapter);
//		// // Assign the ListView to the AnimationAdapter and vice versa
//		// swingInAnimationAdapter.setAbsListView(listView);
//		// listView.setAdapter(swingInAnimationAdapter);
//		// showListAnimation = false;
//		// } else {
//		// listView.setAdapter(mAdapter);
//		// }
//
//		SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, new OnDismissCallback() {
//			@Override
//			public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
//				for (int position : reverseSortedPositions) {
//					Note note = mAdapter.getItem(position);
//					selectedNotes.add(note);
//					mAdapter.remove(note);
//					listView.invalidateViews();
//
//					// Advice to user
//					Crouton.makeText(mActivity, R.string.note_deleted, ONStyle.ALERT).show();
//
//					// Creation of undo bar
//					ubc.showUndoBar(false, getString(R.string.note_deleted), null);
//					undoDelete = true;
//				}
//			}
//		});
//		adapter.setAbsListView(listView);
//		listView.setAdapter(adapter);
//
//		// Replace listview with Mr. Jingles if it is empty
//		if (notes.size() == 0)
//			listView.setEmptyView(mActivity.findViewById(R.id.empty_list));
//
//		// Restores listview position when turning back to list
//		if (listView != null && notes.size() > 0) {
//			if (listView.getCount() > listViewPosition) {
//				listView.setSelectionFromTop(listViewPosition, listViewPositionOffset);
//			} else {
//				listView.setSelectionFromTop(0, 0);
//			}
//		}
//		
//		// Fade in the list view
//		animate(listView).setDuration(getResources().getInteger(R.integer.list_view_fade_anim)).alpha(1);
//	}

	
	
	/**
	 * Notes list adapter initialization and association to view
	 */
	void initNotesList(Intent intent) {
		
		List<Note> notes;
		NoteLoaderTask mNoteLoaderTask = new NoteLoaderTask(mFragment, mFragment);
		
		// Searching
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			intent.setAction(null);
			// Get the intent, verify the action and get the query
			String pattern = intent.getStringExtra(SearchManager.QUERY);
			if (mActivity.loadNotesSync) {
				DbHelper db = new DbHelper(mActivity);
				onNotesLoaded((ArrayList<Note>) db.getMatchingNotes(pattern));
			} else {
				mNoteLoaderTask.execute("getMatchingNotes", pattern);
			}
			mActivity.loadNotesSync = Constants.LOAD_NOTES_SYNC;

		} else {
			// Check if is launched from a widget with tags to set tag
			if ((Constants.ACTION_WIDGET_SHOW_LIST.equals(intent.getAction()) && intent.hasExtra(Constants.INTENT_WIDGET)) 
					|| !TextUtils.isEmpty(mActivity.navigationTmp)) {
				String widgetId = intent.hasExtra(Constants.INTENT_WIDGET) ? intent.getExtras()
						.get(Constants.INTENT_WIDGET).toString() : null;
				if (widgetId != null) {
					String sqlCondition = prefs.getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
					String pattern = DbHelper.KEY_TAG + " = ";
					if (sqlCondition.lastIndexOf(pattern) != -1) {
						String tagId = sqlCondition.substring(sqlCondition.lastIndexOf(pattern) + pattern.length())
								.trim();
						mActivity.navigationTmp = !TextUtils.isEmpty(tagId) ? tagId : null;
					}
				}
				intent.removeExtra(Constants.INTENT_WIDGET);
				if (mActivity.loadNotesSync) {
					DbHelper db = new DbHelper(mActivity);
					onNotesLoaded((ArrayList<Note>) db.getNotesWithTag(mActivity.navigationTmp));
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
							Crouton.makeText(mActivity, R.string.note_deleted,
									ONStyle.ALERT).show();

							// Creation of undo bar
							ubc.showUndoBar(false,
									getString(R.string.note_deleted), null);
							undoDelete = true;
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
	 * Handle search intent
	 * 
	 * @param intent
	 * @return
	 */
//	private List<Note> handleIntent(Intent intent) {
//		List<Note> notesList = new ArrayList<Note>();
//		// Get the intent, verify the action and get the query
//		String pattern = intent.getStringExtra(SearchManager.QUERY);
//		Log.d(Constants.TAG, "Search launched");
//		DbHelper db = new DbHelper(mActivity);
//		notesList = db.getMatchingNotes(pattern);
//		Log.d(Constants.TAG, "Found " + notesList.size() + " elements matching");
//		// if (searchView != null)
//		// searchView.clearFocus();
//		return notesList;
//	}

	
	/**
	 * Batch note deletion
	 */
	public void deleteSelectedNotes() {
		int selectedNotesSize = selectedNotes.size();
		for (Note note : selectedNotes) {
			mAdapter.remove(note);
		}
		// Refresh view
		ListView l = (ListView) mActivity.findViewById(R.id.notes_list);
		l.invalidateViews();

		// If list is empty again Mr Jingles will appear again
		if (l.getCount() == 0)
			listView.setEmptyView(mActivity.findViewById(R.id.empty_list));

		mActionMode.finish(); // Action picked, so close the CAB

		// Advice to user
		Crouton.makeText(mActivity, R.string.note_deleted, ONStyle.ALERT).show();

		// Creation of undo bar
		ubc.showUndoBar(false, selectedNotesSize + " " + getString(R.string.deleted), null);
		undoDelete = true;
	}

	/**
	 * Single note deletion
	 * 
	 * @param note
	 *            Note to be deleted
	 */
	@SuppressLint("NewApi")
	protected void deleteNote(Note note) {

		// Saving changes to the note
		DeleteNoteTask deleteNoteTask = new DeleteNoteTask(mActivity);
		// Forceing parallel execution disabled by default
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			deleteNoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
		} else {
			deleteNoteTask.execute(note);
		}

		// Update adapter content
		mAdapter.remove(note);

		// Informs about update
		Log.d(Constants.TAG, "Deleted note with id '" + note.get_id() + "'");
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
			ubc.showUndoBar(false, selectedNotes.size() + getString(R.string.archived), null);
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
		mActivity.notifyAppWidgets(mActivity);
		Log.d(Constants.TAG, "Note with id '" + note.get_id() + "' " + (archive ? "archived" : "restored from archive"));
	}

	/**
	 * Tags addition and editing
	 * 
	 * @param tag
	 */
	void editTag(Tag tag) {
		Intent tagIntent = new Intent(mActivity, TagActivity.class);
		tagIntent.putExtra(Constants.INTENT_TAG, tag);
		startActivityForResult(tagIntent, REQUEST_CODE_TAG);
	}

	/**
	 * Tag selected notes
	 */
	private void tagSelectedNotes() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

		// Retrieves all available tags
		final ArrayList<Tag> tags = db.getTags();

		// If there is no tag a message will be shown
		if (tags.size() == 0) {
			Crouton.makeText(mActivity, R.string.no_tags_created, ONStyle.WARN).show();
		}

		// A single choice dialog will be displayed
		final String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
		final String navigation = prefs.getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);

		alertDialogBuilder.setTitle(R.string.tag_as)
				.setAdapter(new NavDrawerTagAdapter(mActivity, tags), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Moved to other method, mActivity way the same code
						// block can be called
						// also by onActivityResult when a new tag is created
						tagSelectedNotes2(tags.get(which));
					}
				}).setPositiveButton(R.string.add_tag, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(mActivity, TagActivity.class);
						intent.putExtra("noHome", true);
						startActivityForResult(intent, REQUEST_CODE_TAG_NOTES);
					}
				}).setNeutralButton(R.string.remove_tag, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						for (Note note : selectedNotes) {
							// Update adapter content if actual navigation is
							// the tag
							// associated with actually cycled note
							if (note.getTag() != null && navigation.equals(String.valueOf(note.getTag().getId()))) {
								mAdapter.remove(note);
							}
							note.setTag(null);
							db.updateNote(note, false);
						}
						// Refresh view
						((ListView) mActivity.findViewById(R.id.notes_list)).invalidateViews();
						// Advice to user
						Crouton.makeText(mActivity, R.string.notes_tag_removed, ONStyle.INFO).show();
						selectedNotes.clear();
						mActionMode.finish(); // Action picked, so close the CAB
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						selectedNotes.clear();
						mActionMode.finish(); // Action picked, so close the CAB
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	private void tagSelectedNotes2(Tag tag) {
		final String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
		final String navigation = prefs.getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		for (Note note : selectedNotes) {
			// Update adapter content if actual navigation is the tag
			// associated with actually cycled note
			if (!Arrays.asList(navigationListCodes).contains(navigation) && !navigation.equals(tag.getId())) {
				mAdapter.remove(note);
			}
			note.setTag(tag);
			db.updateNote(note, false);
		}
		// Refresh view
		((ListView) mActivity.findViewById(R.id.notes_list)).invalidateViews();
		// Advice to user
		String msg = getResources().getText(R.string.notes_tagged_as) + " '" + tag.getName() + "'";
		Crouton.makeText(mActivity, msg, ONStyle.INFO).show();
		selectedNotes.clear();
		mActionMode.finish(); // Action picked, so close the CAB
	}

	@Override
	public void onUndo(Parcelable token) {
		undoDelete = false;
		undoArchive = false;
		Crouton.cancelAllCroutons();
		selectedNotes.clear();
		if (mActionMode != null) {
			mActionMode.finish(); // Action picked, so close the CAB
		}
		ubc.hideUndoBar(false);
		initNotesList(mActivity.getIntent());
	}

	void commitPending() {
		if (undoDelete || undoArchive) {

			for (Note note : selectedNotes) {
				if (undoDelete)
					deleteNote(note);
				else if (undoArchive)
					archiveNote(note, sendToArchive);
			}

			undoDelete = false;
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
	

}
