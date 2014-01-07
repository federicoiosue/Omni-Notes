/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.NavigationDrawerAdapter;
import it.feio.android.omninotes.models.NavDrawerTagAdapter;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.NoteAdapter;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;
import it.feio.android.omninotes.async.DeleteNoteTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.R;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ListActivity extends BaseActivity {

	private CharSequence mTitle;
	String[] mNavigationArray;
	TypedArray mNavigationIconsArray;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private LinearLayout mDrawer;
	private ListView listView;
	NoteAdapter mAdapter;
	ActionMode mActionMode;
	HashSet<Note> selectedNotes = new HashSet<Note>();
	private ListView mDrawerList;
	private ListView mDrawerTagList;
	private View tagListHeader;
	private Tag candidateSelectedTag;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
				
		// Get intent, action and MIME type to handle intent-filter requests
		Intent intent = getIntent();
		if ((Intent.ACTION_SEND.equals(intent.getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) && intent.getType() != null) {
			handleFilter(intent);
		}

		// Navigation drawer and listview initialization
//		initNavigationDrawer();
		initListView();

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
				if ( navigation.equals(String.valueOf(tag.getId())) )
						title = tag.getName();						
			}
		}
		setTitle(title == null ? getString(R.string.title_activity_list) : title);
	}


	
	
	
	/**
	 * Handles third party apps requests of sharing
	 * @param intent
	 */
	private void handleFilter(Intent intent) {
		Note note = new Note();
				
		// Text title
		String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		if (title != null) {
			note.setTitle(title);
		}
		// Text content
		String content = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (content != null) {
			note.setContent(content);
		}
		// Single attachment data
		Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (uri != null) {
	    	String mimeType = StorageManager.getMimeTypeInternal(this, intent.getType());
	        note.addAttachment(new Attachment(uri, mimeType));
	    }
	    // Multiple attachment data
	    ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (uris != null) {
	    	String mimeType = StorageManager.getMimeTypeInternal(this, intent.getType());	    	
	    	for (Uri uriSingle : uris) {
		        note.addAttachment(new Attachment(uriSingle, mimeType));				
			}
	    }
	    
	    // Editing activity launch
		Intent detailIntent = new Intent(this, DetailActivity.class);
		detailIntent.putExtra(Constants.INTENT_NOTE, note);
		startActivity(detailIntent);
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.v(Constants.TAG, "OnResume");
		// The initializazion actions are performed only after onNewIntent is called first time
//		if ( getIntent().getAction() == null 
//				|| Intent.ACTION_MAIN.equals(getIntent().getAction())
//				|| Intent.ACTION_CONFIGURATION_CHANGED.equals(getIntent().getAction())) {
			if (getIntent().getAction() != null) {
			initNotesList(getIntent());
			initNavigationDrawer();
		}
	}
	
	@Override
	protected void onPause() {
		Log.v(Constants.TAG, "OnPause");
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	
//	@Override
//	public void onBackPressed() {
//		// To avoid showing splashcreen again
//		Intent intent = new Intent(Intent.ACTION_MAIN);
//		intent.addCategory(Intent.CATEGORY_HOME);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(intent);
//		
//	}
	
	

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
	    		listView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.list_bg));
	    		mAdapter.restoreDrawable(listView.getChildAt(i).findViewById(R.id.card_layout));
	    	}
			selectedNotes.clear();
			mAdapter.clearSelectedItems();
			listView.clearChoices();
			mActionMode = null;
			Log.d(Constants.TAG, "Closed multiselection contextual menu");
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// Here you can perform updates to the CAB due to
			// an invalidate() request
			Log.d(Constants.TAG, "CAB preparation");
			boolean archived = getResources().getStringArray(R.array.navigation_list_codes)[1].equals(prefs.getString(Constants.PREF_NAVIGATION, "0"));
			menu.findItem(R.id.menu_archive).setVisible(!archived);
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
			mAdapter.restoreDrawable(v);
		}
		if (selectedNotes.size() == 0)
			mActionMode.finish();
	}
    

	/**
	 * Notes list initialization. Data, actions and callback are defined here.
	 */
	private void initListView() {
			listView = (ListView) findViewById(R.id.notesList);
			
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listView.setItemsCanFocus(false);
			
			// Note long click to start CAB mode
			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
					if (mActionMode != null) {
			            return false;
			        }
	
			        // Start the CAB using the ActionMode.Callback defined above
			        startSupportActionMode(new ModeCallback());
			        toggleListViewItem(view, position);
			        setCabTitle();
			        
			        return true;
				}
			});
			
			
			// Note list scrolling hide actionbar effect (deactivate for conflicts with listviewanimation library)
	//		listView.setOnScrollListener(new OnScrollListener() {
	//
	//			int mLastFirstVisibleItem = 0;
	//			/*
	//			 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
	//			 */
	//			@Override
	//			public void onScrollStateChanged(AbsListView view, int scrollState) {}
	//			/*
	//			 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
	//			 */
	//			@Override
	//			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
	//					int totalItemCount) {
	//				if (view.getId() == listView.getId()) {
	//					final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
	//
	//					if (currentFirstVisibleItem > mLastFirstVisibleItem) {
	//						getSupportActionBar().hide();
	//					} else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
	//						getSupportActionBar().show();
	//					}
	//					mLastFirstVisibleItem = currentFirstVisibleItem;
	//				}
	//			}
	//		});
	
			// Note single click listener managed by the activity itself
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View view,
						int position, long arg3) {// If no CAB just note editing
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
	}
	

	/**
	 * Initialization of compatibility navigation drawer
	 */
	private void initNavigationDrawer() {

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawer = (LinearLayout) findViewById(R.id.left_drawer);

		// Sets the adapter for the MAIN navigation list view
		mDrawerList = (ListView) findViewById(R.id.drawer_nav_list);
		mNavigationArray = getResources().getStringArray(R.array.navigation_list);
		mNavigationIconsArray = getResources().obtainTypedArray(R.array.navigation_list_icons);
		mDrawerList
				.setAdapter(new NavigationDrawerAdapter(this, mNavigationArray, mNavigationIconsArray));
		
		// Sets click events
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				String navigation = getResources().getStringArray(R.array.navigation_list_codes)[position];
				Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
				selectNavigationItem(mDrawerList, position);
				prefs.edit().putString(Constants.PREF_NAVIGATION, navigation).commit();
				mDrawerList.setItemChecked(position, true);
				mDrawerTagList.setItemChecked(position, false);
				initNotesList(getIntent());
			}
		});

		// Sets the adapter for the TAGS navigation list view		

		// Retrieves data to fill tags list
		ArrayList<Tag> tags = db.getTags();
		
		if (tags.size() > 0) {
			mDrawerTagList = (ListView) findViewById(R.id.drawer_tag_list);
			// Inflation of header view
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			if (tagListHeader == null) {
				tagListHeader = inflater.inflate(R.layout.drawer_tag_list_header, (ViewGroup) findViewById(R.id.layout_root));
				mDrawerTagList.addHeaderView(tagListHeader);
				mDrawerTagList.setHeaderDividersEnabled(true);
			}
			mDrawerTagList
					.setAdapter(new NavDrawerTagAdapter(this, tags));
			
			// Sets click events
			mDrawerTagList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					Object item = mDrawerTagList.getAdapter().getItem(position);
					// Ensuring that clicked item is not the ListView header
					if (item != null) {
						Tag tag = (Tag)item;						
						String navigation = tag.getName();
						Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
						selectNavigationItem(mDrawerTagList, position);
						prefs.edit().putString(Constants.PREF_NAVIGATION, String.valueOf(tag.getId())).commit();
						mDrawerList.setItemChecked(position, false);
						mDrawerTagList.setItemChecked(position, true);
						initNotesList(getIntent());
					}
				}
			});
			
			// Sets long click events
			mDrawerTagList.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
					Object item = mDrawerTagList.getAdapter().getItem(position);
					// Ensuring that clicked item is not the ListView header
					if (item != null) {
						editTag((Tag)item);
					}
					return true;
				}
			});
		} else {
			if (mDrawerTagList != null) {
				mDrawerTagList.removeAllViewsInLayout();
				mDrawerTagList = null;
			}
		}

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle± ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
			this, /* host Activity */
			mDrawerLayout, /* DrawerLayout object */
			R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
			R.string.drawer_open, /* "open drawer" description for accessibility */
			R.string.drawer_close /* "close drawer" description for accessibility */
		) {

			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				mTitle = getSupportActionBar().getTitle();
				getSupportActionBar().setTitle(getApplicationContext().getString(R.string.app_name));
				supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
//		if (savedInstanceState == null) {
//            selectItem(0);
//        }
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
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
		boolean drawerOpen;
		if (mDrawerLayout != null) {
			drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.START);
		} else {
			drawerOpen = false;
		}
		
		// If archived or reminders notes are shown the "add new note" item must be hidden
		String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
		String navArchived = getResources().getStringArray(R.array.navigation_list_codes)[1];
		String navReminders = getResources().getStringArray(R.array.navigation_list_codes)[2];
		boolean showAdd = !navArchived.equals(prefs.getString(Constants.PREF_NAVIGATION, navNotes))
							&& !navReminders.equals(prefs.getString(Constants.PREF_NAVIGATION, navNotes));

		menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_add).setVisible(!drawerOpen && showAdd);
		menu.findItem(R.id.menu_sort).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_add_tag).setVisible(drawerOpen);
		menu.findItem(R.id.menu_settings).setVisible(true);

		// Initialization of SearchView
		initSearchView(menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	


	/**
	 * SearchView initialization.
	 * It's a little complex because it's not using SearchManager but is implementing on its own.
	 * @param menu
	 */
	private void initSearchView(final Menu menu) {
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//		final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
		// Expands the widget hiding other actionbar icons
		searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.d(Constants.TAG, "Search focus");
				menu.findItem(R.id.menu_add).setVisible(!hasFocus);
				menu.findItem(R.id.menu_sort).setVisible(!hasFocus);
//						searchView.setIconified(!hasFocus);
			}
		});


		// Sets events on searchView closing to restore full notes list
		MenuItem menuItem = menu.findItem(R.id.menu_search);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {

				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					// Reinitialize notes list to all notes when search is collapsed
					Log.i(Constants.TAG, "onMenuItemActionCollapse " + item.getItemId());
					getIntent().setAction(Intent.ACTION_MAIN);
					initNotesList(getIntent());
					return true; 
				}

				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					Log.i(Constants.TAG, "onMenuItemActionExpand " + item.getItemId());
					return true;
				}
			});
		} else {
			// Do something for phones running an SDK before froyo
			searchView.setOnCloseListener(new OnCloseListener() {

				@Override
				public boolean onClose() {
					Log.i(Constants.TAG, "mSearchView on close ");
					getIntent().setAction(Intent.ACTION_MAIN);
					initNotesList(getIntent());
					return false;
				}
			});
		}
		
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:				 
	            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
	                mDrawerLayout.closeDrawer(GravityCompat.START);
	            } else {
	                mDrawerLayout.openDrawer(GravityCompat.START);
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
		}
		return super.onOptionsItemSelected(item);
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


	private void editNote(Note note) {
		if (note.get_id() == 0) {
			Log.d(Constants.TAG, "Adding new note");
			// if navigation is a tag it will be set into note
			String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
			String navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
			try {
				int tagId = Integer.parseInt(navigation);
				note.setTag(db.getTag(tagId));
			} catch (NumberFormatException e) {}
		} else {
			Log.d(Constants.TAG, "Editing note with id: " + note.get_id());
		}

		Intent detailIntent = new Intent(this, DetailActivity.class);
		detailIntent.putExtra(Constants.INTENT_NOTE, note);
		startActivity(detailIntent);
		if (prefs.getBoolean("settings_enable_animations", true)) {
			overridePendingTransition(R.animator.slide_back_right, R.animator.slide_back_left);
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
		//  Two array are used, one with db columns and a corrispective with column names human readables
		final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
		final String[] arrayDialog = getResources().getStringArray(R.array.sortable_columns_human_readable);
		
		// Dialog and events creation
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_sorting_column).setItems(arrayDialog,
				new DialogInterface.OnClickListener() {

					// On choosing the new criteria will be saved into preferences and listview redesigned
					public void onClick(DialogInterface dialog, int which) {
						prefs.edit().putString(Constants.PREF_SORTING_COLUMN, (String) arrayDb[which])
								.commit();
						initNotesList(getIntent());
					}
				});
		return builder.create();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getAction() == null) {
			intent.setAction(Constants.ACTION_START_APP);
		}
		setIntent(intent);
		Log.d(Constants.TAG, "onNewIntent");
