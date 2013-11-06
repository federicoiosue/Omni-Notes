package it.feio.android.omninotes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import it.feio.android.omninotes.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


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
        implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
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

        // TODO: If exposing deep links into your app, handle intents here.
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
//		onCreateDialogSingleChoice().show();
		onCreateDialog().show();
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.add).setVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add:
				editNote(null);
				break;
			case R.id.sort:
				sortNotes();
				break;
			case R.id.settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
	            startActivity(settingsIntent);
				break;
			case R.id.about:
				Intent aboutIntent = new Intent(this, AboutActivity.class);
	            startActivity(aboutIntent);
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
    
}
