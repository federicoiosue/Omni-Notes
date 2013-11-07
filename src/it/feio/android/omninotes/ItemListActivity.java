package it.feio.android.omninotes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import it.feio.android.omninotes.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends BaseFragmentActivity
        implements ItemListFragment.Callbacks, OnItemClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
	private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mNavigationArray;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// Set the adapter for the list view
		mNavigationArray = getResources().getStringArray(R.array.navigation_list);
		mDrawerList.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				R.layout.drawer_list_item, mNavigationArray));
		mDrawerList.setOnItemClickListener(this);
		
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        	getActionBar().setHomeButtonEnabled(true);
        

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
            	mTitle = getActionBar().getTitle();
                getActionBar().setTitle(getApplicationContext().getString(R.string.app_name));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        CharSequence title = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_NAVIGATION, "");
        setTitle(title);

    }    
    

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        editNote(id);
    }
    
    
    private void editNote(String id) {
    	if (id == null) {
    		Log.d(Constants.TAG, "Adding new note");
    	} else {
    		Log.d(Constants.TAG, "Editing note with id: " + id);
    	}
    	if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(Constants.INTENT_KEY, id);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(Constants.INTENT_KEY, id);
            startActivity(detailIntent);
        }
	}

    
	private void sortNotes() {
		onCreateDialog().show();
	}
	
	 @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// Setting the conditions to show determinate items in CAB
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        // If archived notes are shown the "add new note" item must be hidden
        boolean archived = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_NAVIGATION, "").equals(getResources().getStringArray(R.array.navigation_list)[1]);
        
		menu.findItem(R.id.menu_add).setVisible(!drawerOpen && !archived);
        menu.findItem(R.id.menu_sort).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add:
				editNote(null);
				break;
			case R.id.menu_sort:
				sortNotes();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}	
	
	
	/**
	 * Creation of a dialog for choose sorting criteria
	 * @return
	 */
	public Dialog onCreateDialog() {
		final Context ctx = this;
		
		//Source of the data in the DIalog
		LinkedHashMap<String, String> map = DbHelper.getSortableColumns();
		final ArrayList<String> arrayList = new ArrayList<String>();
		
		// Iterate through map to get sortable columns to show in dialog
		for (String key : map.keySet()) {
			arrayList.add(map.get(key));
		}
		
		// Creates a cloned list to rename elements in human readable format
		ArrayList<String> clonedList = new ArrayList<String>(arrayList.size());
	    for (String element : arrayList) {
	        clonedList.add(dbColumnsToText(element));
	    }
		
	    // Dialog and events creation
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_sorting_column).setItems(
				clonedList.toArray(new String[clonedList.size()]), new DialogInterface.OnClickListener() {
					// On choosing the new criteria will be saved into preferences and listview redesigned
					public void onClick(DialogInterface dialog, int which) {
						PreferenceManager.getDefaultSharedPreferences(ctx).edit()
								.putString(Constants.PREF_SORTING_COLUMN, (String) arrayList.get(which))
								.commit();
						((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).initNotesList();
					}
				});
	    return builder.create();
	}

	
	/**
	 * Conversion from database table name to human readable
	 * @param string
	 * @return
	 */
	private String dbColumnsToText(String string) {
		String text = "";
		String[] array = string.split("_");
		for (String word : array) {
			text += Character.toUpperCase(word.charAt(0)) + word.substring(1) + " ";			
		}
		text.trim();
		return text;
	}
	
	
	

	

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		String navigation = adapterView.getAdapter().getItem(position).toString();
		Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
		selectNavigationItem(position);
		PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.PREF_NAVIGATION, navigation).commit();
		((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).initNotesList();
	}


	/** Swaps fragments in the main content view */
	@SuppressLint("NewApi") 
	private void selectNavigationItem(int position) {
	    // Create a new fragment and specify the planet to show based on position
//	    Fragment fragment = new Fragment();
//	    Bundle args = new Bundle();
//	    args.putInt(Fragment., position);
//	    fragment.setArguments(args);
//
//	    // Insert the fragment by replacing any existing fragment
//	    FragmentManager fragmentManager = getFragmentManager();
//	    fragmentManager.beginTransaction()
//	                   .replace(R.id.content_frame, fragment)
//	                   .commit();

	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    mTitle = mNavigationArray[position];
	    mDrawerLayout.closeDrawer(mDrawerList);

	 // enable ActionBar app icon to behave as action to toggle nav drawer
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setHomeButtonEnabled(true);

	}
    
}
