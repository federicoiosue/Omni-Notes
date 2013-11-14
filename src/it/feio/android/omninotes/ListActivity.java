package it.feio.android.omninotes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import it.feio.android.omninotes.models.NavigationDrawerItemAdapter;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.NoteAdapter;
import it.feio.android.omninotes.models.ParcelableNote;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import it.feio.android.omninotes.R;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ListActivity extends BaseActivity implements OnItemClickListener {

	private CharSequence mTitle;
	String[] mNavigationArray;
	TypedArray mNavigationIconsArray;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ListView listView;
	NoteAdapter adapter;
	ActionMode mActionMode;
	HashSet<Note> selectedNotes = new HashSet<Note>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		initNavigationDrawer();
		initListView();
		initNotesList();

		CharSequence title = PreferenceManager.getDefaultSharedPreferences(this).getString(
				Constants.PREF_NAVIGATION, "");
		setTitle(title == null ? getString(R.string.title_activity_list) : title);
	}


	@Override
	protected void onResume() {
		initNotesList();
		super.onResume();
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
			// the CAB is removed. By default, selected items are deselected/unchecked.

	    	for (int i=0; i < listView.getChildCount(); i++) {
	    		listView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.list_bg));
	    	}
			selectedNotes.clear();
			adapter.clearSelectedItems();
			listView.clearChoices();
			mActionMode = null;
			Log.d(Constants.TAG, "Closed multiselection contextual menu");
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// Here you can perform updates to the CAB due to
			// an invalidate() request
			Log.d(Constants.TAG, "CAB preparation");
			boolean archived = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
					.getString(Constants.PREF_NAVIGATION, "")
					.equals(getResources().getStringArray(R.array.navigation_list)[1]);
			menu.findItem(R.id.menu_archive).setVisible(!archived);
			menu.findItem(R.id.menu_unarchive).setVisible(archived);
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
				default:
					return false;
			}
		}
    };
    
    
    /**
     * Manage check/uncheck of notes in list during multiple selection phase
     * @param view
     * @param position
     */
    private void toggleListViewItem(View view, int position){
    	Note note = adapter.getItem(position);
    	 if (!selectedNotes.contains(note)) {
				selectedNotes.add(note);
				adapter.addSelectedItem(position);
				view.setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
			} else {
				selectedNotes.remove(note);
				adapter.removeSelectedItem(position);
				view.setBackgroundColor(getResources().getColor(R.color.list_bg));
			}
    }
    

	/**
	 * Notes list initialization. Data, actions and callback are defined here.
	 */
	private void initListView() {
		listView = (ListView) findViewById(R.id.notesList);
		
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setItemsCanFocus(false);
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (mActionMode != null) {
		            return false;
		        }

		        // Start the CAB using the ActionMode.Callback defined above
		        startActionMode(new ModeCallback());
		        toggleListViewItem(view, position);
		        
		        return true;
			}
		});
		
		
		
//		listView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
//
//			private ActionMode actionMode;
//
//			@Override
//			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//				// Here you can do something when items are selected/de-selected,
//				// such as update the title in the CAB
//				Log.d(Constants.TAG, "Multiselection: selected element " + position);
//				final int checkedCount = listView.getCheckedItemCount();
//				if (checked) {
//					selectedNotes.add(adapter.getItem(position));
//					adapter.addSelectedItem(position);
//					listView.getChildAt(position - listView.getFirstVisiblePosition()).setBackgroundColor(
//							getResources().getColor(R.color.list_bg_selected));
//				} else {
//					selectedNotes.remove(adapter.getItem(position));
//					adapter.removeSelectedItem(position);
//					listView.getChildAt(position - listView.getFirstVisiblePosition()).setBackgroundColor(
//							getResources().getColor(R.color.list_bg));
//				}
//
//				switch (checkedCount) {
//					case 0:
//						mode.setTitle(null);
//						break;
//					case 1:
//						mode.setTitle(getResources().getString(R.string.one_item_selected));
//						break;
//					default:
//						mode.setTitle(checkedCount + " "
//								+ getResources().getString(R.string.more_items_selected));
//						break;
//				}
//			}
//
//			
//		});

		listView.setOnScrollListener(new OnScrollListener() {

			int mLastFirstVisibleItem = 0;

			/*
			 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}

			/*
			 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
			 */
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				if (view.getId() == listView.getId()) {
					final int currentFirstVisibleItem = listView.getFirstVisiblePosition();

					if (currentFirstVisibleItem > mLastFirstVisibleItem) {
						getSupportActionBar().hide();
					} else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
						getSupportActionBar().show();
					}

					mLastFirstVisibleItem = currentFirstVisibleItem;
				}
			}
		});

		listView.setOnItemClickListener(this);

	}

	@SuppressLint("NewApi")
	private void initNavigationDrawer() {

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		mNavigationArray = getResources().getStringArray(R.array.navigation_list);
		mNavigationIconsArray = getResources().obtainTypedArray(R.array.navigation_list_icons);
		mDrawerList
				.setAdapter(new NavigationDrawerItemAdapter(this, mNavigationArray, mNavigationIconsArray));

		// Set click events
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				String navigation = mDrawerList.getAdapter().getItem(position).toString();
				Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
				selectNavigationItem(position);
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
						.putString(Constants.PREF_NAVIGATION, navigation).commit();
				initNotesList();
			}
		});

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			getSupportActionBar().setHomeButtonEnabled(true);


		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {

			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				mTitle = getSupportActionBar().getTitle();
				getSupportActionBar().setTitle(getApplicationContext().getString(R.string.app_name));
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
//		if (savedInstanceState == null) {
//            selectItem(0);
//        }

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		// Setting the conditions to show determinate items in CAB
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// If archived notes are shown the "add new note" item must be hidden
		boolean archived = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(Constants.PREF_NAVIGATION, "")
				.equals(getResources().getStringArray(R.array.navigation_list)[1]);

		menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_add).setVisible(!drawerOpen && !archived);
		menu.findItem(R.id.menu_sort).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_settings).setVisible(!drawerOpen);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:				 
	            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
	                mDrawerLayout.closeDrawer(mDrawerList);
	            } else {
	                mDrawerLayout.openDrawer(mDrawerList);
	            }
	            break;
			case R.id.menu_add:
				editNote(new Note());
				break;
			case R.id.menu_sort:
				sortNotes();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

    

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		// If no CAB just note editing
		if (mActionMode == null) { 
			Note note = adapter.getItem(position);
			editNote(note);
			return;
		}

		// If in CAB mode 
        toggleListViewItem(view, position);
