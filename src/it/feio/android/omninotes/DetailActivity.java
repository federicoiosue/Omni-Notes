package it.feio.android.omninotes;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ParcelableNote;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import it.feio.android.omninotes.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ItemDetailFragment}.
 */
public class DetailActivity extends BaseActivity {
    
	private final int HEIGHT_FULLSCREEN_LIMIT = 350;
	private Note note;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        setContentView(R.layout.fragment_item_detail);
        
        if (getWindowManager().getDefaultDisplay().getHeight() < HEIGHT_FULLSCREEN_LIMIT) {
        	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);     	
        }

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null)
        	getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initNote();
    }


	private void initNote() {
		ParcelableNote parcelableNote = (ParcelableNote) getIntent().getParcelableExtra(Constants.INTENT_NOTE);
		note = parcelableNote.getNote();
		
		if (note.get_id() != 0) {
        	((EditText) findViewById(R.id.title)).setText(note.getTitle());
        	((EditText) findViewById(R.id.content)).setText(note.getContent());
        	((TextView) findViewById(R.id.creation)).append(getString(R.string.creation) + " " + note.getCreationShort());        	
        	((TextView) findViewById(R.id.last_modification)).append(getString(R.string.last_update) + " " + note.getLastModificationShort());
        }
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_share).setVisible(true);
		menu.findItem(R.id.menu_delete).setVisible(true);
		
		boolean archived = note.isArchived();
		menu.findItem(R.id.menu_archive).setVisible(!archived);
		menu.findItem(R.id.menu_unarchive).setVisible(archived);
		
		return super.onPrepareOptionsMenu(menu);
	}
	


	private boolean goHome() {
		NavUtils.navigateUpFromSameTask(this);	
		if (prefs.getBoolean("settings_enable_animations", true)) {
			overridePendingTransition(R.animator.slide_left, R.animator.slide_right);	
		}
        return true;
	}

	@Override
	public void onBackPressed() {
		saveNote(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
	            // This ID represents the Home or Up button. In the case of this
	            // activity, the Up button is shown. Use NavUtils to allow users
	            // to navigate up one level in the application structure. For
	            // more details, see the Navigation pattern on Android Design:
	            //
	            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
	            //
	        	saveNote(null);
	        	break;
			case R.id.menu_share:
				shareNote();
				break;
			case R.id.menu_archive:
				saveNote(true);
				break;
			case R.id.menu_unarchive:
				saveNote(false);
				break;
			case R.id.menu_delete:
				deleteNote();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void deleteNote() {
		
		// Confirm dialog creation
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(R.string.delete_note_confirmation)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Simply return to the previous activity/fragment if it was a new note
						if (getIntent().getStringExtra(Constants.INTENT_KEY) == null) {
							goHome();
							return;
						}
						
						// Create note object						
						int _id = Integer.parseInt(getIntent().getStringExtra(Constants.INTENT_KEY));
						Note note = new Note();
						note.set_id(_id);
						
						// Deleting note using DbHelper
						DbHelper db = new DbHelper(getApplicationContext());
						db.deleteNote(note);		
						
						// Informs the user about update
						Log.d(Constants.TAG, "Deleted note with id '" + _id + "'");
						showToast(getResources().getText(R.string.note_deleted), Toast.LENGTH_SHORT);
				        goHome();
						return;
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}


	
	/**
	 * Save new notes, modify them or archive
	 * @param archive Boolean flag used to archive note
	 */
	private void saveNote(Boolean archive) {
		// Changed fields
		String title = ((EditText)findViewById(R.id.title)).getText().toString();
		String content = ((EditText)findViewById(R.id.content)).getText().toString();
			
		Note noteEdited = note;
		if(noteEdited != null) {
			note = noteEdited;
		} else {
			note = new Note();
		}
		
		// Check if some text has ben inserted or is an empty note
		if ((title + content).length() == 0) {
			Log.d(Constants.TAG, "Empty note not saved");
			showToast(getResources().getText(R.string.empty_note_not_saved), Toast.LENGTH_SHORT);
	        goHome();
	        return;
		}
		
		// Logging operation
		Log.d(Constants.TAG, "Saving new note titled: " + title + " (archive var: " + archive + ")");
		
		note.set_id(note.get_id());
		note.setTitle(title);
		note.setContent(content);
		note.setArchived(archive != null ? archive : note.isArchived());
		
		// Saving changes to the note
		DbHelper db = new DbHelper(this);
		db.updateNote(note);
		
		// Logs update
//		Log.d(Constants.TAG, "New note saved with title '" + note.getTitle() + "' and id '" + _id + "'");
		Log.d(Constants.TAG, "New note saved with title '" + note.getTitle() + "'");
		showToast(getResources().getText(R.string.note_updated), Toast.LENGTH_SHORT);

		// Go back on stack
        goHome();
	}
	
	
	private void shareNote() {
		// Changed fields
		String title = ((EditText)findViewById(R.id.title)).getText().toString();
		String content = ((EditText)findViewById(R.id.content)).getText().toString();
		
		// Check if some text has ben inserted or is an empty note
		if ((title + content).length() == 0) {
			Log.d(Constants.TAG, "Empty note not shared");
			showToast(getResources().getText(R.string.empty_note_not_shared), Toast.LENGTH_SHORT);
			return;
		}

		// Definition of shared content
		String text = title + System.getProperty("line.separator") + content
				+ System.getProperty("line.separator") + System.getProperty("line.separator")
				+ getResources().getString(R.string.shared_content_sign);
		
		// Prepare sharing intent
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
//		shareIntent.setType("*/*");		
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_message_chooser)));
	}
}
