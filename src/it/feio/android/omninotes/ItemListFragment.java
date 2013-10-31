package it.feio.android.omninotes;

import java.util.HashSet;
import java.util.List;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.NoteAdapter;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ListView;

/**
 * A list fragment representing a list of Items. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon selection. This helps indicate which item is currently being viewed in a {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class ItemListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	NoteAdapter adapter;
	ActionMode mActionMode;
	HashSet<Note> selectedNotes = new HashSet<Note>();
	/**
	 * A callback interface that all activities containing this fragment must implement. This mechanism allows activities to be notified of item selections.
	 */
	public interface Callbacks {

		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Trying to get available notes
		DbHelper db = new DbHelper(getActivity().getApplicationContext());
		List<Note> notes = db.getAllNotes();
			adapter = new NoteAdapter(getActivity().getApplicationContext(), notes);
		
		setListAdapter(adapter);	
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}
 
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		final ListView listView = getListView();
        
        // Called when user long-clicks on a note in the list 
//		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//			@Override
//			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
//				Log.d(Constants.TAG, "Long clicked note at position " + position);
//				if (mActionMode != null) {
//					return false;
//				}
//				// Start the CAB using the ActionMode.Callback defined above
//				mActionMode = getActivity().startActionMode(mActionModeCallback);
//				view.setSelected(true);
//				return true;
//			}
//		});
		
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {

		    @Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		        // Here you can do something when items are selected/de-selected,
		        // such as update the title in the CAB
		    	Log.d(Constants.TAG, "Multiselection: selected element " + position);
		    	final int checkedCount = getListView().getCheckedItemCount();
		    	if (checked) 
		    		selectedNotes.add(adapter.getItem(position));
		    	else 
		    		selectedNotes.remove(adapter.getItem(position));
//		    	getListView().getChildAt(position).setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
//		    	getListView().getRootView().setSelected(true);
		    	getListView().getChildAt(position).setBackgroundColor(Color.RED);
		        
		        switch (checkedCount)
		        {
		            case 0:
		                mode.setTitle(null);
		                break;
		            case 1:
		                mode.setTitle(getResources().getString(R.string.one_item_selected));
		                break;
		            default:
		                mode.setTitle(checkedCount + " " + getResources().getString(R.string.more_items_selected));
		                break;
		        }
		    }

		    @Override
		    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		        // Respond to clicks on the actions in the CAB
		        switch (item.getItemId()) {
		            case R.id.delete:
		                deleteSelectedNotes();
		                mode.finish(); // Action picked, so close the CAB
		                return true;
		            default:
		                return false;
		        }
		    }

			@Override
		    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		        // Inflate the menu for the CAB
		        MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.menu, menu);
		        return true;
		    }

		    @Override
		    public void onDestroyActionMode(ActionMode mode) {
		        // Here you can make any necessary updates to the activity when
		        // the CAB is removed. By default, selected items are deselected/unchecked.
		    	Log.d(Constants.TAG, "Closed multiselection contextual menu" );
		    }

		    @Override
		    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		        // Here you can perform updates to the CAB due to
		        // an invalidate() request
		    	Log.d(Constants.TAG, "CAB preparation");
				menu.findItem(R.id.delete).setVisible(true);
		        return true;
		    }
		});
    }
    

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected( String.valueOf( ((Note)adapter.getItem(position)).get_id() ) );
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Log.d(Constants.TAG, "Contextual menu creation");
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
//		getListView().setChoiceMode(
//				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	
	
	/**
	 * CAB activation
	 */
//	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
//
//	    // Called when the action mode is created; startActionMode() was called
//	    @Override
//	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//	        // Inflate a menu resource providing context menu items
//	        MenuInflater inflater = mode.getMenuInflater();
//	        inflater.inflate(R.menu.menu, menu);
//	        return true;
//	    }
//
//	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
//	    // may be called multiple times if the mode is invalidated.
//	    @Override
//	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//	    	Log.d(Constants.TAG, "CAB preparation");
//			menu.findItem(R.id.delete).setVisible(true);
//	        return true; // Return false if nothing is done
//	    }
//
//	    // Called when the user selects a contextual menu item
//	    @Override
//	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//	        switch (item.getItemId()) {
////	            case R.id.menu_share:
////	                shareCurrentItem();
////	                mode.finish(); // Action picked, so close the CAB
////	                return true;
//	            default:
//	                return false;
//	        }
//	    }
//
//	    // Called when the user exits the action mode
//	    @Override
//	    public void onDestroyActionMode(ActionMode mode) {
//	        mActionMode = null;
//	    }
//	};

	
	
    public void deleteSelectedNotes() {
		for (Note note : selectedNotes) {
			// Deleting note using DbHelper
			DbHelper db = new DbHelper(getActivity().getApplicationContext());
			db.deleteNote(note);

			// Update adapter content
			adapter.remove(note);
			
			// Informs the user about update
			Log.d(Constants.TAG, "Deleted note with id '" + note.get_id() + "'");
		}
		// Emtpy data structure
		selectedNotes.clear();
		// Refresh view
		getListView().invalidateViews();
		// Advice to user
		Toast.makeText(getActivity().getApplicationContext(),
				getResources().getText(R.string.note_deleted), Toast.LENGTH_SHORT).show();
	}
    
    
	public void deleteSelectedNotesConfirmed() {
		for (Long itemId : getListView().getCheckedItemIds()) {

			Log.i(Constants.TAG, "Deleting element " + itemId);
			// Create note object
//			int _id = Integer.parseInt(getIntent().getStringExtra(Constants.INTENT_KEY));
//			Note note = new Note();
//			note.set_id(_id);
			//
			// Deleting note using DbHelper
//			DbHelper db = new DbHelper(getActivity().getApplicationContext());
//			db.deleteNote(note);

			// Informs the user about update
			// Log.d(Constants.TAG, "Deleted note with id '" + _id + "'");
		}
		Toast.makeText(getActivity().getApplicationContext(),
				getResources().getText(R.string.note_deleted), Toast.LENGTH_SHORT).show();
		return;
	}
}






class MyAlertDialogFragment extends DialogFragment {
	ListFragment listFragment;

    public MyAlertDialogFragment(ListFragment listFragment) {
    	this.listFragment = listFragment;
	}

	public static MyAlertDialogFragment newInstance(ListFragment listFragment, int title) {
        MyAlertDialogFragment frag = new MyAlertDialogFragment(listFragment);
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.confirm,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	((ItemListFragment) listFragment).deleteSelectedNotesConfirmed();
                        }
                    }
                )
                .setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {}
                    }
                )
                .create();
    }
}