//		SparseBooleanArray checked = listView.getCheckedItemPositions();
//		for (int i = 0; i < checked.size(); i++) {
//	        checked.valueAt(i);
//	    }
	}


	private void editNote(Note note) {
		if (note.get_id() == 0) {
			Log.d(Constants.TAG, "Adding new note");
		} else {
			Log.d(Constants.TAG, "Editing note with id: " + note.get_id());
		}

		Intent detailIntent = new Intent(this, DetailActivity.class);
		detailIntent.putExtra(Constants.INTENT_NOTE, new ParcelableNote(note));
		startActivity(detailIntent);
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
		final Context ctx = this;

		//  Two array are used, one with db columns and a corrispective with column names human readables
		final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
		final String[] arrayDialog = getResources().getStringArray(R.array.sortable_columns_human_readable);
		
		// Dialog and events creation
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_sorting_column).setItems(arrayDialog,
				new DialogInterface.OnClickListener() {

					// On choosing the new criteria will be saved into preferences and listview redesigned
					public void onClick(DialogInterface dialog, int which) {
						PreferenceManager.getDefaultSharedPreferences(ctx).edit()
								.putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which])
								.commit();
						initNotesList();
					}
				});
		return builder.create();
	}

	public void initNotesList() {
		List<Note> notes;
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			notes = handleIntent(getIntent());
			getSupportActionBar().setTitle(getString(R.string.search));
		} else {
			DbHelper db = new DbHelper(getApplicationContext());
			notes = db.getAllNotes(true);
		}
		adapter = new NoteAdapter(getApplicationContext(), notes);
		((ListView) findViewById(R.id.notesList)).setAdapter(adapter);
	}


	private List<Note> handleIntent(Intent intent) {
		List<Note> notesList = new ArrayList<Note>();
		// Get the intent, verify the action and get the query
		String pattern = intent.getStringExtra(SearchManager.QUERY);
		Log.d(Constants.TAG, "Search launched");
		DbHelper db = new DbHelper(this);
		notesList = db.getMatchingNotes(pattern);
		Log.d(Constants.TAG, "Found " + notesList.size() + " elements matching");
		return notesList;

	}

	/**
	 * Conversion from database table name to human readable
	 * 
	 * @param string
	 * @return
	 */
//	private String dbColumnsToText(String string) {
//		String text = "";
//		String[] array = string.split("_");
//		for (String word : array) {
//			text += Character.toUpperCase(word.charAt(0)) + word.substring(1) + " ";
//		}
//		text.trim();
//		return text;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Pass the event to ActionBarDrawerToggle, if it returns
//		// true, then it has handled the app icon touch event
//		if (mDrawerToggle.onOptionsItemSelected(item)) {
//			return true;
//		}
//		// Handle your other action bar items...
//
//		return super.onOptionsItemSelected(item);
//	}


	/** Swaps fragments in the main content view */
	private void selectNavigationItem(int position) {
		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		mTitle = mNavigationArray[position];
		mDrawerLayout.closeDrawer(mDrawerList);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);

	}

	/**
	 * Batch note deletion
	 */
	public void deleteSelectedNotes() {

		// Confirm dialog creation
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(R.string.delete_note_confirmation)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						for (Note note : selectedNotes) {
							// Deleting note using DbHelper
							DbHelper db = new DbHelper(getApplicationContext());
							db.deleteNote(note);

							// Update adapter content
							adapter.remove(note);

							// Informs the user about update
							Log.d(Constants.TAG, "Deleted note with id '" + note.get_id() + "'");
						}
						// Refresh view
						((ListView) findViewById(R.id.notesList)).invalidateViews();
						// Advice to user
						Toast.makeText(getApplicationContext(),
								getResources().getText(R.string.note_deleted), Toast.LENGTH_SHORT).show();
						mActionMode.finish(); // Action picked, so close the CAB
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						mActionMode.finish(); // Action picked, so close the CAB
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}
	

	/**
	 * Batch note archiviation
	 */
	public void archiveSelectedNotes(boolean archive) {
		String archivedStatus = archive ? getResources().getText(R.string.note_archived).toString()
				: getResources().getText(R.string.note_unarchived).toString();
		for (Note note : selectedNotes) {
			// Deleting note using DbHelper
			DbHelper db = new DbHelper(this);
			note.setArchived(archive);
			db.updateNote(note);

			// Update adapter content
			adapter.remove(note);

			// Informs the user about update
			Log.d(Constants.TAG, "Note with id '" + note.get_id() + "' " + archivedStatus);
		}
		// Emtpy data structure
		selectedNotes.clear();
		// Refresh view
		((ListView) findViewById(R.id.notesList)).invalidateViews();
		// Advice to user
		Toast.makeText(this, archivedStatus, Toast.LENGTH_SHORT).show();
	}


}