//		initNotesList(intent);
		super.onNewIntent(intent);
	}
	
	/**
	 * Notes list adapter initialization and association to view
	 */
	public void initNotesList(Intent intent) {

		Log.v(Constants.TAG, "initNotesList: intent action " + intent.getAction());
		
		List<Note> notes;
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			notes = handleIntent(intent);
			getSupportActionBar().setTitle(getString(R.string.search));
		} else {
			DbHelper db = new DbHelper(getApplicationContext());
			notes = db.getAllNotes(true);
		}
		mAdapter = new NoteAdapter(getApplicationContext(), notes);


		// Enables or note notes list animation depending on settings
//		if (prefs.getBoolean("settings_enable_swype", true)) {
//			ContextualUndoAdapter adapter = new ContextualUndoAdapter(mAdapter, R.layout.undo_row, R.id.undo_row_undobutton);
//			adapter.setAbsListView(listView);
//			listView.setAdapter(adapter);
//			adapter.setDeleteItemCallback(new DeleteItemCallback() {
//				
//				@Override
//				public void deleteItem(int position) {
//					Log.d(Constants.TAG, "Swipe deleting note " + position);
//					deleteNote(mAdapter.getItem(position));	
//					initNotesList(getIntent());
//				}
//			});
//		} else {
//			listView.setAdapter(mAdapter);
//		}
		if (prefs.getBoolean("settings_enable_animations", true)) {
		    SwingBottomInAnimationAdapter swingInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
		    // Assign the ListView to the AnimationAdapter and vice versa
		    swingInAnimationAdapter.setAbsListView(listView);
		    listView.setAdapter(swingInAnimationAdapter);
		} else {
			listView.setAdapter(mAdapter);
		}
		
	}
	
	

	/**
	 * Handle search intent
	 * @param intent
	 * @return
	 */
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


	/** Swaps fragments in the main content view 
	 * @param list */
	private void selectNavigationItem(ListView list, int position) {
		// Reset to NON-checked alll items from navDrawer before selecting the right one
//		for (int i = 0; i < mDrawerList.getCount(); i++) {
//			mDrawerList.setItemChecked(i, false);
//		}
//		for (int i = 0; i < mDrawerTagList.getCount(); i++) {
//			mDrawerList.setItemChecked(i, false);
//		}
		// Highlight the selected item, update the title, and close the drawer
//		list.setItemChecked(position, true);
//		mTitle = mNavigationArray[position];
		Object itemSelected = list.getItemAtPosition(position);
		if (itemSelected.getClass().isAssignableFrom(String.class)) {
			mTitle = (CharSequence)itemSelected;	
		// Is a tag
		} else {
			mTitle = ((Tag)itemSelected).getName();
		}
		mDrawerLayout.closeDrawer(GravityCompat.START);
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
							deleteNote(note);
						}
						// Refresh view
						((ListView) findViewById(R.id.notesList)).invalidateViews();
						// Advice to user
						showToast(getResources().getText(R.string.note_deleted), Toast.LENGTH_SHORT);
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
	 * Single note deletion
	 * @param note Note to be deleted
	 */
	@SuppressLint("NewApi")
	protected void deleteNote(Note note) {
		
		// Saving changes to the note
		DeleteNoteTask deleteNoteTask = new DeleteNoteTask(getApplicationContext());
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
		String archivedStatus = archive ? getResources().getText(R.string.note_archived).toString()
				: getResources().getText(R.string.note_unarchived).toString();
		for (Note note : selectedNotes) {
			// Deleting note using DbHelper
			DbHelper db = new DbHelper(this);
			note.setArchived(archive);
			db.updateNote(note);

			// Update adapter content
			mAdapter.remove(note);

			// Informs the user about update
			Log.d(Constants.TAG, "Note with id '" + note.get_id() + "' " + archivedStatus);
		}
		// Emtpy data structure
		selectedNotes.clear();
		// Refresh view
		((ListView) findViewById(R.id.notesList)).invalidateViews();
		// Advice to user
		showToast(archivedStatus, Toast.LENGTH_SHORT);
	}
	
	
	/**
	 * Tags addition and editing
	 * @param tag
	 */
	private void editTag(Tag tag){
		Intent tagIntent = new Intent(this, TagActivity.class);
		tagIntent.putExtra(Constants.INTENT_TAG, tag);
		startActivity(tagIntent);
//		if (prefs.getBoolean("settings_enable_animations", true)) {
//			overridePendingTransition(R.animator.slide_back_right, R.animator.slide_back_left);
//		}
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
			showToast(getString(R.string.no_tags_created), Toast.LENGTH_SHORT);
			return;
		}
		
		// Otherwise a single choice dialog will be displayed
		ArrayList<String> tagsNames = new ArrayList<String>();
		int selectedIndex = 0;
		for (Tag tag : tags) {
			tagsNames.add(tag.getName());
		}
		candidateSelectedTag = tags.get(0);
		
		final String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
		final String navigation = prefs.getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		
		final String[] array = tagsNames.toArray(new String[tagsNames.size()]);
		alertDialogBuilder.setTitle(R.string.tag_as)
							.setSingleChoiceItems(array, selectedIndex, new DialogInterface.OnClickListener() {										
								@Override
								public void onClick(DialogInterface dialog, int which) {
									candidateSelectedTag = tags.get(which);
								}
							}).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									for (Note note : selectedNotes) {
										// Update adapter content if actual navigation is the tag
										// associated with actually cycled note
										if (!Arrays.asList(navigationListCodes).contains(navigation)
												&& !navigation.equals(candidateSelectedTag.getId())) {
											mAdapter.remove(note);
										}
										note.setTag(candidateSelectedTag);
										db.updateNote(note);
									}
									// Refresh view
									((ListView) findViewById(R.id.notesList)).invalidateViews();
									// Advice to user
									showToast(getResources().getText(R.string.notes_tagged_as) + " '" + candidateSelectedTag.getName() + "'", Toast.LENGTH_SHORT);
									candidateSelectedTag = null;
									mActionMode.finish(); // Action picked, so close the CAB
								}
							}).setNeutralButton(R.string.remove_tag, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									for (Note note : selectedNotes) {
										// Update adapter content if actual navigation is the tag
										// associated with actually cycled note										
										if ( navigation.equals(String.valueOf(note.getTag().getId())) ) {
											mAdapter.remove(note);
										}
										note.setTag(null);
										db.updateNote(note);
									}
									candidateSelectedTag = null;
									// Refresh view
									((ListView) findViewById(R.id.notesList)).invalidateViews();
									// Advice to user
									showToast(getResources().getText(R.string.notes_tag_removed), Toast.LENGTH_SHORT);
									mActionMode.finish(); // Action picked, so close the CAB
								}
							}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									candidateSelectedTag = null;
									mActionMode.finish(); // Action picked, so close the CAB
								}
							});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();		
	}


}



